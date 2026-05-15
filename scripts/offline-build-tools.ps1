$ErrorActionPreference = "Stop"

function Resolve-ProjectNpmCommand {
    param(
        [Parameter(Mandatory = $true)]
        [string]$OfflineDir,

        [switch]$RequireLocal
    )

    $nodeRoot = Join-Path $OfflineDir "nodejs"

    if (Test-Path -LiteralPath $nodeRoot) {
        $localNodeDirs = Get-ChildItem -LiteralPath $nodeRoot -Directory -ErrorAction SilentlyContinue |
            Where-Object { Test-Path -LiteralPath (Join-Path $_.FullName "npm.cmd") } |
            Sort-Object Name -Descending

        if ($localNodeDirs.Count -gt 0) {
            return Join-Path $localNodeDirs[0].FullName "npm.cmd"
        }
    }

    if ($RequireLocal) {
        throw "Missing project-local npm. Extract the Windows Node.js distribution under offline\nodejs before running an offline build."
    }

    $systemNpm = Get-Command "npm.cmd" -ErrorAction SilentlyContinue
    if ($systemNpm) {
        return $systemNpm.Source
    }

    throw "npm.cmd was not found. Install Node.js/npm or extract Node.js under offline\nodejs."
}

function Assert-OfflinePath {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path,

        [Parameter(Mandatory = $true)]
        [string]$Message
    )

    if (!(Test-Path -LiteralPath $Path)) {
        throw "${Message}: $Path"
    }
}

function Invoke-OfflineCommand {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Command,

        [Parameter(ValueFromRemainingArguments = $true)]
        [string[]]$Arguments
    )

    & $Command @Arguments
    if ($LASTEXITCODE -ne 0) {
        $displayCommand = @($Command) + $Arguments
        throw "Command failed with exit code ${LASTEXITCODE}: $($displayCommand -join ' ')"
    }
}
