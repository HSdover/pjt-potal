$ErrorActionPreference = "Stop"

$projectRoot = (Resolve-Path "$PSScriptRoot\..").Path
$backendDir = Join-Path $projectRoot "backend"
$frontendDir = Join-Path $projectRoot "frontend"
$offlineDir = Join-Path $projectRoot "offline"
$gradleHome = Join-Path $offlineDir "gradle-home"
$npmCache = Join-Path $offlineDir "npm-cache"
$gradleWrapper = Join-Path $backendDir "gradlew.bat"

. (Join-Path $PSScriptRoot "offline-build-tools.ps1")

New-Item -ItemType Directory -Force -Path $gradleHome | Out-Null
New-Item -ItemType Directory -Force -Path $npmCache | Out-Null

$npmCommand = Resolve-ProjectNpmCommand -OfflineDir $offlineDir
Assert-OfflinePath -Path $gradleWrapper -Message "Missing Gradle wrapper"

Write-Host "Preparing offline bundle..."
Write-Host "Project: $projectRoot"
Write-Host "npm command: $npmCommand"
Write-Host "Gradle user home: $gradleHome"
Write-Host "Gradle wrapper: $gradleWrapper"

Push-Location $frontendDir
try {
    Invoke-OfflineCommand $npmCommand ci --cache $npmCache --prefer-offline
    Invoke-OfflineCommand $npmCommand ci --offline --cache $npmCache
    Invoke-OfflineCommand $npmCommand run build
} finally {
    Pop-Location
}

Push-Location $backendDir
try {
    Invoke-OfflineCommand $gradleWrapper --gradle-user-home $gradleHome --no-daemon clean bootJar
    Invoke-OfflineCommand $gradleWrapper --offline --gradle-user-home $gradleHome --no-daemon bootJar
} finally {
    Pop-Location
}

Write-Host ""
Write-Host "Offline bundle is ready."
Write-Host "Move the whole project folder, including the offline directory, into the internal network."
