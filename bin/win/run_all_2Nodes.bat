@echo off

setlocal

REM Base directory = where this script is located
set "BASEDIR=%~dp0"
for %%I in ("%BASEDIR%..\..") do set "ROOTDIR=%%~fI"
set "BUILD_CLASSES=%ROOTDIR%\.mvn-build\my-project\target\classes"
set "RUNTIME_CP=%BUILD_CLASSES%;%ROOTDIR%\lib\*"

if not exist "%ROOTDIR%\pom.xml" (
    echo ERROR: pom.xml not found at "%ROOTDIR%\pom.xml"
    pause
    exit /b 1
)

pushd "%ROOTDIR%"

REM Clean and compile all code for maven
echo Cleaning and compiling Maven project...
call mvn -f "%ROOTDIR%\pom.xml" clean compile -e
echo Finished compiling Maven project

REM Use Maven to copy dependencies into dev\lib folder
echo Copying Maven dependencies.
call mvn -f "%ROOTDIR%\pom.xml" dependency:copy-dependencies -DoutputDirectory="%ROOTDIR%\lib"
echo Finished maven dependencies commands.
pause

REM All compilation handled by Maven above
echo Maven compilation complete - all classes ready in %BUILD_CLASSES%
pause

REM Get instance ID from user input or command line arguments
echo.
echo ===== Instance Configuration =====
echo This script will start 1 coordinator, 1 server, and 2 nodes (node1 and node2).
echo You can specify an instance ID for the server component.
echo Leave blank to use default server configuration.
echo.

REM Use command line arguments if provided, otherwise prompt for input
if "%1"=="" (
    set /p SERVER_INSTANCE="Enter Server Instance ID (e.g., server1, server2, or leave blank for default): "
) else (
    set SERVER_INSTANCE=%1
    echo Using Server Instance ID from command line: %SERVER_INSTANCE%
)

echo.
if "%SERVER_INSTANCE%"=="" (
    echo Starting EdgeServer with default configuration
) else (
    echo Starting EdgeServer with instance ID: %SERVER_INSTANCE%
)

echo Starting EdgeNode with instance ID: node1
echo Starting EdgeNode with instance ID: node2
echo Starting EdgeCoordinator with default configuration
echo.
pause

start cmd /k "cd /d %ROOTDIR% && java -cp %RUNTIME_CP% components.coordinator.EdgeCoordinator"

if "%SERVER_INSTANCE%"=="" (
    start cmd /k "cd /d %ROOTDIR% && java -cp %RUNTIME_CP% components.server.EdgeServer"
) else (
    start cmd /k "cd /d %ROOTDIR% && java -cp %RUNTIME_CP% components.server.EdgeServer %SERVER_INSTANCE%"
)

REM Start two nodes with specific IDs
start cmd /k "cd /d %ROOTDIR% && java -cp %RUNTIME_CP% components.node.EdgeNode node1"
start cmd /k "cd /d %ROOTDIR% && java -cp %RUNTIME_CP% components.node.EdgeNode node2"

popd
endlocal