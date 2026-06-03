package org.mydrugs.mydrugs.recovery;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.addiction.data.DrugAddictionStats;
import org.mydrugs.mydrugs.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.IntegrationRequirementProfile;
import org.mydrugs.mydrugs.core.drug.integration.IntegrationRequirements;
import org.mydrugs.mydrugs.core.drug.integration.IntegrationService;
import org.mydrugs.mydrugs.network.RecoveryRoomParticlesPayload;
import org.mydrugs.mydrugs.psyche.PsycheMapMilestones;
import org.mydrugs.mydrugs.recovery.block.RecoveryJukeboxBlockEntity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

public final class RecoveryRoomManager {
    private static final int DEFAULT_SCAN_RADIUS = 12;
    private static final int FLOOD_RADIUS = 10;
    private static final int MIN_VALID_VOLUME = 24;
    private static final int SOFT_MAX_VOLUME = 180;
    private static final int CACHE_TICKS = 60;
    private static final int MAX_PARTICLE_SAMPLES = 96;
    private static final int AMBIENT_PARTICLE_SYNC_TICKS = 40;

    private static final Map<Level, Map<BlockPos, CachedReport>> CACHE = new WeakHashMap<>();

    private RecoveryRoomManager() {
    }

    public static Optional<RecoveryRoomReport> getBestRoom(Level level, LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            return getBestRoom(player);
        }
        return getBestRoom(level, entity.blockPosition());
    }

    public static Optional<RecoveryRoomReport> getBestRoom(ServerPlayer player) {
        return PlayerRecoveryEnvironmentCache.snapshot(player).recoveryRoomOptional();
    }

    public static Optional<RecoveryRoomReport> getBestRoom(Level level, BlockPos playerPos) {
        int radius = scanRadius();
        RecoveryRoomReport best = null;
        for (BlockPos mutable : BlockPos.betweenClosed(
                playerPos.offset(-radius, -radius, -radius),
                playerPos.offset(radius, radius, radius)
        )) {
            BlockPos pos = mutable.immutable();
            if (!level.getBlockState(pos).is(ModBlocks.RECOVERY_ANCHOR.get())) {
                continue;
            }
            RecoveryRoomReport report = getRoom(level, pos);
            boolean inside = report.contains(playerPos);
            report = report.withPlayerInside(inside);
            if (!inside) {
                continue;
            }
            if (best == null || report.score() > best.score()) {
                best = report;
            }
        }
        return Optional.ofNullable(best);
    }

    public static RecoveryRoomReport getRoom(Level level, BlockPos anchorPos) {
        BlockPos immutable = anchorPos.immutable();
        long now = level.getGameTime();
        Map<BlockPos, CachedReport> levelCache = CACHE.computeIfAbsent(level, ignored -> new HashMap<>());
        CachedReport cached = levelCache.get(immutable);
        if (cached != null && now - cached.gameTime <= CACHE_TICKS) {
            return cached.report;
        }

        RecoveryRoomReport report = scanRoom(level, immutable);
        levelCache.put(immutable, new CachedReport(now, report));
        return report;
    }

    public static boolean isValidRecoveryRoom(RecoveryRoomReport report) {
        return report != null && report.valid() && report.tier().isValidRoom();
    }

    public static float withdrawalRecoveryMultiplier(RecoveryRoomReport report) {
        return isValidRecoveryRoom(report) ? report.tier().withdrawalRecoveryMultiplier() : 1.0F;
    }

    public static float toleranceDecayMultiplier(RecoveryRoomReport report) {
        return isValidRecoveryRoom(report) ? report.tier().toleranceDecayMultiplier() : 1.0F;
    }

    public static float addictionRecoveryMultiplier(RecoveryRoomReport report) {
        return isValidRecoveryRoom(report) ? report.tier().addictionRecoveryMultiplier() : 1.0F;
    }

    public static float stressTargetReduction(RecoveryRoomReport report) {
        if (!isValidRecoveryRoom(report)) {
            return 0.0F;
        }
        float reduction = report.tier().stressTargetReduction();
        if (report.hasModule(SanctuaryModule.PLANT_BREATHING_CORNER)) {
            reduction += 0.015F;
        }
        return Math.min(0.35F, reduction);
    }

    public static float badTripPressureReduction(RecoveryRoomReport report) {
        if (!isValidRecoveryRoom(report)) {
            return 0.0F;
        }
        float reduction = report.tier().badTripPressureReduction();
        if (report.hasModule(SanctuaryModule.MUSIC_CORNER) && report.hasActiveMusic()) {
            reduction += 0.02F;
        }
        return Math.min(0.35F, reduction);
    }

    public static float badTripIntensityReduction(RecoveryRoomReport report) {
        if (!isValidRecoveryRoom(report)) {
            return 0.0F;
        }
        float reduction = report.tier().badTripIntensityReduction();
        if (report.hasModule(SanctuaryModule.MUSIC_CORNER) && report.hasActiveMusic()) {
            reduction += 0.04F;
        }
        return Math.min(0.45F, reduction);
    }

    public static boolean suppressesHostileHallucinations(RecoveryRoomReport report) {
        return isValidRecoveryRoom(report) && report.tier() == RecoveryRoomTier.SANCTUARY;
    }

    public static void inspectAnchor(ServerPlayer player, BlockPos anchorPos, boolean detailed) {
        invalidate(player.level(), anchorPos);
        RecoveryRoomReport report = getRoom(player.level(), anchorPos).withPlayerInside(
                getRoom(player.level(), anchorPos).contains(player.blockPosition())
        );
        sendReportMessages(player, report, detailed);
        sendParticles(player, report, detailed);
    }

    public static void onAnchorPlaced(ServerPlayer player, BlockPos anchorPos) {
        invalidate(player.level(), anchorPos);
        RecoveryRoomReport report = getRoom(player.level(), anchorPos).withPlayerInside(
                getRoom(player.level(), anchorPos).contains(player.blockPosition())
        );
        sendReportMessages(player, report, false);
        sendParticles(player, report, false);
    }

    public static void tickPlayerParticles(ServerPlayer player) {
        if (player.tickCount % AMBIENT_PARTICLE_SYNC_TICKS != 0) {
            return;
        }
        RecoveryRoomReport report = PlayerRecoveryEnvironmentCache.snapshot(player).recoveryRoom();
        if (!isValidRecoveryRoom(report)) {
            return;
        }
        sendAmbientParticles(player, report);
    }

    public static void invalidate(Level level, BlockPos anchorPos) {
        PlayerRecoveryEnvironmentCache.invalidateLevel(level);
        Map<BlockPos, CachedReport> levelCache = CACHE.get(level);
        if (levelCache != null) {
            levelCache.remove(anchorPos.immutable());
        }
    }

    public static void invalidateAround(Level level, BlockPos changedPos) {
        PlayerRecoveryEnvironmentCache.invalidateLevel(level);
        Map<BlockPos, CachedReport> levelCache = CACHE.get(level);
        if (levelCache == null || levelCache.isEmpty()) {
            return;
        }
        int radius = scanRadius() + 2;
        levelCache.keySet().removeIf(anchor -> anchor.distManhattan(changedPos) <= radius * 3);
    }

    static void notifyRoomSeen(ServerPlayer player, RecoveryRoomReport report) {
        if (report != null && report.tier() == RecoveryRoomTier.SANCTUARY && PsycheMapMilestones.sanctuary(player)) {
            player.displayClientMessage(Component.translatable("recovery.mydrugs.room.sanctuary_memory"), true);
        }
    }

    private static RecoveryRoomReport scanRoom(Level level, BlockPos anchorPos) {
        if (!level.getBlockState(anchorPos).is(ModBlocks.RECOVERY_ANCHOR.get())) {
            return emptyReport(anchorPos, List.of("recovery.mydrugs.room.invalid"));
        }

        FloodResult flood = floodInterior(level, anchorPos);
        if (flood.interior.isEmpty()) {
            return emptyReport(anchorPos, List.of("recovery.mydrugs.room.invalid"));
        }

        Bounds bounds = Bounds.of(flood.interior);
        BoundaryStats boundary = analyzeBoundary(level, flood.interiorSet);
        RoomContents contents = analyzeContents(level, flood.interior, flood.interiorSet, bounds);
        int hostileCount = countHostiles(level, bounds);
        int floorArea = computeFloorArea(level, flood.interior, flood.interiorSet);
        float averageHeight = computeAverageHeight(flood.interior);
        float averageLight = flood.interior.isEmpty() ? 0.0F : contents.lightTotal / (float) flood.interior.size();

        boolean volumeValid = flood.interior.size() >= MIN_VALID_VOLUME && flood.interior.size() <= SOFT_MAX_VOLUME;
        boolean enclosedEnough = boundary.enclosureRatio() >= 0.72F
                && boundary.ceilingCoverage() >= 0.55F
                && boundary.openSkyPositions <= Math.max(1, flood.interior.size() / 12)
                && !flood.overflowed;
        boolean hasDoor = boundary.doors > 0;

        RecoveryRoomScore score = scoreRoom(
                flood.interior.size(),
                floorArea,
                averageHeight,
                averageLight,
                boundary,
                contents,
                hostileCount
        );

        List<String> good = new ArrayList<>();
        List<String> improve = new ArrayList<>();
        collectMessages(flood.interior.size(), floorArea, averageHeight, averageLight, boundary, contents, hostileCount, enclosedEnough, good, improve);

        boolean valid = volumeValid && enclosedEnough && hasDoor;
        int total = score.totalBeforeClamp();
        if (!valid) {
            total = Math.min(total, RecoveryRoomTier.FRAGILE_ROOM.minScore() - 1);
        }
        total = Math.max(0, Math.min(100, total));
        if (valid && (total >= RecoveryRoomTier.SAFE_ROOM.minScore() || improve.isEmpty())) {
            collectPolishHints(score, total, improve);
        }
        RecoveryRoomTier tier = RecoveryRoomTier.fromScore(total);
        Set<SanctuaryModule> modules = SanctuaryModuleDetector.detect(contents.moduleScan(averageLight, hostileCount), valid, tier);
        List<String> moduleSuggestions = SanctuaryModuleDetector.suggestionKeys(modules, valid, tier);

        return new RecoveryRoomReport(
                anchorPos,
                bounds.min,
                bounds.max,
                flood.interior,
                total,
                score,
                valid,
                enclosedEnough,
                false,
                flood.interior.size(),
                floorArea,
                averageHeight,
                averageLight,
                boundary.doors,
                contents.beds,
                contents.softLights,
                contents.carpets,
                contents.plants,
                contents.bookshelves,
                contents.music,
                contents.activeMusic,
                score.dangerPenalty(),
                modules,
                moduleSuggestions,
                good,
                improve
        );
    }

    private static FloodResult floodInterior(Level level, BlockPos anchorPos) {
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        Set<BlockPos> interiorSet = new HashSet<>();
        List<BlockPos> interior = new ArrayList<>();
        boolean overflowed = false;

        for (BlockPos start : startPositions(anchorPos)) {
            if (isInteriorPassable(level, start)) {
                queue.add(start.immutable());
            }
        }

        int hardMax = Math.max(maxRoomVolume(), SOFT_MAX_VOLUME) + 40;
        while (!queue.isEmpty()) {
            BlockPos pos = queue.removeFirst();
            if (!visited.add(pos)) {
                continue;
            }
            if (!withinFloodRadius(anchorPos, pos)) {
                continue;
            }
            if (!isInteriorPassable(level, pos)) {
                continue;
            }

            interior.add(pos);
            interiorSet.add(pos);
            if (interior.size() > hardMax) {
                overflowed = true;
                break;
            }

            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);
                if (!visited.contains(next) && withinFloodRadius(anchorPos, next)) {
                    queue.add(next.immutable());
                }
            }
        }

        return new FloodResult(interior, interiorSet, overflowed || interior.size() > maxRoomVolume());
    }

    private static List<BlockPos> startPositions(BlockPos anchorPos) {
        List<BlockPos> starts = new ArrayList<>();
        starts.add(anchorPos.above());
        starts.add(anchorPos.above(2));
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            starts.add(anchorPos.relative(direction));
            starts.add(anchorPos.relative(direction).above());
        }
        return starts;
    }

    private static BoundaryStats analyzeBoundary(Level level, Set<BlockPos> interior) {
        int enclosedFaces = 0;
        int exposedFaces = 0;
        int calmingFaces = 0;
        int ceilingFaces = 0;
        int coveredCeilingFaces = 0;
        int floorFaces = 0;
        int coveredFloorFaces = 0;
        int openSky = 0;
        Set<BlockPos> doors = new HashSet<>();

        for (BlockPos pos : interior) {
            if (level.canSeeSky(pos)) {
                openSky++;
            }

            for (Direction direction : Direction.values()) {
                BlockPos neighbor = pos.relative(direction);
                if (interior.contains(neighbor)) {
                    continue;
                }

                BlockState state = level.getBlockState(neighbor);
                boolean boundary = isBoundaryBlock(level, neighbor, state);
                if (isClosableEntrance(state)) {
                    doors.add(normalizeDoorPos(neighbor, state));
                    boundary = true;
                }

                if (direction == Direction.UP) {
                    ceilingFaces++;
                    if (boundary) {
                        coveredCeilingFaces++;
                    }
                } else if (direction == Direction.DOWN) {
                    floorFaces++;
                    if (boundary) {
                        coveredFloorFaces++;
                    }
                }

                if (boundary) {
                    enclosedFaces++;
                    if (isCalmingBoundary(state)) {
                        calmingFaces++;
                    }
                } else {
                    exposedFaces++;
                }
            }
        }

        return new BoundaryStats(
                enclosedFaces,
                exposedFaces,
                calmingFaces,
                ceilingFaces,
                coveredCeilingFaces,
                floorFaces,
                coveredFloorFaces,
                openSky,
                doors.size()
        );
    }

    private static RoomContents analyzeContents(Level level, List<BlockPos> interior, Set<BlockPos> interiorSet, Bounds bounds) {
        int beds = 0;
        int softLights = 0;
        int torches = 0;
        int carpets = 0;
        int plants = 0;
        int bookshelves = 0;
        int lecterns = 0;
        int seats = 0;
        int tableLikeBlocks = 0;
        int music = 0;
        int activeMusic = 0;
        int teaHeatSources = 0;
        int cauldrons = 0;
        int teaStorageBlocks = 0;
        int integrationMarkers = 0;
        int dangerBlocks = 0;
        int clutter = 0;
        int lightTotal = 0;
        Set<BlockPos> counted = new HashSet<>();

        for (BlockPos pos : interior) {
            lightTotal += level.getBrightness(LightLayer.BLOCK, pos);
        }

        for (BlockPos mutable : BlockPos.betweenClosed(bounds.min.offset(-1, -1, -1), bounds.max.offset(1, 1, 1))) {
            BlockPos pos = mutable.immutable();
            if (!isRelevantContentPos(pos, interiorSet) || !counted.add(pos)) {
                continue;
            }

            BlockState state = level.getBlockState(pos);
            Block block = state.getBlock();
            if (block instanceof BedBlock) {
                beds++;
            }
            if (block instanceof CarpetBlock || state.is(BlockTags.WOOL)) {
                carpets++;
            }
            if (isSoftLight(state)) {
                softLights++;
            }
            if (block instanceof TorchBlock || block instanceof WallTorchBlock) {
                torches++;
            }
            if (isPlant(state)) {
                plants++;
            }
            if (block instanceof LecternBlock) {
                lecterns++;
            }
            if (isBookshelf(state) || block instanceof LecternBlock) {
                bookshelves++;
            }
            if (isSeat(state)) {
                seats++;
            }
            if (isTableLike(state)) {
                tableLikeBlocks++;
            }
            boolean activeJukebox = isActiveJukebox(state) || isActiveRecoveryJukebox(level, pos, state);
            if (activeJukebox) {
                music += 2;
                activeMusic++;
            } else if (state.is(ModBlocks.RECOVERY_JUKEBOX.get())) {
                music += 2;
            } else if (state.is(Blocks.JUKEBOX)) {
                music++;
            }
            if (isTeaHeatSource(state)) {
                teaHeatSources++;
            }
            if (isTeaCauldron(state)) {
                cauldrons++;
            }
            if (isTeaStorage(state)) {
                teaStorageBlocks++;
            }
            if (isIntegrationMarker(state)) {
                integrationMarkers++;
            }
            if (isDangerBlock(state)) {
                dangerBlocks++;
            }
            if (isIndustrialClutter(state)) {
                clutter++;
            }
        }

        int memoryDisplays = countMemoryDisplays(level, bounds);

        return new RoomContents(
                beds,
                softLights,
                torches,
                carpets,
                plants,
                bookshelves,
                lecterns,
                seats,
                tableLikeBlocks,
                music,
                activeMusic,
                teaHeatSources,
                cauldrons,
                teaStorageBlocks,
                memoryDisplays,
                integrationMarkers,
                dangerBlocks,
                clutter,
                lightTotal
        );
    }

    private static RecoveryRoomScore scoreRoom(
            int volume,
            int floorArea,
            float averageHeight,
            float averageLight,
            BoundaryStats boundary,
            RoomContents contents,
            int hostileCount
    ) {
        int size = scoreIdealRange(volume, 45.0F, 100.0F, 18.0F, 180.0F, 8)
                + scoreIdealRange(floorArea, 12.0F, 35.0F, 6.0F, 55.0F, 7)
                + scoreIdealRange(averageHeight, 3.0F, 5.0F, 2.0F, 7.0F, 5);

        int enclosure = Math.round(boundary.enclosureRatio() * 11.0F)
                + Math.round(boundary.ceilingCoverage() * 3.0F)
                + Math.min(1, boundary.calmingFaces / 12);
        enclosure = clampInt(enclosure, 0, 15);

        int door = switch (boundary.doors) {
            case 0 -> 0;
            case 1, 2 -> 10;
            case 3, 4 -> 6;
            default -> 3;
        };

        int bed = contents.beds <= 0 ? 0 : Math.min(12, 10 + Math.min(2, contents.beds - 1));

        int lighting = scoreIdealRange(averageLight, 7.0F, 12.0F, 3.0F, 15.0F, 8)
                + Math.min(4, contents.softLights);
        if (contents.torches > 5) {
            lighting -= Math.min(3, contents.torches - 5);
        }
        lighting = clampInt(lighting, 0, 12);

        float carpetCoverage = floorArea <= 0 ? 0.0F : contents.carpets / (float) floorArea;
        int floorComfort;
        if (carpetCoverage <= 0.0F) {
            floorComfort = 0;
        } else if (carpetCoverage >= 0.15F && carpetCoverage <= 0.55F) {
            floorComfort = 10;
        } else if (carpetCoverage < 0.15F) {
            floorComfort = Math.round(carpetCoverage / 0.15F * 8.0F);
        } else {
            floorComfort = Math.max(3, 10 - Math.round((carpetCoverage - 0.55F) * 18.0F));
        }

        int plants = clampInt(Math.round(Math.min(6, contents.plants) / 6.0F * 8.0F), 0, 8);

        int books;
        if (contents.bookshelves <= 0) {
            books = 0;
        } else if (contents.bookshelves <= 6) {
            books = Math.round(contents.bookshelves / 6.0F * 8.0F);
        } else if (contents.bookshelves <= 10) {
            books = 6;
        } else {
            books = 3;
        }

        int music = contents.music <= 0 ? 0 : Math.min(15, contents.music >= 2 ? 15 : 8);
        int dangerPenalty = contents.dangerBlocks * 8 + hostileCount * 4 + Math.max(0, contents.clutter - 4) * 2;

        return new RecoveryRoomScore(size, enclosure, door, bed, lighting, floorComfort, plants, books, music, dangerPenalty);
    }

    private static void collectMessages(
            int volume,
            int floorArea,
            float averageHeight,
            float averageLight,
            BoundaryStats boundary,
            RoomContents contents,
            int hostileCount,
            boolean enclosedEnough,
            List<String> good,
            List<String> improve
    ) {
        if (enclosedEnough) good.add(RecoveryRoomComponent.ENCLOSURE.translationKey());
        if (boundary.doors > 0) good.add(RecoveryRoomComponent.DOOR.translationKey());
        if (contents.beds > 0) good.add(RecoveryRoomComponent.BED.translationKey());
        if (averageLight >= 7.0F && averageLight <= 12.5F || contents.softLights > 0) good.add(RecoveryRoomComponent.SOFT_LIGHTING.translationKey());
        if (contents.carpets > 0) good.add(RecoveryRoomComponent.CARPETS.translationKey());
        if (contents.plants > 0) good.add(RecoveryRoomComponent.FLOWERS.translationKey());
        if (contents.bookshelves > 0) good.add(RecoveryRoomComponent.BOOKS.translationKey());
        if (contents.music > 0) good.add(RecoveryRoomComponent.MUSIC.translationKey());

        if (volume < MIN_VALID_VOLUME || floorArea < 8 || averageHeight < 2.5F) {
            improve.add("recovery.mydrugs.room.too_small");
        }
        if (volume > SOFT_MAX_VOLUME || floorArea > 55 || averageHeight > 7.0F) {
            improve.add("recovery.mydrugs.room.too_large");
        }
        if (!enclosedEnough) {
            improve.add("recovery.mydrugs.room.too_exposed");
        }
        if (boundary.doors <= 0) {
            improve.add("recovery.mydrugs.room.needs_door");
        } else if (boundary.doors > 2) {
            improve.add("recovery.mydrugs.room.too_many_doors");
        }
        if (contents.beds <= 0) {
            improve.add("recovery.mydrugs.room.needs_bed");
        }
        if (averageLight < 6.0F || averageLight > 13.5F) {
            improve.add("recovery.mydrugs.room.soft_lighting");
        }
        if (contents.carpets <= 0) {
            improve.add("recovery.mydrugs.room.needs_carpets");
        }
        if (contents.plants <= 0) {
            improve.add("recovery.mydrugs.room.needs_flowers");
        }
        if (contents.music <= 0) {
            improve.add("recovery.mydrugs.room.music");
        }
        if (contents.bookshelves > 10) {
            improve.add("recovery.mydrugs.room.too_many_books");
        }
        if (contents.dangerBlocks > 0 || hostileCount > 0 || contents.clutter > 8) {
            improve.add("recovery.mydrugs.room.too_dangerous");
        }
    }

    private static void collectPolishHints(RecoveryRoomScore score, int total, List<String> improve) {
        int initialSize = improve.size();
        addHint(improve, score.size() < 18, "recovery.mydrugs.room.polish_size");
        addHint(improve, score.enclosure() < 14, "recovery.mydrugs.room.polish_enclosure");
        addHint(improve, score.lighting() < 12, "recovery.mydrugs.room.polish_lighting");
        addHint(improve, score.floorComfort() < 10, "recovery.mydrugs.room.polish_carpets");
        addHint(improve, score.plants() < 8, "recovery.mydrugs.room.polish_plants");
        addHint(improve, score.books() < 8, "recovery.mydrugs.room.polish_books");
        addHint(improve, score.music() < 15, "recovery.mydrugs.room.polish_music");
        addHint(improve, score.dangerPenalty() > 0, "recovery.mydrugs.room.too_dangerous");

        while (improve.size() - initialSize > 3) {
            improve.remove(improve.size() - 1);
        }
        if (improve.size() == initialSize && total >= RecoveryRoomTier.SANCTUARY.minScore()) {
            improve.add("recovery.mydrugs.room.polish_complete");
        }
    }

    private static void addHint(List<String> improve, boolean condition, String key) {
        if (condition && !improve.contains(key)) {
            improve.add(key);
        }
    }

    public static RecoveryRoomTier nextTier(RecoveryRoomReport report) {
        int score = report == null ? 0 : report.score();
        for (RecoveryRoomTier tier : RecoveryRoomTier.values()) {
            if (tier.minScore() > score) {
                return tier;
            }
        }
        return RecoveryRoomTier.SANCTUARY;
    }

    public static int pointsToNextTier(RecoveryRoomReport report) {
        if (report == null) {
            return RecoveryRoomTier.FRAGILE_ROOM.minScore();
        }
        RecoveryRoomTier next = nextTier(report);
        return Math.max(0, next.minScore() - report.score());
    }

    public static String bestNextStepKey(RecoveryRoomReport report) {
        if (report == null) {
            return "recovery.mydrugs.room.invalid";
        }
        if (!report.valid()) {
            if (report.volume() < MIN_VALID_VOLUME || report.floorArea() < 8 || report.averageHeight() < 2.5F) {
                return "recovery.mydrugs.room.too_small";
            }
            if (report.volume() > SOFT_MAX_VOLUME || report.floorArea() > 55 || report.averageHeight() > 7.0F) {
                return "recovery.mydrugs.room.too_large";
            }
            if (!report.enclosedEnough()) {
                return "recovery.mydrugs.room.too_exposed";
            }
            if (!report.hasDoor()) {
                return "recovery.mydrugs.room.needs_door";
            }
        }
        if (report.dangerPenalty() > 0) {
            return "recovery.mydrugs.room.too_dangerous";
        }

        RecoveryRoomScore score = report.scoreBreakdown();
        CategoryChoice best = new CategoryChoice("recovery.mydrugs.room.polish_complete", 0);
        best = chooseCategoryGap(best, "recovery.mydrugs.room.polish_size", score.size(), 20);
        best = chooseCategoryGap(best, "recovery.mydrugs.room.polish_enclosure", score.enclosure(), 15);
        best = chooseCategoryGap(best, "recovery.mydrugs.room.needs_door", score.door(), 10);
        best = chooseCategoryGap(best, "recovery.mydrugs.room.needs_bed", score.bed(), 12);
        best = chooseCategoryGap(best, "recovery.mydrugs.room.polish_lighting", score.lighting(), 12);
        best = chooseCategoryGap(best, "recovery.mydrugs.room.polish_carpets", score.floorComfort(), 10);
        best = chooseCategoryGap(best, "recovery.mydrugs.room.polish_plants", score.plants(), 8);
        best = chooseCategoryGap(best, "recovery.mydrugs.room.polish_books", score.books(), 8);
        best = chooseCategoryGap(best, "recovery.mydrugs.room.polish_music", score.music(), 15);
        if (best.missing() > 0) {
            return best.key();
        }
        if (!report.sanctuarySuggestionKeys().isEmpty()) {
            return report.sanctuarySuggestionKeys().getFirst();
        }
        return "recovery.mydrugs.room.polish_complete";
    }

    public static String categoryRating(int points, int maxPoints) {
        if (maxPoints <= 0) {
            return "recovery.mydrugs.room.rating.missing";
        }
        float ratio = Math.max(0.0F, Math.min(1.0F, points / (float) maxPoints));
        if (ratio >= 1.0F) {
            return "recovery.mydrugs.room.rating.complete";
        }
        if (ratio >= 0.75F) {
            return "recovery.mydrugs.room.rating.strong";
        }
        if (ratio >= 0.45F) {
            return "recovery.mydrugs.room.rating.steady";
        }
        if (ratio > 0.0F) {
            return "recovery.mydrugs.room.rating.weak";
        }
        return "recovery.mydrugs.room.rating.missing";
    }

    private static void sendReportMessages(ServerPlayer player, RecoveryRoomReport report, boolean detailed) {
        if (!report.valid()) {
            player.displayClientMessage(Component.translatable("recovery.mydrugs.room.invalid").withStyle(ChatFormatting.YELLOW), true);
        }

        player.sendSystemMessage(Component.translatable(
                "recovery.mydrugs.room.score",
                Component.translatable(report.tier().translationKey()),
                Math.round(report.comfort01() * 100.0F)
        ).withStyle(report.valid() ? ChatFormatting.AQUA : ChatFormatting.YELLOW));

        int pointsToNext = pointsToNextTier(report);
        if (pointsToNext > 0) {
            player.sendSystemMessage(Component.translatable(
                    "recovery.mydrugs.room.next_threshold",
                    Component.translatable(nextTier(report).translationKey()),
                    pointsToNext
            ).withStyle(ChatFormatting.AQUA));
        } else {
            player.sendSystemMessage(Component.translatable("recovery.mydrugs.room.next_threshold.complete")
                    .withStyle(ChatFormatting.AQUA));
        }

        player.sendSystemMessage(Component.translatable(
                "recovery.mydrugs.room.best_next",
                Component.translatable(bestNextStepKey(report))
        ).withStyle(ChatFormatting.GOLD));

        player.sendSystemMessage(Component.translatable(
                "recovery.mydrugs.room.music_status",
                Component.translatable(musicStatusKey(report))
        ).withStyle(report.hasActiveMusic() ? ChatFormatting.GREEN : ChatFormatting.GRAY));

        if (!report.sanctuaryModules().isEmpty()) {
            player.sendSystemMessage(Component.translatable("recovery.mydrugs.room.modules", joinComponents(moduleStatusComponents(player, report)))
                    .withStyle(ChatFormatting.AQUA));
        }
        if (detailed) {
            sendCategoryReadout(player, report);
        }
        if (detailed && !report.sanctuarySuggestionKeys().isEmpty()) {
            player.sendSystemMessage(Component.translatable("recovery.mydrugs.room.module_suggestions", joinTranslated(report.sanctuarySuggestionKeys()))
                    .withStyle(ChatFormatting.GOLD));
        }
        if (detailed && !report.improvementKeys().isEmpty()) {
            player.sendSystemMessage(Component.translatable("recovery.mydrugs.room.improve", joinTranslated(report.improvementKeys()))
                    .withStyle(ChatFormatting.GOLD));
        }
    }

    private static CategoryChoice chooseCategoryGap(CategoryChoice best, String key, int current, int max) {
        int missing = Math.max(0, max - current);
        return missing > best.missing() ? new CategoryChoice(key, missing) : best;
    }

    private static void sendCategoryReadout(ServerPlayer player, RecoveryRoomReport report) {
        RecoveryRoomScore score = report.scoreBreakdown();
        sendCategoryLine(player, "recovery.mydrugs.room.category.size", score.size(), 20);
        sendCategoryLine(player, "recovery.mydrugs.room.category.enclosure", score.enclosure(), 15);
        sendCategoryLine(player, "recovery.mydrugs.room.category.door", score.door(), 10);
        sendCategoryLine(player, "recovery.mydrugs.room.category.bed", score.bed(), 12);
        sendCategoryLine(player, "recovery.mydrugs.room.category.lighting", score.lighting(), 12);
        sendCategoryLine(player, "recovery.mydrugs.room.category.floor_comfort", score.floorComfort(), 10);
        sendCategoryLine(player, "recovery.mydrugs.room.category.plants", score.plants(), 8);
        sendCategoryLine(player, "recovery.mydrugs.room.category.books", score.books(), 8);
        sendCategoryLine(player, "recovery.mydrugs.room.category.music", score.music(), 15);
        player.sendSystemMessage(Component.translatable(
                "recovery.mydrugs.room.category_line.danger",
                Component.translatable("recovery.mydrugs.room.category.danger"),
                Component.translatable(dangerRating(report.dangerPenalty())),
                report.dangerPenalty()
        ).withStyle(report.dangerPenalty() > 0 ? ChatFormatting.RED : ChatFormatting.GREEN));
    }

    private static void sendCategoryLine(ServerPlayer player, String labelKey, int points, int maxPoints) {
        player.sendSystemMessage(Component.translatable(
                "recovery.mydrugs.room.category_line",
                Component.translatable(labelKey),
                Component.translatable(categoryRating(points, maxPoints)),
                points,
                maxPoints
        ).withStyle(points >= maxPoints ? ChatFormatting.GREEN : ChatFormatting.GRAY));
    }

    private static String dangerRating(int dangerPenalty) {
        if (dangerPenalty <= 0) {
            return "recovery.mydrugs.room.danger.clear";
        }
        if (dangerPenalty <= 8) {
            return "recovery.mydrugs.room.danger.watch";
        }
        return "recovery.mydrugs.room.danger.unsafe";
    }

    private static String musicStatusKey(RecoveryRoomReport report) {
        if (report.hasActiveMusic()) {
            return "recovery.mydrugs.room.music_status.active";
        }
        if (report.hasMusic()) {
            return "recovery.mydrugs.room.music_status.present_inactive";
        }
        return "recovery.mydrugs.room.music_status.none";
    }

    private static List<Component> moduleStatusComponents(ServerPlayer player, RecoveryRoomReport report) {
        List<Component> components = new ArrayList<>();
        for (SanctuaryModule module : SanctuaryModule.values()) {
            if (!report.hasModule(module)) {
                continue;
            }
            if (module == SanctuaryModule.MEMORY_WALL) {
                String key = hasEarnedMemory(player)
                        ? "recovery.mydrugs.room.module_status.memory_wall.active"
                        : "recovery.mydrugs.room.module_status.memory_wall.present_empty";
                components.add(Component.translatable(key));
            } else if (module == SanctuaryModule.INTEGRATION_ALCOVE) {
                components.add(Component.translatable(integrationAlcoveStatusKey(player)));
            } else {
                components.add(Component.translatable(module.translationKey()));
            }
        }
        return components;
    }

    private static String integrationAlcoveStatusKey(ServerPlayer player) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        long now = player.level().getGameTime();
        for (DrugId drugId : stats.getTrackedDrugIds()) {
            if (IntegrationService.evaluate(stats, drugId, now).eligible()) {
                return "recovery.mydrugs.room.module_status.integration_alcove.ready";
            }
        }
        for (DrugId drugId : stats.getTrackedDrugIds()) {
            DrugAddictionStats drugStats = stats.getDrugStats(drugId);
            IntegrationRequirementProfile profile = IntegrationRequirements.profile(drugId);
            if (drugStats == null || profile == null || !profile.requiresRecoveryProgress() || drugStats.isIntegrated()) {
                continue;
            }
            float required = Math.max(0.01F, profile.requiredRecoveryProgress());
            if (drugStats.recoveryProgress >= required * 0.75F && drugStats.recoveryProgress < required) {
                return "recovery.mydrugs.room.module_status.integration_alcove.waiting";
            }
        }
        return "recovery.mydrugs.room.module_status.integration_alcove.dormant";
    }

    private static boolean hasEarnedMemory(ServerPlayer player) {
        return !player.getData(ModAttachments.PLAYER_PSYCHE_MAP.get()).getNodes().isEmpty()
                || !player.getData(ModAttachments.PLAYER_INTEGRATION.get()).isEmpty();
    }

    private static Component joinTranslated(List<String> keys) {
        Component result = Component.empty();
        for (int i = 0; i < keys.size(); i++) {
            if (i > 0) {
                result = result.copy().append(Component.literal(", "));
            }
            result = result.copy().append(Component.translatable(keys.get(i)));
        }
        return result;
    }

    private static Component joinComponents(List<Component> components) {
        Component result = Component.empty();
        for (int i = 0; i < components.size(); i++) {
            if (i > 0) {
                result = result.copy().append(Component.literal(", "));
            }
            result = result.copy().append(components.get(i));
        }
        return result;
    }

    private static void sendParticles(ServerPlayer player, RecoveryRoomReport report, boolean highlight) {
        sendParticlesPayload(player, report, highlight, false);
    }

    private static void sendAmbientParticles(ServerPlayer player, RecoveryRoomReport report) {
        sendParticlesPayload(player, report, false, true);
    }

    private static void sendParticlesPayload(ServerPlayer player, RecoveryRoomReport report, boolean highlight, boolean ambient) {
        List<BlockPos> samples = report.valid()
                ? report.particleSamples(MAX_PARTICLE_SAMPLES)
                : List.of(report.anchorPos().above());
        PacketDistributor.sendToPlayer(player, new RecoveryRoomParticlesPayload(
                report.anchorPos(),
                report.min(),
                report.max(),
                samples,
                report.score(),
                report.tier().networkId(),
                SanctuaryModule.flags(report.sanctuaryModules()),
                report.hasActiveMusic(),
                player.level().getRandom().nextLong(),
                highlight,
                ambient
        ));
    }

    private static int countHostiles(Level level, Bounds bounds) {
        AABB box = new AABB(
                bounds.min.getX(),
                bounds.min.getY(),
                bounds.min.getZ(),
                bounds.max.getX() + 1.0D,
                bounds.max.getY() + 1.0D,
                bounds.max.getZ() + 1.0D
        ).inflate(1.0D);
        return level.getEntitiesOfClass(Monster.class, box).size();
    }

    private static int countMemoryDisplays(Level level, Bounds bounds) {
        AABB box = new AABB(
                bounds.min.getX(),
                bounds.min.getY(),
                bounds.min.getZ(),
                bounds.max.getX() + 1.0D,
                bounds.max.getY() + 1.0D,
                bounds.max.getZ() + 1.0D
        ).inflate(1.0D);
        return level.getEntitiesOfClass(ItemFrame.class, box).size()
                + level.getEntitiesOfClass(Painting.class, box).size();
    }

    private static int computeFloorArea(Level level, List<BlockPos> interior, Set<BlockPos> interiorSet) {
        Set<Long> columns = new HashSet<>();
        for (BlockPos pos : interior) {
            BlockPos below = pos.below();
            if (!interiorSet.contains(below) && isBoundaryBlock(level, below, level.getBlockState(below))) {
                columns.add(BlockPos.asLong(pos.getX(), 0, pos.getZ()));
            }
        }
        return columns.size();
    }

    private static float computeAverageHeight(List<BlockPos> interior) {
        Map<Long, Integer> heights = new HashMap<>();
        for (BlockPos pos : interior) {
            long key = BlockPos.asLong(pos.getX(), 0, pos.getZ());
            heights.merge(key, 1, Integer::sum);
        }
        if (heights.isEmpty()) {
            return 0.0F;
        }
        int total = 0;
        for (int height : heights.values()) {
            total += height;
        }
        return total / (float) heights.size();
    }

    private static boolean isRelevantContentPos(BlockPos pos, Set<BlockPos> interiorSet) {
        if (interiorSet.contains(pos)) {
            return true;
        }
        for (Direction direction : Direction.values()) {
            if (interiorSet.contains(pos.relative(direction))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isInteriorPassable(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.getFluidState().isEmpty()) {
            return false;
        }
        if (isClosableEntrance(state)) {
            return false;
        }
        Block block = state.getBlock();
        if (state.isAir()
                || block instanceof CarpetBlock
                || block instanceof FlowerPotBlock
                || state.is(BlockTags.FLOWERS)
                || state.is(BlockTags.SAPLINGS)
                || state.is(Blocks.AZALEA)
                || state.is(Blocks.FLOWERING_AZALEA)
                || state.getCollisionShape(level, pos).isEmpty()) {
            return true;
        }
        return false;
    }

    private static boolean isBoundaryBlock(Level level, BlockPos pos, BlockState state) {
        if (!state.getFluidState().isEmpty()) {
            return false;
        }
        if (isClosableEntrance(state)) {
            return true;
        }
        Block block = state.getBlock();
        if (block instanceof CarpetBlock
                || block instanceof FlowerPotBlock
                || state.is(BlockTags.FLOWERS)
                || state.is(BlockTags.SAPLINGS)) {
            return false;
        }
        return state.isSolidRender()
                || state.blocksMotion()
                || !state.getCollisionShape(level, pos).isEmpty();
    }

    private static boolean isClosableEntrance(BlockState state) {
        Block block = state.getBlock();
        return block instanceof DoorBlock || block instanceof FenceGateBlock || block instanceof TrapDoorBlock;
    }

    private static BlockPos normalizeDoorPos(BlockPos pos, BlockState state) {
        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                && state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER) {
            return pos.below().immutable();
        }
        return pos.immutable();
    }

    private static boolean isCalmingBoundary(BlockState state) {
        ResourceLocationPath id = ResourceLocationPath.of(state.getBlock());
        return state.is(Blocks.GLASS)
                || state.is(Blocks.GLASS_PANE)
                || id.path().contains("glass");
    }

    private static boolean isSoftLight(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof CandleBlock || block instanceof LanternBlock) {
            return true;
        }
        if (block instanceof CampfireBlock) {
            return state.getOptionalValue(BlockStateProperties.LIT).orElse(false);
        }
        if (state.is(Blocks.CAVE_VINES) || state.is(Blocks.CAVE_VINES_PLANT) || state.is(Blocks.GLOW_LICHEN)) {
            return true;
        }
        return state.is(Blocks.REDSTONE_LAMP) && state.getOptionalValue(BlockStateProperties.LIT).orElse(false);
    }

    private static boolean isPlant(BlockState state) {
        return state.is(BlockTags.FLOWERS)
                || state.is(BlockTags.SAPLINGS)
                || state.is(BlockTags.LEAVES)
                || state.is(Blocks.AZALEA)
                || state.is(Blocks.FLOWERING_AZALEA)
                || state.getBlock() instanceof FlowerPotBlock;
    }

    private static boolean isBookshelf(BlockState state) {
        return state.is(Blocks.BOOKSHELF) || state.is(Blocks.CHISELED_BOOKSHELF);
    }

    private static boolean isActiveJukebox(BlockState state) {
        return state.getBlock() instanceof JukeboxBlock
                && state.getOptionalValue(JukeboxBlock.HAS_RECORD).orElse(false);
    }

    private static boolean isActiveRecoveryJukebox(Level level, BlockPos pos, BlockState state) {
        return state.is(ModBlocks.RECOVERY_JUKEBOX.get())
                && level.getBlockEntity(pos) instanceof RecoveryJukeboxBlockEntity jukebox
                && jukebox.isPlaying();
    }

    private static boolean isSeat(BlockState state) {
        return state.getBlock() instanceof StairBlock;
    }

    private static boolean isTableLike(BlockState state) {
        Block block = state.getBlock();
        return block instanceof SlabBlock
                || state.is(Blocks.CRAFTING_TABLE)
                || state.is(Blocks.BARREL)
                || state.is(Blocks.CHISELED_BOOKSHELF)
                || state.is(Blocks.LECTERN);
    }

    private static boolean isTeaHeatSource(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof AbstractFurnaceBlock) {
            return true;
        }
        return block instanceof CampfireBlock && state.getOptionalValue(BlockStateProperties.LIT).orElse(false);
    }

    private static boolean isTeaCauldron(BlockState state) {
        return state.is(Blocks.CAULDRON)
                || state.is(Blocks.WATER_CAULDRON)
                || state.is(Blocks.LAVA_CAULDRON)
                || state.is(Blocks.POWDER_SNOW_CAULDRON);
    }

    private static boolean isTeaStorage(BlockState state) {
        Block block = state.getBlock();
        return block instanceof ChestBlock || block instanceof BarrelBlock || state.is(Blocks.DECORATED_POT);
    }

    private static boolean isIntegrationMarker(BlockState state) {
        return state.is(ModBlocks.PSYCHOTROPE_RESONATOR.get())
                || state.is(ModBlocks.PSY_ANVIL.get());
    }

    private static boolean isDangerBlock(BlockState state) {
        return state.is(Blocks.FIRE)
                || state.is(Blocks.SOUL_FIRE)
                || state.is(Blocks.LAVA)
                || state.is(Blocks.MAGMA_BLOCK)
                || state.is(Blocks.TNT)
                || state.is(Blocks.SPAWNER);
    }

    private static boolean isIndustrialClutter(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof ChestBlock || block instanceof AbstractFurnaceBlock || block instanceof HopperBlock) {
            return true;
        }
        ResourceLocationPath id = ResourceLocationPath.of(block);
        if (!MyDrugs.MODID.equals(id.namespace())) {
            return false;
        }
        return id.path().contains("vat")
                || id.path().contains("reactor")
                || id.path().contains("furnace")
                || id.path().contains("distiller")
                || id.path().contains("centrifuge")
                || id.path().contains("pump")
                || id.path().contains("pipe")
                || id.path().contains("tank")
                || id.path().contains("gasifier")
                || id.path().contains("cracker");
    }

    private static boolean withinFloodRadius(BlockPos anchor, BlockPos pos) {
        return Math.max(
                Math.max(Math.abs(pos.getX() - anchor.getX()), Math.abs(pos.getY() - anchor.getY())),
                Math.abs(pos.getZ() - anchor.getZ())
        ) <= FLOOD_RADIUS;
    }

    private static int scanRadius() {
        try {
            return Config.SERVER.recoveryRoomScanRadius.get();
        } catch (Throwable ignored) {
            return DEFAULT_SCAN_RADIUS;
        }
    }

    private static int maxRoomVolume() {
        try {
            return Config.SERVER.recoveryRoomMaxVolume.get();
        } catch (Throwable ignored) {
            return 220;
        }
    }

    private static int scoreIdealRange(float value, float idealMin, float idealMax, float hardMin, float hardMax, int maxScore) {
        if (value >= idealMin && value <= idealMax) {
            return maxScore;
        }
        if (value < hardMin || value > hardMax) {
            return 0;
        }
        if (value < idealMin) {
            float t = (value - hardMin) / Math.max(0.01F, idealMin - hardMin);
            return Math.round(t * maxScore);
        }
        float t = (hardMax - value) / Math.max(0.01F, hardMax - idealMax);
        return Math.round(t * maxScore);
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static RecoveryRoomReport emptyReport(BlockPos anchorPos, List<String> improvements) {
        return new RecoveryRoomReport(
                anchorPos,
                anchorPos,
                anchorPos,
                List.of(),
                0,
                new RecoveryRoomScore(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
                false,
                false,
                false,
                0,
                0,
                0.0F,
                0.0F,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                Set.of(),
                List.of(),
                List.of(),
                improvements
        );
    }

    private record CachedReport(long gameTime, RecoveryRoomReport report) {
    }

    private record CategoryChoice(String key, int missing) {
    }

    private record FloodResult(List<BlockPos> interior, Set<BlockPos> interiorSet, boolean overflowed) {
    }

    private record Bounds(BlockPos min, BlockPos max) {
        static Bounds of(List<BlockPos> positions) {
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;
            int maxZ = Integer.MIN_VALUE;
            for (BlockPos pos : positions) {
                minX = Math.min(minX, pos.getX());
                minY = Math.min(minY, pos.getY());
                minZ = Math.min(minZ, pos.getZ());
                maxX = Math.max(maxX, pos.getX());
                maxY = Math.max(maxY, pos.getY());
                maxZ = Math.max(maxZ, pos.getZ());
            }
            return new Bounds(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ));
        }
    }

    private record BoundaryStats(
            int enclosedFaces,
            int exposedFaces,
            int calmingFaces,
            int ceilingFaces,
            int coveredCeilingFaces,
            int floorFaces,
            int coveredFloorFaces,
            int openSkyPositions,
            int doors
    ) {
        float enclosureRatio() {
            int total = enclosedFaces + exposedFaces;
            return total <= 0 ? 0.0F : enclosedFaces / (float) total;
        }

        float ceilingCoverage() {
            return ceilingFaces <= 0 ? 0.0F : coveredCeilingFaces / (float) ceilingFaces;
        }
    }

    private record RoomContents(
            int beds,
            int softLights,
            int torches,
            int carpets,
            int plants,
            int bookshelves,
            int lecterns,
            int seats,
            int tableLikeBlocks,
            int music,
            int activeMusic,
            int teaHeatSources,
            int cauldrons,
            int teaStorageBlocks,
            int memoryDisplays,
            int integrationMarkers,
            int dangerBlocks,
            int clutter,
            int lightTotal
    ) {
        SanctuaryModuleScan moduleScan(float averageLight, int hostileCount) {
            return new SanctuaryModuleScan(
                    beds,
                    softLights,
                    averageLight,
                    plants,
                    bookshelves,
                    lecterns,
                    seats,
                    tableLikeBlocks,
                    music,
                    activeMusic,
                    teaHeatSources,
                    cauldrons,
                    teaStorageBlocks,
                    memoryDisplays,
                    integrationMarkers,
                    dangerBlocks,
                    clutter,
                    hostileCount
            );
        }
    }

    private record ResourceLocationPath(String namespace, String path) {
        static ResourceLocationPath of(Block block) {
            net.minecraft.resources.ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            return new ResourceLocationPath(id.getNamespace(), id.getPath());
        }
    }
}
