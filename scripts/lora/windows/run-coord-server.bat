@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
for %%I in ("%SCRIPT_DIR%..\..\..") do set "ROOT_DIR=%%~fI"
set "CORE_DIR=%ROOT_DIR%\core"
set "LORA_DIR=%ROOT_DIR%\lora-simulation"
set "CP=%CORE_DIR%\target\classes;%LORA_DIR%\target\classes;%CORE_DIR%\target\dependency\*;%LORA_DIR%\target\dependency\*"
set "COMMON_JAVA_OPTS=-Ddes.transport.profile=lora -Dtransport.mode=LORA -Dlora.config.path=%ROOT_DIR%\config\lora\lora_simulation\loraConfig.properties"

pushd "%ROOT_DIR%" >nul
call mvn -DskipTests clean package || goto :error
call mvn -pl core dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory="%CORE_DIR%\target\dependency" || goto :error
popd >nul

echo.
echo ===== Instance Configuration =====
if "%1"=="" (
    set /p SERVER_INSTANCE="Enter Server Instance ID (leave blank for default): "
) else (
    set "SERVER_INSTANCE=%1"
)

echo Starting EdgeCoordinator [LORA profile]
if "%SERVER_INSTANCE%"=="" (
    echo Starting EdgeServer (default config)
) else (
    echo Starting EdgeServer (%SERVER_INSTANCE%)
)

echo.
start "EdgeCoordinator" cmd /k cd /d "%ROOT_DIR%" ^&^& java %COMMON_JAVA_OPTS% -cp "%CP%" components.coordinator.EdgeCoordinator
if "%SERVER_INSTANCE%"=="" (
    start "EdgeServer" cmd /k cd /d "%ROOT_DIR%" ^&^& java %COMMON_JAVA_OPTS% -cp "%CP%" components.server.EdgeServer
) else (
    start "EdgeServer" cmd /k cd /d "%ROOT_DIR%" ^&^& java %COMMON_JAVA_OPTS% -cp "%CP%" components.server.EdgeServer %SERVER_INSTANCE%
)

goto :eof

:error
echo Build or dependency copy failed.
exit /b 1

:eof
endlocal
