# 梦幻之屿云仓辅助

**Cloud Storage Helper** 是面向 Minecraft 26.1.2 / Fabric 的梦幻之屿服务器云仓辅助模组。
它通过服务器的 `ci` 指令同步云仓数据，让你不必反复打开云仓界面，就能直接查看库存并取出物品。

## 功能

- 在 REI、JEI 的物品列表和书签中直接显示云仓数量。
- 支持取出 1 个、一组或自定义数量，取出数量会自动限制为云仓实际可用数量。
- 支持普通物品与潜影盒。
- 在世界中 `Shift + 中键` 对准方块、掉落物或 Litematica 投影，即可快速取出对应物品。
- 配置与热键基于 MaLiLib，所有按键均可重新绑定。
- 兼容 REI 配方界面导航，返回时不会卡在配方页与数量输入页之间。
- 提供云仓数据刷新、打开存入界面、显示或隐藏云仓返回消息等配置项。

## 环境要求

| 依赖 | 版本 |
| --- | --- |
| Minecraft | 26.1.2 |
| Fabric Loader | 0.19.3+ |
| Fabric API | 0.155.2+26.1.2 |
| MaLiLib | 0.28.9 ~ 0.28.x |
| Java | 25+ |

以下为可选依赖：

| 依赖 | 版本 |
| --- | --- |
| REI | 26.1.819+ |
| JEI | 29.37.0+ |
| Litematica | 0.27.14+ |
| Mod Menu | 任意版本 |

## 安装

1. 为 Minecraft 26.1.2 安装 Fabric Loader。
2. 将本模组与 MaLiLib 放入 `mods` 文件夹。
3. 按需安装 REI、JEI、Litematica 或 Mod Menu。
4. 若曾安装旧版 `cloudstorage-rei`，请先删除对应 jar，避免同一功能被加载两次。

## 物品管理器支持

| 物品管理器 | 状态 |
| --- | --- |
| REI | 支持 |
| JEI | 支持 |
| EMI | 暂不支持，等待 EMI 发布官方 Minecraft 26.1.2 版本 |

## 默认按键

| 按键 | 功能 |
| --- | --- |
| `V` | 刷新云仓数据 |
| `B` | 打开云仓存入指令 |
| `A` → `Y` | 打开 MaLiLib 配置界面 |
| `Shift + 左键` | 从物品管理器条目取出 1 个 |
| `Shift + 右键` | 从 JEI 或 REI 物品列表取出一组 |
| `Shift + 中键`（世界中） | 快速取出准星所指的物品实体、方块或 Litematica 投影方块 |
| `中键`（JEI 或 REI 列表） | 打开自定义数量界面 |

所有按键均可在 MaLiLib 配置界面中修改。取 1 个、取一组、自定义数量都是完整的 MaLiLib 热键，因此可以使用键盘与鼠标组合，替代旧版的「修饰键 + 鼠标键」设置。

准星快速取出始终按目标物品的最大堆叠数请求；当云仓剩余不足一组时，回退为本地已知的云仓数量。GUI 打开时快速取出会被禁用，物品管理器条目的修饰键点击仍然可用。

## 配置

配置文件位于 `config/cloudstorage-helper.json`。首次启动时会从 `config/cloudstorage.json` 导入设置；若不存在，则尝试读取旧版 Cloth Config 配置 `config/cloudstorage-rei.json`。原有配置文件会保留为备份。

REI 配方界面由自定义数量界面提供支持：其返回状态与 REI 的导航栈相互隔离，按 Escape 返回配方，使用 REI 的后退操作可回到原来的容器界面。

## 从源码构建

需要 JDK 25：

```bash
./gradlew build
```

构建产物位于 `build/libs/`。推送到 `main` 或提交 PR 时，GitHub Actions 会自动构建并上传产物。

## 许可证

本项目采用 CC0-1.0 许可证，详见 [LICENSE](LICENSE)。
