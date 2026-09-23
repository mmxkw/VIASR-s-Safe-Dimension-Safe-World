package cn.blockforge.generated.y65hbmcehbmapi2000;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import net.minecraftforge.fml.common.FMLLog;

/**
 * 与 HBM CE 的运行时兼容层。
 *
 * <p><b>三类生成内容，由 {@link DimConfig} 独立控制</b>：
 * <table border="1">
 *   <tr><th>类别</th><th>配置项</th><th>默认</th><th>说明</th></tr>
 *   <tr><td>常规矿物</td><td>{@link DimConfig#hbmSpawnNormalOres}</td>
 *       <td>关</td><td>铀、钍、钛等普通地表矿物</td></tr>
 *   <tr><td>基岩矿石</td><td>{@link DimConfig#hbmSpawnBedrockOres}</td>
 *       <td>开</td><td>世界底部的特殊矿点，含丰度数据</td></tr>
 *   <tr><td>基岩油层</td><td>{@link DimConfig#hbmSpawnBedrockOil}</td>
 *       <td>开</td><td>深层原油储层</td></tr>
 *   <tr><td>油岩</td><td>{@link DimConfig#hbmSpawnOilDeposits}</td>
 *       <td>开</td><td>地下油砂矿脉</td></tr>
 * </table>
 *
 * <p><b>为什么常规矿物默认关闭</b>：资源维度通常通过基岩特性获得资源，
 * 而不希望地表堆满普通矿脉。开启后可用 {@link DimConfig#hbmNormalOreRate}
 * 调节密度。
 *
 * <p>全部通过反射访问，本模组<b>编译期不依赖</b> HBM CE；
 * 若 HBM 未安装、版本不支持、或类名/字段名变化，则整体静默跳过。
 */
public final class HbmCompat {

    // =========================================================================
    //  HBM CE 配置类名
    // =========================================================================

    /** HBM 兼容配置类（普通矿石维度表在此）。 */
    private static final String COMPAT_CONFIG_CLASS = "com.hbm.config.CompatibilityConfig";

    /** HBM 基岩矿石配置类。 */
    private static final String BEDROCK_ORE_CONFIG_CLASS = "com.hbm.config.BedrockOreConfig";

    /** HBM 基岩油层配置类。 */
    private static final String BEDROCK_OIL_CONFIG_CLASS = "com.hbm.config.BedrockOilConfig";

    /** HBM 油岩 / 油田配置类。 */
    private static final String OIL_DEPOSIT_CONFIG_CLASS = "com.hbm.config.OilDepositConfig";

    /** HBM 通用世界生成配置类（备用）。 */
    private static final String WORLD_GEN_CONFIG_CLASS = "com.hbm.config.WorldGenConfig";

    private HbmCompat() {
    }

    // =========================================================================
    //  主入口
    // =========================================================================

    /**
     * 应用所有启用的 HBM CE 兼容注入。
     *
     * <p>在 {@code FMLPostInitializationEvent} 阶段调用，确保 HBM 已完成配置加载。
     *
     * @param dimId 目标维度号
     * @return 成功注入的类别数量（0~4）
     */
    public static int applyCompat(int dimId) {
        if (!DimConfig.enableHbmCompat) {
            FMLLog.log.info("[HbmCompat] HBM 联动总开关关闭，跳过。");
            return 0;
        }

        int injected = 0;

        // 1. 常规矿物（默认关）
        if (DimConfig.hbmSpawnNormalOres && DimConfig.hbmNormalOreRate > 0.0) {
            if (injectNormalOres(dimId)) {
                injected++;
            }
        }

        // 2. 基岩矿石（默认开）
        if (DimConfig.hbmSpawnBedrockOres) {
            if (injectBedrockOres(dimId)) {
                injected++;
            }
        }

        // 3. 基岩油层（默认开）
        if (DimConfig.hbmSpawnBedrockOil) {
            if (injectBedrockOil(dimId)) {
                injected++;
            }
        }

        // 4. 油岩（默认开）
        if (DimConfig.hbmSpawnOilDeposits) {
            if (injectOilDeposits(dimId)) {
                injected++;
            }
        }

        FMLLog.log.info("[HbmCompat] 维度 {} 共注入 {} 类 HBM 内容（模板维度 {}）。",
                dimId, injected, DimConfig.hbmTemplateDim);
        return injected;
    }

    // =========================================================================
    //  1. 常规矿物
    // =========================================================================

    /**
     * 注入常规矿物：把模板维度的"每区块矿脉数"复制到目标维度，
     * 并按 {@link DimConfig#hbmNormalOreRate} 调整数量。
     *
     * <p>HBM 的常规矿物配置是一组形如 {@code Map<Integer, Integer>} 的静态字段，
     * 键为维度号，值为每区块期望矿脉数。本方法把模板维度的值乘以倍率后写入。
     */
    private static boolean injectNormalOres(int dimId) {
        try {
            Class<?> clazz = Class.forName(COMPAT_CONFIG_CLASS);
            int templateDim = DimConfig.hbmTemplateDim;
            double rate = clamp(DimConfig.hbmNormalOreRate,
                    DimConfig.HBM_ORE_RATE_MIN, DimConfig.HBM_ORE_RATE_MAX);
            if (rate <= 0.0) return false;

            int copied = 0;
            for (Field field : clazz.getFields()) {
                if (!Map.class.isAssignableFrom(field.getType())) continue;

                Object raw = field.get(null);
                if (!(raw instanceof Map)) continue;

                Map<?, ?> map = (Map<?, ?>) raw;
                Object templateVal = map.get(Integer.valueOf(templateDim));

                // 只处理 Integer 值（排除 Float 的辐射强度等）
                if (!(templateVal instanceof Integer)) continue;

                int base = (Integer) templateVal;
                int scaled = (int) Math.round(base * rate);

                if (putIfAbsent(map, dimId, Integer.valueOf(scaled))) {
                    copied++;
                    FMLLog.log.debug("[HbmCompat] 常规矿物 {}：{} × {} → {}",
                            field.getName(), base, rate, scaled);
                }
            }

            if (copied > 0) {
                FMLLog.log.info("[HbmCompat] 已注入 {} 种常规矿物的维度 {} 生成数量（倍率 {}）。",
                        copied, dimId, rate);
                return true;
            }
            return false;
        } catch (ClassNotFoundException e) {
            FMLLog.log.warn("[HbmCompat] 未找到 {}，跳过常规矿物注入。",
                    COMPAT_CONFIG_CLASS);
            return false;
        } catch (Throwable t) {
            FMLLog.log.warn("[HbmCompat] 常规矿物注入失败：{}", t.toString());
            return false;
        }
    }

    // =========================================================================
    //  2. 基岩矿石
    // =========================================================================

    /**
     * 注入基岩矿石配置：从模板维度复制基岩矿石的维度→配置映射。
     *
     * <p>尝试多个候选类，因为不同 HBM 版本把配置放在不同位置。
     * 如果 HBM 使用 JSON 文件（X5012+），所有候选类都不存在，方法返回 false，
     * 并在日志中提示用户手动修改 JSON。
     */
    private static boolean injectBedrockOres(int dimId) {
        String[] candidates = {
                BEDROCK_ORE_CONFIG_CLASS,
                WORLD_GEN_CONFIG_CLASS,
                COMPAT_CONFIG_CLASS
        };
        FieldFilter filter = new OreFilter();

        for (String className : candidates) {
            try {
                Class<?> clazz = Class.forName(className);
                if (copyDimensionMap(clazz, dimId, "基岩矿石", filter)) {
                    return true;
                }
                if (copyDimensionSet(clazz, dimId, "基岩矿石", filter)) {
                    return true;
                }
            } catch (ClassNotFoundException ignored) {
                // 继续尝试下一个
            } catch (Throwable t) {
                FMLLog.log.warn("[HbmCompat] 基岩矿石注入（{}）失败：{}", className, t.toString());
            }
        }

        // 所有候选都失败：很可能是 JSON 模式
        FMLLog.log.info("[HbmCompat] 基岩矿石未通过反射注入——"
                + "若 HBM CE 使用 JSON 配置（X5012+），请手动编辑 "
                + "config/hbm/hbm_bedrock_ores.json 并添加 dimID: {}。", dimId);
        return false;
    }

    // =========================================================================
    //  3. 基岩油层
    // =========================================================================

    private static boolean injectBedrockOil(int dimId) {
        String[] candidates = {
                BEDROCK_OIL_CONFIG_CLASS,
                WORLD_GEN_CONFIG_CLASS,
                COMPAT_CONFIG_CLASS
        };
        FieldFilter filter = new OilFilter();

        for (String className : candidates) {
            try {
                Class<?> clazz = Class.forName(className);
                if (copyDimensionMap(clazz, dimId, "基岩油层", filter)) {
                    return true;
                }
                if (copyDimensionSet(clazz, dimId, "基岩油层", filter)) {
                    return true;
                }
            } catch (ClassNotFoundException ignored) {
                // 继续尝试
            } catch (Throwable t) {
                FMLLog.log.warn("[HbmCompat] 基岩油层注入（{}）失败：{}", className, t.toString());
            }
        }
        return false;
    }

    // =========================================================================
    //  4. 油岩
    // =========================================================================

    private static boolean injectOilDeposits(int dimId) {
        String[] candidates = {
                OIL_DEPOSIT_CONFIG_CLASS,
                WORLD_GEN_CONFIG_CLASS,
                COMPAT_CONFIG_CLASS
        };
        FieldFilter filter = new OilFilter();

        for (String className : candidates) {
            try {
                Class<?> clazz = Class.forName(className);
                if (copyDimensionMap(clazz, dimId, "油岩", filter)) {
                    return true;
                }
                if (copyDimensionSet(clazz, dimId, "油岩", filter)) {
                    return true;
                }
            } catch (ClassNotFoundException ignored) {
                // 继续尝试
            } catch (Throwable t) {
                FMLLog.log.warn("[HbmCompat] 油岩注入（{}）失败：{}", className, t.toString());
            }
        }
        return false;
    }

    // =========================================================================
    //  通用反射工具
    // =========================================================================

    /** 字段过滤器：判断静态字段名是否属于目标特性。 */
    private interface FieldFilter {
        boolean matchesField(String fieldName);
    }

    /** 基岩矿石：字段名需含 "bedrock" 且含 "ore" 或 "deposit"。 */
    private static final class OreFilter implements FieldFilter {
        @Override
        public boolean matchesField(String name) {
            String n = name.toLowerCase();
            return n.contains("bedrock") && (n.contains("ore") || n.contains("deposit"));
        }
    }

    /** 油类：含 "bedrock"+"oil"，或含 "oil"+("deposit"|"sand")。 */
    private static final class OilFilter implements FieldFilter {
        @Override
        public boolean matchesField(String name) {
            String n = name.toLowerCase();
            return (n.contains("bedrock") && n.contains("oil"))
                || (n.contains("oil") && (n.contains("deposit") || n.contains("sand")));
        }
    }

    /**
     * 遍历类中所有匹配过滤器的静态 Map 字段，
     * 把模板维度的条目复制到目标维度。
     *
     * @return 是否至少成功复制了一个字段
     */
    private static boolean copyDimensionMap(Class<?> clazz, int dimId,
                                            String label, FieldFilter filter) {
        int templateDim = DimConfig.hbmTemplateDim;
        boolean anyCopied = false;

        for (Field field : clazz.getFields()) {
            if (!Map.class.isAssignableFrom(field.getType())) continue;
            if (!filter.matchesField(field.getName())) continue;

            try {
                Object raw = field.get(null);
                if (!(raw instanceof Map)) continue;

                Map<?, ?> map = (Map<?, ?>) raw;
                Object templateEntry = map.get(Integer.valueOf(templateDim));

                // 模板维度没有配置 → 跳过（不凭空捏造）
                if (templateEntry == null) {
                    FMLLog.log.debug("[HbmCompat] {}.{} 中模板维度 {} 无配置，跳过。",
                            clazz.getSimpleName(), field.getName(), templateDim);
                    continue;
                }

                // 排除 Float/Double（辐射强度等无关值）
                if (templateEntry instanceof Float || templateEntry instanceof Double) {
                    continue;
                }

                if (putIfAbsent(map, dimId, templateEntry)) {
                    anyCopied = true;
                    FMLLog.log.info("[HbmCompat] 已复制 {}：{}.{} [{} → {}]",
                            label, clazz.getSimpleName(), field.getName(),
                            templateDim, dimId);
                }
            } catch (Throwable t) {
                FMLLog.log.warn("[HbmCompat] 处理字段 {}.{} 时出错：{}",
                        clazz.getSimpleName(), field.getName(), t.toString());
            }
        }
        return anyCopied;
    }

    /**
     * 遍历类中所有匹配过滤器的静态 Set 字段，
     * 若模板维度在集合中，则把目标维度也加入。
     *
     * @return 是否至少成功复制了一个字段
     */
    private static boolean copyDimensionSet(Class<?> clazz, int dimId,
                                            String label, FieldFilter filter) {
        int templateDim = DimConfig.hbmTemplateDim;
        boolean anyCopied = false;

        for (Field field : clazz.getFields()) {
            if (!Set.class.isAssignableFrom(field.getType())) continue;
            if (!filter.matchesField(field.getName())) continue;

            try {
                Object raw = field.get(null);
                if (!(raw instanceof Set)) continue;

                @SuppressWarnings("unchecked")
                Set<Integer> set = (Set<Integer>) raw;

                if (!set.contains(Integer.valueOf(templateDim))) continue;

                if (set.add(Integer.valueOf(dimId))) {
                    anyCopied = true;
                    FMLLog.log.info("[HbmCompat] 已加入 {}：{}.{} [{}]",
                            label, clazz.getSimpleName(), field.getName(), dimId);
                }
            } catch (Throwable t) {
                FMLLog.log.warn("[HbmCompat] 处理 Set 字段 {}.{} 时出错：{}",
                        clazz.getSimpleName(), field.getName(), t.toString());
            }
        }
        return anyCopied;
    }

    /** 向 Map 中写入条目，若目标维度已存在则不覆盖。 */
    @SuppressWarnings("unchecked")
    private static boolean putIfAbsent(Map<?, ?> map, int dimId, Object value) {
        Map<Integer, Object> intMap = (Map<Integer, Object>) map;
        Integer key = Integer.valueOf(dimId);
        if (intMap.containsKey(key)) return false;
        intMap.put(key, value);
        return true;
    }

    /** 通用钳制。 */
    private static double clamp(double v, double min, double max) {
        return v < min ? min : (v > max ? max : v);
    }
}