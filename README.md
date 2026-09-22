# VIASR's Safe Dimension/Safe World · 安全维度

> **给 HBM 核工业整合包用的安全的建家维度**：输入 `/dim tp` 即可进入一个没有怪物、地形可控、可选接入 HBM CE 矿物生成的世界，安心采集资源、建造基地。
>
> **A safe resource dimension for HBM nuclear-tech modpacks.** Type `/dim tp` to enter a mob-free, fully configurable world — with optional HBM CE ore generation — so you can mine and build in peace.

<p>
  <img alt="Minecraft" src="https://img.shields.io/badge/Minecraft-1.12.2-informational">
  <img alt="Forge" src="https://img.shields.io/badge/Forge-14.23.5.2847-informational">
  <img alt="Version" src="https://img.shields.io/badge/version-2.1.0-success">
  <img alt="License" src="https://img.shields.io/badge/license-All%20Rights%20Reserved-lightgrey">
</p>

---

## 中文文档

### 简介

本模组为 HBM CE（NTM 社区版）等重型整合包提供一个**独立的建家维度**，**可单独使用**。

原版 HBM 世界的辐射、怪物与地形往往不适合长期定居，而单纯降低难度又破坏玩法。本模组换一条路：**再开一个维度**——那里没有敌对生物、地形参数全部可配置、按需接入 HBM 的矿物生成，主世界依然保持原本的硬核玩法。

- **模组 ID**：`com.example.safeworld`（见 `GeneratedMod.MOD_ID`）
- **维度注册码**：`66`（非负整数，避开原版 `-1 / 0 / 1`）
- **维度内部名**：`low_plains`
- **作者**：VIASR&Ftahinza
- **程序**: Ftahinza&UNFOX3213
- **平台**：Minecraft 1.12.2 · Forge 14.23.5.2847
- **当前版本**：2.1.0

### 特性

#### 1. 一个「无怪 + 可控地形」的维度
- 世界边界 **200000 × 200000**（可配置）
- 默认**不生成任何原版结构**（村庄 / 要塞 / 废弃矿井等均可单独开关）
- 默认**关闭生物自然刷新**；并由独立的敌对生物清理器兜底（见下）
- 维度 ID 使用非负整数 `66`，不与原版及其它模组冲突

#### 2. 传送记忆（双向，重启不丢）
| 行为 | 效果 |
|---|---|
| `/dim tp` | 记住你在**主世界**的 X/Z → 传送到资源维度 |
| `/dim leave` | 记住你在**资源维度**的 X/Z → 回到你上次进入前的主世界 X/Z |

- 位置记忆保存在**玩家持久化 NBT** 中（`viasr_last_dim` / `viasr_last_overworld`），随存档持久化，**重启服务器不丢失**
- **Y 坐标不记忆**，每次由目标世界的**地表高度动态计算** → 即使地形被挖掉或被其它模组改动，落点始终位于地表上方第一个空气位置，不会卡进方块、悬空或掉进虚空

#### 3. 敌对生物自动清理
- `EntityJoinWorldEvent` 在实体加入维度的**瞬间**拦截 —— 覆盖自然刷新、刷怪笼、命令召唤、跨维度传送、区块重载
- `WorldTickEvent` 每 **5 秒**扫描已加载实体，清理「运行期切换开关后残留的旧生物」等边界情况
- 判定规则：实体实现 `IMob` 且不在白名单中即被移除
  → **玩家、动物、村民、铁傀儡、宠物天然不受影响**

#### 4. 三种地形生成模式（配置切换）
| `generateTerrain` | `grasslandMode` | 生成器 | 特点 |
|---|---|---|---|
| `false` | — | `ChunkGeneratorFlatDim` | 固定高度平坦，最省资源 |
| `true` | `true` | `ChunkGeneratorGrasslandFlat` | **平坦草原 + 大型蜿蜒河流**，单一平原群系，**跨存档一致** |
| `true` | `false` | `ChunkGeneratorOverworldDim` | 原版主世界多群系随机地形，依赖世界种子 |

**草原生成器（默认）的细节**：
- **跨存档一致**：地形与装饰均由固定种子派生，与世界种子无关
- **河岸自然化**：河岸宽度随 Z 轴 ±25% 波动、河床沿流向缓变、过渡带叠加坐标噪声 —— 避免「塑料感」的完美对称
- 河岸表层按离水距离分层混合沙 / 砾石
- **不生成**原版矿物、湖泊、岩浆湖、泉水

#### 5. OptiFine / Iris 光影友好
`WorldProviderModDim` 重写了所有客户端渲染相关的维度属性，让光影模组把它当作主世界对待：
- `isSurfaceWorld() = true` —— 让光影接管云、天气、日照
- `isSkyColored() = true` —— 天空随时间 / 群系着色
- `getFogColor()` / `getSkyColor()` —— 返回主世界近似色
- `getSunBrightness()` / `getStarBrightness()` —— 委托 world
- `getCloudHeight() = 128` —— 与主世界一致

> ⚠️ 注意：**不要**重写 `generateLightBrightnessTable()` —— 这是社区常见误区，会导致该维度光照完全失效（光影也救不回）。

### 指令

| 指令 | 权限 | 说明 |
|---|---|---|
| `/dim tp` | 所有人 | 进入资源维度（有记录则回到上次离开处，否则落点为维度中心） |
| `/dim leave` | 所有人 | 离开资源维度，回到上次进入前的主世界位置 |
| `/dim list` | OP | 查看当前在资源维度内的玩家列表 |

指令别名：`/dimension`、`/resdim`

### 配置文件

生成于 `config/com.example.safeworld.cfg`。**修改后需重启游戏（或重新进入维度）生效**；所有数值都有安全范围，越界会被生成器**自动钳制**，不会抛异常或产生崩坏地形。

#### 基础
| 配置项 | 默认 | 说明 |
|---|---|---|
| `dimensionSize` | `200000` | 世界边界边长（格） |
| `generateTerrain` | `true` | 是否生成地形（`false` = 固定高度平坦） |
| `generateStructures` | `false` | 是否生成结构（村庄 / 要塞 / 废弃矿井等） |
| `spawnMobs` | `false` | 是否允许生物自然刷新 |

#### 草原地形（`grasslandMode = true` 时生效）
| 配置项 | 默认 | 安全范围 | 说明 |
|---|---|---|---|
| `grasslandMode` | `true` | — | `true` = 平坦草原 + 大河；`false` = 原版多群系 |
| `terrainAmplitude` | `6.0` | `[0, 16]` | 起伏总幅度（格），`0` = 完全平坦 |
| `terrainFrequency` | `0.0035` | `[0.001, 0.015]` | 越小地形越舒缓 |
| `riverHalfWidthBase` | `31.0` | `[8, 60]` | 河流半宽（河面全宽 = 半宽 × 2） |
| `riverHalfWidthAmplitude` | `9.0` | `[0, 基准 × 0.x]` | 河道随 Z 缓慢变宽变窄 |
| `riverBed` | `58.0` | `[12, 58]` | 河床底部高度（还会被「地形最低高度 − 1」二次约束） |
| `riverBank` | `30` | `[4, 40]` | 河岸过渡带宽度，越小越陡 |
| `grasslandSeed` | `0x5EED6A551A4D` | — | 草原生成器固定种子，只影响装饰 |

### HBM CE 联动

完全通过**反射**实现 —— **编译期不依赖 HBM CE**；若 HBM 未安装、版本不支持或类名/字段变化，则整体**静默跳过**，不会崩端。

| 生成内容 | 配置项 | 默认 | 说明 |
|---|---|---|---|
| 常规矿物 | `hbmSpawnNormalOres` | **关** | 铀、钍、钛、钨等普通地表矿物 |
| 常规矿物密度 | `hbmNormalOreRate` | `1.0` | 倍率，安全范围 `[0, 10]` |
| 基岩矿石 | `hbmSpawnBedrockOres` | **开** | 世界底部的特殊矿点（含完整丰度数据），需基岩钻机开采 |
| 基岩油层 | `hbmSpawnBedrockOil` | **开** | 深层原油储层，需水力压裂塔开采 |
| 油岩 | `hbmSpawnOilDeposits` | **开** | 地下连片油砂矿脉 |

- `enableHbmCompat` —— HBM 联动总开关，关闭后本维度完全没有 HBM 内容
- `hbmTemplateDim` —— 模板维度，**默认 `0`（主世界）**，即从主世界复制全部 HBM 生成配置；想用其它维度做模板改这里即可

> **为什么常规矿物默认关闭？** 资源维度通常通过**基岩特性**（基岩矿石 / 基岩油层）获取资源，而不希望地表堆满普通矿脉。需要的话打开开关并用 `hbmNormalOreRate` 调节密度。

### 安装

1. 安装 **Minecraft Forge 1.12.2**（推荐 `14.23.5.2847`）
2. 把 `safe_world-<版本>.jar` 放进 `mods/` 文件夹
3. 可选：一并安装 **HBM CE**（不装也能正常使用，只是没有 HBM 矿物联动）
4. 启动游戏 → 进服后输入 `/dim tp`

### 从源码构建

```bash
git clone <本仓库地址>
cd mod
./gradlew build          # Windows: gradlew.bat build
# 产物：build/libs/safe_world-2.1.jar
```

- 需要 **JDK 8**
- 依赖 `ForgeGradle 2.3-SNAPSHOT`、Minecraft `1.12.2-14.23.5.2847`、MCP `stable_39`

### 兼容性

| 项目 | 说明 |
|---|---|
| HBM CE | ✅ 可选联动（纯反射，未安装自动跳过） |
| OptiFine / Iris | ✅ 已针对光影重写维度渲染属性 |
| 其它维度模组 | ✅ 使用独立维度 ID `66`，可通过配置更换 |

---

## English

### Overview

A standalone **resource / home dimension** mod for heavy modpacks such as HBM CE (NTM Community Edition).

Vanilla HBM worlds are rarely suitable for long-term settlement — radiation, hostile mobs and terrain all work against you. Instead of nerfing the gameplay, this mod simply opens **another dimension**: no hostile mobs, fully configurable terrain, and optional HBM ore generation, while the Overworld keeps its original hardcore rules.

- **Mod ID**: `com.example.safeworld` (`GeneratedMod.MOD_ID`)
- **Dimension ID**: `66` (non-negative, avoids vanilla `-1 / 0 / 1`)
- **Dimension name**: `low_plains`
- **Author**: VIASR
- **Platform**: Minecraft 1.12.2 · Forge 14.23.5.2847
- **Current version**: 2.1.0

### Features

#### 1. A mob-free dimension with fully controllable terrain
- World border **200000 × 200000** (configurable)
- **No structures** generated by default (villages / strongholds / mineshafts are individually toggleable)
- Natural mob spawning **disabled by default**, backed by a dedicated hostile-mob remover
- Dimension ID `66` — non-negative, no conflicts with vanilla or other mods

#### 2. Two-way teleport memory (survives restarts)
| Action | Effect |
|---|---|
| `/dim tp` | Remembers your **Overworld** X/Z, then teleports you into the dimension |
| `/dim leave` | Remembers your **dimension** X/Z, then returns you to the Overworld X/Z you entered from |

- Positions are stored in the player's **persistent NBT** (`viasr_last_dim` / `viasr_last_overworld`) — they survive server restarts
- **Y is never remembered**; it is computed dynamically from the target world's surface height, so you always land in the first air block above the ground — no suffocation, no floating, no void drops, even if the terrain was mined out or modified by other mods

#### 3. Automatic hostile-mob removal
- `EntityJoinWorldEvent` intercepts entities the **instant** they join the dimension — covering natural spawning, spawners, command summons, cross-dimension travel and chunk reloads
- `WorldTickEvent` scans loaded entities every **5 seconds** to clean up leftovers after runtime config toggles and other edge cases
- Rule: any entity implementing `IMob` and not on the whitelist is removed
  → **players, animals, villagers, iron golems and pets are unaffected by design**

#### 4. Three terrain modes (configurable)
| `generateTerrain` | `grasslandMode` | Generator | Notes |
|---|---|---|---|
| `false` | — | `ChunkGeneratorFlatDim` | Fixed-height superflat — lightest |
| `true` | `true` | `ChunkGeneratorGrasslandFlat` | **Flat grassland + meandering rivers**, single plains biome, **identical across saves** |
| `true` | `false` | `ChunkGeneratorOverworldDim` | Vanilla Overworld multi-biome terrain, seed-dependent |

**Grassland generator details (default)**:
- **Deterministic across saves** — terrain and decoration derive from a fixed seed, independent of the world seed
- **Naturalised river banks**: bank width varies ±25% along Z, riverbed drifts gently with the flow direction, and coordinate noise is layered over the transition band — no "plastic" perfect symmetry
- Bank surface layers sand/gravel by distance from water
- **No** vanilla ores, lakes, lava lakes or springs

#### 5. OptiFine / Iris friendly
`WorldProviderModDim` overrides every client-render-related dimension property so shader mods treat it exactly like the Overworld:
- `isSurfaceWorld() = true` — shaders take over clouds, weather and sunlight
- `isSkyColored() = true` — sky is tinted by time and biome
- `getFogColor()` / `getSkyColor()` — return Overworld-like colours
- `getSunBrightness()` / `getStarBrightness()` — delegated to the world
- `getCloudHeight() = 128` — matches the Overworld

> ⚠️ Do **not** override `generateLightBrightnessTable()` — a common community mistake that breaks lighting entirely (shaders cannot rescue it).

### Commands

| Command | Permission | Description |
|---|---|---|
| `/dim tp` | Everyone | Enter the dimension (resumes your last exit point, otherwise the dimension centre) |
| `/dim leave` | Everyone | Leave the dimension, returning to your previous Overworld position |
| `/dim list` | OP | List players currently inside the dimension |

Aliases: `/dimension`, `/resdim`

### Configuration

Generated at `config/com.example.safeworld.cfg`. **Changes require a game restart (or re-entering the dimension).** All numeric values have safe ranges — out-of-range values are **clamped automatically** with no exceptions and no corrupted terrain.

#### Basics
| Key | Default | Description |
|---|---|---|
| `dimensionSize` | `200000` | World border edge length (blocks) |
| `generateTerrain` | `true` | Generate terrain (`false` = fixed-height superflat) |
| `generateStructures` | `false` | Generate structures (villages / strongholds / mineshafts) |
| `spawnMobs` | `false` | Allow natural mob spawning |

#### Grassland terrain (active when `grasslandMode = true`)
| Key | Default | Safe range | Description |
|---|---|---|---|
| `grasslandMode` | `true` | — | `true` = flat grassland + rivers; `false` = vanilla multi-biome |
| `terrainAmplitude` | `6.0` | `[0, 16]` | Total relief (blocks); `0` = perfectly flat |
| `terrainFrequency` | `0.0035` | `[0.001, 0.015]` | Lower = smoother terrain |
| `riverHalfWidthBase` | `31.0` | `[8, 60]` | Half width of rivers (full width = 2 × this) |
| `riverHalfWidthAmplitude` | `9.0` | bounded by base | Rivers widen/narrow slowly along Z |
| `riverBed` | `58.0` | `[12, 58]` | Riverbed floor height (further constrained by "lowest terrain height − 1") |
| `riverBank` | `30` | `[4, 40]` | Bank transition width; smaller = steeper |
| `grasslandSeed` | `0x5EED6A551A4D` | — | Fixed generator seed; affects decoration only |

### HBM CE Integration

Implemented purely via **reflection** — there is **no compile-time dependency** on HBM CE. If HBM is missing, unsupported, or its class/field names change, the integration **silently skips**.

| Content | Key | Default | Notes |
|---|---|---|---|
| Regular ores | `hbmSpawnNormalOres` | **off** | Uranium, thorium, titanium, tungsten, … |
| Regular ore density | `hbmNormalOreRate` | `1.0` | Multiplier, safe range `[0, 10]` |
| Bedrock ores | `hbmSpawnBedrockOres` | **on** | Deep ore points with full abundance data (bedrock drill required) |
| Bedrock oil | `hbmSpawnBedrockOil` | **on** | Deep crude reservoirs (fracking tower required) |
| Oil deposits | `hbmSpawnOilDeposits` | **on** | Underground oil-sand veins |

- `enableHbmCompat` — master switch; when off, the dimension contains no HBM content at all
- `hbmTemplateDim` — template dimension, **default `0` (Overworld)**: all HBM generation settings are copied from it

> **Why are regular ores off by default?** Resource dimensions are normally used to obtain resources through **bedrock features** (bedrock ores / oil) rather than by carpeting the surface with common ore veins. Enable the toggle and tune `hbmNormalOreRate` if you want them.

### Installation

1. Install **Minecraft Forge 1.12.2** (`14.23.5.2847` recommended)
2. Drop `safe_world-<version>.jar` into your `mods/` folder
3. Optional: install **HBM CE** as well (the mod works fine without it — just no ore integration)
4. Launch the game and run `/dim tp`

### Building from source

```bash
git clone <this repository>
cd mod
./gradlew build          # Windows: gradlew.bat build
# Output: build/libs/safe_world-2.1.jar
```

- Requires **JDK 8**
- Uses `ForgeGradle 2.3-SNAPSHOT`, Minecraft `1.12.2-14.23.5.2847`, MCP `stable_39`

### Compatibility

| Target | Status |
|---|---|
| HBM CE | ✅ Optional integration (reflection-based; auto-skips if absent) |
| OptiFine / Iris | ✅ Dimension render properties rewritten for shader support |
| Other dimension mods | ✅ Uses dimension ID `66` (configurable) |

---

## 作者与许可 · Author & License

- **作者 / Author**: VIASR
- **许可 / License**: All Rights Reserved（如需开源许可，请在仓库中添加 `LICENSE` 文件并在此处更新）
  <br>*All Rights Reserved. Add a `LICENSE` file to the repository and update this section if you intend to open-source it.*

---

<!--
GitHub 仓库「About」栏短描述（可直接粘贴 / copy-paste into the repo "About" field）：

中文：给 HBM 整合包用的安全资源维度 —— /dim tp 进入一个无怪物、地形可配置、可选接入 HBM CE 矿物生成的世界（Forge 1.12.2）

English: A safe resource dimension for HBM modpacks — /dim tp into a mob-free, configurable world with optional HBM CE ore generation (Forge 1.12.2)

建议 Topics：minecraft, forge, minecraft-mod, 1.12.2, hbm, ntm, dimension, resource-dimension, modpack
-->
