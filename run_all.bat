@echo off

setlocal

REM Base directory = where this script is located
set BASEDIR=%~dp0

REM Compile Coordinator
cd /d %BASEDIR%\dev\coordinator
javac --release 17 edge_coordinator\EdgeCoordinator.java

REM Compile Server
cd /d %BASEDIR%\dev\server
javac --release 17 edge_server\EdgeServer.java

REM Compile Node
cd /d %BASEDIR%\dev\node
javac --release 17 edge_node\EdgeNode.java
pause

REM Run all in separate terminals
cd /d %BASEDIR%\

start cmd /k "cd /d dev\coordinator && java -cp . edge_coordinator.EdgeCoordinator"
start cmd /k "cd /d dev\server && java -cp . edge_server.EdgeServer"
start cmd /k "cd /d dev\node && java -cp . edge_node.EdgeNode"

endlocal