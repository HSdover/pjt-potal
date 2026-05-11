$ErrorActionPreference = "Stop"

$projectRoot = Resolve-Path "$PSScriptRoot\.."
$backendDir = Join-Path $projectRoot "backend"
$frontendDir = Join-Path $projectRoot "frontend"

Write-Host "Building frontend static files..."
Push-Location $frontendDir
try {
    npm.cmd ci
    npm.cmd run build
} finally {
    Pop-Location
}

Write-Host "Building Spring Boot executable JAR..."
Push-Location $backendDir
try {
    .\gradlew.bat clean bootJar
} finally {
    Pop-Location
}

Write-Host ""
Write-Host "Frontend output:"
Write-Host (Join-Path $frontendDir "dist")
Write-Host "Backend output:"
Write-Host (Join-Path $backendDir "build\libs\governance-portal-backend.jar")
