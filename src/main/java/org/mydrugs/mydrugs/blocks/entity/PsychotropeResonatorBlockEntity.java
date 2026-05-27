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
import org.mydrugs.mydrugs.core.drug.integration.IntegrationCoreTier;
import org.mydrugs.mydrugs.core.drug.integration.IntegrationCoreTiers;
import org.mydrugs.mydrugs.core.drug.integration.IntegrationMaterials;
import org.mydrugs.mydrugs.core.drug.integration.IntegrationService;
import org.mydrugs.mydrugs.core.drug.integration.RecoveryProgressManager;
import org.mydrugs.mydrugs.diary.DiaryEntry;
import org.mydrugs.mydrugs.diary.DiaryEntryType;
import org.mydrugs.mydrugs.diary.IntegrationDiary;
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

import java.util.Optional;
import java.util.UUID;

public final class PsychotropeResonatorBlockEntity extends BaseContainerBlockEntity implements MachineStatusProvider {
    public static final int SLOT_MATERIAL = 0;
    public static final int SLOT_DREAM_MATERIAL = SLOT_MATERIAL;
    public static final int SLOT_INTEGRATION_CORE = 1;
    public static final int SLOT_DIARY = 2;
    public static final int SLOT_COUNT = 3;
    public static final int CHECK_KNOWLEDGE = 1;
    public static final int CHECK_REQUIREMENT = 1 << 1;
    public static final int CHECK_LOW_ADDICTION = 1 << 2;
    public static final int CHECK_RECOVERY = 1 << 3;
    public static final int CHECK_LIFETIME = 1 << 4;
    public static final int CHECK_MATERIAL = 1 << 5;
    public static final int CHECK_CORE = 1 << 6;
    public static final int CHECK_DIARY = 1 << 7;
    public static final int CHECK_ROOM = 1 << 8;
    public static final int CHECK_NOT_INTEGRATED = 1 << 9;

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
    private @Nullable UUID lastViewer;
    private @Nullable ResourceLocation activeRitual;
    private @Nullable DrugId activeIntegrationDrug;
    private boolean dimensionReady;
    private PsychotropeResonatorFailureReason lastFailureReason = PsychotropeResonatorFailureReason.NONE;
    private @Nullable DrugId lastCandidateDrug;
    private int lastChecklistMask;
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
                case 4 -> viewerDreamAligned() ? 1 : 0;
                case 5 -> machineStatus.networkId();
                case 6 -> activeIntegrationDrug == null ? 0 : activeIntegrationDrug.networkId();
                case 7 -> lastFailureReason.networkId();
                case 8 -> lastCandidateDrug == null ? 0 : lastCandidateDrug.networkId();
                case 9 -> lastChecklistMask;
                case 10 -> viewerCanOpenDimension() ? 1 : 0;
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
                case 7 -> lastFailureReason = PsychotropeResonatorFailureReason.byNetworkId(value);
                case 8 -> lastCandidateDrug = DrugId.byNetworkId(value);
                case 9 -> lastChecklistMask = value;
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
                changed |= be.setState(be.viewerCanOpenDimension() ? ResonatorState.DIMENSION_READY : ResonatorState.IDLE);
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
            // revalidateActiveRitual already set machineStatus + lastFailureReason; surface the
            // granular reason to the player as a chat message so they can react.
            DrugId surfacedDrug = be.lastCandidateDrug;
            int surfacedMask = be.lastChecklistMask;
            MachineStatus surfacedStatus = be.machineStatus;
            PsychotropeResonatorFailureReason surfacedReason = be.lastFailureReason;
            be.cancelActiveRitual(surfacedStatus);
            be.fail(player, surfacedStatus, surfacedReason, surfacedDrug, surfacedMask);
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

    public static boolean isIntegrationMaterial(ItemStack stack) {
        return IntegrationMaterials.isIntegrationMaterial(stack);
    }

    public static boolean isDreamMaterial(ItemStack stack) {
        return isIntegrationMaterial(stack);
    }

    public static boolean isIntegrationCore(ItemStack stack) {
        return IntegrationCoreTiers.isAnyCore(stack);
    }

    /**
     * True when the slotted core is at least {@code required} tier. {@code null} required tier
     * (uncurated drug) always fails — there is no integration to perform.
     */
    public static boolean coreSatisfies(ItemStack stack, @Nullable IntegrationCoreTier required) {
        if (required == null) {
            return false;
        }
        IntegrationCoreTier slotted = IntegrationCoreTier.ofStack(stack);
        return slotted != null && slotted.satisfies(required);
    }

    public static boolean isDiary(ItemStack stack) {
        return stack.is(ModItems.PERSONAL_DIARY.get());
    }

    public boolean onButtonPressed(Player player, int id) {
        if (!(player instanceof ServerPlayer serverPlayer) || !isPlayerCloseEnough(serverPlayer)) {
            return false;
        }
        this.lastViewer = serverPlayer.getUUID();
        if (this.activeRitual != null || this.cooldownTicks > 0) {
            fail(serverPlayer, MachineStatus.PAUSED, PsychotropeResonatorFailureReason.BUSY);
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
            InnerDimensionService.OpenStatus status = InnerDimensionService.openStatus(serverPlayer);
            if (status != InnerDimensionService.OpenStatus.READY) {
                fail(serverPlayer, MachineStatus.DIMENSION_UNAVAILABLE, PsychotropeResonatorFailureReason.fromOpenStatus(status));
            } else if (!InnerDimensionService.open(serverPlayer, this.worldPosition)) {
                fail(serverPlayer, MachineStatus.DIMENSION_UNAVAILABLE, PsychotropeResonatorFailureReason.INNER_DIMENSION_UNAVAILABLE);
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
        this.lastViewer = inventory.player.getUUID();
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
            case SLOT_MATERIAL -> isIntegrationMaterial(stack);
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
            fail(player, validation.status(), validation.reason());
            return true;
        }
        startRitual(player, DREAM_ALIGNMENT_RITUAL, null, DREAM_ALIGNMENT_TICKS, ResonatorState.RESONATING);
        return true;
    }

    private boolean startRecoveryResonance(ServerPlayer player) {
        Validation validation = validateRecoveryResonance(player);
        if (!validation.ok()) {
            fail(player, validation.status(), validation.reason());
            return true;
        }
        startRitual(player, RECOVERY_RESONANCE_RITUAL, null, RECOVERY_RESONANCE_TICKS, ResonatorState.RESONATING);
        return true;
    }

    private boolean startIntegration(ServerPlayer player) {
        IntegrationCandidate candidate = findIntegrationCandidate(player);
        if (!candidate.ok()) {
            fail(player, candidate.status(), candidate.reason(), candidate.drug(), candidate.checklistMask());
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
        this.lastFailureReason = PsychotropeResonatorFailureReason.NONE;
        this.lastCandidateDrug = drug;
        this.lastChecklistMask = drug == null ? 0 : buildChecklist(player, drug);
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
            this.getItem(SLOT_MATERIAL).shrink(1);
            InnerDimensionService.markDreamCoordinate(player, this.worldPosition);
            IntegrationDiary.dreamAligned(player);
            setState(InnerDimensionService.canOpen(player) ? ResonatorState.DIMENSION_READY : ResonatorState.COOLDOWN);
            setMachineStatus(MachineStatus.PAUSED);
            setChanged();
            return;
        }

        if (RECOVERY_RESONANCE_RITUAL.equals(ritual)) {
            this.getItem(SLOT_MATERIAL).shrink(1);
            RecoveryProgressManager.applyRecoveryResonance(player, RecoveryRoomManager.getBestRoom(player).orElse(null));
            IntegrationDiary.recoveryResonance(player);
            setState(InnerDimensionService.canOpen(player) ? ResonatorState.DIMENSION_READY : ResonatorState.COOLDOWN);
            setMachineStatus(MachineStatus.PAUSED);
            setChanged();
            return;
        }

        if (INTEGRATION_RITUAL.equals(ritual) && drug != null) {
            // Defensive: revalidateActiveRitual is called every tick before progress increments,
            // so reaching this branch with a failing candidate would require a state change racing
            // through completion. Re-validate here for a granular PsychotropeResonatorFailureReason regardless.
            IntegrationCandidate candidate = validateIntegrationCandidate(player, drug);
            if (candidate.ok() && IntegrationService.integrate(player, drug)) {
                this.getItem(SLOT_MATERIAL).shrink(1);
                this.getItem(SLOT_INTEGRATION_CORE).shrink(1);
                InnerDimensionService.onIntegration(player, drug);
                writeDiary(player, "resonator_integration", "It is mine now, clean. Not a hunger. Not a shortcut. A shape I survived.", drug);
                setState(InnerDimensionService.canOpen(player) ? ResonatorState.DIMENSION_READY : ResonatorState.COOLDOWN);
                setMachineStatus(MachineStatus.PAUSED);
            } else {
                PsychotropeResonatorFailureReason reason = candidate.ok() ? PsychotropeResonatorFailureReason.INTEGRATION_LOST : candidate.reason();
                MachineStatus status = candidate.ok() ? MachineStatus.NO_MATCHING_RECIPE : candidate.status();
                fail(player, status, reason, candidate.drug(), candidate.checklistMask());
            }
            setChanged();
        }
    }

    private boolean revalidateActiveRitual(ServerPlayer player) {
        if (DREAM_ALIGNMENT_RITUAL.equals(this.activeRitual)) {
            Validation validation = validateDreamAlignment(player);
            if (!validation.ok()) {
                setMachineStatus(validation.status());
                this.lastFailureReason = validation.reason();
            }
            return validation.ok();
        }
        if (RECOVERY_RESONANCE_RITUAL.equals(this.activeRitual)) {
            Validation validation = validateRecoveryResonance(player);
            if (!validation.ok()) {
                setMachineStatus(validation.status());
                this.lastFailureReason = validation.reason();
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
                this.lastFailureReason = candidate.reason();
                this.lastCandidateDrug = candidate.drug();
                this.lastChecklistMask = candidate.checklistMask();
            }
            return candidate.ok();
        }
        setMachineStatus(MachineStatus.UNKNOWN_ERROR);
        return false;
    }

    private Validation validateDreamAlignment(ServerPlayer player) {
        if (player.getData(ModAttachments.PLAYER_INTEGRATION.get()).isDreamAligned()) {
            return Validation.fail(MachineStatus.BLOCKED, PsychotropeResonatorFailureReason.DREAM_ALREADY_ALIGNED);
        }
        if (!hasLysergicKnowledge(player)) {
            return Validation.fail(MachineStatus.BLOCKED, PsychotropeResonatorFailureReason.REQUIRES_LYSERGIC);
        }
        if (!this.getItem(SLOT_MATERIAL).is(ModItems.DREAM_RESIDUE.get())) {
            return Validation.fail(MachineStatus.MISSING_INPUT_ITEM, PsychotropeResonatorFailureReason.MISSING_DREAM_RESIDUE);
        }
        if (!hasDiaryContext(player)) {
            return Validation.fail(MachineStatus.MISSING_DIARY_CONTEXT, PsychotropeResonatorFailureReason.MISSING_DIARY);
        }
        return Validation.success();
    }

    private Validation validateRecoveryResonance(ServerPlayer player) {
        if (!this.getItem(SLOT_MATERIAL).is(ModItems.CALMING_RESIN.get())) {
            return Validation.fail(MachineStatus.MISSING_INPUT_ITEM, PsychotropeResonatorFailureReason.MISSING_CALMING_RESIN);
        }
        if (!hasDiaryContext(player)) {
            return Validation.fail(MachineStatus.MISSING_DIARY_CONTEXT, PsychotropeResonatorFailureReason.MISSING_DIARY);
        }
        if (!hasValidRecoveryRoom(player)) {
            return Validation.fail(MachineStatus.MISSING_RECOVERY_CONTEXT, PsychotropeResonatorFailureReason.MISSING_RECOVERY_ROOM);
        }
        return Validation.success();
    }

    private IntegrationCandidate findIntegrationCandidate(ServerPlayer player) {
        IntegrationCandidate bestFailure = null;
        for (DrugId drug : CuratedDrugChain.ORDER) {
            IntegrationCandidate candidate = validateIntegrationCandidate(player, drug);
            if (candidate.ok()) {
                return candidate;
            }
            if (bestFailure == null && isRelevantCandidate(player, drug)) {
                bestFailure = candidate;
            }
        }
        return bestFailure != null
                ? bestFailure
                : IntegrationCandidate.fail(null, MachineStatus.NO_MATCHING_RECIPE, PsychotropeResonatorFailureReason.NO_ELIGIBLE_DRUG, 0);
    }

    private IntegrationCandidate validateIntegrationCandidate(ServerPlayer player, DrugId drug) {
        IntegratedTrait trait = IntegratedTrait.bySource(drug);
        int checklist = buildChecklist(player, drug);
        if (trait == null) {
            return IntegrationCandidate.fail(drug, MachineStatus.NO_MATCHING_RECIPE, PsychotropeResonatorFailureReason.NO_ELIGIBLE_DRUG, checklist);
        }
        if (!hasKnowledgeForDrug(player, drug)) {
            return IntegrationCandidate.fail(drug, MachineStatus.BLOCKED, PsychotropeResonatorFailureReason.MISSING_DRUG_KNOWLEDGE, checklist);
        }
        IntegrationService.EligibilityResult eligibility =
                IntegrationService.evaluate(player.getData(ModAttachments.PLAYER_ADDICTION.get()), drug);
        PsychotropeResonatorFailureReason eligibilityFault =
                PsychotropeResonatorFailureReason.firstEligibilityFailure(eligibility);
        if (eligibilityFault != null) {
            return IntegrationCandidate.fail(drug, MachineStatus.NO_MATCHING_RECIPE, eligibilityFault, checklist);
        }
        if (!coreSatisfies(this.getItem(SLOT_INTEGRATION_CORE), IntegrationCoreTier.requiredFor(drug))) {
            return IntegrationCandidate.fail(drug, MachineStatus.MISSING_CATALYST, PsychotropeResonatorFailureReason.MISSING_CORE, checklist);
        }
        if (!hasMaterialForDrug(drug)) {
            return IntegrationCandidate.fail(drug, MachineStatus.MISSING_INPUT_ITEM, PsychotropeResonatorFailureReason.MISSING_MATERIAL, checklist);
        }
        if (!hasDiaryContext(player)) {
            return IntegrationCandidate.fail(drug, MachineStatus.MISSING_DIARY_CONTEXT, PsychotropeResonatorFailureReason.MISSING_DIARY, checklist);
        }
        if (!hasValidRecoveryRoom(player)) {
            return IntegrationCandidate.fail(drug, MachineStatus.MISSING_RECOVERY_CONTEXT, PsychotropeResonatorFailureReason.MISSING_RECOVERY_ROOM, checklist);
        }
        return IntegrationCandidate.ok(drug, checklist);
    }

    private boolean hasMaterialForDrug(DrugId drug) {
        return IntegrationMaterials.matches(drug, this.getItem(SLOT_MATERIAL));
    }

    private int buildChecklist(ServerPlayer player, DrugId drug) {
        int mask = 0;
        if (hasKnowledgeForDrug(player, drug)) {
            mask |= CHECK_KNOWLEDGE;
        }
        IntegrationService.EligibilityResult eligibility =
                IntegrationService.evaluate(player.getData(ModAttachments.PLAYER_ADDICTION.get()), drug);
        if (eligibility.peakMet() && eligibility.cleanDoseStreakMet()) {
            mask |= CHECK_REQUIREMENT;
        }
        if (eligibility.lowAddictionMet()) {
            mask |= CHECK_LOW_ADDICTION;
        }
        if (eligibility.recoveryMet()) {
            mask |= CHECK_RECOVERY;
        }
        if (eligibility.lifetimeDoseMet()) {
            mask |= CHECK_LIFETIME;
        }
        if (!eligibility.alreadyIntegrated()) {
            mask |= CHECK_NOT_INTEGRATED;
        }
        if (hasMaterialForDrug(drug)) {
            mask |= CHECK_MATERIAL;
        }
        if (coreSatisfies(this.getItem(SLOT_INTEGRATION_CORE), IntegrationCoreTier.requiredFor(drug))) {
            mask |= CHECK_CORE;
        }
        if (hasDiaryContext(player)) {
            mask |= CHECK_DIARY;
        }
        if (hasValidRecoveryRoom(player)) {
            mask |= CHECK_ROOM;
        }
        return mask;
    }

    private boolean isRelevantCandidate(ServerPlayer player, DrugId drug) {
        if (hasKnowledgeForDrug(player, drug)) {
            return true;
        }
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        DrugAddictionStats d = stats.getDrugStats(drug);
        return d != null && (d.lifetimeDoseConsumed > 0.0F
                || d.peakHistoricalAddiction > 0.0F
                || d.cleanIntegrationDoseStreak > 0
                || d.addictionValue > 0.0F);
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
        ResonatorState next = this.viewerCanOpenDimension() ? ResonatorState.DIMENSION_READY : ResonatorState.IDLE;
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

    private void fail(ServerPlayer player, MachineStatus status, PsychotropeResonatorFailureReason reason) {
        fail(player, status, reason, null, 0);
    }

    private void fail(ServerPlayer player, MachineStatus status, PsychotropeResonatorFailureReason reason, @Nullable DrugId drug, int checklistMask) {
        setState(status == MachineStatus.INVALID_MULTIBLOCK ? ResonatorState.INVALID_STRUCTURE : ResonatorState.BLOCKED);
        setMachineStatus(status);
        this.lastFailureReason = reason == null ? PsychotropeResonatorFailureReason.NONE : reason;
        this.lastCandidateDrug = drug;
        this.lastChecklistMask = checklistMask;
        player.displayClientMessage(Component.translatable(this.lastFailureReason.translationKey()).withStyle(ChatFormatting.DARK_PURPLE), true);
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

    private boolean viewerDreamAligned() {
        ServerPlayer player = resolveViewer();
        return player != null
                ? player.getData(ModAttachments.PLAYER_INTEGRATION.get()).isDreamAligned()
                : this.dimensionReady;
    }

    private boolean viewerCanOpenDimension() {
        ServerPlayer player = resolveViewer();
        return player != null && InnerDimensionService.canOpen(player);
    }

    private @Nullable ServerPlayer resolveViewer() {
        if (!(this.level instanceof ServerLevel serverLevel) || this.lastViewer == null) {
            return null;
        }
        return serverLevel.getServer().getPlayerList().getPlayer(this.lastViewer);
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

    private record Validation(boolean ok, MachineStatus status, PsychotropeResonatorFailureReason reason) {
        static Validation success() {
            return new Validation(true, MachineStatus.RUNNING, PsychotropeResonatorFailureReason.NONE);
        }

        static Validation fail(MachineStatus status, PsychotropeResonatorFailureReason reason) {
            return new Validation(false, status, reason);
        }
    }

    private record IntegrationCandidate(boolean ok, @Nullable DrugId drug, MachineStatus status,
                                        PsychotropeResonatorFailureReason reason, int checklistMask) {
        static IntegrationCandidate ok(DrugId drug, int checklistMask) {
            return new IntegrationCandidate(true, drug, MachineStatus.RUNNING, PsychotropeResonatorFailureReason.NONE, checklistMask);
        }

        static IntegrationCandidate fail(@Nullable DrugId drug, MachineStatus status,
                                         PsychotropeResonatorFailureReason reason, int checklistMask) {
            return new IntegrationCandidate(false, drug, status, reason, checklistMask);
        }
    }
}
