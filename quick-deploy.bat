@echo off
REM Batch-Version für einfache Verwendung ohne PowerShell-Einstellungen

echo ================================
echo CyberWarfare Auto-Deploy (Batch)
echo ================================
echo.

REM Maven-Pfad finden
set MAVEN_CMD=
if exist "mvnw.cmd" (
    set MAVEN_CMD=mvnw.cmd
    echo Verwende Maven Wrapper
) else if exist "C:\apache-maven-3.9.11\bin\mvn.cmd" (
    set MAVEN_CMD="C:\apache-maven-3.9.11\bin\mvn.cmd"
    echo Verwende Maven: C:\apache-maven-3.9.11\bin\mvn.cmd
) else if exist "C:\Program Files\Apache\maven\bin\mvn.cmd" (
    set MAVEN_CMD="C:\Program Files\Apache\maven\bin\mvn.cmd"
    echo Verwende Maven: C:\Program Files\Apache\maven\bin\mvn.cmd
) else (
    echo Fehler: Maven nicht gefunden!
    echo Bitte Maven installieren oder Maven Wrapper hinzufuegen
    pause
    exit /b 1
)

REM Build
echo.
echo [1/2] Building Plugin...
echo Fuehre aus: %MAVEN_CMD% clean package
call %MAVEN_CMD% clean package

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Fehler: Build fehlgeschlagen!
    pause  
    exit /b 1
)

echo Build erfolgreich!

REM Server stoppen (falls läuft)
echo.
echo [2/2] Stoppe Server falls laufend...
taskkill /F /IM java.exe >nul 2>&1
timeout /t 3 /nobreak >nul

REM Alte Plugin-Version entfernen
echo Entferne alte Plugin-Version...
if exist "C:\Users\lelew\Documents\MCServer\plugins\cyberwarfare*.jar" (
    del "C:\Users\lelew\Documents\MCServer\plugins\cyberwarfare*.jar"
)

REM Neues Plugin kopieren
echo Kopiere neues Plugin...
if not exist "target\cyberwarfare-plugin-1.0.0-SNAPSHOT.jar" (
    echo Fehler: Plugin JAR nicht gefunden in target/
    pause
    exit /b 1
)

copy "target\cyberwarfare-plugin-1.0.0-SNAPSHOT.jar" "C:\Users\lelew\Documents\MCServer\plugins\" >nul

if %ERRORLEVEL% NEQ 0 (
    echo Fehler beim Kopieren des Plugins!
    pause
    exit /b 1
)

echo Plugin erfolgreich kopiert!

REM Server starten (optional)
echo.
set /p RESTART="Server neu starten? (j/n): "
if /i "%RESTART%"=="j" (
    echo Starte Server...
    cd /d "C:\Users\lelew\Documents\MCServer"
    start "Minecraft Server" java -Xmx4G -Xms1G -jar server.jar nogui
    echo Server gestartet!
)

echo.
echo ================================
echo Deploy abgeschlossen!
echo ================================
echo.
echo Plugin: cyberwarfare-plugin-1.0.0-SNAPSHOT.jar  
echo Teste ingame: /cyber help
echo.
pause