$ErrorActionPreference = "Stop"

$projectRoot = Resolve-Path "$PSScriptRoot\.."
$backendDir = Join-Path $projectRoot "backend"
$frontendDir = Join-Path $projectRoot "frontend"
$offlineDir = Join-Path $projectRoot "offline"
$gradleHome = Join-Path $offlineDir "gradle-home"
$npmCache = Join-Path $offlineDir "npm-cache"
$localGradle = Join-Path $offlineDir "gradle\bin\gradle.bat"

if (!(Test-Path $gradleHome)) {
    throw "Missing offline Gradle cache: $gradleHome"
}

if (!(Test-Path $npmCache)) {
    throw "Missing offline npm cache: $npmCache"
}

if (Test-Path $localGradle) {
    $gradleCommand = $localGradle
} else {
    $gradleCommand = "gradle"
}

Write-Host "Building frontend static files and Spring Boot JAR in offline mode..."
Write-Host "Project: $projectRoot"
Write-Host "Gradle user home: $gradleHome"
Write-Host "Gradle command: $gradleCommand"

Push-Location $frontendDir
try {
    npm.cmd ci --offline --cache $npmCache
    npm.cmd run build
} finally {
    Pop-Location
}

Push-Location $backendDir
try {
    & $gradleCommand --offline --gradle-user-home $gradleHome bootJar
} finally {
    Pop-Location
}

Write-Host ""
Write-Host "Frontend output:"
Write-Host (Join-Path $frontendDir "dist")
Write-Host "Backend output:"
Write-Host (Join-Path $backendDir "build\libs\governance-portal-backend.jar")
