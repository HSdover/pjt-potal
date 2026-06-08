param(
    [string]$ManifestPath = "$PSScriptRoot\extensions.json",
    [string]$OutputDirectory = "$PSScriptRoot\vsix"
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path -LiteralPath $ManifestPath)) {
    throw "Manifest not found: $ManifestPath"
}

New-Item -ItemType Directory -Force -Path $OutputDirectory | Out-Null

$extensions = Get-Content -LiteralPath $ManifestPath -Encoding UTF8 | ConvertFrom-Json

foreach ($extension in $extensions) {
    $parts = $extension.id -split "\.", 2
    if ($parts.Count -ne 2) {
        Write-Warning "Skipping invalid extension id: $($extension.id)"
        continue
    }

    $publisher = $parts[0]
    $name = $parts[1]
    $fileName = "$publisher.$name.vsix"
    $target = Join-Path $OutputDirectory $fileName
    $url = "https://marketplace.visualstudio.com/_apis/public/gallery/publishers/$publisher/vsextensions/$name/latest/vspackage"

    if ((Test-Path -LiteralPath $target) -and ((Get-Item -LiteralPath $target).Length -gt 0)) {
        Write-Host "Skipping $($extension.id); already downloaded."
        continue
    }

    if (Test-Path -LiteralPath $target) {
        Remove-Item -LiteralPath $target -Force
    }

    Write-Host "Downloading $($extension.id) -> $fileName"
    Invoke-WebRequest -Uri $url -OutFile $target -MaximumRedirection 10 -UseBasicParsing
}

Write-Host "Downloaded $($extensions.Count) extension package(s) to $OutputDirectory"
