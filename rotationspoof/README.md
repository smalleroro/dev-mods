# rotationspoof

外发朝向伪装，Minecraft 1.20.1 / Forge 47.4.x，纯客户端 Mixin 模组。

开启后，服务端和所有其他玩家看到你的角色在不停转圈，而**你本机视角完全不受影响**。

## 原理

只改写外发移动包的角度字段，不动位置数据：

- 在 `ClientPacketListener.send()` 的 HEAD 处拦截，命中 `ServerboundMovePlayerPacket` 且
  该包携带角度时，原地覆写 yaw / pitch。
- 不取消原包、不补发包 —— 包数量与原版完全一致，服务端不会因此回拉玩家位置。
- 每发出一个带角度的包，伪 yaw 递增 `degreesPerPacket`（默认 20°），在 `[0, 360)` 内回绕。
- 离开世界时自动关闭并清空累计角度，下个世界从干净状态开始。

## 热键

| 键 | 作用 |
|---|---|
| `R` | 开启 / 关闭 |

可在 `选项 → 控制` 中重新绑定。切换时会在动作栏提示当前状态。

默认 `R` 可能与部分模组冲突，冲突时改键即可。

## 前置模组

| 前置 | 是否必需 | 说明 |
|---|---|---|
| [Kotlin for Forge](https://www.curseforge.com/minecraft/mc-mods/kotlin-for-forge) | **必需** | 本模组用 Kotlin 编写，`mods.toml` 首行声明 `modLoader="kotlinforforge"`，缺失时游戏无法加载 |

除以上前置外，无其它运行时依赖，服务端无需安装。

## 构建

```bash
./gradlew build
```

产物：`build/libs/rotationspoof-<version>+mc1.20.1.jar`

## 注意

- **纯客户端模组**。只影响别人看到的画面，不改变服务端的实际判定逻辑。
- 面向私人服务器使用。**不提供反检测规避** —— 不抖动、不限速、不模拟人类操作特征，
  在有反作弊的服务器上使用会被识别。
- 服务端的视角校验（移动合法性检查）仍按真实视角运行，两者互不干扰。
