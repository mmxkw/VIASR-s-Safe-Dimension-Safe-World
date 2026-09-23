package cn.blockforge.generated.y65hbmcehbmapi2000;

import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProviderSingle;

/**
 * 草原维度的生物群系提供者：整个维度只有<b>平原（plains）</b>一种群系。
 *
 * <p>由 {@link WorldProviderModDim#init()} 在 {@code grasslandMode = true} 时安装，
 * 必须与 {@link ChunkGeneratorGrasslandFlat} 配对使用——
 * 否则会出现"生成器认为是平原、装饰器认为是森林"的矛盾，
 * 表现为草皮颜色错乱、树/花按错误群系生成。
 *
 * <p>直接复用原版 {@link BiomeProviderSingle}：它把所有群系查询都返回同一个群系，
 * 不参与任何噪声计算，因此：
 * <ul>
 *   <li>跨存档、跨种子完全一致；</li>
 *   <li>草皮颜色、降雨、温度、刷怪表全部按平原处理；</li>
 *   <li>地形高度与河流走向由生成器的纯函数决定，与群系无关。</li>
 * </ul>
 */
public class GrasslandBiomeProvider extends BiomeProviderSingle {

    /** 草原群系（原版平原，id = 1）。 */
    public static final Biome GRASSLAND_BIOME = Biome.getBiome(1);

    public GrasslandBiomeProvider(World world) {
        super(GRASSLAND_BIOME);
    }
}
