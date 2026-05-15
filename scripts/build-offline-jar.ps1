$ErrorActionPreference = "Stop"

$projectRoot = (Resolve-Path "$PSScriptRoot\..").Path
$backendDir = Join-Path $projectRoot "backend"
$frontendDir = Join-Path $projectRoot "frontend"
$offlineDir = Join-Path $projectRoot "offline"
$gradleHome = Join-Path $offlineDir "gradle-home"
$npmCache = Join-Path $offlineDir "npm-cache"
$gradleWrapper = Join-Path $backendDir "gradlew.bat"

. (Join-Path $PSScriptRoot "offline-build-tools.ps1")

$npmCommand = Resolve-ProjectNpmCommand -OfflineDir $offlineDir -RequireLocal
Assert-OfflinePath -Path $gradleWrapper -Message "Missing Gradle wrapper"
Assert-OfflinePath -Path $gradleHome -Message "Missing offline Gradle cache"
Assert-OfflinePath -Path (Join-Path $gradleHome "wrapper\dists") -Message "Missing cached Gradle wrapper distribution"
Assert-OfflinePath -Path $npmCache -Message "Missing offline npm cache"
Assert-OfflinePath -Path (Join-Path $npmCache "_cacache") -Message "Missing npm content cache"

Write-Host "Building frontend static files and Spring Boot JAR in offline mode..."
Write-Host "Project: $projectRoot"
Write-Host "npm command: $npmCommand"
Write-Host "Gradle user home: $gradleHome"
Write-Host "Gradle wrapper: $gradleWrapper"

Push-Location $frontendDir
try {
    Invoke-OfflineCommand $npmCommand ci --offline --cache $npmCache
    Invoke-OfflineCommand $npmCommand run build
} finally {
    Pop-Location
}

Push-Location $backendDir
try {
    Invoke-OfflineCommand $gradleWrapper --offline --gradle-user-home $gradleHome --no-daemon bootJar
} finally {
    Pop-Location
}

Write-Host ""
Write-Host "Frontend output:"
Write-Host (Join-Path $frontendDir "dist")
Write-Host "Backend output:"
Write-Host (Join-Path $backendDir "build\libs\governance-portal-backend.jar")
