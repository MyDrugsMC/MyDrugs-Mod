package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.CuratedDrugChain;
import org.mydrugs.mydrugs.dimension.InnerDimensionSavedData;
import org.mydrugs.mydrugs.dimension.InnerDimensions;
import org.mydrugs.mydrugs.dimension.ModInnerDimensionBlocks;
import org.mydrugs.mydrugs.items.ModItems;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public final class InnerTrialManager {
    private static final int COFFEE_REQUIRED_TICKS = 20 * 6;
    private static final int WEED_REQUIRED_PLACEMENTS = 6;
    private static final int COCAINE_TIME_LIMIT_TICKS = 20 * 14;
    private static final double LANDMARK_NOTICE_RADIUS_SQR = 24.0D * 24.0D;

    private static final Map<UUID, InnerTrialProgress> PROGRESS = new LinkedHashMap<>();
    private static final Map<DrugId, Supplier<Item>> TRIAL_REWARDS = trialRewards();

    private InnerTrialManager() {
    }

    public static void tickPlayer(ServerPlayer player) {
        if (player == null
                || !(player.level() instanceof ServerLevel level)
                || !isInnerDimension(level)) {
            return;
        }
        InnerDimensionSavedData data = InnerDimensionSavedData.get(level);
        InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(player.getUUID());
        InnerTrialProgress progress = progress(player);
        if (player.tickCount % 20 == 0) {
            announceNearbyTrial(player, level, island, progress);
        }
        tickCoffee(player, level, island, progress);
        tickCocaine(player, level, island, progress);
    }

    public static boolean handleRightClick(
            ServerPlayer player,
            ServerLevel level,
            BlockPos clicked,
            InteractionHand hand
    ) {
        if (player == null
                || clicked == null
                || hand == null
                || !isInnerDimension(player.level())
                || !isInnerDimension(level)) {
            return false;
        }
        InnerDimensionSavedData data = InnerDimensionSavedData.get(level);
        InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(player.getUUID());
        if (!isOwnIsland(island, clicked)) {
            return false;
        }
        InnerTrialProgress progress = progress(player);
        ItemStack held = player.getItemInHand(hand);

        if (handleTobacco(player, level, island, progress, clicked)) {
            return true;
        }
        if (handleWeedSpores(player, level, island, progress, clicked, held)) {
            consumeOne(player, held);
            return true;
        }
        if (handleAlcohol(player, level, island, clicked)) {
            return true;
        }
        if (handleLsd(player, level, island, clicked, held)) {
            return true;
        }
        if (handleMeth(player, level, island, progress, clicked, hand, held)) {
            return true;
        }
        return handleMushrooms(player, level, island, progress, clicked);
    }

    public static void handlePlacement(
            ServerPlayer player,
            ServerLevel level,
            BlockPos placedPos,
            BlockState placedState
    ) {
        if (player == null
                || placedPos == null
                || placedState == null
                || !isInnerDimension(player.level())
                || !isInnerDimension(level)) {
            return;
        }
        InnerDimensionSavedData data = InnerDimensionSavedData.get(level);
        InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(player.getUUID());
        if (!isOwnIsland(island, placedPos)) {
            return;
        }
        InnerTrialProgress progress = progress(player);
        if (canAttempt(island, DrugId.WEED)
                && isNearLandmark(level, island, DrugId.WEED, placedPos, 18.0D)
                && isCalmingFlora(placedState.getBlock())) {
            progress.weedPlacements++;
            int current = Math.min(WEED_REQUIRED_PLACEMENTS, progress.weedPlacements);
            player.displayClientMessage(Component.translatable(
                    "message.mydrugs.inner_trial.progress",
                    current,
                    WEED_REQUIRED_PLACEMENTS
            ).withStyle(ChatFormatting.GREEN), true);
            if (progress.weedPlacements >= WEED_REQUIRED_PLACEMENTS) {
                completeTrial(level, player, island, DrugId.WEED, true);
            }
        }

        if (canAttempt(island, DrugId.HASH)
                && isHashSocket(level, island, placedPos)
                && isHashSocketMaterial(placedState.getBlock())) {
            int filled = hashFilledSockets(level, island);
            player.displayClientMessage(Component.translatable(
                    "message.mydrugs.inner_trial.progress",
                    filled,
                    InnerTrialDefinition.hashSockets().size()
            ).withStyle(ChatFormatting.LIGHT_PURPLE), true);
            if (filled >= InnerTrialDefinition.hashSockets().size()) {
                completeTrial(level, player, island, DrugId.HASH, true);
            }
        }
    }

    public static void resetStillPoint(ServerPlayer player) {
        if (player == null || !isInnerDimension(player.level())) {
            return;
        }
        InnerTrialProgress progress = PROGRESS.get(player.getUUID());
        if (progress != null && progress.coffeeTicks > 0) {
            if (progress.coffeeTicks >= 20) {
                InnerMessageCooldowns.actionBar(
                        player,
                        "trial:coffee:failed",
                        40,
                        Component.translatable("message.mydrugs.inner_trial.coffee.failed")
                                .withStyle(ChatFormatting.RED)
                );
            }
            progress.coffeeTicks = 0;
        }
    }

    public static boolean completeTrial(
            ServerLevel level,
            ServerPlayer player,
            InnerDimensionSavedData.IslandState island,
            DrugId drug,
            boolean grantReward
    ) {
        if (player == null
                || island == null
                || island.owner() == null
                || drug == null
                || !CuratedDrugChain.ORDER.contains(drug)
                || !isInnerDimension(player.level())
                || !isInnerDimension(level)) {
            return false;
        }
        InnerDimensionSavedData data = InnerDimensionSavedData.get(level);
        if (!data.completeInnerTrial(island.owner(), drug)) {
            return false;
        }

        if (grantReward) {
            ItemStack reward = rewardFor(drug);
            if (!reward.isEmpty() && !player.addItem(reward)) {
                player.drop(reward, false);
            }
        }
        player.sendSystemMessage(Component.translatable(
                "message.mydrugs.inner_trial.completed",
                Component.translatable("drug.mydrugs." + drug.serializedName())
        ).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        if (player.level() == level) {
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.15F);
            level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0D, player.getZ(),
                    28, 1.2D, 1.5D, 1.2D, 0.04D);
        }
        InnerOverlayQueue.enqueueTrialCompletion(island, drug);
        clearDrugProgress(progress(player), drug);
        InnerProgressionMilestones.trialCompleted(level, data, island, player, drug);

        if (data.allInnerTrialsCompleted(island.owner())) {
            player.sendSystemMessage(Component.translatable(
                    "message.mydrugs.inner_trial.all_completed"
            ).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        }
        return true;
    }

    public static boolean resetTrials(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island
    ) {
        if (island == null || island.owner() == null || !isInnerDimension(level)) {
            return false;
        }
        boolean reset = InnerDimensionSavedData.get(level).resetInnerTrials(island.owner());
        PROGRESS.remove(island.owner());
        if (reset) {
            InnerOverlayQueue.enqueueOwnerOverlayRefresh(island);
        }
        return reset;
    }

    public static DrugId nearestIncompleteTrial(
            InnerDimensionSavedData.IslandState island,
            BlockPos origin
    ) {
        if (island == null || origin == null) {
            return null;
        }
        DrugId nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (DrugId drug : CuratedDrugChain.ORDER) {
            if (!island.hasIntegrated(drug) || island.hasCompletedInnerTrial(drug)) {
                continue;
            }
            BlockPos landmark = InnerRegionMap.landmarkFor(island.centerX(), island.centerZ(), drug);
            double distance = landmark.distSqr(origin);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = drug;
            }
        }
        return nearest;
    }

    public static void clearAll() {
        PROGRESS.clear();
    }

    public static void clearPlayer(UUID playerId) {
        PROGRESS.remove(playerId);
    }

    private static boolean isInnerDimension(Level level) {
        return level != null && level.dimension().equals(InnerDimensions.INNER_LEVEL);
    }

    private static void tickCoffee(
            ServerPlayer player,
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            InnerTrialProgress progress
    ) {
        if (!canAttempt(island, DrugId.COFFEE)) {
            progress.coffeeTicks = 0;
            return;
        }
        BlockPos center = landmarkSurface(level, island, DrugId.COFFEE);
        if (horizontalDistanceSqr(player.blockPosition(), center) > 5.5D * 5.5D
                || player.isSprinting()
                || !player.onGround()
                || horizontalSpeedSqr(player) > 0.028D) {
            if (progress.coffeeTicks >= 20) {
                InnerMessageCooldowns.actionBar(
                        player,
                        "trial:coffee:failed",
                        40,
                        Component.translatable("message.mydrugs.inner_trial.coffee.failed")
                                .withStyle(ChatFormatting.RED)
                );
            }
            progress.coffeeTicks = 0;
            return;
        }
        progress.coffeeTicks++;
        if (progress.coffeeTicks % 20 == 0) {
            player.displayClientMessage(Component.translatable(
                    "message.mydrugs.inner_trial.still_point",
                    progress.coffeeTicks / 20,
                    COFFEE_REQUIRED_TICKS / 20
            ).withStyle(ChatFormatting.AQUA), true);
        }
        if (progress.coffeeTicks >= COFFEE_REQUIRED_TICKS) {
            completeTrial(level, player, island, DrugId.COFFEE, true);
        }
    }

    private static void tickCocaine(
            ServerPlayer player,
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            InnerTrialProgress progress
    ) {
        if (!canAttempt(island, DrugId.COCAINE)) {
            progress.cocaineStartTick = -1L;
            return;
        }
        BlockPos center = landmarkSurface(level, island, DrugId.COCAINE);
        BlockPos pos = player.blockPosition();
        BlockPos start = center.offset(-14, 0, 0);
        BlockPos end = center.offset(14, 0, 0);
        long now = level.getGameTime();
        if (progress.cocaineStartTick < 0L) {
            if (horizontalDistanceSqr(pos, start) <= 2.4D * 2.4D) {
                progress.cocaineStartTick = now;
                player.displayClientMessage(Component.translatable(
                        "message.mydrugs.inner_trial.cocaine.started"
                ).withStyle(ChatFormatting.RED), true);
            }
            return;
        }

        boolean outsideRoute = pos.getX() < center.getX() - 16
                || pos.getX() > center.getX() + 16
                || Math.abs(pos.getZ() - center.getZ()) > 3;
        boolean touchedThorn = level.getBlockState(pos).is(ModInnerDimensionBlocks.REDLINE_THORN.get())
                || level.getBlockState(pos.below()).is(ModInnerDimensionBlocks.REDLINE_THORN.get());
        if (outsideRoute || touchedThorn || now - progress.cocaineStartTick > COCAINE_TIME_LIMIT_TICKS) {
            progress.cocaineStartTick = -1L;
            InnerMessageCooldowns.actionBar(
                    player,
                    "trial:cocaine:failed",
                    40,
                    Component.translatable("message.mydrugs.inner_trial.cocaine.failed")
                            .withStyle(ChatFormatting.RED)
            );
            return;
        }
        if (horizontalDistanceSqr(pos, end) <= 2.4D * 2.4D) {
            completeTrial(level, player, island, DrugId.COCAINE, true);
        }
    }

    private static boolean handleTobacco(
            ServerPlayer player,
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            InnerTrialProgress progress,
            BlockPos clicked
    ) {
        if (!canAttempt(island, DrugId.TOBACCO)) {
            return false;
        }
        BlockPos center = landmarkSurface(level, island, DrugId.TOBACCO);
        int node = InnerTrialDefinition.horizontalIndex(
                clicked,
                center,
                InnerTrialDefinition.tobaccoOrder(island.centerX(), island.centerZ())
        );
        if (node < 0 || (!level.getBlockState(clicked).is(ModInnerDimensionBlocks.BITTER_ECHO_NODE.get())
                && !level.getBlockState(clicked).is(ModInnerDimensionBlocks.LUCID_ECHO_NODE.get()))) {
            return false;
        }
        if (node == progress.tobaccoStep) {
            progress.tobaccoStep++;
            player.displayClientMessage(Component.translatable(
                    "message.mydrugs.inner_trial.progress",
                    progress.tobaccoStep,
                    4
            ).withStyle(ChatFormatting.GRAY), true);
            if (progress.tobaccoStep >= 4) {
                completeTrial(level, player, island, DrugId.TOBACCO, true);
            }
        } else {
            progress.tobaccoStep = 0;
            InnerMessageCooldowns.actionBar(
                    player,
                    "trial:tobacco:failed",
                    20,
                    Component.translatable("message.mydrugs.inner_trial.tobacco.failed")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        return true;
    }

    private static boolean handleWeedSpores(
            ServerPlayer player,
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            InnerTrialProgress progress,
            BlockPos clicked,
            ItemStack held
    ) {
        if (!canAttempt(island, DrugId.WEED)
                || !held.is(ModItems.CALMING_SPORES.get())
                || !isNearLandmark(level, island, DrugId.WEED, clicked, 18.0D)) {
            return false;
        }
        progress.weedPlacements += 2;
        int current = Math.min(WEED_REQUIRED_PLACEMENTS, progress.weedPlacements);
        player.displayClientMessage(Component.translatable(
                "message.mydrugs.inner_trial.progress",
                current,
                WEED_REQUIRED_PLACEMENTS
        ).withStyle(ChatFormatting.GREEN), true);
        if (progress.weedPlacements >= WEED_REQUIRED_PLACEMENTS) {
            completeTrial(level, player, island, DrugId.WEED, true);
        }
        return true;
    }

    private static boolean handleAlcohol(
            ServerPlayer player,
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            BlockPos clicked
    ) {
        if (!canAttempt(island, DrugId.ALCOHOL)
                || !level.getBlockState(clicked).is(ModInnerDimensionBlocks.FERMENTED_MEMORY_NODE.get())
                || !isNearLandmark(level, island, DrugId.ALCOHOL, clicked, 8.0D)) {
            return false;
        }
        if (!player.isUnderWater()) {
            player.displayClientMessage(Component.translatable(
                    "message.mydrugs.inner_trial.alcohol.submerge"
            ).withStyle(ChatFormatting.BLUE), true);
            return true;
        }
        completeTrial(level, player, island, DrugId.ALCOHOL, true);
        return true;
    }

    private static boolean handleLsd(
            ServerPlayer player,
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            BlockPos clicked,
            ItemStack held
    ) {
        if (!canAttempt(island, DrugId.LSD)
                || !level.getBlockState(clicked).is(ModInnerDimensionBlocks.DREAM_RESIDUE_GEODE.get())) {
            return false;
        }
        BlockPos center = landmarkSurface(level, island, DrugId.LSD);
        int node = InnerTrialDefinition.horizontalIndex(clicked, center, InnerTrialDefinition.lsdNodes());
        if (node < 0) {
            return false;
        }
        int trueNode = InnerTrialDefinition.lsdTrueNodeIndex(island.centerX(), island.centerZ());
        if (node == trueNode) {
            completeTrial(level, player, island, DrugId.LSD, true);
        } else {
            String key = held.is(ModItems.PSYCHOTROPE_LENS.get())
                    ? "message.mydrugs.inner_trial.lsd.lens_decoy"
                    : "message.mydrugs.inner_trial.lsd.decoy";
            player.displayClientMessage(Component.translatable(key).withStyle(ChatFormatting.LIGHT_PURPLE), true);
            level.sendParticles(ParticleTypes.PORTAL, clicked.getX() + 0.5D, clicked.getY() + 0.8D, clicked.getZ() + 0.5D,
                    12, 0.35D, 0.45D, 0.35D, 0.02D);
        }
        return true;
    }

    private static boolean handleMeth(
            ServerPlayer player,
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            InnerTrialProgress progress,
            BlockPos clicked,
            InteractionHand hand,
            ItemStack held
    ) {
        if (!canAttempt(island, DrugId.METH)
                || !level.getBlockState(clicked).is(ModInnerDimensionBlocks.OVERDRIVE_SLAG.get())
                || !isMethTool(held)) {
            return false;
        }
        BlockPos center = landmarkSurface(level, island, DrugId.METH);
        int node = InnerTrialDefinition.horizontalIndex(clicked, center, InnerTrialDefinition.methNodes());
        if (node < 0) {
            return false;
        }
        int nodeBit = 1 << node;
        boolean newlyStabilized = (progress.methMask & nodeBit) == 0;
        progress.methMask |= nodeBit;
        int stabilized = Integer.bitCount(progress.methMask);
        player.displayClientMessage(Component.translatable(
                "message.mydrugs.inner_trial.progress",
                stabilized,
                InnerTrialDefinition.methNodes().size()
        ).withStyle(ChatFormatting.GOLD), true);
        level.sendParticles(ParticleTypes.CLOUD, clicked.getX() + 0.5D, clicked.getY() + 0.8D, clicked.getZ() + 0.5D,
                10, 0.25D, 0.35D, 0.25D, 0.02D);
        if (newlyStabilized) {
            consumeMethTool(player, hand, held);
        }
        if (stabilized >= InnerTrialDefinition.methNodes().size()) {
            completeTrial(level, player, island, DrugId.METH, true);
        }
        return true;
    }

    private static boolean handleMushrooms(
            ServerPlayer player,
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            InnerTrialProgress progress,
            BlockPos clicked
    ) {
        if (!canAttempt(island, DrugId.MUSHROOMS)) {
            return false;
        }
        BlockState state = level.getBlockState(clicked);
        if (!state.is(ModInnerDimensionBlocks.MYCELIAL_ROOT.get())
                && !state.is(ModInnerDimensionBlocks.MYCELIAL_INSIGHT_NODE.get())) {
            return false;
        }
        BlockPos center = landmarkSurface(level, island, DrugId.MUSHROOMS);
        int root = InnerTrialDefinition.horizontalIndex(clicked, center, InnerTrialDefinition.mushroomRoots());
        if (root < 0) {
            return false;
        }
        progress.mushroomMask |= 1 << root;
        int connected = Integer.bitCount(progress.mushroomMask);
        player.displayClientMessage(Component.translatable(
                "message.mydrugs.inner_trial.progress",
                connected,
                InnerTrialDefinition.mushroomRoots().size()
        ).withStyle(ChatFormatting.DARK_GREEN), true);
        if (connected >= InnerTrialDefinition.mushroomRoots().size()) {
            completeTrial(level, player, island, DrugId.MUSHROOMS, true);
        }
        return true;
    }

    private static void announceNearbyTrial(
            ServerPlayer player,
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            InnerTrialProgress progress
    ) {
        for (DrugId drug : CuratedDrugChain.ORDER) {
            if (!canAttempt(island, drug) || progress.announced.contains(drug)) {
                continue;
            }
            BlockPos center = landmarkSurface(level, island, drug);
            if (horizontalDistanceSqr(player.blockPosition(), center) > LANDMARK_NOTICE_RADIUS_SQR) {
                continue;
            }
            InnerTrialDefinition definition = InnerTrialDefinition.forDrug(drug);
            if (definition == null) {
                MyDrugs.getLOGGER().debug("Skipping Inner Dimension trial announcement for {} because no definition is registered", drug);
                continue;
            }
            player.sendSystemMessage(Component.translatable(definition.titleKey())
                    .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
            player.sendSystemMessage(Component.translatable(definition.hintKey())
                    .withStyle(ChatFormatting.GRAY));
            progress.announced.add(drug);
            InnerProgressionMilestones.trialStarted(player, drug);
        }
    }

    private static boolean canAttempt(InnerDimensionSavedData.IslandState island, DrugId drug) {
        return island != null && drug != null && island.hasIntegrated(drug) && !island.hasCompletedInnerTrial(drug);
    }

    private static boolean isOwnIsland(InnerDimensionSavedData.IslandState island, BlockPos pos) {
        return island != null
                && pos != null
                && InnerTerrain.slotCenter(pos.getX()) == island.centerX()
                && InnerTerrain.slotCenter(pos.getZ()) == island.centerZ();
    }

    private static boolean isNearLandmark(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            DrugId drug,
            BlockPos pos,
            double radius
    ) {
        return horizontalDistanceSqr(pos, landmarkSurface(level, island, drug)) <= radius * radius;
    }

    private static BlockPos landmarkSurface(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            DrugId drug
    ) {
        BlockPos landmark = InnerRegionMap.landmarkFor(island.centerX(), island.centerZ(), drug);
        return InnerPlacement.surfaceTop(level, landmark.getX(), landmark.getZ());
    }

    private static boolean isHashSocket(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            BlockPos pos
    ) {
        BlockPos center = landmarkSurface(level, island, DrugId.HASH);
        return InnerTrialDefinition.horizontalIndex(pos, center, InnerTrialDefinition.hashSockets()) >= 0;
    }

    private static int hashFilledSockets(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island
    ) {
        BlockPos center = landmarkSurface(level, island, DrugId.HASH);
        int filled = 0;
        for (BlockPos offset : InnerTrialDefinition.hashSockets()) {
            BlockPos top = InnerPlacement.surfaceTop(level, center.getX() + offset.getX(), center.getZ() + offset.getZ());
            if (isHashSocketMaterial(level.getBlockState(top.above()).getBlock())) {
                filled++;
            }
        }
        return filled;
    }

    private static boolean isHashSocketMaterial(Block block) {
        return block == Blocks.CALCITE
                || block == Blocks.AMETHYST_BLOCK
                || block == ModInnerDimensionBlocks.PRESSED_CALM_NODE.get();
    }

    private static boolean isCalmingFlora(Block block) {
        return block == ModInnerDimensionBlocks.CALMING_FERN.get()
                || block == ModInnerDimensionBlocks.CALMING_BUSH.get()
                || block == ModInnerDimensionBlocks.MOSS_BREATH_CARPET.get()
                || block == ModInnerDimensionBlocks.BREATH_GRASS.get()
                || block == ModInnerDimensionBlocks.MYCELIAL_ROOT.get();
    }

    private static boolean isMethTool(ItemStack stack) {
        return stack.is(Items.WATER_BUCKET)
                || stack.is(Items.SNOWBALL)
                || stack.is(ModItems.CURRENT_REGULATOR.get())
                || stack.is(ModItems.THUNDER_BOTTLE.get())
                || stack.is(ModItems.LIGHTNING_BOTTLE.get());
    }

    private static void consumeMethTool(ServerPlayer player, InteractionHand hand, ItemStack stack) {
        if (player.getAbilities().instabuild || stack.is(ModItems.CURRENT_REGULATOR.get())) {
            return;
        }
        if (stack.is(Items.WATER_BUCKET)) {
            player.setItemInHand(hand, new ItemStack(Items.BUCKET));
        } else {
            stack.shrink(1);
        }
    }

    private static void consumeOne(ServerPlayer player, ItemStack stack) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    private static ItemStack rewardFor(DrugId drug) {
        Supplier<Item> reward = TRIAL_REWARDS.get(drug);
        if (reward == null) {
            MyDrugs.getLOGGER().warn("No Inner Dimension trial reward mapping for {}", drug);
            return ItemStack.EMPTY;
        }
        return new ItemStack(reward.get());
    }

    private static Map<DrugId, Supplier<Item>> trialRewards() {
        EnumMap<DrugId, Supplier<Item>> rewards = new EnumMap<>(DrugId.class);
        rewards.put(DrugId.COFFEE, () -> ModItems.LUCID_EXTRACT.get());
        rewards.put(DrugId.TOBACCO, () -> ModItems.BITTER_RESIDUE.get());
        rewards.put(DrugId.WEED, () -> ModItems.CALMING_SPORES.get());
        rewards.put(DrugId.HASH, () -> ModItems.PRESSED_CALM.get());
        rewards.put(DrugId.ALCOHOL, () -> ModItems.FERMENTED_MEMORY.get());
        rewards.put(DrugId.COCAINE, () -> ModItems.REDLINE_FUEL.get());
        rewards.put(DrugId.LSD, () -> ModItems.DREAM_RESIDUE.get());
        rewards.put(DrugId.METH, () -> ModItems.OVERDRIVE_FUEL.get());
        rewards.put(DrugId.MUSHROOMS, () -> ModItems.MYCELIAL_INSIGHT.get());
        return Map.copyOf(rewards);
    }

    static boolean hasRewardMappingForTest(DrugId drug) {
        return drug != null && TRIAL_REWARDS.containsKey(drug);
    }

    private static InnerTrialProgress progress(ServerPlayer player) {
        return PROGRESS.computeIfAbsent(player.getUUID(), ignored -> new InnerTrialProgress());
    }

    private static void clearDrugProgress(InnerTrialProgress progress, DrugId drug) {
        // Announcements are intentionally retained for the session: completing a trial makes
        // canAttempt false, while a full trial reset removes the whole progress object.
        switch (drug) {
            case COFFEE -> progress.coffeeTicks = 0;
            case TOBACCO -> progress.tobaccoStep = 0;
            case WEED -> progress.weedPlacements = 0;
            case COCAINE -> progress.cocaineStartTick = -1L;
            case METH -> progress.methMask = 0;
            case MUSHROOMS -> progress.mushroomMask = 0;
            default -> {
            }
        }
    }

    private static double horizontalDistanceSqr(BlockPos first, BlockPos second) {
        double dx = first.getX() - second.getX();
        double dz = first.getZ() - second.getZ();
        return dx * dx + dz * dz;
    }

    private static double horizontalSpeedSqr(ServerPlayer player) {
        double x = player.getDeltaMovement().x;
        double z = player.getDeltaMovement().z;
        return x * x + z * z;
    }
}
