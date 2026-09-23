package cn.blockforge.generated.y65hbmcehbmapi2000;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

/**
 * 资源维度模组（Forge 1.12.2）。
 * 作者：VIASR。
 * 程序：Ftahinza&UNFOX3213
 * 通过 /dim tp 传送到一个草原群系生成、无生物刷新的资源维度（边界 200000x200000），
 * 用 /dim list 查看维度内玩家。
 * 通过 /dim leave 离开。
 */
@Mod(modid = GeneratedMod.MOD_ID, name = GeneratedMod.MOD_NAME, version = GeneratedMod.VERSION)
public final class GeneratedMod {

    /**
     * 模组 ID。必须与 {@code mcmod.info} 中的 {@code modid} 一致，
     * 且只能包含 {@code [a-z0-9_]}（Forge 会拒绝含点号的 ID）。
     * 这里沿用 2.0 的 ID，配置文件与存档数据可以直接沿用。
     */
    public static final String MOD_ID = "y_65_hbm_ce_hbm_api_2000";

    /** 模组显示名。 */
    public static final String MOD_NAME = "VIASR's Resource Dimension";

    /** 模组版本，与 build.gradle 的 {@code version} 对应。 */
    public static final String VERSION = "2.1";

    /** 敌对生物清理器（注册在 Forge 事件总线上）。 */
    private HostileMobRemover hostileMobRemover;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // 先读配置文件（生成 config/safeworld.cfg），再注册维度，
        // 保证边界大小、地表高度、生物/地形/结构开关在维度创建前就已生效。
        DimConfig.load(event.getSuggestedConfigurationFile());

        // 按标准流程注册维度：DimensionType + DimensionManager.registerDimension(int, DimensionType)
        ModDimension.register();

        // 敌对生物清理器：必须注册到 MinecraftForge.EVENT_BUS 才会收到事件。
        // 放在 preInit 注册，避免服务端世界加载早期漏掉实体。
        this.hostileMobRemover = new HostileMobRemover();
        MinecraftForge.EVENT_BUS.register(this.hostileMobRemover);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        // HBM CE 已加载：把模板维度（默认主世界）的矿石生成配置复制给本维度，
        // 使其能产出 HBM 矿石。未安装 HBM CE 时整体静默跳过。
        HbmCompat.applyCompat(ModDimension.DIM_ID);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        // /dim <tp|leave|list> 三个子命令由同一个命令类实现。
        event.registerServerCommand(new CommandDim());
    }
}
