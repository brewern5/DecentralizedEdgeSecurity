@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
for %%I in ("%SCRIPT_DIR%..\..") do set "ROOT_DIR=%%~fI"
set "CORE_DIR=%ROOT_DIR%\core"
set "CP=%CORE_DIR%\target\classes;%CORE_DIR%\target\dependency\*"

pushd "%ROOT_DIR%" >nul
call mvn -pl core -am clean package -DskipTests || goto :error
call mvn -pl core dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory="%CORE_DIR%\target\dependency" || goto :error
popd >nul

echo.
echo ===== Instance Configuration =====
if "%1"=="" (
    set /p SERVER_INSTANCE="Enter Server Instance ID (leave blank for default): "
) else (
    set "SERVER_INSTANCE=%1"
)

echo Starting EdgeCoordinator (default config)
if "%SERVER_INSTANCE%"=="" (
    echo Starting EdgeServer (default config)
) else (
    echo Starting EdgeServer (%SERVER_INSTANCE%)
)
echo Starting EdgeNode (node1)
echo Starting EdgeNode (node2)

echo.
start "EdgeCoordinator" cmd /k cd /d "%ROOT_DIR%" ^&^& java -cp "%CP%" components.coordinator.EdgeCoordinator
if "%SERVER_INSTANCE%"=="" (
    start "EdgeServer" cmd /k cd /d "%ROOT_DIR%" ^&^& java -cp "%CP%" components.server.EdgeServer
) else (
    start "EdgeServer" cmd /k cd /d "%ROOT_DIR%" ^&^& java -cp "%CP%" components.server.EdgeServer %SERVER_INSTANCE%
)
start "EdgeNode node1" cmd /k cd /d "%ROOT_DIR%" ^&^& java -cp "%CP%" components.node.EdgeNode node1
start "EdgeNode node2" cmd /k cd /d "%ROOT_DIR%" ^&^& java -cp "%CP%" components.node.EdgeNode node2

goto :eof

:error
echo Build or dependency copy failed.
exit /b 1

:eof
endlocal
