# VIASR's Safe Dimension/Safe World · 安全维度

> **为 HBM 核工业整合包提供的安全建家维度**：输入 `/dim tp` 即可进入一个没有怪物、地形可控、可选接入 HBM CE 矿物生成的世界，安心建造基地。
>
> **HBM 核能科技模组包的安全资源维度。**输入 `/dim tp`以进入一个无怪物、完全可配置的世界——可选 HBM CE 矿石生成——这样你就可以安心地建造你的基地。

<p>
  <img alt="Minecraft" src="https://img.shields.io/badge/Minecraft-1.12.2-informational">
  <img alt="Forge" src="https://img.shields.io/badge/Forge-14.23.5.2847-informational">
  <img alt="Version" src="https://img.shields.io/badge/version-2.1.0-success">
  <img alt="License" src="https://img.shields.io/badge/license-All%20Rights%20Reserved-lightgrey">
</p>

---

## 中文文档

### 简介

本模组为**逃逸寄生体模组**提供了**独立的建家维度**并且兼容了HBM社区版的基岩类资源生成，**可单独使用**。

逃逸寄生体的生物导致主世界往往不适合长期定居，而单纯降低难度又破坏玩法。本模组换一条路：**再开一个维度**——那里没有敌对生物、地形参数全部可配置、按需接入矿物生成，主世界依然保持原本的硬核玩法。

- **模组 ID**：`com.example.safeworld`（见 `GeneratedMod.MOD_ID`）
- **维度注册码**：`66`（非负整数，避开原版 `-1 / 0 / 1`）
- **维度内部名**：`low_plains`
- **作者**：VIASR&Ftahinza
- **程序**: Ftahinza&UNFOX3213&DeepSeek v4.1flash
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
|`生成地形`|`草地模式`| 生成器 | 特点 |    
|---|---|---|---|
| `false` | — | `ChunkGeneratorFlatDim` | 固定高度平坦，最省资源 |
| `true` | `true` | `ChunkGeneratorGrasslandFlat` | **平坦草原 + 大型蜿蜒河流**，单一平原群系，**跨存档一致** |
| `true` | `false` | `ChunkGeneratorOverworldDim` | 原版主世界多群系随机地形，依赖世界种子 |

**草原生成器（默认）的细节**：
- **跨存档一致**：地形与装饰均由固定种子派生，与世界种子无关
- **河岸自然化**：河岸宽度随 Z 轴 ±25% 波动、河床沿流向缓变、过渡带叠加坐标噪声 —— 避免「塑料感」的完美对称
- 河岸表层按离水距离分层混合沙 / 砾石
- **不生成**原版矿物、湖泊、岩浆湖、泉水


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

> **A Safe Base-Building Dimension for HBM Nuclear Industry Modpacks**: Type `/dim tp` to enter a world with no mobs, controllable terrain, and optional HBM CE ore generation — build your base with peace of mind.
>
> **A safe resource dimension for HBM nuclear tech modpacks.** Type `/dim tp` to enter a mob-free, fully configurable world — with optional HBM CE ore generation — so you can build your base without worry.

<p>
  <img alt="Minecraft" src="https://img.shields.io/badge/Minecraft-1.12.2-informational">
  <img alt="Forge" src="https://img.shields.io/badge/Forge-14.23.5.2847-informational">
  <img alt="Version" src="https://img.shields.io/badge/version-2.1.0-success">
  <img alt="License" src="https://img.shields.io/badge/license-All%20Rights%20Reserved-lightgrey">
</p>

---

## English Documentation

### Introduction

This mod provides a **standalone base-building dimension** for the **Scape and Run: Parasites mod** and is compatible with HBM Community Edition's bedrock-type resource generation. It **can be used on its own**.

The creatures from Scape and Run: Parasites often make the Overworld unsuitable for long-term settlement, yet simply lowering the difficulty ruins the gameplay. This mod takes a different approach: **open another dimension** — one with no hostile mobs, fully configurable terrain parameters, and on-demand ore generation — while the Overworld retains its original hardcore gameplay.

- **Mod ID**: `com.example.safeworld` (see `GeneratedMod.MOD_ID`)
- **Dimension Registration ID**: `66` (non-negative integer, avoiding vanilla `-1 / 0 / 1`)
- **Internal Dimension Name**: `low_plains`
- **Authors**: VIASR & Ftahinza
- **Programmers**: Ftahinza & UNFOX3213 & DeepSeek v4.1flash
- **Platform**: Minecraft 1.12.2 · Forge 14.23.5.2847
- **Current Version**: 2.1.0

### Features

#### 1. A "Mob-Free + Controllable Terrain" Dimension
- World border **200000 × 200000** (configurable)
- By default, **no vanilla structures are generated** (villages / strongholds / abandoned mineshafts, etc. can each be toggled individually)
- By default, **natural mob spawning is disabled**; backed up by an independent hostile mob cleaner (see below)
- Dimension ID uses non-negative integer `66`, avoiding conflicts with vanilla and other mods

#### 2. Teleport Memory (Bidirectional, Persists Across Restarts)
| Action | Effect |
|---|---|
| `/dim tp` | Remembers your **Overworld** X/Z → teleports you to the resource dimension |
| `/dim leave` | Remembers your **resource dimension** X/Z → returns you to the Overworld X/Z from before your last entry |

- Position memory is stored in **player persistent NBT** (`viasr_last_dim` / `viasr_last_overworld`), persists with the save file, and **survives server restarts**
- **Y coordinate is not memorized**; each time it is dynamically calculated from the target world's **surface height** → even if terrain has been mined out or modified by other mods, the landing point is always the first air position above the surface, so you won't get stuck in blocks, float in mid-air, or fall into the void

#### 3. Automatic Hostile Mob Cleanup
- `EntityJoinWorldEvent` intercepts the **instant** an entity joins the dimension — covering natural spawning, spawners, command summons, cross-dimensional teleportation, and chunk reloads
- `WorldTickEvent` scans loaded entities every **5 seconds**, cleaning up edge cases such as "old mobs left over after toggling the switch at runtime"
- Rule: entities implementing `IMob` that are not on the whitelist are removed
  → **Players, animals, villagers, iron golems, and pets are naturally unaffected**

#### 4. Three Terrain Generation Modes (Config-Switchable)
|`generateTerrain`|`grasslandMode`| Generator | Characteristics |
|---|---|---|---|
| `false` | — | `ChunkGeneratorFlatDim` | Fixed-height flat terrain, most resource-efficient |
| `true` | `true` | `ChunkGeneratorGrasslandFlat` | **Flat grassland + large meandering rivers**, single plains biome, **consistent across saves** |
| `true` | `false` | `ChunkGeneratorOverworldDim` | Vanilla Overworld multi-biome random terrain, depends on world seed |

**Details of the Grassland Generator (default)**:
- **Consistent across saves**: terrain and decoration are derived from a fixed seed, independent of the world seed
- **Naturalized riverbanks**: riverbank width fluctuates ±25% along the Z-axis, riverbed gradually changes along the flow direction, and transition zones are layered with coordinate noise — avoiding a "plastic-feeling" perfectly symmetrical look
- Riverbank surface layers mix sand / gravel in layers based on distance from water
- **Does not generate** vanilla ores, lakes, lava lakes, or springs


### Commands

| Command | Permission | Description |
|---|---|---|
| `/dim tp` | Everyone | Enter the resource dimension (returns to last departure point if a record exists, otherwise lands at the dimension center) |
| `/dim leave` | Everyone | Leave the resource dimension, returning to the Overworld position from before your last entry |
| `/dim list` | OP | View the list of players currently in the resource dimension |

Command aliases: `/dimension`, `/resdim`

### Configuration File

Generated at `config/com.example.safeworld.cfg`. **Changes take effect after restarting the game (or re-entering the dimension)**; all values have safe ranges, and out-of-range values are **automatically clamped** by the generator — no exceptions thrown or corrupted terrain produced.

#### Basic
| Config Option | Default | Description |
|---|---|---|
| `dimensionSize` | `200000` | World border side length (blocks) |
| `generateTerrain` | `true` | Whether to generate terrain (`false` = fixed-height flat) |
| `generateStructures` | `false` | Whether to generate structures (villages / strongholds / abandoned mineshafts, etc.) |
| `spawnMobs` | `false` | Whether to allow natural mob spawning |

#### Grassland Terrain (effective when `grasslandMode = true`)
| Config Option | Default | Safe Range | Description |
|---|---|---|---|
| `grasslandMode` | `true` | — | `true` = flat grassland + large rivers; `false` = vanilla multi-biome |
| `terrainAmplitude` | `6.0` | `[0, 16]` | Total terrain relief amplitude (blocks), `0` = completely flat |
| `terrainFrequency` | `0.0035` | `[0.001, 0.015]` | Smaller values make terrain gentler |
| `riverHalfWidthBase` | `31.0` | `[8, 60]` | River half-width (full river surface width = half-width × 2) |
| `riverHalfWidthAmplitude` | `9.0` | `[0, base × 0.x]` | River channel slowly widens and narrows along Z |
| `riverBed` | `58.0` | `[12, 58]` | Riverbed bottom height (also secondarily constrained by "terrain minimum height − 1") |
| `riverBank` | `30` | `[4, 40]` | Riverbank transition zone width; smaller values make it steeper |
| `grasslandSeed` | `0x5EED6A551A4D` | — | Fixed seed for the grassland generator, only affects decoration |

### HBM CE Integration

Implemented entirely through **reflection** — **no compile-time dependency on HBM CE**; if HBM is not installed, the version is unsupported, or class names/fields change, it **silently skips** entirely without crashing.

| Generated Content | Config Option | Default | Description |
|---|---|---|---|
| Regular Ores | `hbmSpawnNormalOres` | **Off** | Common surface ores such as uranium, thorium, titanium, tungsten, etc. |
| Regular Ore Density | `hbmNormalOreRate` | `1.0` | Multiplier, safe range `[0, 10]` |
| Bedrock Ores | `hbmSpawnBedrockOres` | **On** | Special ore deposits at the world bottom (with full abundance data), requiring bedrock drills to mine |
| Bedrock Oil Deposits | `hbmSpawnBedrockOil` | **On** | Deep crude oil reservoirs, requiring hydraulic fracking towers to extract |
| Oil Sands | `hbmSpawnOilDeposits` | **On** | Underground continuous oil sand veins |

- `enableHbmCompat` — Master switch for HBM integration; when disabled, this dimension contains no HBM content at all
- `hbmTemplateDim` — Template dimension, **default `0` (Overworld)**, meaning all HBM generation configs are copied from the Overworld; change this if you want to use another dimension as the template

> **Why are regular ores disabled by default?** Resource dimensions typically acquire resources through **bedrock features** (bedrock ores / bedrock oil deposits), and you don't want the surface covered in ordinary ore veins. If needed, turn on the switch and adjust density with `hbmNormalOreRate`.

### Installation

1. Install **Minecraft Forge 1.12.2** (recommended `14.23.5.2847`)
2. Place `safe_world-<version>.jar` into the `mods/` folder
3. Optional: Also install **HBM CE** (works fine without it, just no HBM ore integration)
4. Launch the game → type `/dim tp` after joining

### Building from Source

```bash
git clone <repository URL>
cd mod
./gradlew build          # Windows: gradlew.bat build
# Output: build/libs/safe_world-2.1.jar
```

- Requires **JDK 8**
- Depends on `ForgeGradle 2.3-SNAPSHOT`, Minecraft `1.12.2-14.23.5.2847`, MCP `stable_39`

### Compatibility

| Item | Description |
|---|---|
| HBM CE | ✅ Optional integration (pure reflection, automatically skipped if not installed) |
| OptiFine / Iris | ✅ Dimension rendering properties rewritten for shader compatibility |
| Other dimension mods | ✅ Uses independent dimension ID `66`, changeable via config |

---

<!--
GitHub 仓库「About」栏短描述（可直接粘贴 / copy-paste into the repo "About" field）：

中文：给 HBM 整合包用的安全资源维度 —— /dim tp 进入一个无怪物、地形可配置、可选接入 HBM CE 矿物生成的世界（Forge 1.12.2）

English: A safe resource dimension for HBM modpacks — /dim tp into a mob-free, configurable world with optional HBM CE ore generation (Forge 1.12.2)

建议 Topics：minecraft, forge, minecraft-mod, 1.12.2, hbm, ntm, dimension, resource-dimension, modpack
-->
