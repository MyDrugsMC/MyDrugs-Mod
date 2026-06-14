package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.CuratedDrugChain;
import org.mydrugs.mydrugs.dimension.ModInnerDimensionBlocks;

/**
 * Renders the nine colossal region set pieces (A2), chunk slice by chunk slice. Every block is a
 * pure function of the form geometry ({@link InnerMegaForms}) and world coordinates, so slices
 * from neighbouring chunks join seamlessly and the initial and overlay passes agree exactly.
 *
 * <p>The plinth/relief under each form is shaped by {@code computeSample}; this builder only adds
 * the structure above (and occasionally carves into) that prepared ground.
 */
final class InnerMegaFormBuilder {
    private InnerMegaFormBuilder() {
    }

    static void placeInitialMegaForms(ChunkAccess chunk, InnerChunkSampleCache cache) {
        ChunkPos chunkPos = chunk.getPos();
        placeMegaForms(cache, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(), (pos, state) -> {
            if (pos.getY() < chunk.getMinY() || pos.getY() >= chunk.getMinY() + chunk.getHeight()) {
                return;
            }
            if (!chunkPos.equals(new ChunkPos(pos))) {
                return;
            }
            chunk.setBlockState(pos, state, 2);
        });
    }

    static void placeOverlayMegaForms(
            ServerLevel level,
            ChunkPos chunkPos,
            InnerChunkSampleCache cache,
            InnerPlacement.MutablePlacementCount count
    ) {
        placeMegaForms(cache, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(),
                (pos, state) -> InnerPlacement.safeSet(level, pos, state, true, count));
    }

    private static void placeMegaForms(InnerChunkSampleCache cache, int minX, int minZ, BlockSetter setter) {
        if (!cache.anyLand()) {
            return;
        }
        int centerX = cache.islandCenterX();
        int centerZ = cache.islandCenterZ();
        for (DrugId drug : CuratedDrugChain.ORDER) {
            InnerMegaForms.Form form = InnerMegaForms.formFor(centerX, centerZ, drug);
            if (!chunkIntersects(minX, minZ, form)) {
                continue;
            }
            renderSlice(form, cache, minX, minZ, setter);
        }
    }

    private static boolean chunkIntersects(int minX, int minZ, InnerMegaForms.Form form) {
        int reach = form.radius() + 8;
        return minX + 15 >= form.x() - reach && minX <= form.x() + reach
                && minZ + 15 >= form.z() - reach && minZ <= form.z() + reach;
    }

    private static void renderSlice(InnerMegaForms.Form form, InnerChunkSampleCache cache, int minX, int minZ, BlockSetter setter) {
        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                int worldX = minX + localX;
                int worldZ = minZ + localZ;
                double d = form.distance(worldX, worldZ);
                if (d > form.radius() + 8) {
                    continue;
                }
                InnerTerrain.Sample sample = cache.sample(localX, localZ);
                if (!sample.land()) {
                    continue;
                }
                int g = sample.topY();
                long hash = InnerNoise.mix64(form.hash()
                        ^ (long) worldX * 0x6C62_272EL
                        ^ (long) worldZ * 0x27D4_EB2FL);
                switch (form.drug()) {
                    case COFFEE -> stillPoint(form, worldX, g, worldZ, d, hash, setter);
                    case TOBACCO -> petrifiedChoir(form, worldX, g, worldZ, d, hash, setter);
                    case WEED -> verdantCrater(form, worldX, g, worldZ, d, hash, setter);
                    case HASH -> geodeOfQuiet(form, worldX, g, worldZ, d, hash, setter);
                    case ALCOHOL -> drownedMemory(form, worldX, g, worldZ, hash, setter);
                    case COCAINE -> whiteRazor(form, worldX, g, worldZ, d, hash, setter);
                    case LSD -> prismSpan(form, worldX, g, worldZ, d, hash, setter);
                    case METH -> theFault(form, worldX, g, worldZ, d, hash, setter);
                    case MUSHROOMS -> motherCap(form, worldX, g, worldZ, d, hash, setter);
                    default -> {
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // COFFEE — "Still Point": raked concentric rings and a levitating stone halo.
    // -------------------------------------------------------------------------
    private static void stillPoint(InnerMegaForms.Form form, int x, int g, int z, double d, long hash, BlockSetter setter) {
        if (d < 1.5D) {
            setter.set(new BlockPos(x, g + 1, z), Blocks.SMOOTH_STONE.defaultBlockState());
            setter.set(new BlockPos(x, g + 2, z), Blocks.LANTERN.defaultBlockState());
            return;
        }
        for (int ring = 6; ring <= 18; ring += 6) {
            if (Math.abs(d - ring) < 0.8D) {
                BlockState band = ((int) d & 1) == 0
                        ? Blocks.SMOOTH_STONE.defaultBlockState()
                        : Blocks.BOOKSHELF.defaultBlockState();
                setter.set(new BlockPos(x, g, z), band);
            }
        }
        // Levitating halo at radius ~11, nine blocks up, with four cardinal lanterns.
        if (Math.abs(d - 11.0D) < 1.4D) {
            setter.set(new BlockPos(x, form.baseY() + 9, z), Blocks.SMOOTH_STONE.defaultBlockState());
            double angle = Math.atan2(z - form.z(), x - form.x());
            if (Math.abs(Math.cos(angle * 2.0D)) > 0.985D) {
                setter.set(new BlockPos(x, form.baseY() + 10, z), Blocks.LANTERN.defaultBlockState());
            }
        }
    }

    // -------------------------------------------------------------------------
    // TOBACCO — "Petrified Choir": colossal hollow burned trunks in an ash bowl.
    // -------------------------------------------------------------------------
    private static void petrifiedChoir(InnerMegaForms.Form form, int x, int g, int z, double d, long hash, BlockSetter setter) {
        for (int k = 0; k < 7; k++) {
            long husk = InnerNoise.mix64(form.hash() + 0x4855_534BL + k * 0x9E37L);
            double angle = k * (Math.PI * 2.0D / 7.0D) + ((husk & 63L) / 63.0D - 0.5D) * 0.5D;
            double radius = 7.0D + ((husk >>> 8) & 63L) / 63.0D * 8.0D;
            double hx = form.x() + Math.cos(angle) * radius;
            double hz = form.z() + Math.sin(angle) * radius;
            double dist = Math.hypot(x - hx, z - hz);
            if (dist > 2.6D) {
                continue;
            }
            int height = 14 + (int) ((husk >>> 16) & 7L);
            BlockState trunk = ((husk & 1L) == 0L)
                    ? Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState()
                    : Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState();
            for (int dy = 1; dy <= height; dy++) {
                double taper = 2.6D * (1.0D - dy / (double) height * 0.55D);
                if (dist > taper) {
                    continue;
                }
                // Hollow heart: standing inside a husk and looking up is the point.
                BlockState state = dist < taper - 1.2D
                        ? Blocks.AIR.defaultBlockState()
                        : trunk;
                if (dy == height && dist >= taper - 1.2D && (husk & 3L) != 0L) {
                    state = Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
                }
                setter.set(new BlockPos(x, g + dy, z), state);
            }
            return;
        }
        // Ash bowl floor accents between the husks.
        if ((hash & 31L) == 0L && d < form.radius() - 2) {
            setter.set(new BlockPos(x, g, z), Blocks.TUFF.defaultBlockState());
        }
    }

    // -------------------------------------------------------------------------
    // WEED — "Verdant Crater": terraced garden bowl with a pool and rim glow.
    // -------------------------------------------------------------------------
    private static void verdantCrater(InnerMegaForms.Form form, int x, int g, int z, double d, long hash, BlockSetter setter) {
        if (d < 3.0D) {
            setter.set(new BlockPos(x, g, z), Blocks.WATER.defaultBlockState());
            return;
        }
        if (Math.abs(d - (form.radius() - 2.0D)) < 0.8D && (hash & 7L) == 0L) {
            setter.set(new BlockPos(x, g + 1, z),
                    ModInnerDimensionBlocks.CALMING_ECHO_NODE.get().defaultBlockState());
            return;
        }
        if (d < form.radius() && (hash & 7L) == 0L) {
            BlockState plant = (hash & 8L) == 0L
                    ? ModInnerDimensionBlocks.CALMING_FERN.get().defaultBlockState()
                    : ModInnerDimensionBlocks.BREATH_LILY.get().defaultBlockState();
            setter.set(new BlockPos(x, g + 1, z), plant);
        }
    }

    // -------------------------------------------------------------------------
    // HASH — "Geode of Quiet": a hollow walk-in amethyst dome with crystal teeth.
    // -------------------------------------------------------------------------
    private static void geodeOfQuiet(InnerMegaForms.Form form, int x, int g, int z, double d, long hash, BlockSetter setter) {
        double r = form.radius();
        if (d > r) {
            return;
        }
        // Hemispherical shell, three strata thick, with an entrance arch facing the island centre.
        double shellTop = Math.sqrt(Math.max(0.0D, r * r - d * d)) * 0.80D;
        int yTop = form.baseY() + (int) Math.round(shellTop);
        double angle = Math.atan2(z - form.z(), x - form.x());
        double entrance = InnerRegionMap.angularDistance(angle, form.orientation() + Math.PI);
        boolean inDoorway = entrance < 0.40D && d > r * 0.55D;
        for (int y = yTop - 2; y <= yTop; y++) {
            if (y <= form.baseY()) {
                continue;
            }
            if (inDoorway && y - form.baseY() < 6) {
                continue; // the doorway arch
            }
            BlockState state;
            if (y == yTop) {
                state = Blocks.SMOOTH_BASALT.defaultBlockState();
            } else if (y == yTop - 1) {
                state = Blocks.CALCITE.defaultBlockState();
            } else {
                state = (hash & 15L) == 0L
                        ? Blocks.SEA_LANTERN.defaultBlockState()
                        : Blocks.AMETHYST_BLOCK.defaultBlockState();
            }
            setter.set(new BlockPos(x, y, z), state);
        }
        // Interior: calcite/amethyst floor and budding crystal teeth.
        if (d < r - 4.0D) {
            setter.set(new BlockPos(x, g, z), ((x + z) & 1) == 0
                    ? Blocks.CALCITE.defaultBlockState()
                    : Blocks.AMETHYST_BLOCK.defaultBlockState());
            if ((hash & 15L) == 0L) {
                int teeth = 1 + (int) ((hash >>> 8) & 1L);
                for (int dy = 1; dy <= teeth; dy++) {
                    setter.set(new BlockPos(x, g + dy, z), Blocks.AMETHYST_BLOCK.defaultBlockState());
                }
                setter.set(new BlockPos(x, g + teeth + 1, z), Blocks.AMETHYST_CLUSTER.defaultBlockState());
            }
        }
    }

    // -------------------------------------------------------------------------
    // ALCOHOL — "Drowned Memory": a half-sunken ruined hall with broken arch ribs.
    // -------------------------------------------------------------------------
    private static void drownedMemory(InnerMegaForms.Form form, int x, int g, int z, long hash, BlockSetter setter) {
        double u = form.along(x, z);
        double v = form.perp(x, z);
        double halfLength = 13.0D;
        double halfWidth = 7.0D;
        if (Math.abs(u) > halfLength + 1 || v > halfWidth + 1) {
            return;
        }
        boolean onWall = (Math.abs(Math.abs(u) - halfLength) < 0.6D && v <= halfWidth)
                || (Math.abs(v - halfWidth) < 0.6D && Math.abs(u) <= halfLength);
        if (onWall) {
            int height = 3 + (int) (hash % 6L); // broken, uneven wall tops
            for (int dy = 1; dy <= height; dy++) {
                BlockState wall = ((hash >>> dy) & 3L) == 0L
                        ? Blocks.CRACKED_DEEPSLATE_TILES.defaultBlockState()
                        : Blocks.DEEPSLATE_TILES.defaultBlockState();
                setter.set(new BlockPos(x, g + dy, z), wall);
            }
            return;
        }
        if (Math.abs(u) < halfLength && v < halfWidth) {
            // Tiled floor, flooded down the middle.
            if (v < 3.0D && Math.abs(u) < halfLength - 2) {
                setter.set(new BlockPos(x, g, z), Blocks.WATER.defaultBlockState());
            } else {
                setter.set(new BlockPos(x, g, z), ((hash & 7L) == 0L
                        ? Blocks.CRACKED_DEEPSLATE_TILES
                        : Blocks.DEEPSLATE_TILES).defaultBlockState());
            }
            // Broken arch ribs every five blocks along the hall.
            double rib = Math.abs(u) % 5.0D;
            if ((rib < 0.6D || rib > 4.4D) && ((hash >>> 4) & 3L) != 0L) {
                int archY = g + 8 - (int) Math.round(v * v / 6.0D);
                setter.set(new BlockPos(x, archY, z), Blocks.DEEPSLATE_TILES.defaultBlockState());
                if (v < 1.0D && ((hash >>> 6) & 3L) == 0L) {
                    setter.set(new BlockPos(x, archY - 1, z), Blocks.SOUL_LANTERN.defaultBlockState());
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // COCAINE — "White Razor": tapered monolithic quartz blades with red seams.
    // -------------------------------------------------------------------------
    private static void whiteRazor(InnerMegaForms.Form form, int x, int g, int z, double d, long hash, BlockSetter setter) {
        for (int k = 0; k < 5; k++) {
            long blade = InnerNoise.mix64(form.hash() + 0x424C_4144L + k * 0x85EBL);
            double angle = (blade & 1023L) / 1023.0D * Math.PI;
            double offset = ((blade >>> 10) & 63L) / 63.0D * (form.radius() - 10.0D);
            double offsetAngle = ((blade >>> 20) & 1023L) / 1023.0D * Math.PI * 2.0D;
            double bx = form.x() + Math.cos(offsetAngle) * offset;
            double bz = form.z() + Math.sin(offsetAngle) * offset;
            double along = Math.cos(angle) * (x - bx) + Math.sin(angle) * (z - bz);
            double perp = Math.abs(-Math.sin(angle) * (x - bx) + Math.cos(angle) * (z - bz));
            double halfLength = 5.0D + ((blade >>> 30) & 7L);
            if (perp > 1.2D || Math.abs(along) > halfLength) {
                continue;
            }
            int height = 12 + (int) ((blade >>> 34) & 15L);
            int columnHeight = (int) Math.round(height * (1.0D - Math.abs(along) / halfLength * 0.55D));
            for (int dy = 1; dy <= columnHeight; dy++) {
                BlockState state = dy % 6 == 0
                        ? Blocks.REDSTONE_BLOCK.defaultBlockState()
                        : Blocks.SMOOTH_QUARTZ.defaultBlockState();
                setter.set(new BlockPos(x, g + dy, z), state);
            }
            if (columnHeight > 2 && (hash & 31L) == 0L) {
                setter.set(new BlockPos(x, g + 1, z),
                        ModInnerDimensionBlocks.REDLINE_CRYSTAL_NODE.get().defaultBlockState());
            }
            return;
        }
    }

    // -------------------------------------------------------------------------
    // LSD — "Prism Span": a glowing canyon crossed by tinted-glass arch bridges.
    // -------------------------------------------------------------------------
    private static void prismSpan(InnerMegaForms.Form form, int x, int g, int z, double d, long hash, BlockSetter setter) {
        double perp = form.perp(x, z);
        double along = form.along(x, z);
        if (perp < 4.5D && d < form.radius()) {
            // Canyon floor: a luminous seam running down the middle.
            if (perp < 0.9D) {
                setter.set(new BlockPos(x, g - 1, z), Blocks.SEA_LANTERN.defaultBlockState());
                setter.set(new BlockPos(x, g, z), Blocks.TINTED_GLASS.defaultBlockState());
            } else {
                setter.set(new BlockPos(x, g, z), Blocks.PRISMARINE.defaultBlockState());
            }
        } else if (perp < 6.5D && d < form.radius()) {
            // Canyon lip veneer.
            setter.set(new BlockPos(x, g, z), ((hash & 3L) == 0L
                    ? Blocks.SEA_LANTERN
                    : Blocks.PRISMARINE).defaultBlockState());
        }
        // Two glass arch bridges crossing the canyon.
        for (double bridgeAt : new double[]{-8.0D, 8.0D}) {
            if (Math.abs(along - bridgeAt) < 0.8D && perp <= 6.0D) {
                int deckY = form.baseY() + 5 - (int) Math.round(perp * perp / 20.0D);
                setter.set(new BlockPos(x, deckY, z), Blocks.TINTED_GLASS.defaultBlockState());
                if (((int) Math.round(perp) % 3) == 0) {
                    setter.set(new BlockPos(x, deckY + 1, z), Blocks.SEA_LANTERN.defaultBlockState());
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // METH — "The Fault": a torn obsidian rift, magma veins, lightning spires.
    // -------------------------------------------------------------------------
    private static void theFault(InnerMegaForms.Form form, int x, int g, int z, double d, long hash, BlockSetter setter) {
        double perp = form.perp(x, z);
        double along = form.along(x, z);
        if (perp < 5.5D && d < form.radius()) {
            // Rift floor: blackstone crossed by glowing magma veins.
            boolean vein = Math.floorMod((int) Math.round(along), 6) == 0;
            setter.set(new BlockPos(x, g, z), (vein ? Blocks.MAGMA_BLOCK : Blocks.BLACKSTONE).defaultBlockState());
        } else if (perp < 7.5D && d < form.radius()) {
            // Torn obsidian lips.
            setter.set(new BlockPos(x, g, z), ((hash & 3L) == 0L
                    ? Blocks.CRYING_OBSIDIAN
                    : Blocks.OBSIDIAN).defaultBlockState());
        }
        // Six lightning spires staggered along the rift edges (B3 strikes these).
        int[][] spires = InnerMegaForms.faultSpireOffsets(form);
        for (int i = 0; i < spires.length; i++) {
            double dist = Math.hypot(x - spires[i][0], z - spires[i][1]);
            if (dist > 1.8D) {
                continue;
            }
            long spire = InnerNoise.mix64(form.hash() + 0x5350_4952L + i);
            int height = 12 + (int) (spire & 15L);
            for (int dy = 1; dy <= height; dy++) {
                double taper = 1.8D * (1.0D - dy / (double) height * 0.7D);
                if (dist > taper) {
                    continue;
                }
                setter.set(new BlockPos(x, g + dy, z), (dy % 7 == 0
                        ? Blocks.MAGMA_BLOCK
                        : Blocks.BASALT).defaultBlockState());
            }
            if (dist < 0.8D) {
                setter.set(new BlockPos(x, g + height + 1, z), Blocks.LIGHTNING_ROD.defaultBlockState());
            }
            return;
        }
    }

    // -------------------------------------------------------------------------
    // MUSHROOMS — "Mother Cap": a hill-sized mushroom with a walkable gill ring.
    // -------------------------------------------------------------------------
    private static void motherCap(InnerMegaForms.Form form, int x, int g, int z, double d, long hash, BlockSetter setter) {
        int stemTop = form.baseY() + 16;
        // Stem with a hollow climbing shaft lit by shroomlight.
        if (d < 4.2D) {
            for (int y = g + 1; y <= stemTop; y++) {
                BlockState state;
                if (d < 2.4D) {
                    state = Blocks.AIR.defaultBlockState();
                } else if ((y - form.baseY()) % 4 == 0 && (hash & 3L) == 0L) {
                    state = Blocks.SHROOMLIGHT.defaultBlockState();
                } else {
                    state = Blocks.MUSHROOM_STEM.defaultBlockState();
                }
                setter.set(new BlockPos(x, y, z), state);
            }
            return;
        }
        double capRadius = 20.0D;
        if (d <= capRadius) {
            // Gill ring: walkable radial stripes of stem and shroomlight under the cap.
            if (d >= 5.0D && d <= 18.0D) {
                double angle = Math.atan2(z - form.z(), x - form.x());
                int spoke = (int) Math.floor((angle + Math.PI) * 12.0D / Math.PI);
                setter.set(new BlockPos(x, stemTop, z), ((spoke & 1) == 0
                        ? Blocks.MUSHROOM_STEM
                        : Blocks.SHROOMLIGHT).defaultBlockState());
            }
            // The cap dome above.
            BlockState cap = (form.hash() & 1L) == 0L
                    ? Blocks.RED_MUSHROOM_BLOCK.defaultBlockState()
                    : Blocks.BROWN_MUSHROOM_BLOCK.defaultBlockState();
            double lift = Math.sqrt(Math.max(0.0D, 1.0D - (d / capRadius) * (d / capRadius))) * 9.0D;
            int capY = stemTop + 1 + (int) Math.round(lift);
            setter.set(new BlockPos(x, capY, z), cap);
            setter.set(new BlockPos(x, capY - 1, z), cap);
            // Spore strands drifting beneath the gills.
            if ((hash & 63L) == 0L && d > 6.0D) {
                setter.set(new BlockPos(x, stemTop - 2 - (int) ((hash >>> 8) & 3L), z),
                        Blocks.SHROOMLIGHT.defaultBlockState());
            }
        }
    }

    private interface BlockSetter {
        void set(BlockPos pos, BlockState state);
    }
}
