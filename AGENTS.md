# AI 协作准则

本文件是本仓库（Cloud Storage Helper / 梦幻之屿云仓辅助）中 AI 编码助手的强制工作准则。
规则优先级高于助手的默认习惯；与用户当次明确指令冲突时，以用户当次指令为准。第 1、2 条只有在用户明确要求修改本文件时才变更。

## 一、三条硬性规则

### 1. 游戏内测试由用户完成

- 不得自行启动游戏做验证；只有用户明确要求时才可启动 dev 客户端（`./gradlew runClient`），启动后由用户操作与判断。
- 不替用户判断手感、渲染效果、按键冲突，以及与服务器 `ci` 指令的实际交互；这些仍由用户完成。
- AI 可以且应当：编译（`./gradlew build`）、跑单元测试（`./gradlew test`）、静态检查；按用户要求启动 dev 客户端，并读取 `run/logs/latest.log` 分析。
- 改动必须进游戏才能确认时：说明原因，给交接单，等用户反馈；不得自行决定“先启动试试”。

游戏内测试交接单使用固定格式：

```text
变更：
验证步骤：
1. ...
预期结果：
需要重点观察：
失败时请提供：
- run/logs/latest.log 相关片段 / 截图 / 复现步骤
```

### 2. 启动游戏必须使用正版登录

- 连接线上服务器必须使用正版 Microsoft/Mojang 账号，包括 dev 客户端。
- 禁止：离线模式（offline）、盗版启动器、第三方验证绕过、用 `--username` 伪造账号、借用或共享账号。
- Loom 的 `runClient` 默认使用 FabricMC 离线开发账号，只能用于本地单人世界 / 开发调试，禁止用它连接线上服务器；需要正版验证时，请在 dev 环境配置正版登录，或用官方启动器的正版档案加载本模组。
- 环境无法正版登录时：停止并询问用户，不得改用离线方式“先跑起来”。
- 用户反馈登录问题时：引导其使用官方启动器的正版档案，不提供任何绕过方案。

### 3. 没有要求做的事情，先询问再执行

默认只做用户明确要求的事。以下操作一律先问，得到同意后再做：

- 新增 / 删除 / 重命名 / 移动文件，或“顺手”重构、格式化、改注释、改文案。
- 修改 `build.gradle`、`gradle.properties`、`fabric.mod.json`、依赖版本、`CONFIG_VERSION`。
- git 写操作：commit、push、branch、tag、reset、stash、rebase。
- 启动游戏或服务端、连接服务器、修改 `run/` 下的配置与存档。
- 联网下载、上传、发布、提交 Issue / PR。
- 修改 CI（`.github/`）、README、LICENSE。
- 跨包或大范围改动，或引入新的架构 / 模式。

询问时必须说明：做什么、为什么、涉及哪些文件、风险与回滚方式、备选方案。
一次同意只覆盖当次范围，范围扩大要重新问。
拿不准是否属于“已要求”时，按“未要求”处理。

可自行执行的最低限度动作（仅限为完成已要求任务）：读代码与搜索、修改本次任务直接相关的文件、`./gradlew build`、`./gradlew test`；用户要求时可启动 dev 客户端并读取日志。做完在汇报中列明。若用户要求更严格，以用户要求为准。

## 二、项目速览

客户端专用模组，从服务器聊天消息同步云仓库存，在 REI / JEI 中显示数量并支持快捷取出。

| 项 | 值 |
| --- | --- |
| Minecraft | 26.1.2 |
| Fabric Loader | 0.19.3+ |
| Fabric API | 0.155.2+26.1.2 |
| MaLiLib | 0.28.9 ~ 0.28.x |
| Java | 25 |
| 模组版本 / 组 | 1.0 / `cloudstoragerei` |
| 许可证 | CC0-1.0 |

分层与关键文件：

- `src/main/java/com/cloudstoragerei/core/`：纯逻辑，可单测，禁止引用 Minecraft 类。
  - `CloudStorageMessageParser.java`：解析聊天消息。
  - `CloudStorageMath.java`：潜影盒数量与取出数量钳制。
  - `CloudStorageCountFormatter.java`：数量的 k / w 缩写。
- `src/client/java/com/cloudstoragerei/`：客户端实现。
  - `CloudStorageModClient.java`、`CloudStorageInitHandler.java`：入口与初始化。
  - `core/CloudStorageManager.java`：库存状态、命令发送、乐观扣减。
  - `input/CloudStorageInteractionHandler.java`：准星 / 物品管理器命中与取出。
  - `gui/CustomAmountScreen.java`：自定义数量界面（含潜影盒模式）。
  - `render/CloudCountRenderer.java`：数量渲染。
  - `config/CloudStorageConfigs.java`：配置加载与保存（当前 `CONFIG_VERSION = 1`）。
  - `config/CloudStorageHotkeys.java`、`config/CloudStorageConfigGui.java`：MaLiLib 热键与配置界面。
- `src/client/java/com/cloudstoragerei/compat/`：可选模组适配，全部通过 `FabricLoader.isModLoaded` 门控。
- `src/client/resources/*.mixins.json`：REI / JEI 兼容 mixin，由 `plugin` 字段做加载检查，均为 `"required": false`。

数据流：

```text
服务器聊天消息
  -> ClientReceiveMessageEvents.ALLOW_GAME
  -> CloudStorageMessageParser 解析
  -> Map<itemId, count>（CloudStorageManager）
  -> REI / JEI 渲染 + 输入处理
  -> 发送 ci 指令（并乐观扣减本地数量）
```

服务器指令约定：

| 指令 | 用途 |
| --- | --- |
| `ci textlist` | 刷新云仓列表（聊天消息回传库存） |
| `ci get <itemPath> <amount>` | 取出指定数量 |
| `ci getbox <itemPath>` | 取一盒潜影盒；多盒按数量重复发送该指令，发送限速 5 条/秒（每 4 tick 一条），本地按 `27 × maxStackSize` 扣减 |
| `ci put` | 打开云仓存入界面 |

注意：消息解析依赖中文字符串 `你的云仓物品如下:` 与 `你的存档已切换至`，属于服务器约定，不要擅自改动。

## 三、构建与环境

- Windows：`.gradlew.bat build`、`.gradlew.bat test`。
- Linux / CI：`./gradlew build`。
- 产物位于 `build/libs/`。
- 可选依赖（JEI、REI、Litematica、Mod Menu）仅在编译期以 `compileOnly` 使用，不得打包进 jar。
- EMI 暂不支持，等待其发布官方 26.1.2 版本，不要自行实现。

## 四、代码约定

- 缩进使用 Tab，与现有文件保持一致。
- 不引入新依赖或新库，除非用户明确同意。
- 纯逻辑放 `main` 源集并配单测；Minecraft 相关代码放 `client`。
- 可选模组集成必须能在模组缺失时安全跳过（`isModLoaded` + mixin plugin + `required: false`）。
- 配置结构变更必须提升 `CONFIG_VERSION` 并编写迁移逻辑，旧配置文件保留为备份。
- 所有界面文案同时更新 `en_us.json` 与 `zh_cn.json`。
- 代码注释使用英文，与现有风格一致。
- 仅在用户授权提交时使用提交信息，沿用现有中文 Conventional Commits（如 `fix:`、`docs:`、`chore:`）。

## 五、汇报要求

- 列出改动的文件（相对路径，必要时带行号）。
- 列出执行过的命令与结果（构建 / 测试通过还是失败，失败给出关键报错）。
- 明确“未做”的事，尤其：未启动游戏、未做游戏内测试、未提交、未推送。
- 需要游戏内验证时，附上【游戏内测试交接单】。

## 六、执行前自检

1. 这件事是用户明确要求的吗？不是 → 先问。
2. 这个动作会启动游戏吗？用户明确要求启动 dev 客户端 → 可以启动，测试交给用户；连接线上服务器必须正版登录。
3. 改动范围是否超出要求？超出 → 先问。
