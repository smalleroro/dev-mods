# dev-mods

Minecraft **1.20.1 / Forge 47.4.x** 自研模组源码与构建产物。

## 仓库结构

一个模组一个目录，源码与成品 jar 放在一起：

```
dev-mods/
├── baritone-ui/                 # Baritone 可视化控制面板 + 状态 HUD
│   ├── src/main/kotlin/...      # Kotlin 源码
│   ├── src/main/resources/...   # mods.toml、语言文件
│   ├── build.gradle.kts
│   ├── gradle.properties
│   ├── gradlew / gradlew.bat
│   └── build/libs/
│       └── baritoneui-1.0.0+mc1.20.1.jar
├── rotationspoof/               # 外发朝向伪装（客户端 Mixin）
│   ├── src/main/java/...        # Mixin 注入代码
│   ├── src/main/kotlin/...      # Kotlin 源码
│   ├── src/main/resources/...   # mods.toml、mixins.json、语言文件
│   ├── build.gradle.kts
│   ├── gradle.properties
│   ├── gradlew / gradlew.bat
│   └── build/libs/
│       └── rotationspoof-1.0.0+mc1.20.1.jar
└── LICENSE
```

## baritone-ui

Baritone 的可视化控制面板（停止 / 前往 / 挖掘 / 跟随 / 探索 / 农耕）与左上角状态 HUD。
挖掘预设支持多选，跨分类合并成一条 `#mine` 请求。

**运行时依赖**：Baritone 本体（可选）。模组通过反射桥接，Baritone 缺失时降级显示
`not installed`，不会崩溃。

**编译依赖**：需要自备 `baritone-api-forge-1.10.5.jar` 放到 `libs/`。
仓库不分发该 jar（第三方组件，无再分发授权）。

**热键**：`B` 打开面板，`H` 切换 HUD（可在按键设置中修改）。

## rotationspoof

客户端 Mixin 模组。改写外发移动包的 yaw/pitch，让其他玩家看到你的角色在转圈，
而本机视角完全不受影响。

- 只动 `ServerboundMovePlayerPacket` 的角度字段，位置数据原样保留，服务端不会回拉。
- 热键默认 `R`，可在按键设置中修改。
- 纯客户端，服务端无需安装。

**依赖**：仅 Minecraft / Forge 本体，无第三方运行时依赖。

## 构建

```bash
cd baritone-ui      # 或 cd rotationspoof
./gradlew build
```

产物输出到各自目录的 `build/libs/`。

## 环境

| 项 | 版本 |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.4.x |
| Java | 21 |
| Kotlin for Forge | 4.11 |
| Gradle | 8.10.2 |

## 说明

- `baritone-ui/libs/` 下的 Baritone API jar 不纳入版本控制。
- 各模组的 `build/libs/*.jar` 保留，便于直接取成品。
