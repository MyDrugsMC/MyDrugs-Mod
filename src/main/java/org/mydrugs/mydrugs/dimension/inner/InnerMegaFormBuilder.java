package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
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
    private static final int MARGIN = 0;

    private InnerMegaFormBuilder() {
    }

    static void place(InnerBlockSink sink, InnerChunkSampleCache cache, int minX, int minZ) {
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
            renderSlice(form, cache, minX, minZ, sink);
        }
    }

    private static boolean chunkIntersects(int minX, int minZ, InnerMegaForms.Form form) {
        int reach = form.radius() + 8;
        return minX + 15 >= form.x() - reach && minX <= form.x() + reach
                && minZ + 15 >= form.z() - reach && minZ <= form.z() + reach;
    }

    private static void renderSlice(InnerMegaForms.Form form, InnerChunkSampleCache cache, int minX, int minZ, InnerBlockSink sink) {
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
                    case COFFEE -> stillPoint(form, worldX, g, worldZ, d, hash, sink);
                    case TOBACCO -> petrifiedChoir(form, worldX, g, worldZ, d, hash, sink);
                    case WEED -> verdantCrater(form, worldX, g, worldZ, d, hash, sink);
                    case HASH -> geodeOfQuiet(form, worldX, g, worldZ, d, hash, sink);
                    case ALCOHOL -> drownedMemory(form, worldX, g, worldZ, hash, sink);
                    case COCAINE -> whiteRazor(form, worldX, g, worldZ, d, hash, sink);
                    case LSD -> prismSpan(form, worldX, g, worldZ, d, hash, sink);
                    case METH -> theFault(form, worldX, g, worldZ, d, hash, sink);
                    case MUSHROOMS -> motherCap(form, worldX, g, worldZ, d, hash, sink);
                    default -> {
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // COFFEE — "Still Point": raked concentric rings and a levitating stone halo.
    // -------------------------------------------------------------------------
    private static void stillPoint(InnerMegaForms.Form form, int x, int g, int z, double d, long hash, InnerBlockSink sink) {
        if (d < 1.5D) {
            sink.setBlock(new BlockPos(x, g + 1, z), Blocks.SMOOTH_STONE.defaultBlockState(), true);
            sink.setBlock(new BlockPos(x, g + 2, z), Blocks.LANTERN.defaultBlockState(), true);
            return;
        }
        for (int ring = 6; ring <= 18; ring += 6) {
            if (Math.abs(d - ring) < 0.8D) {
                BlockState band = ((int) d & 1) == 0
                        ? Blocks.SMOOTH_STONE.defaultBlockState()
                        : Blocks.BOOKSHELF.defaultBlockState();
                sink.setBlock(new BlockPos(x, g, z), band, true);
            }
        }
        // Levitating halo at radius ~11, nine blocks up, with four cardinal lanterns.
        if (Math.abs(d - 11.0D) < 1.4D) {
            sink.setBlock(new BlockPos(x, form.baseY() + 9, z), Blocks.SMOOTH_STONE.defaultBlockState(), true);
            double angle = Math.atan2(z - form.z(), x - form.x());
            if (Math.abs(Math.cos(angle * 2.0D)) > 0.985D) {
                sink.setBlock(new BlockPos(x, form.baseY() + 10, z), Blocks.LANTERN.defaultBlockState(), true);
            }
        }
    }

    // -------------------------------------------------------------------------
    // TOBACCO — "Petrified Choir": colossal hollow burned trunks in an ash bowl.
    // -------------------------------------------------------------------------
    private static void petrifiedChoir(InnerMegaForms.Form form, int x, int g, int z, double d, long hash, InnerBlockSink sink) {
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
                sink.setBlock(new BlockPos(x, g + dy, z), state, true);
            }
            return;
        }
        // Ash bowl floor accents between the husks.
        if ((hash & 31L) == 0L && d < form.radius() - 2) {
            sink.setBlock(new BlockPos(x, g, z), Blocks.TUFF.defaultBlockState(), true);
        }
    }

    // -------------------------------------------------------------------------
    // WEED — "Verdant Crater": terraced garden bowl with a pool and rim glow.
    // -------------------------------------------------------------------------
    private static void verdantCrater(InnerMegaForms.Form form, int x, int g, int z, double d, long hash, InnerBlockSink sink) {
        if (d < 3.0D) {
            sink.setBlock(new BlockPos(x, g, z), Blocks.WATER.defaultBlockState(), true);
            return;
        }
        if (Math.abs(d - (form.radius() - 2.0D)) < 0.8D && (hash & 7L) == 0L) {
            sink.setBlock(new BlockPos(x, g + 1, z),
                    ModInnerDimensionBlocks.CALMING_ECHO_NODE.get().defaultBlockState(), true);
            return;
        }
        if (d < form.radius() && (hash & 7L) == 0L) {
            BlockState plant = (hash & 8L) == 0L
                    ? ModInnerDimensionBlocks.CALMING_FERN.get().defaultBlockState()
                    : ModInnerDimensionBlocks.BREATH_LILY.get().defaultBlockState();
            sink.setBlock(new BlockPos(x, g + 1, z), plant, true);
        }
    }

    // -------------------------------------------------------------------------
    // HASH — "Geode of Quiet": a hollow walk-in amethyst dome with crystal teeth.
    // -------------------------------------------------------------------------
    private static void geodeOfQuiet(InnerMegaForms.Form form, int x, int g, int z, double d, long hash, InnerBlockSink sink) {
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
            sink.setBlock(new BlockPos(x, y, z), state, true);
        }
        // Interior: calcite/amethyst floor and budding crystal teeth.
        if (d < r - 4.0D) {
            sink.setBlock(new BlockPos(x, g, z), ((x + z) & 1) == 0
                    ? Blocks.CALCITE.defaultBlockState()
                    : Blocks.AMETHYST_BLOCK.defaultBlockState(), true);
            if ((hash & 15L) == 0L) {
                int teeth = 1 + (int) ((hash >>> 8) & 1L);
                for (int dy = 1; dy <= teeth; dy++) {
                    sink.setBlock(new BlockPos(x, g + dy, z), Blocks.AMETHYST_BLOCK.defaultBlockState(), true);
                }
                sink.setBlock(new BlockPos(x, g + teeth + 1, z), Blocks.AMETHYST_CLUSTER.defaultBlockState(), true);
            }
        }
    }

    // -------------------------------------------------------------------------
    // ALCOHOL — "Drowned Memory": a half-sunken ruined hall with broken arch ribs.
    // -------------------------------------------------------------------------
    private static void drownedMemory(InnerMegaForms.Form form, int x, int g, int z, long hash, InnerBlockSink sink) {
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
                sink.setBlock(new BlockPos(x, g + dy, z), wall, true);
            }
            return;
        }
        if (Math.abs(u) < halfLength && v < halfWidth) {
            // Tiled floor, flooded down the middle.
            if (v < 3.0D && Math.abs(u) < halfLength - 2) {
                sink.setBlock(new BlockPos(x, g, z), Blocks.WATER.defaultBlockState(), true);
            } else {
                sink.setBlock(new BlockPos(x, g, z), ((hash & 7L) == 0L
                        ? Blocks.CRACKED_DEEPSLATE_TILES
                        : Blocks.DEEPSLATE_TILES).defaultBlockState(), true);
            }
            // Broken arch ribs every five blocks along the hall.
            double rib = Math.abs(u) % 5.0D;
            if ((rib < 0.6D || rib > 4.4D) && ((hash >>> 4) & 3L) != 0L) {
                int archY = g + 8 - (int) Math.round(v * v / 6.0D);
                sink.setBlock(new BlockPos(x, archY, z), Blocks.DEEPSLATE_TILES.defaultBlockState(), true);
                if (v < 1.0D && ((hash >>> 6) & 3L) == 0L) {
                    sink.setBlock(new BlockPos(x, archY - 1, z), Blocks.SOUL_LANTERN.defaultBlockState(), true);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // COCAINE — "White Razor": tapered monolithic quartz blades with red seams.
    // -------------------------------------------------------------------------
    private static void whiteRazor(InnerMegaForms.Form form, int x, int g, int z, double d, long hash, InnerBlockSink sink) {
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
                sink.setBlock(new BlockPos(x, g + dy, z), state, true);
            }
            if (columnHeight > 2 && (hash & 31L) == 0L) {
                sink.setBlock(new BlockPos(x, g + 1, z),
                        ModInnerDimensionBlocks.REDLINE_CRYSTAL_NODE.get().defaultBlockState(), true);
            }
            return;
        }
    }

    // -------------------------------------------------------------------------
    // LSD — "Prism Span": a glowing canyon crossed by tinted-glass arch bridges.
    // -------------------------------------------------------------------------
    private static void prismSpan(InnerMegaForms.Form form, int x, int g, int z, double d, long hash, InnerBlockSink sink) {
        double perp = form.perp(x, z);
        double along = form.along(x, z);
        if (perp < 4.5D && d < form.radius()) {
            // Canyon floor: a luminous seam running down the middle.
            if (perp < 0.9D) {
                sink.setBlock(new BlockPos(x, g - 1, z), Blocks.SEA_LANTERN.defaultBlockState(), true);
                sink.setBlock(new BlockPos(x, g, z), Blocks.TINTED_GLASS.defaultBlockState(), true);
            } else {
                sink.setBlock(new BlockPos(x, g, z), Blocks.PRISMARINE.defaultBlockState(), true);
            }
        } else if (perp < 6.5D && d < form.radius()) {
            // Canyon lip veneer.
            sink.setBlock(new BlockPos(x, g, z), ((hash & 3L) == 0L
                    ? Blocks.SEA_LANTERN
                    : Blocks.PRISMARINE).defaultBlockState(), true);
        }
        // Two glass arch bridges crossing the canyon.
        for (double bridgeAt : new double[]{-8.0D, 8.0D}) {
            if (Math.abs(along - bridgeAt) < 0.8D && perp <= 6.0D) {
                int deckY = form.baseY() + 5 - (int) Math.round(perp * perp / 20.0D);
                sink.setBlock(new BlockPos(x, deckY, z), Blocks.TINTED_GLASS.defaultBlockState(), true);
                if (((int) Math.round(perp) % 3) == 0) {
                    sink.setBlock(new BlockPos(x, deckY + 1, z), Blocks.SEA_LANTERN.defaultBlockState(), true);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // METH — "The Fault": a torn obsidian rift, magma veins, lightning spires.
    // -------------------------------------------------------------------------
    private static void theFault(InnerMegaForms.Form form, int x, int g, int z, double d, long hash, InnerBlockSink sink) {
        double perp = form.perp(x, z);
        double along = form.along(x, z);
        if (perp < 5.5D && d < form.radius()) {
            // Rift floor: blackstone crossed by glowing magma veins.
            boolean vein = Math.floorMod((int) Math.round(along), 6) == 0;
            sink.setBlock(new BlockPos(x, g, z), (vein ? Blocks.MAGMA_BLOCK : Blocks.BLACKSTONE).defaultBlockState(), true);
        } else if (perp < 7.5D && d < form.radius()) {
            // Torn obsidian lips.
            sink.setBlock(new BlockPos(x, g, z), ((hash & 3L) == 0L
                    ? Blocks.CRYING_OBSIDIAN
                    : Blocks.OBSIDIAN).defaultBlockState(), true);
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
                sink.setBlock(new BlockPos(x, g + dy, z), (dy % 7 == 0
                        ? Blocks.MAGMA_BLOCK
                        : Blocks.BASALT).defaultBlockState(), true);
            }
            if (dist < 0.8D) {
                sink.setBlock(new BlockPos(x, g + height + 1, z), Blocks.LIGHTNING_ROD.defaultBlockState(), true);
            }
            return;
        }
    }

    // -------------------------------------------------------------------------
    // MUSHROOMS — "Mother Cap": a hill-sized mushroom with a walkable gill ring.
    // -------------------------------------------------------------------------
    private static void motherCap(InnerMegaForms.Form form, int x, int g, int z, double d, long hash, InnerBlockSink sink) {
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
                sink.setBlock(new BlockPos(x, y, z), state, true);
            }
            return;
        }
        double capRadius = 20.0D;
        if (d <= capRadius) {
            // Gill ring: walkable radial stripes of stem and shroomlight under the cap.
            if (d >= 5.0D && d <= 18.0D) {
                double angle = Math.atan2(z - form.z(), x - form.x());
                int spoke = (int) Math.floor((angle + Math.PI) * 12.0D / Math.PI);
                sink.setBlock(new BlockPos(x, stemTop, z), ((spoke & 1) == 0
                        ? Blocks.MUSHROOM_STEM
                        : Blocks.SHROOMLIGHT).defaultBlockState(), true);
            }
            // The cap dome above.
            BlockState cap = (form.hash() & 1L) == 0L
                    ? Blocks.RED_MUSHROOM_BLOCK.defaultBlockState()
                    : Blocks.BROWN_MUSHROOM_BLOCK.defaultBlockState();
            double lift = Math.sqrt(Math.max(0.0D, 1.0D - (d / capRadius) * (d / capRadius))) * 9.0D;
            int capY = stemTop + 1 + (int) Math.round(lift);
            sink.setBlock(new BlockPos(x, capY, z), cap, true);
            sink.setBlock(new BlockPos(x, capY - 1, z), cap, true);
            // Spore strands drifting beneath the gills.
            if ((hash & 63L) == 0L && d > 6.0D) {
                sink.setBlock(new BlockPos(x, stemTop - 2 - (int) ((hash >>> 8) & 3L), z),
                        Blocks.SHROOMLIGHT.defaultBlockState(), true);
            }
        }
    }
}
