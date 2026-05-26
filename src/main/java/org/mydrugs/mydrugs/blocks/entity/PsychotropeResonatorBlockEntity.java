package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.addiction.data.DrugAddictionStats;
import org.mydrugs.mydrugs.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.CuratedDrugChain;
import org.mydrugs.mydrugs.core.drug.integration.IntegratedTrait;
import org.mydrugs.mydrugs.core.drug.integration.IntegrationConstants;
import org.mydrugs.mydrugs.core.drug.integration.IntegrationService;
import org.mydrugs.mydrugs.diary.DiaryEntry;
import org.mydrugs.mydrugs.diary.DiaryEntryType;
import org.mydrugs.mydrugs.diary.PlayerDiaryAttachment;
import org.mydrugs.mydrugs.dimension.InnerDimensionService;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.machine.MachineStatus;
import org.mydrugs.mydrugs.machine.MachineStatusProvider;
import org.mydrugs.mydrugs.machine.MachineSync;
import org.mydrugs.mydrugs.menu.PsychotropeResonatorMenu;
import org.mydrugs.mydrugs.progression.PsyKnowledgeKey;
import org.mydrugs.mydrugs.progression.PsyKnowledgeManager;
import org.mydrugs.mydrugs.recovery.RecoveryRoomManager;
import org.mydrugs.mydrugs.recovery.RecoveryRoomReport;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class PsychotropeResonatorBlockEntity extends BaseContainerBlockEntity implements MachineStatusProvider {
    public static final int SLOT_DREAM_MATERIAL = 0;
    public static final int SLOT_INTEGRATION_CORE = 1;
    public static final int SLOT_DIARY = 2;
    public static final int SLOT_OUTPUT = 3;
    public static final int SLOT_COUNT = 4;

    private static final ResourceLocation DREAM_ALIGNMENT_RITUAL =
            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "dream_alignment");
    private static final ResourceLocation RECOVERY_RESONANCE_RITUAL =
            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "recovery_resonance");
    private static final ResourceLocation INTEGRATION_RITUAL =
            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "integration");
    private static final int DREAM_ALIGNMENT_TICKS = 160;
    private static final int RECOVERY_RESONANCE_TICKS = 120;
    private static final int INTEGRATION_TICKS = 240;
    private static final int COOLDOWN_TICKS = 80;
    private static final int MAX_PLAYER_DISTANCE_SQR = 64 * 64;

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int progress;
    private int maxProgress;
    private int cooldownTicks;
    private @Nullable UUID activePlayer;
    private @Nullable ResourceLocation activeRitual;
    private @Nullable DrugId activeIntegrationDrug;
    private boolean dimensionReady;
    private ResonatorState state = ResonatorState.IDLE;
    private MachineStatus machineStatus = MachineStatus.IDLE;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> maxProgress;
                case 2 -> state.networkId();
                case 3 -> cooldownTicks;
                case 4 -> dimensionReady ? 1 : 0;
                case 5 -> machineStatus.networkId();
                case 6 -> activeIntegrationDrug == null ? 0 : activeIntegrationDrug.networkId();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> maxProgress = value;
                case 2 -> state = ResonatorState.byNetworkId(value);
                case 3 -> cooldownTicks = Math.max(0, value);
                case 4 -> dimensionReady = value != 0;
                case 5 -> machineStatus = MachineStatus.byNetworkId(value);
                case 6 -> activeIntegrationDrug = DrugId.byNetworkId(value);
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return PsychotropeResonatorMenu.DATA_COUNT;
        }
    };

    public PsychotropeResonatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PSYCHOTROPE_RESONATOR.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PsychotropeResonatorBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        boolean changed = false;
        if (be.cooldownTicks > 0) {
            be.cooldownTicks--;
            changed |= be.setState(ResonatorState.COOLDOWN);
            changed |= be.setMachineStatus(MachineStatus.PAUSED);
            if (be.cooldownTicks == 0 && be.activeRitual == null) {
                changed |= be.setState(be.dimensionReady ? ResonatorState.DIMENSION_READY : ResonatorState.IDLE);
                changed |= be.setMachineStatus(MachineStatus.IDLE);
            }
            if (changed) {
                be.sync();
            }
            return;
        }

        if (be.activeRitual == null) {
            changed |= be.idleState();
            if (changed) {
                be.sync();
            }
            return;
        }

        ServerPlayer player = be.resolveActivePlayer(serverLevel);
        if (player == null || !be.isPlayerCloseEnough(player)) {
            be.cancelActiveRitual(MachineStatus.BLOCKED);
            be.sync();
            return;
        }

        if (!be.revalidateActiveRitual(player)) {
            be.cancelActiveRitual(be.machineStatus);
            be.sync();
            return;
        }

        be.progress++;
        changed = true;
        changed |= be.setState(be.activeRitual.equals(INTEGRATION_RITUAL)
                ? ResonatorState.INTEGRATING
                : ResonatorState.RESONATING);
        changed |= be.setMachineStatus(MachineStatus.RUNNING);

        if (be.progress >= be.maxProgress) {
            be.completeActiveRitual(player);
            changed = true;
        }

        if (changed) {
            be.sync();
        }
    }

    public static boolean isDreamMaterial(ItemStack stack) {
        return stack.is(ModItems.DREAM_RESIDUE.get())
                || stack.is(ModItems.MYCELIAL_INSIGHT.get())
                || stack.is(ModItems.CALMING_RESIN.get());
    }

    public static boolean isIntegrationCore(ItemStack stack) {
        return stack.is(ModItems.INTEGRATION_CORE.get());
    }

    public static boolean isDiary(ItemStack stack) {
        return stack.is(ModItems.PERSONAL_DIARY.get());
    }

    public boolean onButtonPressed(Player player, int id) {
        if (!(player instanceof ServerPlayer serverPlayer) || !isPlayerCloseEnough(serverPlayer)) {
            return false;
        }
        if (this.activeRitual != null || this.cooldownTicks > 0) {
            fail(serverPlayer, MachineStatus.PAUSED, "message.mydrugs.resonator.busy");
            return true;
        }
        if (!hasLysergicKnowledge(serverPlayer)) {
            fail(serverPlayer, MachineStatus.BLOCKED, "message.mydrugs.resonator.requires_lysergic");
            return true;
        }

        if (id == PsychotropeResonatorMenu.DREAM_ALIGNMENT_BUTTON_ID) {
            return startDreamAlignment(serverPlayer);
        }
        if (id == PsychotropeResonatorMenu.RECOVERY_RESONANCE_BUTTON_ID) {
            return startRecoveryResonance(serverPlayer);
        }
        if (id == PsychotropeResonatorMenu.INTEGRATION_BUTTON_ID) {
            return startIntegration(serverPlayer);
        }
        if (id == PsychotropeResonatorMenu.OPEN_DIMENSION_BUTTON_ID) {
            if (!InnerDimensionService.canOpen(serverPlayer)) {
                fail(serverPlayer, MachineStatus.DIMENSION_UNAVAILABLE, "message.mydrugs.inner_dimension.requires_integration");
            } else if (!InnerDimensionService.open(serverPlayer, this.worldPosition)) {
                fail(serverPlayer, MachineStatus.DIMENSION_UNAVAILABLE, "message.mydrugs.inner_dimension.unavailable");
            }
            return true;
        }
        return false;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.mydrugs.psychotrope_resonator");
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new PsychotropeResonatorMenu(
                containerId,
                inventory,
                this,
                this.data,
                ContainerLevelAccess.create(this.level, this.worldPosition)
        );
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_DREAM_MATERIAL -> isDreamMaterial(stack);
            case SLOT_INTEGRATION_CORE -> isIntegrationCore(stack);
            case SLOT_DIARY -> isDiary(stack);
            default -> false;
        };
    }

    @Override
    public MachineStatus getMachineStatus() {
        return this.machineStatus;
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
        this.progress = input.getIntOr("progress", 0);
        this.maxProgress = input.getIntOr("max_progress", 0);
        this.cooldownTicks = input.getIntOr("cooldown_ticks", 0);
        this.dimensionReady = input.getBooleanOr("dimension_ready", false);
        this.state = ResonatorState.byNetworkId(input.getIntOr("state", ResonatorState.IDLE.networkId()));
        this.machineStatus = MachineStatus.byNetworkId(input.getIntOr("machine_status", MachineStatus.IDLE.networkId()));
        this.activePlayer = readUuid(input.getStringOr("active_player", ""));
        this.activeRitual = readResourceLocation(input.getStringOr("active_ritual", ""));
        this.activeIntegrationDrug = DrugId.bySerializedNameOrNull(input.getStringOr("active_integration_drug", ""));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        output.putInt("progress", this.progress);
        output.putInt("max_progress", this.maxProgress);
        output.putInt("cooldown_ticks", this.cooldownTicks);
        output.putBoolean("dimension_ready", this.dimensionReady);
        output.putInt("state", this.state.networkId());
        output.putInt("machine_status", this.machineStatus.networkId());
        output.putString("active_player", this.activePlayer == null ? "" : this.activePlayer.toString());
        output.putString("active_ritual", this.activeRitual == null ? "" : this.activeRitual.toString());
        output.putString("active_integration_drug", this.activeIntegrationDrug == null ? "" : this.activeIntegrationDrug.serializedName());
    }

    private boolean startDreamAlignment(ServerPlayer player) {
        Validation validation = validateDreamAlignment(player);
        if (!validation.ok()) {
            fail(player, validation.status(), validation.messageKey());
            return true;
        }
        startRitual(player, DREAM_ALIGNMENT_RITUAL, null, DREAM_ALIGNMENT_TICKS, ResonatorState.RESONATING);
        return true;
    }

    private boolean startRecoveryResonance(ServerPlayer player) {
        Validation validation = validateRecoveryResonance(player);
        if (!validation.ok()) {
            fail(player, validation.status(), validation.messageKey());
            return true;
        }
        startRitual(player, RECOVERY_RESONANCE_RITUAL, null, RECOVERY_RESONANCE_TICKS, ResonatorState.RESONATING);
        return true;
    }

    private boolean startIntegration(ServerPlayer player) {
        IntegrationCandidate candidate = findIntegrationCandidate(player);
        if (!candidate.ok()) {
            fail(player, candidate.status(), candidate.messageKey());
            return true;
        }
        startRitual(player, INTEGRATION_RITUAL, candidate.drug(), INTEGRATION_TICKS, ResonatorState.INTEGRATING);
        return true;
    }

    private void startRitual(ServerPlayer player, ResourceLocation ritual, @Nullable DrugId drug, int ticks, ResonatorState activeState) {
        this.activePlayer = player.getUUID();
        this.activeRitual = ritual;
        this.activeIntegrationDrug = drug;
        this.progress = 0;
        this.maxProgress = ticks;
        setState(activeState);
        setMachineStatus(MachineStatus.RUNNING);
        sync();
    }

    private void completeActiveRitual(ServerPlayer player) {
        ResourceLocation ritual = this.activeRitual;
        DrugId drug = this.activeIntegrationDrug;
        this.activeRitual = null;
        this.activeIntegrationDrug = null;
        this.activePlayer = null;
        this.progress = 0;
        this.maxProgress = 0;
        this.cooldownTicks = COOLDOWN_TICKS;

        if (DREAM_ALIGNMENT_RITUAL.equals(ritual)) {
            this.getItem(SLOT_DREAM_MATERIAL).shrink(1);
            this.dimensionReady = true;
            InnerDimensionService.markDreamCoordinate(player, this.worldPosition);
            writeDiary(player, "resonator_dream_alignment", "The dream has coordinates now. It still asks for integration, not escape.", DrugId.LSD);
            setState(ResonatorState.DIMENSION_READY);
            setMachineStatus(MachineStatus.PAUSED);
            setChanged();
            return;
        }

        if (RECOVERY_RESONANCE_RITUAL.equals(ritual)) {
            this.getItem(SLOT_DREAM_MATERIAL).shrink(1);
            applyRecoveryResonance(player);
            writeDiary(player, "resonator_recovery_resonance", "The room held. The craving had somewhere to go besides back into me.", null);
            setState(this.dimensionReady ? ResonatorState.DIMENSION_READY : ResonatorState.COOLDOWN);
            setMachineStatus(MachineStatus.PAUSED);
            setChanged();
            return;
        }

        if (INTEGRATION_RITUAL.equals(ritual) && drug != null) {
            if (IntegrationService.canIntegrate(player, drug) && IntegrationService.integrate(player, drug)) {
                this.getItem(SLOT_DREAM_MATERIAL).shrink(1);
                this.getItem(SLOT_INTEGRATION_CORE).shrink(1);
                InnerDimensionService.onIntegration(player, drug);
                writeDiary(player, "resonator_integration", "It is mine now, clean. Not a hunger. Not a shortcut. A shape I survived.", drug);
                setState(InnerDimensionService.canOpen(player) ? ResonatorState.DIMENSION_READY : ResonatorState.COOLDOWN);
                setMachineStatus(MachineStatus.PAUSED);
            } else {
                fail(player, MachineStatus.NO_MATCHING_RECIPE, "message.mydrugs.resonator.integration_lost");
            }
            setChanged();
        }
    }

    private boolean revalidateActiveRitual(ServerPlayer player) {
        if (DREAM_ALIGNMENT_RITUAL.equals(this.activeRitual)) {
            Validation validation = validateDreamAlignment(player);
            if (!validation.ok()) {
                setMachineStatus(validation.status());
            }
            return validation.ok();
        }
        if (RECOVERY_RESONANCE_RITUAL.equals(this.activeRitual)) {
            Validation validation = validateRecoveryResonance(player);
            if (!validation.ok()) {
                setMachineStatus(validation.status());
            }
            return validation.ok();
        }
        if (INTEGRATION_RITUAL.equals(this.activeRitual)) {
            DrugId drug = this.activeIntegrationDrug;
            if (drug == null) {
                setMachineStatus(MachineStatus.NO_MATCHING_RECIPE);
                return false;
            }
            IntegrationCandidate candidate = validateIntegrationCandidate(player, drug);
            if (!candidate.ok()) {
                setMachineStatus(candidate.status());
            }
            return candidate.ok();
        }
        setMachineStatus(MachineStatus.UNKNOWN_ERROR);
        return false;
    }

    private Validation validateDreamAlignment(ServerPlayer player) {
        if (!hasLysergicKnowledge(player)) {
            return Validation.fail(MachineStatus.BLOCKED, "message.mydrugs.resonator.requires_lysergic");
        }
        if (!this.getItem(SLOT_DREAM_MATERIAL).is(ModItems.DREAM_RESIDUE.get())) {
            return Validation.fail(MachineStatus.MISSING_INPUT_ITEM, "message.mydrugs.resonator.missing_dream_residue");
        }
        if (!hasDiaryContext(player)) {
            return Validation.fail(MachineStatus.MISSING_DIARY_CONTEXT, "message.mydrugs.resonator.missing_diary");
        }
        return Validation.success();
    }

    private Validation validateRecoveryResonance(ServerPlayer player) {
        if (!this.getItem(SLOT_DREAM_MATERIAL).is(ModItems.CALMING_RESIN.get())) {
            return Validation.fail(MachineStatus.MISSING_INPUT_ITEM, "message.mydrugs.resonator.missing_calming_resin");
        }
        if (!hasDiaryContext(player)) {
            return Validation.fail(MachineStatus.MISSING_DIARY_CONTEXT, "message.mydrugs.resonator.missing_diary");
        }
        if (!hasValidRecoveryRoom(player)) {
            return Validation.fail(MachineStatus.MISSING_RECOVERY_CONTEXT, "message.mydrugs.resonator.missing_recovery_room");
        }
        return Validation.success();
    }

    private IntegrationCandidate findIntegrationCandidate(ServerPlayer player) {
        for (DrugId drug : CuratedDrugChain.ORDER) {
            IntegrationCandidate candidate = validateIntegrationCandidate(player, drug);
            if (candidate.ok()) {
                return candidate;
            }
        }
        return IntegrationCandidate.fail(MachineStatus.NO_MATCHING_RECIPE, "message.mydrugs.resonator.no_eligible_drug");
    }

    private IntegrationCandidate validateIntegrationCandidate(ServerPlayer player, DrugId drug) {
        IntegratedTrait trait = IntegratedTrait.bySource(drug);
        if (trait == null) {
            return IntegrationCandidate.fail(MachineStatus.NO_MATCHING_RECIPE, "message.mydrugs.resonator.no_eligible_drug");
        }
        if (!hasKnowledgeForDrug(player, drug)) {
            return IntegrationCandidate.fail(MachineStatus.BLOCKED, "message.mydrugs.resonator.missing_drug_knowledge");
        }
        if (!IntegrationService.canIntegrate(player, drug)) {
            return IntegrationCandidate.fail(MachineStatus.NO_MATCHING_RECIPE, "message.mydrugs.resonator.no_eligible_drug");
        }
        if (!this.getItem(SLOT_INTEGRATION_CORE).is(ModItems.INTEGRATION_CORE.get())) {
            return IntegrationCandidate.fail(MachineStatus.MISSING_CATALYST, "message.mydrugs.resonator.missing_integration_core");
        }
        if (!hasMaterialForDrug(drug)) {
            return IntegrationCandidate.fail(MachineStatus.MISSING_INPUT_ITEM, materialMessageFor(drug));
        }
        if (!hasDiaryContext(player)) {
            return IntegrationCandidate.fail(MachineStatus.MISSING_DIARY_CONTEXT, "message.mydrugs.resonator.missing_diary");
        }
        if (!hasValidRecoveryRoom(player)) {
            return IntegrationCandidate.fail(MachineStatus.MISSING_RECOVERY_CONTEXT, "message.mydrugs.resonator.missing_recovery_room");
        }
        return IntegrationCandidate.ok(drug);
    }

    private boolean hasMaterialForDrug(DrugId drug) {
        ItemStack stack = this.getItem(SLOT_DREAM_MATERIAL);
        return switch (drug) {
            case LSD -> stack.is(ModItems.DREAM_RESIDUE.get());
            case MUSHROOMS -> stack.is(ModItems.MYCELIAL_INSIGHT.get());
            default -> stack.is(ModItems.CALMING_RESIN.get());
        };
    }

    private String materialMessageFor(DrugId drug) {
        return switch (drug) {
            case LSD -> "message.mydrugs.resonator.missing_dream_residue";
            case MUSHROOMS -> "message.mydrugs.resonator.missing_mycelial_insight";
            default -> "message.mydrugs.resonator.missing_calming_resin";
        };
    }

    private boolean hasLysergicKnowledge(ServerPlayer player) {
        return PsyKnowledgeManager.has(player, PsyKnowledgeKey.LYSERGIC);
    }

    private boolean hasKnowledgeForDrug(ServerPlayer player, DrugId drug) {
        PsyKnowledgeKey key = CuratedDrugChain.stageKnowledge(drug);
        return key != null && PsyKnowledgeManager.has(player, key);
    }

    private boolean hasDiaryContext(ServerPlayer player) {
        if (!this.getItem(SLOT_DIARY).is(ModItems.PERSONAL_DIARY.get())) {
            return false;
        }
        PlayerDiaryAttachment diary = player.getData(ModAttachments.PLAYER_DIARY.get());
        return !diary.getEntries().isEmpty();
    }

    private boolean hasValidRecoveryRoom(ServerPlayer player) {
        Optional<RecoveryRoomReport> room = RecoveryRoomManager.getBestRoom(player);
        return room.isPresent() && RecoveryRoomManager.isValidRecoveryRoom(room.get());
    }

    private void applyRecoveryResonance(ServerPlayer player) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        if (stats.perDrug.isEmpty()) {
            return;
        }

        for (Map.Entry<DrugId, DrugAddictionStats> entry : stats.perDrug.entrySet()) {
            DrugAddictionStats drugStats = entry.getValue();
            if (drugStats.peakHistoricalAddiction < IntegrationConstants.PEAK_THRESHOLD
                    || drugStats.integrationStage >= 2
                    || drugStats.addictionValue >= drugStats.peakHistoricalAddiction) {
                continue;
            }

            drugStats.addictionValue = Math.max(
                    0.0F,
                    drugStats.addictionValue
                            - IntegrationConstants.DETOX_PER_ACTION * IntegrationConstants.RECOVERY_RESONANCE_ACTION_WEIGHT
            );
            if (drugStats.recoveryProgress < IntegrationConstants.RECOVERY_RESONANCE_PROGRESS_CAP) {
                drugStats.recoveryProgress = Math.min(
                        IntegrationConstants.RECOVERY_RESONANCE_PROGRESS_CAP,
                        drugStats.recoveryProgress
                                + IntegrationConstants.RECOVERY_PROGRESS_PER_ACTION * IntegrationConstants.RECOVERY_RESONANCE_ACTION_WEIGHT
                );
            }
        }
    }

    private @Nullable ServerPlayer resolveActivePlayer(ServerLevel level) {
        return this.activePlayer == null ? null : level.getServer().getPlayerList().getPlayer(this.activePlayer);
    }

    private boolean isPlayerCloseEnough(ServerPlayer player) {
        return player.level() == this.level
                && player.distanceToSqr(
                this.worldPosition.getX() + 0.5D,
                this.worldPosition.getY() + 0.5D,
                this.worldPosition.getZ() + 0.5D
        ) <= MAX_PLAYER_DISTANCE_SQR;
    }

    private boolean idleState() {
        ResonatorState next = this.dimensionReady ? ResonatorState.DIMENSION_READY : ResonatorState.IDLE;
        boolean changed = setState(next);
        changed |= setMachineStatus(MachineStatus.IDLE);
        return changed;
    }

    private void cancelActiveRitual(MachineStatus status) {
        this.activeRitual = null;
        this.activeIntegrationDrug = null;
        this.activePlayer = null;
        this.progress = 0;
        this.maxProgress = 0;
        setState(ResonatorState.BLOCKED);
        setMachineStatus(status);
        setChanged();
    }

    private void fail(ServerPlayer player, MachineStatus status, String messageKey) {
        setState(status == MachineStatus.INVALID_MULTIBLOCK ? ResonatorState.INVALID_STRUCTURE : ResonatorState.BLOCKED);
        setMachineStatus(status);
        player.displayClientMessage(Component.translatable(messageKey).withStyle(ChatFormatting.DARK_PURPLE), true);
        sync();
    }

    private void writeDiary(ServerPlayer player, String source, String content, @Nullable DrugId drug) {
        PlayerDiaryAttachment diary = player.getData(ModAttachments.PLAYER_DIARY.get());
        long gameTime = player.level().getGameTime();
        diary.append(new DiaryEntry(
                PlayerDiaryAttachment.currentDay(gameTime),
                gameTime,
                DiaryEntryType.AUTO,
                PlayerDiaryAttachment.sanitizeCustomContent(content),
                source,
                drug == null ? "" : drug.serializedName()
        ));
        diary.markWritten(gameTime);
    }

    private boolean setState(ResonatorState state) {
        if (this.state == state) {
            return false;
        }
        this.state = state;
        return true;
    }

    private boolean setMachineStatus(MachineStatus status) {
        if (this.machineStatus == status) {
            return false;
        }
        this.machineStatus = status;
        return true;
    }

    private void sync() {
        setChanged();
        MachineSync.sync(this);
    }

    private static @Nullable UUID readUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static @Nullable ResourceLocation readResourceLocation(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return ResourceLocation.tryParse(value);
    }

    public enum ResonatorState {
        IDLE(0),
        INVALID_STRUCTURE(1),
        AWAITING_CONDITIONS(2),
        READY(3),
        RESONATING(4),
        INTEGRATING(5),
        DIMENSION_READY(6),
        COOLDOWN(7),
        BLOCKED(8);

        private final int networkId;

        ResonatorState(int networkId) {
            this.networkId = networkId;
        }

        public int networkId() {
            return networkId;
        }

        public String translationKey() {
            return "resonator_state.mydrugs." + name().toLowerCase();
        }

        public static ResonatorState byNetworkId(int networkId) {
            for (ResonatorState state : values()) {
                if (state.networkId == networkId) {
                    return state;
                }
            }
            return IDLE;
        }
    }

    private record Validation(boolean ok, MachineStatus status, String messageKey) {
        static Validation success() {
            return new Validation(true, MachineStatus.RUNNING, "");
        }

        static Validation fail(MachineStatus status, String messageKey) {
            return new Validation(false, status, messageKey);
        }
    }

    private record IntegrationCandidate(boolean ok, @Nullable DrugId drug, MachineStatus status, String messageKey) {
        static IntegrationCandidate ok(DrugId drug) {
            return new IntegrationCandidate(true, drug, MachineStatus.RUNNING, "");
        }

        static IntegrationCandidate fail(MachineStatus status, String messageKey) {
            return new IntegrationCandidate(false, null, status, messageKey);
        }
    }
}
