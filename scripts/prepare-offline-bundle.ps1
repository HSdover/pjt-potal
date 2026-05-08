$ErrorActionPreference = "Stop"

$projectRoot = Resolve-Path "$PSScriptRoot\.."
$backendDir = Join-Path $projectRoot "backend"
$offlineDir = Join-Path $projectRoot "offline"
$gradleHome = Join-Path $offlineDir "gradle-home"
$localGradle = Join-Path $offlineDir "gradle\bin\gradle.bat"

New-Item -ItemType Directory -Force -Path $gradleHome | Out-Null

if (Test-Path $localGradle) {
    $gradleCommand = $localGradle
} else {
    $gradleCommand = "gradle"
}

Write-Host "Preparing offline bundle..."
Write-Host "Project: $projectRoot"
Write-Host "Gradle user home: $gradleHome"
Write-Host "Gradle command: $gradleCommand"

Push-Location $backendDir
try {
    & $gradleCommand --gradle-user-home $gradleHome clean bootWar
} finally {
    Pop-Location
}

Write-Host ""
Write-Host "Offline bundle is ready."
Write-Host "Move the whole project folder, including the offline directory, into the internal network."
