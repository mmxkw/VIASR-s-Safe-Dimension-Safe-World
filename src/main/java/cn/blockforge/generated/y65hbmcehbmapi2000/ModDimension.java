package cn.blockforge.generated.y65hbmcehbmapi2000;

import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;

/**
 * 维度定义与注册。
 *
 * 维度注册码（DIM_ID）按要求使用非负整数 Int：不允许负数、不允许小数。
 * 使用 Forge 1.12.2 标准流程：DimensionType.register(...) 生成类型，
 * 再由 DimensionManager.registerDimension(int, DimensionType) 绑定到世界。
 */
public final class ModDimension {

    /** 维度注册码（Int）。用 66，避开原版 -1/0/1，非负、非小数。 */
    public static final int DIM_ID = 66;

    /** 维度名称（文件夹后缀）。 */
    public static final String DIM_NAME = "low_plains";

    /** 维度边界中心。边长由 {@link DimConfig#dimensionSize} 配置决定。 */
    public static final double BORDER_CENTER_X = 0.0;
    public static final double BORDER_CENTER_Z = 0.0;

    /** 传送落点（维度中心附近），X/Z 定在中心 (0,0)。 */
    public static final int SPAWN_X = 0;
    public static final int SPAWN_Z = 0;

    private static DimensionType type;

    private ModDimension() {
    }

    public static void register() {
        if (type != null || DimensionManager.isDimensionRegistered(DIM_ID)) {
            return;
        }
        type = DimensionType.register(DIM_NAME, "_" + DIM_NAME, DIM_ID, WorldProviderModDim.class, true);
        DimensionManager.registerDimension(DIM_ID, type);
    }

    public static DimensionType getType() {
        return type;
    }

    /**
     * 计算维度中心点的传送落点。
     *
     * <p>生成地形时高度有起伏（可能是山地或水面），不能再用固定的地表高度；改由世界高度图
     * 取中心点正上方第一个空气方块的高度，让玩家落地在地表。若世界尚未加载（world 为空），
     * 则回退到配置的地表固定高度 {@link DimConfig#surfaceY}。
     */
    public static net.minecraft.util.math.BlockPos computeSpawnPos(net.minecraft.world.World world) {
        int x = SPAWN_X;
        int z = SPAWN_Z;
        int y = -1;
        if (world != null) {
            int h = world.getHeight(x, z);
            if (h > 1 && h < 254) {
                y = h;
            }
        }
        if (y < 0) {
            y = DimConfig.surfaceY + 1;
        }
        return new net.minecraft.util.math.BlockPos(x, y, z);
    }

    public static boolean isDim(int id) {
        return id == DIM_ID;
    }
}
