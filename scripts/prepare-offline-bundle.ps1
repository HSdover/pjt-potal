$ErrorActionPreference = "Stop"

$projectRoot = Resolve-Path "$PSScriptRoot\.."
$backendDir = Join-Path $projectRoot "backend"
$frontendDir = Join-Path $projectRoot "frontend"
$offlineDir = Join-Path $projectRoot "offline"
$gradleHome = Join-Path $offlineDir "gradle-home"
$npmCache = Join-Path $offlineDir "npm-cache"
$localGradle = Join-Path $offlineDir "gradle\bin\gradle.bat"

New-Item -ItemType Directory -Force -Path $gradleHome | Out-Null
New-Item -ItemType Directory -Force -Path $npmCache | Out-Null

if (Test-Path $localGradle) {
    $gradleCommand = $localGradle
} else {
    $gradleCommand = "gradle"
}

Write-Host "Preparing offline bundle..."
Write-Host "Project: $projectRoot"
Write-Host "Gradle user home: $gradleHome"
Write-Host "Gradle command: $gradleCommand"

Push-Location $frontendDir
try {
    npm.cmd ci --cache $npmCache --prefer-offline
    npm.cmd run build
} finally {
    Pop-Location
}

Push-Location $backendDir
try {
    & $gradleCommand --gradle-user-home $gradleHome clean bootJar
} finally {
    Pop-Location
}

Write-Host ""
Write-Host "Offline bundle is ready."
Write-Host "Move the whole project folder, including the offline directory, into the internal network."
