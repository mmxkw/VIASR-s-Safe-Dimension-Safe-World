package cn.blockforge.generated.y65hbmcehbmapi2000;

import java.util.Collections;
import java.util.List;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGeneratorOverworld;

/**
 * 基于原版主世界生成器的新维度地形生成器（生成地形开启时使用）。
 *
 * <p>直接复用原版 {@link ChunkGeneratorOverworld}：它会按世界种子正常生成多种生物群系、
 * 对应的地形与装饰（树、花、矿石、洞穴），并在 populate 时触发 Forge 的事件
 * （{@code DecorateBiomeEvent} 等），是第三方模组资源生成的标准分发路径。
 *
 * <p>可调项（见 {@link DimConfig}，修改后需重启生效）：
 * <ul>
 *   <li>{@code generateStructures}：false 时关闭村庄、要塞、废弃矿井等生成结构；</li>
 *   <li>{@code spawnMobs}：false 时通过 {@link #getPossibleCreatures} 返回空列表，
 *       从而在本维度禁用所有生物的刷新。</li>
 * </ul>
 */
public class ChunkGeneratorOverworldDim extends ChunkGeneratorOverworld {

    public ChunkGeneratorOverworldDim(World world, long seed) {
        // 第三个参数 mapFeaturesEnabled 控制结构（村庄/要塞/地牢等）是否生成；
        // 第四个参数为空串，使用世界默认地形配置。
        super(world, seed, DimConfig.generateStructures, "");
    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        if (!DimConfig.spawnMobs) {
            return Collections.emptyList();
        }
        return super.getPossibleCreatures(creatureType, pos);
    }
}
