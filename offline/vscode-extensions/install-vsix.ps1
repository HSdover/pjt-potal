param(
    [string]$VsixDirectory = "$PSScriptRoot\vsix",
    [string]$CodeCommand = "code"
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path -LiteralPath $VsixDirectory)) {
    throw "VSIX directory not found: $VsixDirectory"
}

$packages = Get-ChildItem -LiteralPath $VsixDirectory -Filter "*.vsix" | Sort-Object Name
if ($packages.Count -eq 0) {
    throw "No VSIX files found in: $VsixDirectory"
}

foreach ($package in $packages) {
    Write-Host "Installing $($package.Name)"
    & $CodeCommand --install-extension $package.FullName --force
}

Write-Host "Installed $($packages.Count) extension package(s). Restart VS Code after installation."
