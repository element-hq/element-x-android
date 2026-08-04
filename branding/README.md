# Connect branding assets

Source logo: `branding/logo.png` (copied from project root `Logo.png`).

## Regenerating launcher icons

On Windows (requires .NET / System.Drawing):

```powershell
.\tools\branding\generate_icons.ps1 -SourcePath "..\..\..\Logo.png"
```

This writes adaptive-icon foreground and monochrome PNGs into `appicon/element/src/main/res/mipmap-*`.

## Asset layout

| Asset | Location |
|-------|----------|
| Source logo | `branding/logo.png` |
| Launcher foreground | `appicon/element/src/main/res/mipmap-*/ic_launcher_foreground.png` |
| Launcher monochrome | `appicon/element/src/main/res/mipmap-*/ic_launcher_monochrome.png` |
| Launcher background (release) | `appicon/element/src/release/res/drawable/ic_launcher_background.xml` (white) |
| Launcher background (debug) | `appicon/element/src/debug/res/drawable/ic_launcher_background.xml` (light blue) |
| Launcher background (nightly) | `appicon/element/src/nightly/res/drawable/ic_launcher_background.xml` (brand blue) |
| Splash icon | `app/src/main/res/drawable/ic_splash_logo.xml` |

## Notes

- Foreground is scaled to 72% of the 108dp adaptive-icon canvas (Android safe zone).
- Monochrome icons are white silhouettes for themed launcher icons.
- Replace `branding/logo.png` and re-run the script when the logo changes.
