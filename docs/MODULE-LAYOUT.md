# 目录规范

本仓库为多人协作，所有模组目录**必须**遵循同一套结构。新增模组请对照本文与根
[README](../README.md) 的检查清单。

## 标准结构

```
<module>/
├── README.md                               # 必需
├── src/
│   └── main/
│       ├── kotlin/com/<作者>/<module>/     # Kotlin 源码
│       ├── java/com/<作者>/<module>/       # 仅 Mixin 类，无则省略
│       └── resources/
│           ├── META-INF/mods.toml
│           ├── <module>.mixins.json        # 仅有 Mixin 时需要
│           ├── pack.mcmeta
│           └── assets/<module>/lang/
│               ├── en_us.json
│               └── zh_cn.json
├── build/libs/<module>-<version>+mc1.20.1.jar
├── gradle/wrapper/
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
└── .gitignore
```

## 约束

### 命名

- **目录名** = `mods.toml` 里的 `modId`，小写，使用连字符或下划线，二者不混用。
- **jar 名** = `<modId>-<version>+mc<mc版本>.jar`，由 Gradle 生成，不手工改名。
- **包名** = `com.<作者>.<module>`，与目录名一一对应。

### 源码位置

- 业务代码放 `src/main/kotlin/`。
- **Mixin 类必须放 `src/main/java/`**。Mixin 的注解处理器与 refmap 生成对 Java 源
  可靠，对 Kotlin 源不稳，混放会导致运行时找不到注入点。
- 资源统一放 `src/main/resources/`，不另建目录。

### 模组 README

每个模组目录必须有自己的 `README.md`，内容限于该模组：做什么、功能或原理、热键、
依赖、构建方式与产物路径。**不要**重复根 README 的通用规范。

### 语言文件

- `en_us.json` 与 `zh_cn.json` 必须同时存在且 key 集合一致。
- key 统一以模组 ID 前缀，例如 `baritoneui.title`、`baritoneui.button.stop`。
- 热键与分类 key 放 `key.*` 与 `key.categories.*`。

### 成品 jar

- `build/libs/<module>-*.jar` **提交进版本库**，方便他人直接取用。
- `build/` 下的其余产物（中间类、混淆缓存等）不提交。

### 第三方依赖

- 第三方 jar 放 `libs/`，且 `libs/*.jar` **加入 `.gitignore`**，不在仓库内分发。
- 需要的人按 README 说明自行放置。

## 禁止

- 根目录散落 jar、源码、日志、临时脚本。
- 提交 `.gradle/`、`run/`、`out/`、`.idea/`、`*.iml`。
- 提交含 token、RCON 口令、API key 的文件。
- 修改别人模组的目录结构。

## 检查命令

提交前自查：

```bash
cd <module>
git status --porcelain --ignored=matching
./gradlew build
```

`git status` 里不应出现 `build/` 下的中间产物；`./gradlew build` 应成功且产出 jar。
