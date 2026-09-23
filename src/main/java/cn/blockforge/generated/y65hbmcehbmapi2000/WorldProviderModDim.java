package cn.blockforge.generated.y65hbmcehbmapi2000;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 资源维度的世界提供者。
 *
 * <p><b>生成模式</b>由 {@link DimConfig} 决定：
 * <ol>
 *   <li>{@code generateTerrain = false} → 固定高度平坦生成器
 *       {@link ChunkGeneratorFlatDim}；</li>
 *   <li>{@code generateTerrain = true} 且 {@code grasslandMode = true}
 *       → 平坦草原 + 大河生成器 {@link ChunkGeneratorGrasslandFlat}，
 *       单一平原群系，<b>跨存档一致</b>；</li>
 *   <li>{@code generateTerrain = true} 且 {@code grasslandMode = false}
 *       → 原版主世界生成器 {@link ChunkGeneratorOverworldDim}，
 *       多群系随机地形，依赖世界种子。</li>
 * </ol>
 *
 * <p><b>光影支持</b>：本类只重写必须的维度属性，让本维度在 OptiFine / Iris
 * 等光影模组眼中"看起来"与主世界一致：
 * <ul>
 *   <li>{@link #isSurfaceWorld()} = true —— 让光影接管云、天气、日照；</li>
 *   <li>{@link #isSkyColored()} = true —— 天空随时间/群系着色；</li>
 *   <li>{@link #getSunBrightness} / {@link #getStarBrightness} —— 委托 world 的原生计算；</li>
 *   <li>{@link #getCloudHeight()} = 128 —— 与主世界一致。</li>
 * </ul>
 *
 * <p><b>天空色与雾色刻意不重写</b>：曾经写过 {@code getSkyColor()} 返回固定值
 * (0.5, 0.7, 1.0)，结果把原版"随时间与生物群系变化"的天色/雾色彻底覆盖掉——
 * 天空永远是同一种颜色，日出日落不变，视觉上"光线很奇怪"。
 * 现在交回 Forge 默认实现（{@code world.getSkyColorBody(...)} 与原版雾色计算），
 * 与主世界表现一致。
 *
 * <p><b>不要重写 {@code generateLightBrightnessTable()}</b>：这是社区中常见的
 * 错误做法，会导致光照完全失效（包括光影也无法救回）。
 *
 * <p>保留 200000×200000 的世界边界与中心传送点。
 */
public class WorldProviderModDim extends WorldProvider {

    // =========================================================================
    //  初始化
    // =========================================================================

    @Override
    protected void init() {
        super.init();

        // 天光：光照计算需要，光影也依赖此标志
        this.hasSkyLight = true;

        // 根据模式选择生物群系提供者。
        // 关键：必须与 createChunkGenerator() 返回的生成器配对，
        // 否则会出现"生成器认为是平原、装饰器认为是森林"的矛盾。
        if (DimConfig.generateTerrain && DimConfig.grasslandMode) {
            // 草原模式：整个维度只有平原群系
            this.biomeProvider = new GrasslandBiomeProvider(this.world);
        } else {
            // 其它模式：按世界种子正常生成多种群系
            this.biomeProvider = new BiomeProvider(this.world.getWorldInfo());
        }
    }

    // =========================================================================
    //  维度类型
    // =========================================================================

    @Override
    public DimensionType getDimensionType() {
        return ModDimension.getType();
    }

    // =========================================================================
    //  区块生成器
    // =========================================================================

    @Override
    public IChunkGenerator createChunkGenerator() {
        // 1. 关闭地形 → 固定高度平坦
        if (!DimConfig.generateTerrain) {
            return new ChunkGeneratorFlatDim(this.world, this.world.getSeed());
        }
        // 2. 开启地形 + 草原模式 → 平坦草原 + 大河（跨存档一致，不传世界种子）
        if (DimConfig.grasslandMode) {
            return new ChunkGeneratorGrasslandFlat(this.world);
        }
        // 3. 开启地形 + 非草原模式 → 原版主世界生成器
        return new ChunkGeneratorOverworldDim(this.world, this.world.getSeed());
    }

    // =========================================================================
    //  维度基础属性
    // =========================================================================

    /**
     * 是否为"地表世界"。
     *
     * <p>返回 true 让本维度拥有：
     * <ul>
     *   <li>云层渲染；</li>
     *   <li>天气系统（雨、雪、雷暴）；</li>
     *   <li>日照计算（太阳位置影响光照强度）；</li>
     *   <li>光影模组将其视为主世界类环境接管渲染。</li>
     * </ul>
     */
    @Override
    public boolean isSurfaceWorld() {
        return true;
    }

    /**
     * 是否允许在该维度重生。
     */
    @Override
    public boolean canRespawnHere() {
        return true;
    }

    /**
     * 移动速度倍率。1.0 表示与主世界一致。
     */
    @Override
    public double getMovementFactor() {
        return 1.0;
    }

    /**
     * 虚空雾的 Y 系数。1.0 让虚空雾在主世界高度开始出现。
     */
    @Override
    public double getVoidFogYFactor() {
        return 1.0;
    }

    /**
     * 是否允许指定坐标作为出生点。
     */
    @Override
    public boolean canCoordinateBeSpawn(int x, int z) {
        // 允许在中心附近生成，保证玩家落地在地表上方
        return true;
    }

    /**
     * 玩家首次进入维度的落点（传送门/传送指令使用）。
     */
    @Override
    public BlockPos getSpawnCoordinate() {
        // 按实际地表高度取中心点，避免复活/落地时埋进山体
        return ModDimension.computeSpawnPos(this.world);
    }

    // =========================================================================
    //  光影支持：天空与雾
    // =========================================================================

    /**
     * 天空是否为彩色。
     *
     * <p>返回 true 时，MC 会根据时间（日出/日落）与群系颜色对天空着色，
     * 而不是使用固定的纯色。光影模组也依赖此标志判断是否启用动态天空。
     */
    @Override
    @SideOnly(Side.CLIENT)
    public boolean isSkyColored() {
        return true;
    }

    /**
     * 云层高度（格）。
     *
     * <p>使用主世界默认值 128.0F。光影模组会读取此值来确定云层位置与
     * 大气散射的参考高度。
     */
    @Override
    @SideOnly(Side.CLIENT)
    public float getCloudHeight() {
        return 128.0F;
    }

    /**
     * 天空颜色。
     *
     * <p><b>刻意不重写。</b>曾经在这里返回固定值 {@code (0.5, 0.7, 1.0)}，
     * 代价是原版按时间（日出/日落）与生物群系计算的天色被完全覆盖，
     * 天空永远是一种颜色，看上去"光线很奇怪"。
     * Forge 的默认实现是 {@code world.getSkyColorBody(cameraEntity, partialTicks)}，
     * 也就是主世界用的那套计算，正是我们想要的"与主世界一致"。
     */

    /**
     * 雾的颜色。
     *
     * <p><b>刻意不重写。</b>曾经在这里返回固定值 {@code (0.7, 0.8, 1.0)}，
     * 会让雾色不随生物群系、昼夜、水下环境变化，同样是"光线很奇怪"的来源。
     * 默认实现是原版的雾色计算，与主世界一致。
     */

    // =========================================================================
    //  光影支持：光照与星光
    // =========================================================================

    /**
     * 太阳亮度。
     *
     * <p>委托给 {@code world} 的原生计算，使其行为与主世界完全一致——
     * 白天最亮、夜晚最低、雨天减弱。光影模组会读取此值计算全局光照。
     *
     * <p><b>⚠ 必须调用 {@code world.getSunBrightnessBody()}，绝对不要调用
     * {@code world.getSunBrightness()}！</b>
     *
     * <p>1.12.2 里 {@code World.getSunBrightness(float)} 的实现本身就是
     * <pre>return this.provider.getSunBrightness(partialTicks);</pre>
     * 即它会回调到<b>本方法</b>。若在这里再调回 world 的同名方法，就会形成
     * {@code World → WorldProvider(本类) → World → …} 的无限递归，
     * 直接抛 {@code StackOverflowError} 把客户端打崩
     * （见 crash-2026-09-20_21.55.05-client.txt）。
     * {@code *Body} 系列是不经过 provider 的原生计算，也正是 Forge 给
     * {@code WorldProvider.getSunBrightness} 写的默认实现所使用的目标。
     *
     * @param partialTicks 渲染插值系数
     */
    @Override
    @SideOnly(Side.CLIENT)
    public float getSunBrightness(float partialTicks) {
        return this.world.getSunBrightnessBody(partialTicks);
    }

    /**
     * 星光亮度。
     *
     * <p>委托给 {@code world} 的原生计算，使夜晚星光强度与主世界一致。
     * 光影模组用它来增强夜间的天空与地面微弱照明。
     *
     * <p><b>⚠ 必须调用 {@code world.getStarBrightnessBody()}，绝对不要调用
     * {@code world.getStarBrightness()}！</b>原因同
     * {@link #getSunBrightness(float)}：{@code World.getStarBrightness(float)}
     * 会回调 provider，互相委托会造成无限递归栈溢出。
     *
     * @param partialTicks 渲染插值系数
     */
    @Override
    @SideOnly(Side.CLIENT)
    public float getStarBrightness(float partialTicks) {
        return this.world.getStarBrightnessBody(partialTicks);
    }

    // =========================================================================
    //  光影支持：天气与时间
    // =========================================================================

    /**
     * 维度内是否会下雨。
     *
     * <p>返回 true 允许降雨；光影模组检测到降雨后会启用湿润的大气散射。
     * 若你不希望该维度下雨，改为返回 false。
     */
    @Override
    public boolean canDoRainSnowIce(net.minecraft.world.chunk.Chunk chunk) {
        return true;
    }

    /**
     * 是否为白昼静态维度（如末地）。
     *
     * <p>返回 false，让维度拥有正常昼夜循环——这是光影动态天空的基础。
     */
    @Override
    public boolean isDaytime() {
        return super.isDaytime();
    }

    // =========================================================================
    //  世界边界
    // =========================================================================

    @Override
    public WorldBorder createWorldBorder() {
        WorldBorder border = super.createWorldBorder();
        // 边界中心 (0,0)，边长由配置文件 dimensionSize 决定，超出即边界。
        border.setCenter(ModDimension.BORDER_CENTER_X, ModDimension.BORDER_CENTER_Z);
        border.setSize(DimConfig.dimensionSize);
        return border;
    }
}