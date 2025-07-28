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

REM Delete old class files from output directories
echo Deleting old class files from directories
del /S /Q %BASEDIR%\src\*.class > $null
echo Finished deleting old Class files.
pause

REM Compile Coordinator
echo Compiling Coordinator...
cd /d %BASEDIR%\src\main\java\coordinator
javac --release 17 -cp ".;%BASEDIR%\lib\*" edge_coordinator\EdgeCoordinator.java
echo Finished compiling Coordinator.

REM Compile Server
echo Compiling Server...
cd /d %BASEDIR%\src\main\java\server
javac --release 17 -cp ".;%BASEDIR%\lib\*" edge_server\EdgeServer.java
echo Finished compiling Server.

REM Compile Node
echo Compiling Node...
cd /d %BASEDIR%\src\main\java\node
javac --release 17 -cp ".;%BASEDIR%\lib\*" edge_node\EdgeNode.java
echo Finished compiling Node

echo Finished compiling files.
pause

REM Run all in separate terminals
cd /d %BASEDIR%\

start cmd /k "cd /d %BASEDIR% && java -cp target/classes;%BASEDIR%\lib\* edge_coordinator.EdgeCoordinator"
start cmd /k "cd /d %BASEDIR% && java -cp target/classes;%BASEDIR%\lib\* edge_server.EdgeServer"
start cmd /k "cd /d %BASEDIR% && java -cp target/classes;%BASEDIR%\lib\* edge_node.EdgeNode"

endlocal