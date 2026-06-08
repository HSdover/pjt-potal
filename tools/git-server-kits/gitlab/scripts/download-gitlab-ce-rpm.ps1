param(
    [ValidateSet(8, 9)]
    [int] $OracleLinuxMajor = 9,
    [string] $Version = "18.6.6",
    [string] $OutputDir = ""
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$kitDir = Resolve-Path (Join-Path $scriptDir "..")

if ([string]::IsNullOrWhiteSpace($OutputDir)) {
    $OutputDir = Join-Path $kitDir "downloads"
}

New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null

$fileName = "gitlab-ce-$Version-ce.0.el$OracleLinuxMajor.x86_64.rpm"
$url = "https://packages.gitlab.com/gitlab/gitlab-ce/packages/ol/$OracleLinuxMajor/$fileName/download.rpm"
$outFile = Join-Path $OutputDir $fileName

Write-Host "Downloading $url"
curl.exe -L $url -o $outFile

Write-Host "Downloaded: $outFile"
Get-FileHash $outFile -Algorithm SHA256
