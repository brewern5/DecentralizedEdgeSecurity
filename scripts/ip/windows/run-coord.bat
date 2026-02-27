@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
for %%I in ("%SCRIPT_DIR%..\..\..") do set "ROOT_DIR=%%~fI"
set "CORE_DIR=%ROOT_DIR%\core"
set "CP=%CORE_DIR%\target\classes;%CORE_DIR%\target\dependency\*"
set "COMMON_JAVA_OPTS=-Ddes.transport.profile=ip -Dtransport.mode=IP"

pushd "%ROOT_DIR%" >nul
call mvn -pl core -am clean package -DskipTests || goto :error
call mvn -pl core dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory="%CORE_DIR%\target\dependency" || goto :error
popd >nul

echo Starting EdgeCoordinator [IP profile]
start "EdgeCoordinator" cmd /k cd /d "%ROOT_DIR%" ^&^& java %COMMON_JAVA_OPTS% -cp "%CP%" components.coordinator.EdgeCoordinator

goto :eof

:error
echo Build or dependency copy failed.
exit /b 1

:eof
endlocal
