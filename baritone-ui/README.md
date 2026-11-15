# baritone-ui

Baritone 的可视化控制面板与状态 HUD，Minecraft 1.20.1 / Forge 47.4.x。

在不打开聊天栏、不输命令的前提下，用界面按钮控制 Baritone：前往、挖掘、跟随、探索、农耕。

## 功能

| 功能 | 说明 |
|---|---|
| 前往 | 按 X / Y / Z 寻路，只填 X Z 则不限 Y 层 |
| 挖掘 | 多选预设，跨分类合并成一条 `#mine` 请求 |
| 跟随 | 跟随指定玩家名 |
| 探索 | 从当前位置向外探索 |
| 农耕 | 运行 Baritone 自动农耕流程 |
| 停止 | 取消所有 Baritone 进程 |
| 状态 HUD | 左上角显示当前任务、坐标、与目标的距离 |

挖掘预设覆盖原版矿物、Mekanism / Thermal / Create / AE2 / 神秘农业 / 匠魂 / 以太系等
模组矿物，以及石料、木材。预设只是 id 列表，某模组不存在时对应条目不会被匹配，无副作用。

## 热键

| 键 | 作用 |
|---|---|
| `B` | 打开控制面板 |
| `H` | 切换状态 HUD |

均可在 `选项 → 控制` 中重新绑定。

## 前置模组

安装本模组前，请先装好以下前置，**两个都是必需**：

| 前置 | 是否必需 | 说明 |
|---|---|---|
| [Kotlin for Forge](https://www.curseforge.com/minecraft/mc-mods/kotlin-for-forge) | **必需** | 本模组用 Kotlin 编写，`mods.toml` 首行声明 `modLoader="kotlinforforge"`，缺失时游戏无法加载 |
| [Baritone](https://www.curseforge.com/minecraft/mc-mods/baritone) | **必需** | 面板与 HUD 的全部功能都靠它。`mods.toml` 中 `mandatory=true`，缺失时 Forge 会在加载阶段直接阻止启动并提示缺失的前置，**不会**静默降级 |

## 构建

编译需要 Baritone 的 API jar 作为编译期依赖，自行放到 `libs/`：

```
libs/baritone-api-forge-1.10.5.jar
```

该 jar 属于前置模组 Baritone，不随本仓库分发——**文件不存在时构建会直接失败**。

```bash
./gradlew build
```

产物：`build/libs/baritoneui-<version>+mc1.20.1.jar`

放入游戏 `mods/` 目录即可。加载时 Forge 会按 `mods.toml` 中声明的版本范围校验前置，
不满足会在启动日志里提示。

## 配置

`config/baritoneui-client.toml`：

| 项 | 默认 | 说明 |
|---|---|---|
| `hud.enabled` | `true` | HUD 初始是否显示 |
| `hud.x` / `hud.y` | `4` / `4` | HUD 相对左上角的像素偏移 |
| `hud.opacity` | `0.5` | HUD 背景不透明度 |
| `hud.show_distance` | `true` | 显示与目标的距离 |
| `hud.show_task_name` | `true` | 显示当前任务名 |

`H` 切换的热键只改内存状态，不写回配置文件。
