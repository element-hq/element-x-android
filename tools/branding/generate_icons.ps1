# Generates Connect launcher icon assets from a source logo PNG.
# Requires Windows PowerShell with .NET System.Drawing.
param(
    [string]$SourcePath = (Join-Path $PSScriptRoot "..\..\branding\logo.png"),
    [string]$OutputBase = (Join-Path $PSScriptRoot "..\..\appicon\element\src\main\res")
)

$ErrorActionPreference = 'Stop'
$densities = @{
    'mipmap-mdpi'    = 108
    'mipmap-hdpi'    = 162
    'mipmap-xhdpi'   = 216
    'mipmap-xxhdpi'  = 324
    'mipmap-xxxhdpi' = 432
}

Add-Type -AssemblyName System.Drawing

function New-MonochromeBitmap([System.Drawing.Bitmap]$src) {
    $mono = New-Object System.Drawing.Bitmap $src.Width, $src.Height, ([System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    for ($y = 0; $y -lt $src.Height; $y++) {
        for ($x = 0; $x -lt $src.Width; $x++) {
            $c = $src.GetPixel($x, $y)
            if ($c.A -lt 10) {
                $mono.SetPixel($x, $y, [System.Drawing.Color]::FromArgb(0, 0, 0, 0))
                continue
            }
            $brightness = ($c.R + $c.G + $c.B) / 3
            if ($brightness -lt 25) {
                $mono.SetPixel($x, $y, [System.Drawing.Color]::FromArgb(0, 0, 0, 0))
            } else {
                $mono.SetPixel($x, $y, [System.Drawing.Color]::FromArgb(255, 255, 255, 255))
            }
        }
    }
    return $mono
}

function Save-LauncherAsset(
    [System.Drawing.Bitmap]$source,
    [int]$size,
    [string]$path,
    [bool]$monochrome
) {
    $working = if ($monochrome) { New-MonochromeBitmap $source } else { $source }
    $bmp = New-Object System.Drawing.Bitmap $size, $size, ([System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.Clear([System.Drawing.Color]::FromArgb(0, 0, 0, 0))
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $logoSize = [int][Math]::Round($size * 0.72)
    $offset = [int][Math]::Round(($size - $logoSize) / 2.0)
    $g.DrawImage($working, $offset, $offset, $logoSize, $logoSize)
    $g.Dispose()
    $dir = Split-Path $path -Parent
    if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
    $bmp.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
    if ($monochrome) { $working.Dispose() }
}

if (-not (Test-Path $SourcePath)) {
    throw "Source logo not found: $SourcePath"
}

$source = [System.Drawing.Bitmap]::FromFile($SourcePath)
foreach ($entry in $densities.GetEnumerator()) {
    $folder = $entry.Key
    $size = $entry.Value
    Save-LauncherAsset $source $size (Join-Path $OutputBase "$folder\ic_launcher_foreground.png") $false
    Save-LauncherAsset $source $size (Join-Path $OutputBase "$folder\ic_launcher_monochrome.png") $true
    Write-Host "Generated $folder ($size px)"
}
$source.Dispose()
Write-Host "Done."
