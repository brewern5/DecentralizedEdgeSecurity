@echo off

setlocal

REM Base directory = where this script is located
set BASEDIR=%~dp0

REM Use Maven to copy dependencies into dev\lib folder
call mvn dependency:copy-dependencies -DoutputDirectory=%BASEDIR%\dev\lib
echo Finished maven dependencies commands.
pause

REM Delete old class files from output directories
del /S /Q %BASEDIR%\dev\*.class > $null

echo Finished deleting old Class files.
pause

REM Compile Coordinator
cd /d %BASEDIR%\dev\coordinator
javac --release 17 -cp ".;%BASEDIR%\dev\lib\*" edge_coordinator\EdgeCoordinator.java

REM Compile Server
cd /d %BASEDIR%\dev\server
javac --release 17 -cp ".;%BASEDIR%\dev\lib\*" edge_server\EdgeServer.java

REM Compile Node
cd /d %BASEDIR%\dev\node
javac --release 17 -cp ".;%BASEDIR%\dev\lib\*" edge_node\EdgeNode.java
echo Finished compiling files.
pause

REM Run all in separate terminals
cd /d %BASEDIR%\

start cmd /k "cd /d dev\coordinator && java -cp .;%BASEDIR%\dev\lib\* edge_coordinator.EdgeCoordinator"
start cmd /k "cd /d dev\server && java -cp .;%BASEDIR%\dev\lib\* edge_server.EdgeServer"
start cmd /k "cd /d dev\node && java -cp .;%BASEDIR%\dev\lib\* edge_node.EdgeNode"

endlocal