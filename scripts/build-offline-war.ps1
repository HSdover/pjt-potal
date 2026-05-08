$ErrorActionPreference = "Stop"

$projectRoot = Resolve-Path "$PSScriptRoot\.."
$backendDir = Join-Path $projectRoot "backend"
$offlineDir = Join-Path $projectRoot "offline"
$gradleHome = Join-Path $offlineDir "gradle-home"
$localGradle = Join-Path $offlineDir "gradle\bin\gradle.bat"

if (!(Test-Path $gradleHome)) {
    throw "Missing offline Gradle cache: $gradleHome"
}

if (Test-Path $localGradle) {
    $gradleCommand = $localGradle
} else {
    $gradleCommand = "gradle"
}

Write-Host "Building WAR in offline mode..."
Write-Host "Project: $projectRoot"
Write-Host "Gradle user home: $gradleHome"
Write-Host "Gradle command: $gradleCommand"

Push-Location $backendDir
try {
    & $gradleCommand --offline --gradle-user-home $gradleHome bootWar
} finally {
    Pop-Location
}

Write-Host ""
Write-Host "WAR output:"
Write-Host (Join-Path $backendDir "build\libs\governance-portal-backend.war")
