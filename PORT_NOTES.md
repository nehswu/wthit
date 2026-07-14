# Port notes — Minecraft 26.3-snapshot-3 (mc/26.3-snapshot-3)

**This is a provisional working document, not a changelog entry.** 26.3 is
still in snapshot and its APIs are not final — anything here may need to
change again before an official 26.3 release. Do not fold this into
`CHANGELOG.txt` until porting against a stable 26.3 release, at which point
revisit these notes rather than starting from scratch.

Branched from `mc/26.2` (verified, see `CHANGELOG.txt` 20.0.0 and
`KNOWN_ISSUES_26.2.txt`). Everything logged there carries forward
unchanged unless noted otherwise below.

## Changes specific to this snapshot

**RenderPipeline relocation** — confirmed via `jar tf` on the actual merged
26.3-snapshot-3 client jar (not assumed): `RenderPipeline` moved from
`com.mojang.blaze3d.pipeline` to `com.mojang.renderpearl.api.pipeline`.
Updated the import in:
- `src/api/java/.../component/SpriteBarComponent.java`
- `src/main/java/.../gui/hud/ComponentRenderer.java`
- `src/pluginCore/java/.../theme/GradientTheme.java`
- `src/pluginCore/java/.../theme/NinePatchTheme.java`

This is part of an ongoing Order-Independent Transparency rendering rework
this cycle — worth re-checking on every subsequent snapshot, since the
package could move again before 26.3 stabilizes.

**ReloadableServerResources constructor signature change** — confirmed via
the Mixin injection failure's own crash-log bytecode descriptor.
`LayeredRegistryAccess<?>` + `HolderLookup.Provider` collapsed into a
single `net.minecraft.server.ReloadableServerRegistries.LoadResult`
parameter. Updated `ReloadableServerResourcesMixin.java` accordingly.

## Platform availability (as of this snapshot)

- **Fabric**: available and verified (build + runClient + visual checklist).
- **Textile**: available and compiles clean, no changes needed.
- **Forge**: disabled in `settings.gradle.kts`. Forge does not build against
  Vanilla snapshots as a matter of policy — not expected to become available
  until an official 26.3 release.
- **NeoForge**: disabled in `settings.gradle.kts`. No published snapshot
  build was available as of this port. NeoForge does sometimes publish
  snapshot builds on a best-effort basis — worth re-checking on future
  snapshots before assuming this stays disabled.

## Known issues

Carries forward everything in `KNOWN_ISSUES_26.2.txt` (shears tier-logic
bug, Forge coverage regression — though Forge itself is disabled here so
this is moot until Forge returns, Leaf Litter stale-mixin-list gap, tab
styling cosmetic difference). Nothing new identified specific to this
snapshot beyond the two items above.

## Verification status

Fabric build passed the full visual checklist, human-confirmed (title
screen, new world, settings tabs, core tooltips, boss bar). Tagged
`26.3-snapshot-3-verified`.

## Re-basing note for future snapshots

If porting to a later `26.3-snapshot-N`, branch fresh from `mc/26.2`
again rather than continuing this branch, per the project's branch
strategy — this keeps `git diff mc/26.2 mc/26.3-snapshot-N` clean and
chronologically accurate. Use this file as a starting reference for what
changed last time, but re-verify each item against the new snapshot's
actual decompiled/jar source rather than assuming it still applies.