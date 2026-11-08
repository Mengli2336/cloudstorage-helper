# Cloud Storage Helper Roadmap

This roadmap lists practical follow-up work in recommended priority order.
None of these items is part of the current release unless explicitly marked.

## Completed

- Global crosshair quick-take for item entities and blocks.
- Max-stack quick-take for the Litematica schematic block under the crosshair,
  using Litematica's build-item mapping.
- REI recipe navigation fix that prevents the custom amount screen from
  replacing REI's previous container screen.

## Near term

- Show action-bar feedback when a cloud sync starts, succeeds, times out, or
  fails because the server does not expose the expected `ci` command.
- Add quantity presets to the custom amount screen, such as 1, 16, 64, one
  stack, and one shulker box.
- Detect overlapping MaLiLib hotkeys and warn when a cloud-storage action would
  also trigger a native REI or JEI command.
- Clamp requested quantities to the locally known cloud count and make the
  optimistic count rollback behavior explicit when a server command fails.

## Medium term

- Add a standalone cloud item browser with search, sorting, favorites, and a
  recent-items list, so no item-manager mod is required for browsing.
- Make command templates and chat parsing patterns configurable for servers
  using different cloud-storage plugins.
- Detect whether a connected server supports the expected commands before
  enabling automatic refresh and item actions.
- Integrate with Litematica material lists by showing required, inventory,
  cloud, and missing counts per item.
- Add a throttled one-click action for withdrawing the missing materials for a
  selected Litematica placement or material list.

## Longer term

- Keep independent caches and configuration profiles for multiple servers and
  dimensions.
- Add a safe batch-withdrawal queue with configurable command rate limiting,
  confirmation for large requests, and cancellation.
- Add first-class EMI support after EMI publishes an official Minecraft 26.1.2
  build.
- Expose a small item-manager adapter API so future manager mods can provide
  hovered items and rendering bounds without changing cloud-storage core code.
- Add optional MiniHUD/Tweakeroo integrations for cloud status, pinned items,
  and repeated material withdrawal workflows.
