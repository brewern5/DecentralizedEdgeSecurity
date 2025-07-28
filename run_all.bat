@echo off

setlocal

REM Base directory = where this script is located
set BASEDIR=%~dp0

REM Use Maven to copy dependencies into dev\lib folder
call mvn dependency:copy-dependencies -DoutputDirectory=%BASEDIR%\lib
echo Finished maven dependencies commands.
pause

REM Delete old class files from output directories
del /S /Q %BASEDIR%\src\*.class > $null

echo Finished deleting old Class files.
pause

REM Compile Coordinator
cd /d %BASEDIR%\src\main\java\coordinator
javac --release 17 -cp ".;%BASEDIR%\lib\*" edge_coordinator\EdgeCoordinator.java

REM Compile Server
cd /d %BASEDIR%\src\main\java\server
javac --release 17 -cp ".;%BASEDIR%\lib\*" edge_server\EdgeServer.java

REM Compile Node
cd /d %BASEDIR%\src\main\java\node
javac --release 17 -cp ".;%BASEDIR%\lib\*" edge_node\EdgeNode.java
echo Finished compiling files.
pause

REM Run all in separate terminals
cd /d %BASEDIR%\

start cmd /k "cd /d src\main\java\coordinator && java -cp .;%BASEDIR%\lib\* edge_coordinator.EdgeCoordinator"
start cmd /k "cd /d src\main\java\server && java -cp .;%BASEDIR%\lib\* edge_server.EdgeServer"
start cmd /k "cd /d src\main\java\node && java -cp .;%BASEDIR%\lib\* edge_node.EdgeNode"

endlocal