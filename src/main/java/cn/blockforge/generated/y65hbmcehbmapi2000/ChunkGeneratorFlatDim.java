package cn.blockforge.generated.y65hbmcehbmapi2000;

import java.util.Collections;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;

/**
 * 平坦地形生成器（生成地形关闭时使用）。
 *
 * <p>当 {@link DimConfig#generateTerrain} 为 {@code false} 时，本维度不再生成起伏地形，
 * 而是在固定高度 {@link DimConfig#surfaceY} 处铺一层平坦地表：最上层是草方块，下面两层泥土，
 * 其余到基岩都是石头。玩家落点统一在这一层，不会出现山体或水面。
 *
 * <p>生物刷新遵循 {@link DimConfig#spawnMobs}；平坦模式下不生成结构（村庄、要塞等）——
 * 结构开关只在生成地形开启时起作用。
 */
public class ChunkGeneratorFlatDim implements IChunkGenerator {

    private final World world;

    public ChunkGeneratorFlatDim(World world, long seed) {
        this.world = world;
    }

    @Override
    public Chunk generateChunk(int x, int z) {
        ChunkPrimer primer = new ChunkPrimer();
        int top = DimConfig.surfaceY;

        for (int y = 0; y <= top; y++) {
            IBlockState state;
            if (y == 0) {
                state = Blocks.BEDROCK.getDefaultState();
            } else if (y == top) {
                state = Blocks.GRASS.getDefaultState();
            } else if (y >= top - 2) {
                state = Blocks.DIRT.getDefaultState();
            } else {
                state = Blocks.STONE.getDefaultState();
            }
            for (int lx = 0; lx < 16; lx++) {
                for (int lz = 0; lz < 16; lz++) {
                    primer.setBlockState(lx, y, lz, state);
                }
            }
        }

        Chunk chunk = new Chunk(this.world, primer, x, z);

        // 填群系：让平坦世界也按种子分布群系，保证草皮颜色、降雨、刷怪按群系来。
        BiomeProvider biomeProvider = this.world.getBiomeProvider();
        byte[] biomeArray = chunk.getBiomeArray();
        for (int lz = 0; lz < 16; lz++) {
            for (int lx = 0; lx < 16; lx++) {
                int wx = x * 16 + lx;
                int wz = z * 16 + lz;
                Biome biome = biomeProvider.getBiome(new BlockPos(wx, top, wz));
                biomeArray[lz * 16 + lx] = (byte) Biome.getIdForBiome(biome);
            }
        }
        chunk.setBiomeArray(biomeArray);

        return chunk;
    }

    @Override
    public void populate(int x, int z) {
        // 平坦模式不额外生成装饰/结构，保持一块干净的草地。
    }

    @Override
    public boolean generateStructures(Chunk chunk, int x, int z) {
        return false;
    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        if (!DimConfig.spawnMobs) {
            return Collections.emptyList();
        }
        return this.world.getBiome(pos).getSpawnableList(creatureType);
    }

    @Override
    public BlockPos getNearestStructurePos(World world, String structureName, BlockPos pos, boolean findUnexplored) {
        return null;
    }

    @Override
    public void recreateStructures(Chunk chunk, int x, int z) {
        // 平坦模式无结构。
    }

    @Override
    public boolean isInsideStructure(World world, String structureName, BlockPos pos) {
        return false;
    }
}
