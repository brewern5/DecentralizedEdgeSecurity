@echo off

setlocal

REM Base directory = where this script is located
set BASEDIR=%~dp0

REM Clean and compile all code for maven
echo Cleaning and compiling Maven project...
call mvn clean compile -e
echo Finished compiling Maven project

REM Use Maven to copy dependencies into dev\lib folder
echo Copying Maven dependencies.
call mvn dependency:copy-dependencies -DoutputDirectory=%BASEDIR%\lib
echo Finished maven dependencies commands.
pause

REM All compilation handled by Maven above
echo Maven compilation complete - all classes ready in target/classes
pause

REM Run all in separate terminals
cd /d %BASEDIR%\

start cmd /k "cd /d %BASEDIR% && java -cp target/classes;%BASEDIR%\lib\* coordinator.edge_coordinator.EdgeCoordinator"
start cmd /k "cd /d %BASEDIR% && java -cp target/classes;%BASEDIR%\lib\* server.edge_server.EdgeServer"
start cmd /k "cd /d %BASEDIR% && java -cp target/classes;%BASEDIR%\lib\* node.edge_node.EdgeNode"

endlocal