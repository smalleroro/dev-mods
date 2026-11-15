# dev-mods

Minecraft **1.20.1 / Forge 47.4.x** 自研模组源码与构建产物。

仓库由多人共同维护。**所有模组必须遵循下面这套目录规范**，新增模组时照抄示例结构，
不要各自发明。

## 目录规范

### 顶层

```
dev-mods/
├── <module>/            # 一个模组一个目录，目录名 = 模组 ID（小写、连字符）
├── <module>/
├── docs/                # 跨模组的共享约定
├── README.md
└── LICENSE
```

顶层只放模组目录和这几个固定文件。**不允许**在根目录散落 jar、源码或临时文件。

### 单个模组目录（标准结构）

以 `baritone-ui` 为例：

```
baritone-ui/
├── README.md                                # 必需，见下方「模组 README」
├── src/
│   └── main/
│       ├── kotlin/com/<作者>/<module>/     # Kotlin 源码（Mixin 类除外）
│       ├── java/com/<作者>/<module>/       # 仅放 Mixin 类，无则整目录省略
│       └── resources/
│           ├── META-INF/mods.toml          # 必需
│           ├── <module>.mixins.json        # 仅含 Mixin 的模组需要
│           ├── pack.mcmeta                 # 必需
│           └── assets/<module>/lang/       # en_us.json、zh_cn.json
├── build/
│   └── libs/
│       └── <module>-<version>+mc1.20.1.jar # 成品，保留在版本库里
├── libs/                                    # 第三方编译依赖，jar 不入库
│   └── .gitkeep                             # 占位，保证目录存在
├── gradle/wrapper/                          # gradle-wrapper.jar + properties
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
└── .gitignore
```

### 命名规则

| 项 | 规则 |
|---|---|
| 模组目录名 | 与 `mods.toml` 的 `modId` 一致，小写、连字符分隔 || jar 文件名 | `<modId>-<version>+mc<mc版本>.jar`，由 `build.gradle.kts` 生成 |
| 包名 | `com.<作者>.<module>`，与目录名对应的类名保持驼峰 |
| 语言文件 | 必须同时提供 `en_us.json` 与 `zh_cn.json`，key 前缀用模组 ID |

### 必须提交

- `README.md`（模组目录下的独立说明，见下节）
- `src/` 下全部源码与资源
- `build.gradle.kts`、`gradle.properties`、`settings.gradle.kts`
- `gradlew`、`gradlew.bat`、`gradle/wrapper/`
- `build/libs/<module>-*.jar`（成品，方便直接取用）
- `libs/.gitkeep`（占位，保证第三方依赖目录存在）
- 模组级 `.gitignore`

### 禁止提交

| 内容 | 原因 |
|---|---|
| `.gradle/`、`run/`、`out/` | 本地缓存与运行目录 |
| `build/` 下除 `build/libs/*.jar` 以外的产物 | 体积大、可重新生成 |
| `libs/` 下的第三方 jar | 无再分发授权 |
| `.idea/`、`*.iml`、`.vscode/`、`.settings/` | IDE 个人配置 |
| 日志、崩溃报告、`*.mmdp`、临时脚本 | 排障材料，留在本地 |
| 任何含口令、token、密钥的文件 | 安全 |

## 模组 README

**每个模组目录下必须有 `README.md`**，内容只写这个模组自身的事，方便别人单独查看：

1. 模组做什么（一段话）
2. 功能 / 原理要点
3. 热键或命令
4. 前置模组（表格：名称、是否必需、缺失时的表现）
5. 运行时依赖与编译依赖
6. 构建方式与产物路径

不要重复根 README 的通用规范，只写该模组特有的信息。

## 新增模组检查清单

1. 目录名 = `modId`，小写、连字符分隔。
2. 目录下有 `README.md`，按上节六项填写，其中**前置模组表格**不可省略。
3. 复制现有模组作为起点（推荐 `rotationspoof`，依赖最少）。
4. `src/main/resources/META-INF/mods.toml` 已填 `modId`、`version`、`displayName`。
5. `pack.mcmeta` 的 `pack_format` 与 1.20.1 匹配。
6. 语言文件 `en_us.json` 与 `zh_cn.json` 齐全。
7. 模组目录下有自己的 `.gitignore`（内容见下方统一模板）。
8. 本 README 的「模组列表」表格补一行。
9. 运行 `./gradlew build` 通过，成品 jar 出现在 `build/libs/`。

## 统一 `.gitignore` 模板

每个模组目录使用同一份，保证规则一致。模板原文见
[`docs/gitignore.example`](docs/gitignore.example)，复制到模组目录下改名为 `.gitignore`：

```gitignore
# Gradle
.gradle/
out/

# Build output, EXCEPT the final jar (kept on purpose for release)
build/*
!build/libs/
build/libs/*
!build/libs/*.jar

# IDE
.idea/
*.iml
.vscode/
.classpath
.project
.settings/

# Minecraft run directory
run/
runs/

# Third-party jars (compileOnly, not redistributed)
libs/*.jar
!libs/.gitkeep

# OS
Thumbs.db
.DS_Store
```

## 模组列表

| 目录 | 说明 | 运行时依赖 |
|---|---|---|
| [`baritone-ui`](baritone-ui/) | Baritone 可视化控制面板与状态 HUD | Kotlin for Forge、Baritone（均必需） |
| [`rotationspoof`](rotationspoof/) | 外发朝向伪装，改写移动包 yaw/pitch | Kotlin for Forge（必需） |

## 构建

```bash
cd <module>
./gradlew build
```

产物输出到 `build/libs/`。

## 参考环境

以下是当前各模组使用的版本，**仅供参考，不是硬性要求**。具体前置版本与兼容范围以各模组
`mods.toml` 和 `gradle.properties` 的声明为准，升级时自行调整。

| 项 | 当前使用 |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.4.x |
| Java | 21 |
| Kotlin for Forge | 4.11 |
| Gradle | 8.10.2 |
