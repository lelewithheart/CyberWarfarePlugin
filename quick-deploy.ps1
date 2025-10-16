# Quick Deploy Script - Build & Deploy in einem Schritt
# Verwendet Maven Wrapper falls verfügbar, sonst vollständigen Maven-Pfad

param(
    [switch]$NoRestart,
    [switch]$CleanConfig,
    [string]$ServerPath = "C:\Users\lelew\Documents\MCServer",
    [string]$PluginPath = "C:\Users\lelew\Documents\MyPlugins\CyberWarfare"
)

$Green = "Green"
$Red = "Red" 
$Yellow = "Yellow"
$Cyan = "Cyan"

Write-Host "================================" -ForegroundColor $Cyan
Write-Host "Quick Build & Deploy" -ForegroundColor $Cyan  
Write-Host "================================" -ForegroundColor $Cyan
Write-Host ""

# Maven finden
$MavenCmd = $null

# 1. Maven Wrapper prüfen
if (Test-Path ".\mvnw.cmd") {
    $MavenCmd = ".\mvnw.cmd"
    Write-Host "✓ Verwende Maven Wrapper" -ForegroundColor $Green
} 
# 2. Globales Maven prüfen
elseif (Get-Command mvn -ErrorAction SilentlyContinue) {
    $MavenCmd = "mvn"  
    Write-Host "✓ Verwende globales Maven" -ForegroundColor $Green
}
# 3. Häufige Maven-Installationspfade prüfen
else {
    $PossiblePaths = @(
        "${env:MAVEN_HOME}\bin\mvn.cmd",
        "${env:M2_HOME}\bin\mvn.cmd", 
        "C:\Program Files\Apache\maven\bin\mvn.cmd",
        "C:\apache-maven*\bin\mvn.cmd"
    )
    
    foreach ($Path in $PossiblePaths) {
        if (Test-Path $Path) {
            $MavenCmd = $Path
            Write-Host "✓ Maven gefunden: $Path" -ForegroundColor $Green
            break
        }
    }
}

if (-not $MavenCmd) {
    Write-Host "✗ Maven nicht gefunden!" -ForegroundColor $Red
    Write-Host ""
    Write-Host "Lösungen:" -ForegroundColor $Yellow
    Write-Host "1. Maven installieren: https://maven.apache.org/download.cgi" -ForegroundColor $Gray
    Write-Host "2. Maven Wrapper hinzufügen: mvn wrapper:wrapper" -ForegroundColor $Gray
    Write-Host "3. Manuell builden und dann .\deploy.ps1 ausführen" -ForegroundColor $Gray
    exit 1
}

# Build ausführen
Write-Host "[1/2] Building Plugin..." -ForegroundColor $Yellow
Write-Host "Führe aus: $MavenCmd clean package" -ForegroundColor $Gray

try {
    $BuildResult = & $MavenCmd clean package
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Build erfolgreich!" -ForegroundColor $Green
    } else {
        Write-Host "✗ Build fehlgeschlagen!" -ForegroundColor $Red
        Write-Host $BuildResult -ForegroundColor $Gray
        exit 1
    }
} catch {
    Write-Host "✗ Build-Fehler: $($_.Exception.Message)" -ForegroundColor $Red
    exit 1
}

# Deploy ausführen  
Write-Host ""
Write-Host "[2/2] Deploying Plugin..." -ForegroundColor $Yellow

$DeployArgs = @()
if ($NoRestart) { $DeployArgs += "-NoRestart" }
if ($CleanConfig) { $DeployArgs += "-CleanConfig" } 
$DeployArgs += "-ServerPath", $ServerPath
$DeployArgs += "-PluginPath", $PluginPath

try {
    & ".\deploy.ps1" @DeployArgs
} catch {
    Write-Host "✗ Deploy-Fehler: $($_.Exception.Message)" -ForegroundColor $Red
    exit 1
}

Write-Host ""
Write-Host "🎉 Build & Deploy abgeschlossen!" -ForegroundColor $Green