package cn.blockforge.generated.y65hbmcehbmapi2000;

/**
 * 维度配置。修改后需重启游戏（或重新进入维度）生效。
 *
 * <p>所有数值都有安全范围。超出范围时生成器会在内部自动钳制到边界值，
 * 不会抛异常、也不会产生崩坏地形。每个字段注释写明了安全范围与越界后的行为。
 */
public final class DimConfig {

    private DimConfig() {}

    // =========================================================================
    //  基础开关（你原有）
    // =========================================================================

    /** 世界边界边长（格）。由 {@link WorldProviderModDim#createWorldBorder()} 使用。 */
    public static int dimensionSize = 200000;

    /**
     * 是否生成地形。
     * <ul>
     *   <li>false → 固定高度平坦生成器 {@link ChunkGeneratorFlatDim}；</li>
     *   <li>true  → 由 {@link #grasslandMode} 决定是原版多群系还是平坦草原。</li>
     * </ul>
     */
    public static boolean generateTerrain = true;

    /** 是否生成结构（村庄、要塞、废弃矿井等）。 */
    public static boolean generateStructures = false;

    /** 是否允许生物自然刷新。 */
    public static boolean spawnMobs = false;

    /**
     * 平坦模式（{@link #generateTerrain} = false）下的地表高度。
     *
     * <p>最上层在这一高度铺草方块，下面两层泥土，其余到基岩都是石头。
     *
     * <p><b>安全范围</b>：[1, 250]，越界自动钳制。
     */
    public static int surfaceY = 64;

    // =========================================================================
    //  生成模式选择
    // =========================================================================

    /**
     * 草原模式开关。<b>只在 {@link #generateTerrain} 为 true 时生效</b>。
     * <ul>
     *   <li>true  → {@link ChunkGeneratorGrasslandFlat}：
     *       单一平原群系、极平坦草原 + 大型蜿蜒河流，<b>跨存档一致</b>；</li>
     *   <li>false → {@link ChunkGeneratorOverworldDim}：
     *       原版多群系随机地形，依赖世界种子。</li>
     * </ul>
     */
    public static boolean grasslandMode = true;

    // =========================================================================
    //  草原地形参数
    // =========================================================================

    /**
     * 草原起伏总幅度（格）。三个正弦波按比例分摊。
     *
     * <p><b>安全范围</b>：[0.0, 16.0]，越界自动钳制。
     * <ul>
     *   <li>0    → 完全平坦；</li>
     *   <li>6    → 缓坡草原（默认）；</li>
     *   <li>12+  → 明显丘陵草原。</li>
     * </ul>
     */
    public static double terrainAmplitude = 6.0;

    /**
     * 地形基础角频率（1/格）。
     *
     * <p><b>安全范围</b>：[0.0010, 0.0150]，越界自动钳制。
     * 数值越小地形越舒缓，越大越紧凑。
     *
     * <p>与 {@link #terrainAmplitude} 共同决定平均坡度：
     * <pre>  平均坡度 ≈ 2 × 幅度 × 频率 / π</pre>
     * 默认 (2×6×0.0035)/π ≈ 0.0134 → 约每 75 格升降 1 格。
     */
    public static double terrainFrequency = 0.0035;

    // =========================================================================
    //  河流参数
    // =========================================================================

    /**
     * 河流半宽基准值（格）。河面全宽 = 半宽 × 2。
     *
     * <p><b>安全范围</b>：[8.0, 60.0]，越界自动钳制。默认 31 → 全宽约 62 格。
     */
    public static double riverHalfWidthBase = 31.0;

    /**
     * 河流半宽波动幅度（格）。河道随 z 缓慢变宽变窄。
     *
     * <p><b>安全范围</b>：[0.0, riverHalfWidthBase × 0.6]，越界自动钳制。
     * 保证最窄处半宽 ≥ base × 0.4 > 0，河道永不消失。
     * 默认 9 → 半宽 22~40（全宽 44~80）。
     */
    public static double riverHalfWidthAmplitude = 9.0;

    /**
     * 河床底部高度。
     *
     * <p><b>安全范围</b>：[12.0, 58.0]，同时还会被"地形最低高度 - 1"二次约束
     * （避免河床高于周围地面）。越界自动钳制。
     * 默认 58 → 水面 62，水深 4 格。数值越小河越深。
     */
    public static double riverBed = 58.0;

    /**
     * 河岸过渡带宽度（格）。
     *
     * <p><b>安全范围</b>：[4.0, 40.0]，越界自动钳制。
     * 越小河岸越陡，越大越缓。默认 15。
     */
    public static double riverBank = 30;

    // =========================================================================
    //  跨存档一致性
    // =========================================================================

    /**
     * 草原生成器固定种子。<b>只在 {@link #grasslandMode} 为 true 时有意义</b>。
     *
     * <p>只影响<b>装饰</b>（草、花、树的分布），不影响地形高度与河流走向——
     * 那两者是纯函数，与世界种子无关。
     *
     * <p>改动后，所有存档新生成的区块都会使用新布局，且仍然跨存档一致。
     */
    public static long grasslandSeed = 0x5EED_6A55_1A4DL;

    // =========================================================================
    //  安全范围常量
    // =========================================================================
    // =========================================================================
//  HBM CE 联动
// =========================================================================

/**
 * HBM CE 联动总开关。
 *
 * <p>关闭后，下列所有 HBM 相关生成都不会注入，等同于"本维度完全没有 HBM 内容"。
 * 未安装 HBM CE 时此开关无效（反射静默失败）。
 */
public static boolean enableHbmCompat = true;

/**
 * 模板维度：所有 HBM 配置都从该维度复制。
 *
 * <p>默认 0（主世界）。想用其它维度做模板（例如某个自定义世界）改这里即可。
 */
public static int hbmTemplateDim = 0;

// -------------------------------------------------------------------------
//  常规矿物（默认关闭）
// -------------------------------------------------------------------------

/**
 * 是否在本维度生成 HBM CE 的<b>常规矿物</b>（铀矿、钍矿、钛矿、钨矿等）。
 *
 * <p><b>默认关闭</b>：资源维度通常希望通过基岩特性获得资源，
 * 而不是在地表堆满普通矿脉。开启后，本维度会按主世界的密度生成普通矿石。
 *
 * <p>需要 {@link #enableHbmCompat} 为 true 才生效。
 */
public static boolean hbmSpawnNormalOres = false;

/**
 * 常规矿物生成密度倍率（相对模板维度）。
 *
 * <p><b>安全范围</b>：[0.0, 10.0]，越界自动钳制。
 * <ul>
 *   <li>0.0 → 等效于关闭；</li>
 *   <li>1.0 → 与主世界一致（默认）；</li>
 *   <li>2.0 → 主世界的两倍；</li>
 *   <li>0.5 → 主世界的一半。</li>
 * </ul>
 *
 * <p>仅在 {@link #hbmSpawnNormalOres} 为 true 时生效。
 */
public static double hbmNormalOreRate = 1.0;

// -------------------------------------------------------------------------
//  基岩特性（默认开启）
// -------------------------------------------------------------------------

/**
 * 是否在本维度生成 HBM CE 的<b>基岩矿石</b>（含完整丰度数据）。
 *
 * <p>基岩矿石是位于世界底部的特殊矿点，使用基岩钻机开采。
 * 不同矿点按丰度列表产出不同矿物，是与普通矿石完全独立的一套系统。
 *
 * <p><b>默认开启</b>，需要 {@link #enableHbmCompat} 为 true 才生效。
 */
public static boolean hbmSpawnBedrockOres = true;

/**
 * 是否在本维度生成 HBM CE 的<b>基岩油层</b>。
 *
 * <p>深层原油储层，需用水力压裂塔（Fracking Tower）开采。
 *
 * <p><b>默认开启</b>，需要 {@link #enableHbmCompat} 为 true 才生效。
 */
public static boolean hbmSpawnBedrockOil = true;

/**
 * 是否在本维度生成 HBM CE 的<b>油岩</b>（油田 / 油岩矿脉）。
 *
 * <p>地下连片生成的油砂矿脉，开采后可得原油。
 *
 * <p><b>默认开启</b>，需要 {@link #enableHbmCompat} 为 true 才生效。
 */
public static boolean hbmSpawnOilDeposits = true;

// -------------------------------------------------------------------------
//  敌对生物清理
// -------------------------------------------------------------------------

/**
 * 是否在资源维度中自动移除敌对生物（{@link HostileMobRemover}）。
 *
 * <p>开启后：实体进入本维度的瞬间就会被拦截，每 5 秒还会扫描一次已加载实体。
 * 玩家、动物、村民、铁傀儡、宠物不受影响。
 */
public static boolean removeHostileMobs = true;

/**
 * 敌对生物白名单（{@code modid:entity_name} 列表）。
 *
 * <p>列表内实体即使实现 {@code IMob} 也不会被移除，例如
 * {@code "minecraft:zombie"}。留空表示移除所有敌对生物。
 */
public static String[] removeHostileMobsWhitelist = new String[0];

/** 是否把每次移除的实体打到日志里（调试用）。 */
public static boolean debugHostileMobRemover = false;

// -------------------------------------------------------------------------
//  安全范围常量
// -------------------------------------------------------------------------

    public static final double HBM_ORE_RATE_MIN = 0.0;
    public static final double HBM_ORE_RATE_MAX = 10.0;
    public static final double TERRAIN_AMP_MIN  = 0.0;
    public static final double TERRAIN_AMP_MAX  = 16.0;
    public static final double TERRAIN_FREQ_MIN = 0.0010;
    public static final double TERRAIN_FREQ_MAX = 0.0150;

    public static final double RIVER_HW_BASE_MIN = 8.0;
    public static final double RIVER_HW_BASE_MAX = 60.0;
    public static final double RIVER_HW_AMP_RATIO_MAX = 0.6;

    public static final double RIVER_BED_MIN  = 12.0;
    public static final double RIVER_BED_MAX  = 58.0;
    public static final double RIVER_BANK_MIN = 4.0;
    public static final double RIVER_BANK_MAX = 40.0;

    // =========================================================================
    //  配置文件（JSON，键名 = 下面的字段名）
    // =========================================================================

    /** 写盘用（缩进友好、不转义中文，方便直接手改）。 */
    private static final com.google.gson.Gson GSON =
            new com.google.gson.GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * 读取配置文件。文件格式是 JSON，键名就是本类的字段名，
     * 例如 {@code {"dimensionSize": 200000, "grasslandMode": true}}。
     *
     * <p>由 {@code GeneratedMod.preInit} 在注册维度<b>之前</b>调用，
     * 因此边界大小、地表高度、生成模式、生物开关等在维度创建时就已生效。
     *
     * <p>文件不存在时会按当前字段值生成一份默认配置。
     * 读到的数值会再做一次安全范围钳制，手改配置写出崩坏地形的可能性被排除。
     * 任何读写失败都只记录日志，不会阻止模组加载。
     *
     * @param file Forge 传入的建议配置文件路径
     */
    public static void load(java.io.File file) {
        if (file == null) {
            return;
        }
        try {
            if (!file.exists()) {
                save(file);
                net.minecraftforge.fml.common.FMLLog.log.info(
                        "[SafeWorld] 未找到配置，已生成默认配置：{}", file.getAbsolutePath());
                return;
            }
            try (java.io.Reader reader = new java.io.InputStreamReader(
                    new java.io.FileInputStream(file), java.nio.charset.StandardCharsets.UTF_8)) {
                com.google.gson.JsonElement root =
                        new com.google.gson.JsonParser().parse(reader);
                if (root != null && root.isJsonObject()) {
                    readFrom(root.getAsJsonObject());
                }
            }
            net.minecraftforge.fml.common.FMLLog.log.info(
                    "[SafeWorld] 配置已加载：尺寸 {}，地形 {}，草原模式 {}，生物刷新 {}。",
                    dimensionSize, generateTerrain, grasslandMode, spawnMobs);
        } catch (Throwable t) {
            net.minecraftforge.fml.common.FMLLog.log.warn(
                    "[SafeWorld] 配置读取失败，使用内置默认值：{}", t.toString());
        }
    }

    /** 把当前字段值写成 JSON 配置文件。 */
    public static void save(java.io.File file) {
        if (file == null) {
            return;
        }
        try {
            java.io.File dir = file.getParentFile();
            if (dir != null && !dir.exists()) {
                dir.mkdirs();
            }
            try (java.io.Writer writer = new java.io.OutputStreamWriter(
                    new java.io.FileOutputStream(file), java.nio.charset.StandardCharsets.UTF_8)) {
                GSON.toJson(writeTo(), writer);
            }
        } catch (Throwable t) {
            net.minecraftforge.fml.common.FMLLog.log.warn(
                    "[SafeWorld] 配置保存失败：{}", t.toString());
        }
    }

    /** 按字段名把 JSON 里的值写回字段；缺失或非法的项保留默认值。 */
    private static void readFrom(com.google.gson.JsonObject obj) {
        for (java.lang.reflect.Field field : DimConfig.class.getFields()) {
            int mods = field.getModifiers();
            if (!java.lang.reflect.Modifier.isStatic(mods)
                    || java.lang.reflect.Modifier.isFinal(mods)) {
                continue;
            }
            com.google.gson.JsonElement el = obj.get(field.getName());
            if (el == null || el.isJsonNull()) {
                continue;
            }
            Class<?> type = field.getType();
            try {
                if (type == int.class) {
                    field.setInt(null, el.getAsInt());
                } else if (type == boolean.class) {
                    field.setBoolean(null, el.getAsBoolean());
                } else if (type == double.class) {
                    field.setDouble(null, el.getAsDouble());
                } else if (type == long.class) {
                    field.setLong(null, el.getAsLong());
                } else if (type == float.class) {
                    field.setFloat(null, el.getAsFloat());
                } else if (type == String.class) {
                    field.set(null, el.getAsString());
                } else if (type == String[].class) {
                    field.set(null, GSON.fromJson(el, String[].class));
                }
            } catch (Throwable ignored) {
                // 单项非法 → 保留默认值，不影响其它配置
            }
        }
        sanitize();
    }

    /** 把全部非 final 的静态字段导出成 JSON。 */
    private static com.google.gson.JsonObject writeTo() throws IllegalAccessException {
        com.google.gson.JsonObject obj = new com.google.gson.JsonObject();
        for (java.lang.reflect.Field field : DimConfig.class.getFields()) {
            int mods = field.getModifiers();
            if (!java.lang.reflect.Modifier.isStatic(mods)
                    || java.lang.reflect.Modifier.isFinal(mods)) {
                continue;
            }
            Object v = field.get(null);
            if (v instanceof Integer) {
                obj.addProperty(field.getName(), (Integer) v);
            } else if (v instanceof Boolean) {
                obj.addProperty(field.getName(), (Boolean) v);
            } else if (v instanceof Double) {
                obj.addProperty(field.getName(), (Double) v);
            } else if (v instanceof Long) {
                obj.addProperty(field.getName(), (Long) v);
            } else if (v instanceof Float) {
                obj.addProperty(field.getName(), (Float) v);
            } else if (v instanceof String) {
                obj.addProperty(field.getName(), (String) v);
            } else if (v instanceof String[]) {
                obj.add(field.getName(), GSON.toJsonTree(v));
            }
        }
        return obj;
    }

    /** 把读入的数值钳制到安全范围。 */
    private static void sanitize() {
        dimensionSize = clampInt(dimensionSize, 16, 30000000);
        surfaceY = clampInt(surfaceY, 1, 250);
        terrainAmplitude = clamp(terrainAmplitude, TERRAIN_AMP_MIN, TERRAIN_AMP_MAX);
        terrainFrequency = clamp(terrainFrequency, TERRAIN_FREQ_MIN, TERRAIN_FREQ_MAX);
        riverHalfWidthBase = clamp(riverHalfWidthBase, RIVER_HW_BASE_MIN, RIVER_HW_BASE_MAX);
        riverHalfWidthAmplitude = clamp(riverHalfWidthAmplitude, 0.0,
                riverHalfWidthBase * RIVER_HW_AMP_RATIO_MAX);
        riverBed = clamp(riverBed, RIVER_BED_MIN, RIVER_BED_MAX);
        riverBank = clamp(riverBank, RIVER_BANK_MIN, RIVER_BANK_MAX);
        hbmNormalOreRate = clamp(hbmNormalOreRate, HBM_ORE_RATE_MIN, HBM_ORE_RATE_MAX);
        if (removeHostileMobsWhitelist == null) {
            removeHostileMobsWhitelist = new String[0];
        }
    }

    private static double clamp(double v, double min, double max) {
        return v < min ? min : (v > max ? max : v);
    }

    private static int clampInt(int v, int min, int max) {
        return v < min ? min : (v > max ? max : v);
    }
}