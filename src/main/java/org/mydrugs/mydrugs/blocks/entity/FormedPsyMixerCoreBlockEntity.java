package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.blocks.PsyMixerMultiblock;
import org.mydrugs.mydrugs.blocks.entity.psy_mixer.PsyMixerRitualEngine;
import org.mydrugs.mydrugs.blocks.entity.psy_mixer.PsyMixerRitualFocus;
import org.mydrugs.mydrugs.blocks.entity.psy_mixer.PsyMixerRitualJudgement;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.DrugRegistry;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.core.drug.ritual.RitualIngredientEffectRegistry;
import org.mydrugs.mydrugs.core.drug.ritual.ServerDrugFormulaRegistry;
import org.mydrugs.mydrugs.effects.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.effects.addiction.data.DrugAddictionStats;
import org.mydrugs.mydrugs.effects.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.effects.addiction.config.AddictionConstants;
import org.mydrugs.mydrugs.effects.addiction.manager.effect.DrugEffectRuntimeManager;
import org.mydrugs.mydrugs.effects.addiction.manager.state.BadTripManager;
import org.mydrugs.mydrugs.effects.addiction.manager.state.StressManager;
import org.mydrugs.mydrugs.menu.PsyMixerMenu;
import org.mydrugs.mydrugs.machine.manual.ManualMachineSpeedHelper;
import org.mydrugs.mydrugs.machine.manual.ManualMachineType;
import org.mydrugs.mydrugs.progression.PsyKnowledgeKey;
import org.mydrugs.mydrugs.progression.PsyKnowledgeManager;
import org.mydrugs.mydrugs.progression.PsyMixerMasteryAttachment;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.recipes.psy_mixer.PsyMixerRecipe;
import org.mydrugs.mydrugs.recipes.psy_mixer.PsyMixerRecipeInput;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class FormedPsyMixerCoreBlockEntity extends BlockEntity implements MenuProvider, Container {
    public static final int SLOT_COUNT = PsyMixerMultiblock.SLOT_COUNT;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final List<SavedSlot> savedSlots = new ArrayList<>();

    private Direction facing = Direction.NORTH;
    private boolean running = false;
    private int progress = 0;
    private int ritualMaxTime = 400;
    private float instability = 0.25F;
    private @Nullable ResourceLocation activeRecipeId;
    private @Nullable UUID ritualPlayer;
    private int messageCooldown = 0;

    // PASS 2: rhythm timing
    private float timingWindow = 0.12F;
    private int rhythmInputCooldown = 0;
    private int goodHits = 0;
    private int badHits = 0;
    private int rhythmMessageCooldown = 0;
    private int focusIndex = PsyMixerRitualFocus.CATALYST.id();
    private float resonance = PsyMixerRitualEngine.START_RESONANCE;
    private float zoneMotionScale = 1.0F;
    private float zoneSizeScale = 1.0F;
    private int streak = 0;
    private int lastJudgement = PsyMixerRitualJudgement.NONE.id();
    private int feedbackTicks = 0;
    private float lastAccuracy = 0.0F;
    private boolean restoringStructure = false;

    public FormedPsyMixerCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FORMED_PSY_MIXER_CORE.get(), pos, state);
    }

    public Direction getFacing() { return facing; }
    public boolean isRunning() { return running; }
    public int getProgress() { return progress; }
    public int getRitualMaxTime() { return ritualMaxTime; }
    public float getInstability() { return instability; }
    public NonNullList<ItemStack> getItems() { return items; }
    public int getRhythmInputCooldown() { return rhythmInputCooldown; }
    public int getGoodHits() { return goodHits; }
    public int getBadHits() { return badHits; }
    public int getFocusIndex() { return focusIndex; }
    public float getResonance() { return resonance; }
    public int getStreak() { return streak; }
    public int getLastJudgement() { return lastJudgement; }
    public int getFeedbackTicks() { return feedbackTicks; }
    public float getLastAccuracy() { return lastAccuracy; }

    public void initFromActivation(Direction facing, List<SavedSlot> saved) {
        this.facing = facing;
        this.savedSlots.clear();
        this.savedSlots.addAll(saved);
        markDirtyAndSync();
    }

    public boolean isStructureIntact() {
        if (this.level == null || savedSlots.isEmpty()) {
            return false;
        }
        if (!this.level.getBlockState(this.worldPosition).is(ModBlocks.FORMED_PSY_MIXER_CORE.get())) {
            return false;
        }
        if (this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        }
        for (SavedSlot slot : savedSlots) {
            BlockPos worldPos = slot.worldPos;
            if (worldPos.equals(this.worldPosition)) {
                continue;
            }
            BlockState currentState = this.level.getBlockState(worldPos);
            if (slot.blockState.isAir()) {
                if (!currentState.isAir()) {
                    return false;
                }
                continue;
            }
            if (!currentState.is(ModBlocks.FORMED_PSY_MIXER_PART.get())) {
                return false;
            }
            if (!(this.level.getBlockEntity(worldPos) instanceof FormedPsyMixerPartBlockEntity part)
                    || !this.worldPosition.equals(part.getCorePos())) {
                return false;
            }
        }
        return true;
    }

    public ItemStack getRenderStack(int slot) {
        return slot >= 0 && slot < SLOT_COUNT ? items.get(slot) : ItemStack.EMPTY;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FormedPsyMixerCoreBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!be.running) return;
        if (be.activeRecipeId == null) {
            be.cancelRitual();
            return;
        }

        be.progress++;
        if (be.messageCooldown > 0) be.messageCooldown--;
        if (be.rhythmMessageCooldown > 0) be.rhythmMessageCooldown--;
        if (be.rhythmInputCooldown > 0) be.rhythmInputCooldown--;
        if (be.feedbackTicks > 0) be.feedbackTicks--;
        if (be.progress % 40 == 0) {
            be.resonance = Math.max(0.0F, be.resonance - 0.015F);
        }

        // Particles
        if (be.progress % 6 == 0) {
            int particleCount = 1 + Math.round(be.resonance * 3.0F);
            serverLevel.sendParticles(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5,
                    particleCount, 0.2, 0.1, 0.2, 0.01
            );
        }

        if (be.progress >= be.ritualMaxTime) {
            be.completeRitual(serverLevel);
        }

        be.setChanged();
    }

    public boolean canStartRitual() {
        return !running;
    }

    public boolean tryStartRitual(ServerPlayer player) {
        if (this.level == null || this.level.isClientSide()) return false;
        if (running) {
            sendMessage(player, "message.mydrugs.psy_mixer.already_running");
            return false;
        }

        Optional<RecipeHolder<PsyMixerRecipe>> match = ((ServerLevel) this.level).recipeAccess().getRecipeFor(
                ModRecipeTypes.PSY_MIXER.get(),
                buildInput(),
                this.level
        );
        if (match.isEmpty()) {
            sendRandomMessage(player, NO_RECIPE);
            return false;
        }

        RecipeHolder<PsyMixerRecipe> holder = match.get();
        PsyMixerRecipe recipe = holder.value();

        if (recipe.requiredKnowledge().isPresent()) {
            PsyKnowledgeKey key = new PsyKnowledgeKey(recipe.requiredKnowledge().get());
            if (!PsyKnowledgeManager.has(player, key)) {
                sendRandomMessage(player, MISSING_KNOWLEDGE);
                return false;
            }
        }

        if (recipe.requiredDrug().isPresent() && recipe.requiredLifetimeDose() > 0.0F) {
            DrugId drugId = DrugId.bySerializedNameOrNull(recipe.requiredDrug().get());
            if (drugId == null) {
                sendRandomMessage(player, MISSING_KNOWLEDGE);
                return false;
            }
            PlayerAddictionStats playerStats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
            DrugAddictionStats drugStats = playerStats.getDrugStats(drugId);
            float lifetime = drugStats == null ? 0.0F : drugStats.lifetimeDoseConsumed;
            if (lifetime + 0.001F < recipe.requiredLifetimeDose()) {
                sendRandomMessage(player, MISSING_LIFETIME_DOSE, recipe.requiredLifetimeDose(), lifetime);
                return false;
            }
        }

        PlayerAddictionStats playerStats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        if (!hasRequiredDrugCategory(playerStats, recipe.requiredDrugCategory())) {
            sendRandomMessage(player, MISSING_KNOWLEDGE);
            return false;
        }
        if (!hasRequiredActiveEffect(player, recipe.requiredActiveEffect())) {
            sendRandomMessage(player, MISSING_KNOWLEDGE);
            return false;
        }
        if (recipe.requiredBadTripState() && !hasBadTripState(player, playerStats)) {
            sendRandomMessage(player, MISSING_KNOWLEDGE);
            return false;
        }

        ItemStack resultPreview = recipe.result();
        if (!items.get(PsyMixerMultiblock.SLOT_OUTPUT).isEmpty()) {
            ItemStack out = items.get(PsyMixerMultiblock.SLOT_OUTPUT);
            if (!ItemStack.isSameItemSameComponents(out, resultPreview) || out.getCount() + resultPreview.getCount() > out.getMaxStackSize()) {
                sendMessage(player, "message.mydrugs.psy_mixer.output_blocked");
                return false;
            }
        }

        PsyMixerMasteryAttachment mastery = player.getData(ModAttachments.PSY_MIXER_MASTERY.get());
        ResourceLocation recipeId = holder.id().location();

        float speedMul = mastery.getSpeedMultiplier(recipeId);
        float instabReduction = mastery.getInstabilityReduction(recipeId);
        float manualDrugSpeed = ManualMachineSpeedHelper.getSpeedMultiplier(player, ManualMachineType.PSY_MIXER);
        float ritualZoneWidthBonus = ManualMachineSpeedHelper.getRitualZoneWidthBonus(player);
        float ritualInstabilityReduction = ManualMachineSpeedHelper.getRitualInstabilityReduction(player);
        float ritualZoneMotionScale = ManualMachineSpeedHelper.getRitualZoneMotionScale(player);

        float effInstab = recipe.baseInstability() + recipe.ritualStabilityModifier() - instabReduction - ritualInstabilityReduction;
        if (recipe.stabilizer().isPresent() && !items.get(PsyMixerMultiblock.SLOT_STABILIZER).isEmpty()) {
            effInstab -= 0.10F;
        }
        effInstab = Math.max(0.03F, Math.min(0.95F, effInstab));

        this.activeRecipeId = recipeId;
        float recipeSpeed = Math.max(0.25F, 1.0F + recipe.machineSpeedModifier());
        this.ritualMaxTime = Math.max(20, Math.round(recipe.ritualTime() * speedMul / (manualDrugSpeed * recipeSpeed)));
        this.progress = 0;
        this.instability = effInstab;
        this.running = true;
        this.ritualPlayer = player.getUUID();
        this.timingWindow = ManualMachineSpeedHelper.hasPsychedelicRitualInsight(player)
                ? 1.0F
                : Math.min(0.62F, 0.08F + mastery.getTimingWindowBonus(recipeId) + ritualZoneWidthBonus);
        this.zoneMotionScale = ritualZoneMotionScale;
        this.zoneSizeScale = ritualZoneMotionScale;
        this.goodHits = 0;
        this.badHits = 0;
        this.focusIndex = findNextAvailableFocus(-1);
        this.resonance = PsyMixerRitualEngine.START_RESONANCE;
        this.streak = 0;
        this.lastJudgement = PsyMixerRitualJudgement.NONE.id();
        this.feedbackTicks = 0;
        this.lastAccuracy = 0.0F;
        sendRandomMessage(player, START_MESSAGES);

        if (this.level instanceof ServerLevel sl) {
            sl.playSound(null, this.worldPosition, SoundEvents.SOUL_ESCAPE.value(), SoundSource.BLOCKS, 0.8F, 0.6F);
        }

        markDirtyAndSync();
        return true;
    }

    private PsyMixerRecipeInput buildInput() {
        return new PsyMixerRecipeInput(
                items.get(PsyMixerMultiblock.SLOT_BASE),
                items.get(PsyMixerMultiblock.SLOT_MATERIAL),
                items.get(PsyMixerMultiblock.SLOT_CATALYST),
                items.get(PsyMixerMultiblock.SLOT_STABILIZER),
                items.get(PsyMixerMultiblock.SLOT_VESSEL)
        );
    }

    private void completeRitual(ServerLevel level) {
        if (activeRecipeId == null) {
            cancelRitual();
            return;
        }

        PsyMixerRecipeInput input = buildInput();
        Optional<RecipeHolder<PsyMixerRecipe>> match = level.recipeAccess().recipeMap()
                .byType(ModRecipeTypes.PSY_MIXER.get())
                .stream()
                .filter(holder -> holder.id().location().equals(activeRecipeId))
                .findFirst();
        if (match.isEmpty()) {
            cancelRitual();
            return;
        }
        PsyMixerRecipe recipe = match.get().value();

        ServerPlayer player = ritualPlayer == null ? null : level.getServer().getPlayerList().getPlayer(ritualPlayer);

        float finalInstability = PsyMixerRitualEngine.finalInstability(instability, resonance);
        boolean success = level.random.nextFloat() >= finalInstability;

        if (success) {
            var formula = RitualIngredientEffectRegistry.buildFormula(
                    input.base(),
                    List.of(input.material(), input.catalyst(), input.stabilizer(), input.vessel())
            );
            consumeInputs(recipe, true);
            boolean completedOutput = player == null || ServerDrugFormulaRegistry.finishOrRequestName(player, this, formula);

            if (player != null) {
                PsyMixerMasteryAttachment mastery = player.getData(ModAttachments.PSY_MIXER_MASTERY.get());
                mastery.incrementCompleted(activeRecipeId);
                if (completedOutput) {
                    sendRandomMessage(player, SUCCESS_MESSAGES);
                }
            }

            level.playSound(null, worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.6F, 1.4F);
            for (int i = 0; i < 30; i++) {
                level.sendParticles(
                        ParticleTypes.END_ROD,
                        worldPosition.getX() + 0.5, worldPosition.getY() + 0.8, worldPosition.getZ() + 0.5,
                        1, 0.4, 0.6, 0.4, 0.05
                );
            }
        } else {
            consumeInputs(recipe, false);
            ItemStack failureResult = recipe.failureResult();
            if (failureResult.isEmpty()) {
                failureResult = new ItemStack(ModItems_UNSTABLE_RESIDUE());
            }
            placeIntoOutput(failureResult);

            if (player != null) {
                PsyMixerMasteryAttachment mastery = player.getData(ModAttachments.PSY_MIXER_MASTERY.get());
                mastery.incrementFailed(activeRecipeId);
                applyFailureSeverity(player, recipe.failureSeverity());
                sendRandomMessage(player, FAIL_MESSAGES);
            }

            level.playSound(null, worldPosition, SoundEvents.WITHER_HURT, SoundSource.BLOCKS, 0.5F, 0.6F);
            for (int i = 0; i < 30; i++) {
                level.sendParticles(
                        new DustParticleOptions(0x331144, 1.5F),
                        worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
                        1, 0.4, 0.4, 0.4, 0.0
                );
            }
        }

        running = false;
        progress = 0;
        activeRecipeId = null;
        ritualPlayer = null;
        streak = 0;
        feedbackTicks = 0;
        markDirtyAndSync();
    }

    private static net.minecraft.world.item.Item ModItems_UNSTABLE_RESIDUE() {
        return org.mydrugs.mydrugs.items.ModItems.UNSTABLE_RESIDUE.get();
    }

    private static boolean hasRequiredDrugCategory(PlayerAddictionStats stats, Optional<String> requiredCategory) {
        if (requiredCategory.isEmpty()) {
            return true;
        }

        DrugCategory category;
        try {
            category = DrugCategory.valueOf(requiredCategory.get().trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return false;
        }

        for (DrugId drugId : stats.getTrackedDrugIds()) {
            if (DrugRegistry.getCategory(drugId) != category) {
                continue;
            }
            DrugAddictionStats drugStats = stats.getDrugStats(drugId);
            if (drugStats != null && drugStats.currentDose() > 0.001F) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasRequiredActiveEffect(ServerPlayer player, Optional<String> requiredEffect) {
        if (requiredEffect.isEmpty()) {
            return true;
        }

        EffectType type = EffectType.bySerializedNameOrNull(requiredEffect.get());
        return type != null && DrugEffectRuntimeManager.getServerIntensity(player, type) > 0.001F;
    }

    private static boolean hasBadTripState(ServerPlayer player, PlayerAddictionStats stats) {
        return BadTripManager.isActive(stats);
    }

    private static void applyFailureSeverity(ServerPlayer player, float severity) {
        if (severity <= 0.0F) {
            return;
        }

        float clamped = Math.min(2.0F, severity);
        StressManager.addStress(player, clamped * AddictionConstants.STRESS_PSY_MIXER_FAILURE_SPIKE_SCALE);
        DrugEffectRuntimeManager.addEffect(player, EffectType.CONFUSION, 0.15F + clamped * 0.25F, 20 * 8);
        DrugEffectRuntimeManager.addEffect(player, EffectType.CUSTOM_NAUSEA, 0.10F + clamped * 0.20F, 20 * 6);
        if (clamped >= 1.0F) {
            DrugEffectRuntimeManager.addEffect(player, EffectType.CAMERA_SWAY, 0.10F + clamped * 0.10F, 20 * 5);
        }
    }

    private void consumeInputs(PsyMixerRecipe recipe, boolean success) {
        shrink(PsyMixerMultiblock.SLOT_BASE);
        shrink(PsyMixerMultiblock.SLOT_MATERIAL);
        if (recipe.catalyst().isPresent()) shrink(PsyMixerMultiblock.SLOT_CATALYST);
        if (recipe.stabilizer().isPresent()) shrink(PsyMixerMultiblock.SLOT_STABILIZER);
        if (recipe.vessel().isPresent()) {
            boolean preserve = success ? recipe.preserveVesselOnSuccess() : recipe.preserveVesselOnFailure();
            if (!preserve) shrink(PsyMixerMultiblock.SLOT_VESSEL);
        }
    }

    private void shrink(int slot) {
        ItemStack stack = items.get(slot);
        if (stack.isEmpty()) return;
        stack.shrink(1);
        if (stack.isEmpty()) {
            items.set(slot, ItemStack.EMPTY);
        }
    }

    public void placeIntoOutput(ItemStack result) {
        if (result.isEmpty()) return;
        ItemStack current = items.get(PsyMixerMultiblock.SLOT_OUTPUT);
        if (current.isEmpty()) {
            items.set(PsyMixerMultiblock.SLOT_OUTPUT, result.copy());
        } else if (ItemStack.isSameItemSameComponents(current, result)) {
            current.grow(result.getCount());
        }
    }

    public void cancelRitual() {
        running = false;
        progress = 0;
        activeRecipeId = null;
        ritualPlayer = null;
        goodHits = 0;
        badHits = 0;
        resonance = PsyMixerRitualEngine.START_RESONANCE;
        zoneMotionScale = 1.0F;
        zoneSizeScale = 1.0F;
        streak = 0;
        lastJudgement = PsyMixerRitualJudgement.NONE.id();
        feedbackTicks = 0;
        lastAccuracy = 0.0F;
        markDirtyAndSync();
    }

    public float getTimingWindow() {
        return timingWindow;
    }

    public float getCurrentTimingWindow() {
        return PsyMixerRitualEngine.timingWindow(timingWindow, progress, ritualMaxTime, zoneSizeScale);
    }

    public float getCurrentTargetPhase() {
        return PsyMixerRitualEngine.targetPhase(PsyMixerRitualFocus.byId(focusIndex), progress, ritualMaxTime, zoneMotionScale);
    }

    private int findNextAvailableFocus(int currentIndex) {
        for (int step = 1; step <= PsyMixerRitualFocus.count(); step++) {
            int next = Math.floorMod(currentIndex + step, PsyMixerRitualFocus.count());
            PsyMixerRitualFocus focus = PsyMixerRitualFocus.byId(next);
            if (!items.get(focus.slot()).isEmpty()) {
                return next;
            }
        }
        return PsyMixerRitualFocus.MATERIAL.id();
    }

    /**
     * PASS 2: Returns server-authoritative ritual phase as a value in [0, 1).
     * The client renders a marker at this phase. Sacred zone is at phase ~ 0.0 +/- timingWindow/2.
     */
    public float getServerPhase() {
        return PsyMixerRitualEngine.phase(progress, ritualMaxTime);
    }

    public void handleRhythmInput(ServerPlayer player) {
        if (!running) return;
        if (rhythmInputCooldown > 0) {
            rhythmInputCooldown--;
            return;
        }
        rhythmInputCooldown = PsyMixerRitualEngine.INPUT_COOLDOWN_TICKS;

        PsyMixerRitualFocus focus = PsyMixerRitualFocus.byId(focusIndex);
        PsyMixerRitualEngine.JudgementResult result = PsyMixerRitualEngine.judge(
                getServerPhase(),
                PsyMixerRitualEngine.targetPhase(focus, progress, ritualMaxTime, zoneMotionScale),
                PsyMixerRitualEngine.timingWindow(timingWindow, progress, ritualMaxTime, zoneSizeScale),
                streak
        );
        PsyMixerRitualJudgement judgement = result.judgement();
        lastJudgement = judgement.id();
        feedbackTicks = PsyMixerRitualEngine.FEEDBACK_TICKS;
        lastAccuracy = result.accuracy();

        if (judgement.isHit()) {
            goodHits++;
            streak++;
            progress = Math.min(ritualMaxTime, progress + result.progressBonus());
            instability = PsyMixerRitualEngine.applyInstability(instability, judgement, streak);
            resonance = PsyMixerRitualEngine.applyResonance(resonance, judgement, streak);
            focusIndex = findNextAvailableFocus(focusIndex);
            sendRhythmMessage(player, judgement);
            if (this.level instanceof ServerLevel sl) {
                sl.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.35F, 1.0F + Math.min(0.8F, streak * 0.06F));
                sl.sendParticles(ParticleTypes.END_ROD,
                        worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5,
                        4 + Math.min(8, streak), 0.2, 0.2, 0.2, 0.02);
            }
        } else if (judgement == PsyMixerRitualJudgement.NEAR) {
            streak = 0;
            progress = Math.min(ritualMaxTime, progress + result.progressBonus());
            instability = PsyMixerRitualEngine.applyInstability(instability, judgement, streak);
            resonance = PsyMixerRitualEngine.applyResonance(resonance, judgement, streak);
            sendRhythmMessage(player, judgement);
            if (this.level instanceof ServerLevel sl) {
                sl.playSound(null, worldPosition, SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.BLOCKS, 0.25F, 0.75F);
                sl.sendParticles(new DustParticleOptions(0xCCAA44, 1.2F),
                        worldPosition.getX() + 0.5, worldPosition.getY() + 0.85, worldPosition.getZ() + 0.5,
                        4, 0.25, 0.18, 0.25, 0.0);
            }
        } else {
            badHits++;
            streak = 0;
            instability = PsyMixerRitualEngine.applyInstability(instability, judgement, streak);
            resonance = PsyMixerRitualEngine.applyResonance(resonance, judgement, streak);
            sendRhythmMessage(player, judgement);
            if (this.level instanceof ServerLevel sl) {
                sl.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.BLOCKS, 0.35F, 0.65F);
                sl.sendParticles(new DustParticleOptions(0x882244, 1.5F),
                        worldPosition.getX() + 0.5, worldPosition.getY() + 0.7, worldPosition.getZ() + 0.5,
                        4, 0.3, 0.3, 0.3, 0.0);
            }
        }
        markDirtyAndSync();
    }

    private void sendRhythmMessage(ServerPlayer player, PsyMixerRitualJudgement judgement) {
        if (player == null || judgement == PsyMixerRitualJudgement.NONE) return;
        player.displayClientMessage(ritualComponent(judgement.messageKey(), streak), true);
        rhythmMessageCooldown = 10;
    }

    public boolean onPartBroken(ServerLevel level, BlockPos brokenPos) {
        return restoreStructure(level, brokenPos, true);
    }

    public boolean onBrokenByPlayer(ServerLevel level, BlockPos brokenPos) {
        return restoreStructure(level, brokenPos, true);
    }

    public boolean onExploded(ServerLevel level, BlockPos brokenPos) {
        return restoreStructure(level, brokenPos, false);
    }

    public boolean onPartExploded(ServerLevel level, BlockPos brokenPos) {
        return restoreStructure(level, brokenPos, false);
    }

    private boolean restoreStructure(ServerLevel level, BlockPos brokenPos, boolean dropBrokenOriginal) {
        if (restoringStructure || savedSlots.isEmpty()) return false;
        restoringStructure = true;
        try {
            closeOpenMenus(level);

            // Drop inventory
            for (ItemStack stack : items) {
                if (!stack.isEmpty()) {
                    Containers.dropItemStack(level, brokenPos.getX() + 0.5, brokenPos.getY() + 0.5, brokenPos.getZ() + 0.5, stack);
                }
            }
            items.clear();

            for (SavedSlot slot : savedSlots) {
                BlockPos worldPos = slot.worldPos;
                BlockState currentState = level.getBlockState(worldPos);
                boolean isThisCore = worldPos.equals(this.worldPosition);
                boolean isFormedPart = currentState.is(ModBlocks.FORMED_PSY_MIXER_PART.get());

                if (worldPos.equals(brokenPos)) {
                    // The broken slot - drop original block and remove formed
                    BlockState original = slot.blockState;
                    if (dropBrokenOriginal) {
                        ItemStack drop = new ItemStack(original.getBlock().asItem());
                        if (!drop.isEmpty()) {
                            Containers.dropItemStack(level, worldPos.getX() + 0.5, worldPos.getY() + 0.5, worldPos.getZ() + 0.5, drop);
                        }
                    }
                    level.removeBlock(worldPos, false);
                } else if (isThisCore || isFormedPart) {
                    // Restore original
                    level.setBlock(worldPos, slot.blockState, Block.UPDATE_ALL);
                }
            }

            // Visual rupture
            level.playSound(null, brokenPos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 0.8F, 0.6F);
            for (int i = 0; i < 20; i++) {
                level.sendParticles(
                        new DustParticleOptions(0x442233, 1.5F),
                        brokenPos.getX() + 0.5, brokenPos.getY() + 0.5, brokenPos.getZ() + 0.5,
                        1, 0.5, 0.5, 0.5, 0.0
                );
            }

            savedSlots.clear();
            running = false;
            progress = 0;
            activeRecipeId = null;
            ritualPlayer = null;
            goodHits = 0;
            badHits = 0;
            rhythmInputCooldown = 0;
            resonance = PsyMixerRitualEngine.START_RESONANCE;
            zoneMotionScale = 1.0F;
            zoneSizeScale = 1.0F;
            streak = 0;
            lastJudgement = PsyMixerRitualJudgement.NONE.id();
            feedbackTicks = 0;
            lastAccuracy = 0.0F;
            setChanged();
            return true;
        } finally {
            restoringStructure = false;
        }
    }

    private void closeOpenMenus(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            if (player.containerMenu instanceof PsyMixerMenu menu && this.worldPosition.equals(menu.getCorePos())) {
                player.closeContainer();
            }
        }
    }

    private void sendMessage(@Nullable ServerPlayer player, String key) {
        if (player == null) return;
        player.displayClientMessage(ritualComponent(key), true);
    }

    private void sendRandomMessage(@Nullable ServerPlayer player, String[] keys) {
        sendRandomMessage(player, keys, new Object[0]);
    }

    private void sendRandomMessage(@Nullable ServerPlayer player, String[] keys, Object... args) {
        if (player == null || keys.length == 0) return;
        RandomSource random = player.level().getRandom();
        String key = keys[random.nextInt(keys.length)];
        player.displayClientMessage(ritualComponent(key, args), false);
        messageCooldown = 30;
    }

    private static Component ritualComponent(String key, Object... args) {
        return Component.literal("[")
                .withStyle(ChatFormatting.RED)
                .append(Component.literal("???").withStyle(ChatFormatting.RED, ChatFormatting.OBFUSCATED))
                .append(Component.literal("] ").withStyle(ChatFormatting.RED))
                .append(Component.translatable(key, args).withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    private void markDirtyAndSync() {
        setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("menu.mydrugs.psy_mixer");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        if (!isStructureIntact()) {
            return null;
        }
        return new PsyMixerMenu(containerId, inventory, this, this.worldPosition);
    }

    // Container
    @Override
    public int getContainerSize() { return SLOT_COUNT; }

    @Override
    public boolean isEmpty() {
        for (ItemStack s : items) if (!s.isEmpty()) return false;
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < SLOT_COUNT ? items.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot < 0 || slot >= SLOT_COUNT) return ItemStack.EMPTY;
        if (running) return ItemStack.EMPTY;
        ItemStack taken = items.get(slot).split(amount);
        if (items.get(slot).isEmpty()) items.set(slot, ItemStack.EMPTY);
        markDirtyAndSync();
        return taken;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) return ItemStack.EMPTY;
        ItemStack stack = items.get(slot);
        items.set(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= SLOT_COUNT) return;
        items.set(slot, stack);
        markDirtyAndSync();
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.level == null || this.level.getBlockEntity(this.worldPosition) != this) return false;
        return player.distanceToSqr(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < SLOT_COUNT; i++) items.set(i, ItemStack.EMPTY);
        markDirtyAndSync();
    }

    public boolean canPlayerInsert(int slot) {
        return !running && slot != PsyMixerMultiblock.SLOT_OUTPUT;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("facing", facing.getSerializedName());
        output.putBoolean("running", running);
        output.putInt("progress", progress);
        output.putInt("ritual_max_time", ritualMaxTime);
        output.putFloat("instability", instability);
        output.putFloat("timing_window", timingWindow);
        output.putInt("rhythm_input_cooldown", rhythmInputCooldown);
        output.putInt("good_hits", goodHits);
        output.putInt("bad_hits", badHits);
        output.putInt("focus_index", focusIndex);
        output.putFloat("resonance", resonance);
        output.putFloat("zone_motion_scale", zoneMotionScale);
        output.putFloat("zone_size_scale", zoneSizeScale);
        output.putInt("streak", streak);
        output.putInt("last_judgement", lastJudgement);
        output.putInt("feedback_ticks", feedbackTicks);
        output.putFloat("last_accuracy", lastAccuracy);
        if (activeRecipeId != null) output.putString("active_recipe", activeRecipeId.toString());
        if (ritualPlayer != null) {
            output.putLong("ritual_player_msb", ritualPlayer.getMostSignificantBits());
            output.putLong("ritual_player_lsb", ritualPlayer.getLeastSignificantBits());
        }

        ValueOutput.ValueOutputList itemList = output.childrenList("items");
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack stack = items.get(i);
            if (stack.isEmpty()) continue;
            ValueOutput child = itemList.addChild();
            child.putInt("slot", i);
            child.store("stack", ItemStack.CODEC, stack);
        }
        if (itemList.isEmpty()) output.discard("items");

        ValueOutput.ValueOutputList savedList = output.childrenList("saved_slots");
        for (SavedSlot slot : savedSlots) {
            ValueOutput child = savedList.addChild();
            child.putInt("x", slot.worldPos.getX());
            child.putInt("y", slot.worldPos.getY());
            child.putInt("z", slot.worldPos.getZ());
            child.store("state", BlockState.CODEC, slot.blockState);
        }
        if (savedList.isEmpty()) output.discard("saved_slots");
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        try {
            facing = Direction.valueOf(input.getStringOr("facing", "NORTH").toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            facing = Direction.NORTH;
        }
        running = input.getBooleanOr("running", false);
        progress = input.getIntOr("progress", 0);
        ritualMaxTime = input.getIntOr("ritual_max_time", 400);
        instability = input.getFloatOr("instability", 0.25F);
        timingWindow = input.getFloatOr("timing_window", 0.12F);
        rhythmInputCooldown = input.getIntOr("rhythm_input_cooldown", 0);
        goodHits = input.getIntOr("good_hits", 0);
        badHits = input.getIntOr("bad_hits", 0);
        focusIndex = input.getIntOr("focus_index", PsyMixerRitualFocus.CATALYST.id());
        resonance = input.getFloatOr("resonance", PsyMixerRitualEngine.START_RESONANCE);
        zoneMotionScale = input.getFloatOr("zone_motion_scale", 1.0F);
        zoneSizeScale = input.getFloatOr("zone_size_scale", 1.0F);
        streak = input.getIntOr("streak", 0);
        lastJudgement = input.getIntOr("last_judgement", PsyMixerRitualJudgement.NONE.id());
        feedbackTicks = input.getIntOr("feedback_ticks", 0);
        lastAccuracy = input.getFloatOr("last_accuracy", 0.0F);
        String recipeStr = input.getStringOr("active_recipe", "");
        activeRecipeId = recipeStr.isEmpty() ? null : ResourceLocation.tryParse(recipeStr);
        long msb = input.getLongOr("ritual_player_msb", 0L);
        long lsb = input.getLongOr("ritual_player_lsb", 0L);
        ritualPlayer = (msb == 0L && lsb == 0L) ? null : new UUID(msb, lsb);

        for (int i = 0; i < SLOT_COUNT; i++) items.set(i, ItemStack.EMPTY);
        for (ValueInput child : input.childrenListOrEmpty("items")) {
            int slot = child.getIntOr("slot", -1);
            ItemStack stack = child.read("stack", ItemStack.CODEC).orElse(ItemStack.EMPTY);
            if (slot >= 0 && slot < SLOT_COUNT && !stack.isEmpty()) {
                items.set(slot, stack);
            }
        }

        savedSlots.clear();
        for (ValueInput child : input.childrenListOrEmpty("saved_slots")) {
            int x = child.getIntOr("x", 0);
            int y = child.getIntOr("y", 0);
            int z = child.getIntOr("z", 0);
            Optional<BlockState> stateOpt = child.read("state", BlockState.CODEC);
            stateOpt.ifPresent(state -> savedSlots.add(new SavedSlot(new BlockPos(x, y, z), state)));
        }
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ValueInput input) {
        super.onDataPacket(connection, input);
    }

    public record SavedSlot(BlockPos worldPos, BlockState blockState) {
    }

    private static final String[] START_MESSAGES = {
            "message.mydrugs.psy_mixer.start.1",
            "message.mydrugs.psy_mixer.start.2",
            "message.mydrugs.psy_mixer.start.3",
            "message.mydrugs.psy_mixer.start.4"
    };
    private static final String[] SUCCESS_MESSAGES = {
            "message.mydrugs.psy_mixer.success.1",
            "message.mydrugs.psy_mixer.success.2",
            "message.mydrugs.psy_mixer.success.3",
            "message.mydrugs.psy_mixer.success.4"
    };
    private static final String[] FAIL_MESSAGES = {
            "message.mydrugs.psy_mixer.fail.1",
            "message.mydrugs.psy_mixer.fail.2",
            "message.mydrugs.psy_mixer.fail.3",
            "message.mydrugs.psy_mixer.fail.4",
            "message.mydrugs.psy_mixer.fail.5"
    };
    private static final String[] NO_RECIPE = {
            "message.mydrugs.psy_mixer.no_recipe.1",
            "message.mydrugs.psy_mixer.no_recipe.2",
            "message.mydrugs.psy_mixer.no_recipe.3",
            "message.mydrugs.psy_mixer.no_recipe.4"
    };
    private static final String[] MISSING_KNOWLEDGE = {
            "message.mydrugs.psy_mixer.missing_knowledge.1",
            "message.mydrugs.psy_mixer.missing_knowledge.2",
            "message.mydrugs.psy_mixer.missing_knowledge.3",
            "message.mydrugs.psy_mixer.missing_knowledge.4"
    };
    private static final String[] MISSING_LIFETIME_DOSE = {
            "message.mydrugs.psy_mixer.missing_dose.1",
            "message.mydrugs.psy_mixer.missing_dose.2"
    };
}
