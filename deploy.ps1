# CyberWarfare Plugin Auto-Deploy Script
# Stoppt den Server, kopiert das neue Plugin und startet den Server neu

param(
    [switch]$NoRestart,
    [switch]$CleanConfig,
    [string]$ServerPath = "C:\Users\lelew\Documents\MCServer",
    [string]$PluginPath = "C:\Users\lelew\Documents\MyPlugins\CyberWarfare"
)

# Farben für Output
$Green = "Green"
$Red = "Red"
$Yellow = "Yellow"
$Cyan = "Cyan"

Write-Host "================================" -ForegroundColor $Cyan
Write-Host "CyberWarfare Auto-Deploy Script" -ForegroundColor $Cyan
Write-Host "================================" -ForegroundColor $Cyan
Write-Host ""

# Pfade definieren
$ServerJar = Join-Path $ServerPath "server.jar"
$PluginsDir = Join-Path $ServerPath "plugins"
$OldPluginPattern = "cyberwarfare*.jar"
$NewPlugin = Join-Path $PluginPath "target\cyberwarfare-plugin-1.0.0-SNAPSHOT.jar"
$ConfigDir = Join-Path $PluginsDir "CyberWarfare"

# 1. Server-Status prüfen
Write-Host "[1/6] Prüfe Server-Status..." -ForegroundColor $Yellow

$ServerProcess = Get-Process -Name "java" -ErrorAction SilentlyContinue | Where-Object {
    $_.MainWindowTitle -like "*Minecraft*" -or 
    $_.CommandLine -like "*server.jar*" -or
    $_.WorkingSet -gt 100MB
}

if ($ServerProcess) {
    Write-Host "✓ Minecraft Server läuft (PID: $($ServerProcess.Id))" -ForegroundColor $Green
    
    # Server stoppen
    Write-Host "[2/6] Stoppe Minecraft Server..." -ForegroundColor $Yellow
    
    # Versuche graceful shutdown über RCON oder Konsole
    try {
        # Hier könntest du RCON verwenden, falls konfiguriert
        # Für jetzt: Prozess beenden
        $ServerProcess | Stop-Process -Force
        Start-Sleep -Seconds 3
        
        # Warten bis Prozess beendet ist
        $timeout = 30
        $counter = 0
        while ((Get-Process -Name "java" -ErrorAction SilentlyContinue) -and $counter -lt $timeout) {
            Start-Sleep -Seconds 1
            $counter++
        }
        
        if ($counter -lt $timeout) {
            Write-Host "✓ Server erfolgreich gestoppt" -ForegroundColor $Green
        } else {
            Write-Host "⚠ Server-Stop Timeout nach 30 Sekunden" -ForegroundColor $Yellow
        }
    } catch {
        Write-Host "✗ Fehler beim Stoppen des Servers: $($_.Exception.Message)" -ForegroundColor $Red
        exit 1
    }
} else {
    Write-Host "✓ Server läuft nicht" -ForegroundColor $Green
    Write-Host "[2/6] Server stoppen übersprungen" -ForegroundColor $Yellow
}

# 2. Neues Plugin prüfen
Write-Host "[3/6] Prüfe neues Plugin..." -ForegroundColor $Yellow

if (-not (Test-Path $NewPlugin)) {
    Write-Host "✗ Plugin nicht gefunden: $NewPlugin" -ForegroundColor $Red
    Write-Host "Bitte zuerst 'mvn clean package' ausführen!" -ForegroundColor $Red
    exit 1
}

$PluginSize = (Get-Item $NewPlugin).Length / 1MB
Write-Host "✓ Plugin gefunden: $([math]::Round($PluginSize, 1)) MB" -ForegroundColor $Green

# 3. Altes Plugin entfernen
Write-Host "[4/6] Entferne alte Plugin-Version..." -ForegroundColor $Yellow

$OldPlugins = Get-ChildItem -Path $PluginsDir -Filter $OldPluginPattern -ErrorAction SilentlyContinue

if ($OldPlugins) {
    foreach ($OldPlugin in $OldPlugins) {
        try {
            Remove-Item $OldPlugin.FullName -Force
            Write-Host "✓ Entfernt: $($OldPlugin.Name)" -ForegroundColor $Green
        } catch {
            Write-Host "✗ Fehler beim Entfernen: $($OldPlugin.Name)" -ForegroundColor $Red
        }
    }
} else {
    Write-Host "✓ Keine alte Plugin-Version gefunden" -ForegroundColor $Green
}

# 4. Konfiguration löschen (optional)
if ($CleanConfig -and (Test-Path $ConfigDir)) {
    Write-Host "[4.5/6] Lösche Plugin-Konfiguration..." -ForegroundColor $Yellow
    try {
        Remove-Item $ConfigDir -Recurse -Force
        Write-Host "✓ Konfiguration gelöscht (wird neu erstellt)" -ForegroundColor $Green
    } catch {
        Write-Host "⚠ Fehler beim Löschen der Konfiguration: $($_.Exception.Message)" -ForegroundColor $Yellow
    }
}

# 5. Neues Plugin kopieren
Write-Host "[5/6] Kopiere neues Plugin..." -ForegroundColor $Yellow

try {
    $DestinationPath = Join-Path $PluginsDir "cyberwarfare-plugin-1.0.0-SNAPSHOT.jar"
    Copy-Item $NewPlugin $DestinationPath -Force
    Write-Host "✓ Plugin erfolgreich kopiert" -ForegroundColor $Green
} catch {
    Write-Host "✗ Fehler beim Kopieren: $($_.Exception.Message)" -ForegroundColor $Red
    exit 1
}

# 6. Server starten (optional)
if (-not $NoRestart) {
    Write-Host "[6/6] Starte Minecraft Server..." -ForegroundColor $Yellow
    
    try {
        # Server im Hintergrund starten
        $ServerArgs = @(
            "-Xmx4G",
            "-Xms1G", 
            "-jar", 
            "server.jar",
            "nogui"
        )
        
        Start-Process -FilePath "java" -ArgumentList $ServerArgs -WorkingDirectory $ServerPath -WindowStyle Minimized
        
        Start-Sleep -Seconds 3
        
        # Prüfen ob Server gestartet ist
        $NewServerProcess = Get-Process -Name "java" -ErrorAction SilentlyContinue | Where-Object {
            $_.WorkingSet -gt 50MB
        }
        
        if ($NewServerProcess) {
            Write-Host "✓ Server erfolgreich gestartet (PID: $($NewServerProcess.Id))" -ForegroundColor $Green
        } else {
            Write-Host "⚠ Server-Start konnte nicht verifiziert werden" -ForegroundColor $Yellow
        }
        
    } catch {
        Write-Host "✗ Fehler beim Starten des Servers: $($_.Exception.Message)" -ForegroundColor $Red
        exit 1
    }
} else {
    Write-Host "[6/6] Server-Start übersprungen (-NoRestart)" -ForegroundColor $Yellow
}

Write-Host ""
Write-Host "================================" -ForegroundColor $Cyan
Write-Host "✓ Deployment erfolgreich!" -ForegroundColor $Green
Write-Host "================================" -ForegroundColor $Cyan
Write-Host ""
Write-Host "Plugin Informationen:" -ForegroundColor $Cyan
Write-Host "- Version: 1.0.0-SNAPSHOT" -ForegroundColor $Gray
Write-Host "- Größe: $([math]::Round($PluginSize, 1)) MB" -ForegroundColor $Gray
Write-Host "- Pfad: plugins\cyberwarfare-plugin-1.0.0-SNAPSHOT.jar" -ForegroundColor $Gray
Write-Host ""

if (-not $NoRestart) {
    Write-Host "Server Status:" -ForegroundColor $Cyan
    Write-Host "- Server läuft im Hintergrund" -ForegroundColor $Gray
    Write-Host "- Logge dich ein und teste: /cyber help" -ForegroundColor $Gray
    Write-Host ""
}

Write-Host "Nützliche Commands:" -ForegroundColor $Cyan
Write-Host "- .\deploy.ps1                    # Normales Deployment" -ForegroundColor $Gray
Write-Host "- .\deploy.ps1 -NoRestart         # Ohne Server-Neustart" -ForegroundColor $Gray
Write-Host "- .\deploy.ps1 -CleanConfig       # Mit Config-Reset" -ForegroundColor $Gray
Write-Host ""