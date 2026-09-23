package cn.blockforge.generated.y65hbmcehbmapi2000;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenFlowers;
import net.minecraft.world.gen.feature.WorldGenTallGrass;
import net.minecraft.world.gen.feature.WorldGenTrees;
import net.minecraftforge.event.ForgeEventFactory;

/**
 * 极平坦草原地形生成器 + 大型蜿蜒河流，跨存档一致。
 *
 * <p><b>河岸自然化</b>（本版本重点）：
 * <ul>
 *   <li><b>河岸更宽</b>：默认 25 格（原 15），坡度摊薄约 40%；</li>
 *   <li><b>河岸宽度波动</b>：随 z 有 ±25% 变化，避免整条河完全对称；</li>
 *   <li><b>河床沿流向缓变</b>：±0.6 格、波长约 8400 格，无网格感；</li>
 *   <li><b>位置噪声</b>：用坐标哈希在河岸过渡带叠加 ±0.6 格起伏，
 *       中段最强、两端归零，打破光滑曲线的"塑料感"。</li>
 * </ul>
 *
 * <p><b>其他特性</b>：
 * <ul>
 *   <li>河床完全平坦（无网格状起伏）；</li>
 *   <li>河岸表层为沙/砾石按离水距离分层混合；</li>
 *   <li>不生成原版矿物、湖泊、岩浆湖、泉水；</li>
 *   <li>跨存档一致（地形与装饰均与世界种子无关）。</li>
 * </ul>
 */
public class ChunkGeneratorGrasslandFlat implements IChunkGenerator {

    // =========================================================================
    //  不可配置常量
    // =========================================================================

    /** 海平面高度，同时作为河流水面高度。 */
    private static final int SEA_LEVEL = 62;

    /** 草原基准高度。 */
    private static final int BASE_HEIGHT = 70;

    // =========================================================================
    //  河岸材质参数
    // =========================================================================

    /** 近水带宽度（格）。此段以沙子为主。 */
    private static final double BANK_NEAR_W = 2.0;

    /** 过渡带宽度（格）。此段沙砾各半。 */
    private static final double BANK_MID_W = 6.0;

    /** 近水带砾石比例。 */
    private static final float GRAVEL_NEAR = 0.10f;

    /** 过渡带砾石比例。 */
    private static final float GRAVEL_MID  = 0.50f;

    /** 远岸带砾石比例。 */
    private static final float GRAVEL_FAR  = 0.75f;

    // =========================================================================
    //  河岸形态参数（本版本新增）
    // =========================================================================

    /** 河岸宽度随 z 波动的相对幅度（0.25 → ±25%）。 */
    private static final double BANK_WIDTH_VARIATION = 0.25;

    /** 河床高度沿流向缓变的幅度（格）。 */
    private static final double BED_DRIFT_AMPLITUDE = 0.6;

    /** 河岸过渡带位置噪声的振幅（格）。 */
    private static final double BANK_NOISE_AMP = 1.2;

    // =========================================================================
    //  参数缓存
    // =========================================================================

    private static final class Params {

        final double ampX, ampZ, ampDiag;
        final double freqX, freqZ, freqDiag;

        final double hwBase, hwAmp;
        final double riverBed;
        final double riverBank;

        Params() {
            double amp = clamp(DimConfig.terrainAmplitude,
                    DimConfig.TERRAIN_AMP_MIN, DimConfig.TERRAIN_AMP_MAX);
            double freq = clamp(DimConfig.terrainFrequency,
                    DimConfig.TERRAIN_FREQ_MIN, DimConfig.TERRAIN_FREQ_MAX);

            this.ampX    = amp * 0.50;
            this.ampZ    = amp * 0.42;
            this.ampDiag = amp * 0.25;

            this.freqX    = freq;
            this.freqZ    = freq * 0.91;
            this.freqDiag = freq * 0.63;

            this.hwBase = clamp(DimConfig.riverHalfWidthBase,
                    DimConfig.RIVER_HW_BASE_MIN, DimConfig.RIVER_HW_BASE_MAX);

            double hwAmpMax = hwBase * DimConfig.RIVER_HW_AMP_RATIO_MAX;
            this.hwAmp = clamp(DimConfig.riverHalfWidthAmplitude, 0.0, hwAmpMax);

            double terrainMin = BASE_HEIGHT - (ampX + ampZ + ampDiag);
            double bedMax = Math.min(DimConfig.RIVER_BED_MAX, terrainMin - 1.0);
            bedMax = Math.max(bedMax, DimConfig.RIVER_BED_MIN);

            this.riverBed = clamp(DimConfig.riverBed,
                    DimConfig.RIVER_BED_MIN, bedMax);

            this.riverBank = clamp(DimConfig.riverBank,
                    DimConfig.RIVER_BANK_MIN, DimConfig.RIVER_BANK_MAX);
        }

        private static double clamp(double v, double min, double max) {
            return v < min ? min : (v > max ? max : v);
        }
    }

    // =========================================================================
    //  成员变量
    // =========================================================================

    private final World world;
    private final Random rand;
    private final Params par = new Params();

    // =========================================================================
    //  构造
    // =========================================================================

    public ChunkGeneratorGrasslandFlat(World world) {
        this.world = world;
        this.rand = new Random(DimConfig.grasslandSeed);
    }

    // =========================================================================
    //  河流几何（纯函数）
    // =========================================================================

    /** 河流中心线 X 坐标（三层正弦叠加，波长 4200/1600/630）。 */
    private double riverCenterX(double z) {
        return 80.0 * Math.sin(z * 0.00150)
             + 35.0 * Math.sin(z * 0.00400 + 1.7)
             + 15.0 * Math.sin(z * 0.01000 + 0.5);
    }

    /** 河流半宽（基准 31 + 波动 ±9 → 全宽 44~80 格）。 */
    private double riverHalfWidth(double z) {
        return par.hwBase + par.hwAmp * Math.sin(z * 0.00160 + 2.3);
    }

    /**
     * 河岸过渡带宽度。<b>本版本新增波动</b>：基准值乘上 ±25% 的缓慢正弦，
     * 让整条河的河岸时宽时窄，视觉上更接近自然河流。
     */
    private double riverBankWidth(double z) {
        return par.riverBank
             * (1.0 + BANK_WIDTH_VARIATION * Math.sin(z * 0.00210 + 4.1));
    }

    /**
     * 河床高度。<b>本版本新增沿流向缓变</b>：波长约 8400 格，
     * 幅度 ±0.6 格，模拟河床沿流动方向的整体微倾斜。
     *
     * <p>因波长极长、且只沿 z 方向变化，不会产生网格感。
     */
    private double riverBedHeight(double z) {
        return par.riverBed + BED_DRIFT_AMPLITUDE * Math.sin(z * 0.00075);
    }

    /** 点 (x, z) 到河流中心线的水平距离近似值。 */
    private double distToRiver(double x, double z) {
        return Math.abs(x - riverCenterX(z));
    }

    // =========================================================================
    //  确定性哈希
    // =========================================================================

    /**
     * 基于世界坐标的确定性伪随机值，范围 [0, 1)。
     *
     * <p>不依赖任何 {@code Random} 实例，同一坐标永远返回同一结果，
     * 无论区块生成顺序如何。用于河岸材质与河岸噪声。
     */
    private static float hash01(int wx, int wy, int wz) {
        int h = wx * 374761393 + wy * 668265263 + wz * 1274126177;
        h = (h ^ (h >>> 13)) * 1274126177;
        h = h ^ (h >>> 16);
        return (h & 0x7FFFFFFF) / (float) 0x7FFFFFFF;
    }

    // =========================================================================
    //  地形高度
    // =========================================================================

    /**
     * 计算某世界坐标处的地形最高方块 Y 坐标（浮点）。
     *
     * <p><b>河岸自然化的核心</b>：过渡带在基础 smoothstep 曲线上
     * 叠加了一个位置噪声，其振幅在中段（t≈0.5）最大、两端归零，
     * 因此不会破坏"河床连通"和"草原衔接"两个边界条件，
     * 只让中段剖面有自然凹凸。
     */
    private double getHeight(int wx, int wz) {
        // ---- 1. 草原基础地形 ----
        double h = BASE_HEIGHT;
        h += par.ampX    * Math.sin(wx * par.freqX);
        h += par.ampZ    * Math.cos(wz * par.freqZ);
        h += par.ampDiag * Math.sin((wx + wz) * par.freqDiag);

        // ---- 2. 河流下切 ----
        double hw        = riverHalfWidth(wz);
        double bankW     = riverBankWidth(wz);
        double d         = distToRiver(wx, wz);
        double influence = hw + bankW;

        if (d < influence) {
            double bedH = riverBedHeight(wz);

            if (d < hw) {
                // 河道内：平坦河床
                h = bedH;
            } else {
                // 河岸过渡带：smoothstep + 位置噪声
                double t = MathHelper.clamp((d - hw) / bankW, 0.0, 1.0);
                double smooth = t * t * (3.0 - 2.0 * t);

                // 噪声在中段最强，两端归零（t·(1-t)·4 在 t=0.5 取到 1）
                double noise = (hash01(wx, 0, wz) - 0.5)
                             * BANK_NOISE_AMP * (t * (1.0 - t) * 4.0);

                h = bedH + (h - bedH) * smooth + noise;
            }
        }

        return h;
    }

    // =========================================================================
    //  河岸材质
    // =========================================================================

    /**
     * 根据位置与离岸距离，选择河岸表层方块是沙子还是砾石。
     */
    private IBlockState pickBankSurface(int wx, int wy, int wz, double d, double hw) {
        double bankDist = d - hw;

        float gravelChance;
        if (bankDist < BANK_NEAR_W) {
            gravelChance = GRAVEL_NEAR;
        } else if (bankDist < BANK_MID_W) {
            gravelChance = GRAVEL_MID;
        } else {
            gravelChance = GRAVEL_FAR;
        }

        return (hash01(wx, wy, wz) < gravelChance)
                ? Blocks.GRAVEL.getDefaultState()
                : Blocks.SAND.getDefaultState();
    }

    // =========================================================================
    //  区块生成
    // =========================================================================

    @Override
    public Chunk generateChunk(int chunkX, int chunkZ) {
        this.rand.setSeed((long) chunkX * 341873128712L
                        + (long) chunkZ * 132897987541L);

        ChunkPrimer primer = new ChunkPrimer();
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;

        IBlockState grass   = Blocks.GRASS.getDefaultState();
        IBlockState dirt    = Blocks.DIRT.getDefaultState();
        IBlockState stone   = Blocks.STONE.getDefaultState();
        IBlockState bedrock = Blocks.BEDROCK.getDefaultState();
        IBlockState water   = Blocks.WATER.getDefaultState();

        // ---- 1. 逐列填充固体地形 ----
        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int wx = baseX + lx;
                int wz = baseZ + lz;
                int top = MathHelper.floor(getHeight(wx, wz));

                double hw    = riverHalfWidth(wz);
                double bankW = riverBankWidth(wz);
                double d     = distToRiver(wx, wz);

                // 表层材质判断：河道附近 + 不高于海平面 → 沙/砾石；否则草
                boolean nearRiver = d < hw + bankW * 1.5;
                IBlockState surface;
                if (nearRiver && top <= SEA_LEVEL) {
                    surface = pickBankSurface(wx, top, wz, d, hw);
                } else {
                    surface = grass;
                }

                for (int y = 0; y <= top; y++) {
                    IBlockState s;
                    if (y == 0)            s = bedrock;
                    else if (y == top)     s = surface;
                    else if (y > top - 4)  s = dirt;
                    else                   s = stone;
                    primer.setBlockState(lx, y, lz, s);
                }
            }
        }

        // ---- 2. 填水 ----
        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int wx = baseX + lx;
                int wz = baseZ + lz;
                int top = MathHelper.floor(getHeight(wx, wz));

                for (int y = top + 1; y <= SEA_LEVEL; y++) {
                    if (primer.getBlockState(lx, y, lz).getBlock() == Blocks.AIR) {
                        primer.setBlockState(lx, y, lz, water);
                    }
                }
            }
        }

        // ---- 3. 生成 Chunk 并写入群系 ----
        Chunk chunk = new Chunk(this.world, primer, chunkX, chunkZ);

        byte[] biomeBytes = chunk.getBiomeArray();
        Biome[] biomes = this.world.getBiomeProvider()
                .getBiomesForGeneration(new Biome[256], baseX, baseZ, 16, 16);
        for (int i = 0; i < biomeBytes.length; i++) {
            biomeBytes[i] = (byte) (Biome.getIdForBiome(biomes[i]) & 0xFF);
        }

        chunk.generateSkylightMap();
        return chunk;
    }

    // =========================================================================
    //  区块装饰
    // =========================================================================

    @Override
    public void populate(int chunkX, int chunkZ) {
        BlockFalling.fallInstantly = true;
        int bx = chunkX << 4;
        int bz = chunkZ << 4;
        BlockPos origin = new BlockPos(bx, 0, bz);

        long seed = DimConfig.grasslandSeed;
        this.rand.setSeed(seed);
        long k = this.rand.nextLong() / 2L * 2L + 1L;
        long l = this.rand.nextLong() / 2L * 2L + 1L;
        this.rand.setSeed((long) chunkX * k + (long) chunkZ * l ^ seed);

        ForgeEventFactory.onChunkPopulate(true, this, this.world, this.rand,
                chunkX, chunkZ, false);

        decorateGrassland(origin);

        ForgeEventFactory.onChunkPopulate(false, this, this.world, this.rand,
                chunkX, chunkZ, false);

        BlockFalling.fallInstantly = false;
    }

    /**
     * 手动执行平原风格的视觉装饰：高草、花、稀疏橡树。
     * 不含任何原版矿物、湖泊、岩浆湖、泉水。
     */
    private void decorateGrassland(BlockPos origin) {
        World w = this.world;

        // 高草：每区块约 10 簇
        for (int i = 0; i < 10; i++) {
            BlockPos p = origin.add(
                    this.rand.nextInt(16) + 8, 0,
                    this.rand.nextInt(16) + 8);
            int y = w.getHeight(p.getX(), p.getZ());
            new WorldGenTallGrass(BlockTallGrass.EnumType.GRASS)
                    .generate(w, this.rand, new BlockPos(p.getX(), y, p.getZ()));
        }

        // 花（蒲公英 / 虞美人）：每区块约 4 簇
        for (int i = 0; i < 4; i++) {
            BlockPos p = origin.add(
                    this.rand.nextInt(16) + 8, 0,
                    this.rand.nextInt(16) + 8);
            int y = w.getHeight(p.getX(), p.getZ());
            BlockPos fp = new BlockPos(p.getX(), y, p.getZ());

            if (this.rand.nextBoolean()) {
                new WorldGenFlowers(Blocks.YELLOW_FLOWER,
                        BlockFlower.EnumFlowerType.DANDELION)
                        .generate(w, this.rand, fp);
            } else {
                new WorldGenFlowers(Blocks.RED_FLOWER,
                        BlockFlower.EnumFlowerType.POPPY)
                        .generate(w, this.rand, fp);
            }
        }

        // 稀疏橡树：每区块约 1/3 概率一棵
        if (this.rand.nextInt(3) == 0) {
            BlockPos p = origin.add(
                    this.rand.nextInt(16) + 8, 0,
                    this.rand.nextInt(16) + 8);
            int y = w.getHeight(p.getX(), p.getZ());
            new WorldGenTrees(false)
                    .generate(w, this.rand, new BlockPos(p.getX(), y, p.getZ()));
        }
    }

    // =========================================================================
    //  IChunkGenerator 其余接口
    // =========================================================================

    @Override
    public boolean generateStructures(Chunk chunkIn, int x, int z) {
        return DimConfig.generateStructures;
    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType type, BlockPos pos) {
        if (!DimConfig.spawnMobs) {
            return Collections.emptyList();
        }
        return this.world.getBiome(pos).getSpawnableList(type);
    }

    @Override
    public BlockPos getNearestStructurePos(World worldIn, String structureName,
                                           BlockPos position, boolean findUnexplored) {
        // 草原模式不生成任何结构
        return null;
    }

    @Override
    public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos) {
        return false;
    }

    @Override
    public void recreateStructures(Chunk chunkIn, int x, int z) {
        // 无结构
    }
}