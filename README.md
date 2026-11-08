# 梦幻之屿云仓辅助

Cloud Storage Helper 是面向 Minecraft 26.1.2 / Fabric 的梦幻之屿服务器云仓辅助模组。
它通过服务器的 `ci` 指令同步云仓数据，让玩家不必反复打开云仓界面，就能直接查看库存并取出物品。

## 功能

- 在 REI、JEI 的物品列表和书签中显示云仓数量。
- 在物品管理器中快捷取出 1 个、一组或自定义数量。
- 支持直接取出物品或潜影盒，并自动限制为云仓实际可用数量。
- `Shift + 中键` 对准世界中的方块、掉落物或 Litematica 投影，可直接快速取出对应物品。
- 使用 MaLiLib 管理配置和热键，所有按键均可重新绑定。
- 兼容 REI 配方界面导航，返回时不会卡在配方页和数量输入页之间。
- 提供云仓数据刷新、打开存入界面以及显示或隐藏云仓返回消息等配置。

## Requirements

- Minecraft 26.1.2
- Fabric Loader 0.19.3+
- Fabric API 0.155.2+26.1.2
- MaLiLib 0.28.9 至 0.28.x
- 可选：REI 26.1.819+
- 可选：JEI 29.37.0+

## Item manager support

| Item manager | Status |
| --- | --- |
| REI | Supported |
| JEI | Supported |
| EMI | Unavailable until EMI publishes an official Minecraft 26.1.2 build |

## Default controls

- `V`: refresh cloud storage data
- `B`: open the cloud storage input command
- `A`, then `Y`: open the MaLiLib configuration screen
- `Shift + left click`: take one item from an item manager entry
- `Shift + right click`: take one stack from a JEI or REI item list
- `Shift + middle click` in the world: quick-take the item entity, block, or Litematica schematic block under the crosshair
- `Middle click` in a JEI or REI item list: open the custom amount screen

All controls can be changed in the MaLiLib configuration screen.
The take one, take stack, and custom amount actions are complete MaLiLib
hotkeys, so they may use keyboard/mouse combinations and replace the old
modifier plus mouse-button settings.

Crosshair quick-take always requests the target item's maximum stack size and
falls back to the locally known cloud count when less than one stack remains.
Quick-take is intentionally disabled while a GUI is open; existing
modifier-click actions remain available for manager entries.

## Configuration

Settings are stored in `config/cloudstorage-helper.json`. On first launch,
values are imported from `config/cloudstorage.json`, or from the older Cloth
Config file `config/cloudstorage-rei.json` when necessary. Existing files are
kept as backups.

The mod ID is now `cloudstorage-helper`. Remove any older `cloudstorage-rei`
jar before installing this version so the same behavior is not loaded twice.

REI recipe screens are supported by the custom amount UI. Its return state is
isolated from REI's navigation stack, so Escape returns to the recipe and REI's
back action can return to the original container screen.

See [ROADMAP.md](ROADMAP.md) for planned features and their priority.

## License

CC0-1.0
