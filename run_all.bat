@echo off

setlocal

REM Base directory = where this script is located
set BASEDIR=%~dp0

REM Path to Gson.jar
set GSON_JAR=%BASEDIR%\dev\lib\gson-2.13.1.jar

REM Delete old class files from output directories
del /S /Q %BASEDIR%\dev\*.class > $null

REM Compile Coordinator
cd /d %BASEDIR%\dev\coordinator
javac --release 17 -cp ".;%GSON_JAR%" edge_coordinator\EdgeCoordinator.java

REM Compile Server
cd /d %BASEDIR%\dev\server
javac --release 17 -cp ".;%GSON_JAR%" edge_server\EdgeServer.java

REM Compile Node
cd /d %BASEDIR%\dev\node
javac --release 17 -cp ".;%GSON_JAR%" edge_node\EdgeNode.java
pause

REM Run all in separate terminals
cd /d %BASEDIR%\

start cmd /k "cd /d dev\coordinator && java -cp .;%GSON_JAR% edge_coordinator.EdgeCoordinator"
start cmd /k "cd /d dev\server && java -cp .;%GSON_JAR% edge_server.EdgeServer"
start cmd /k "cd /d dev\node && java -cp .;%GSON_JAR% edge_node.EdgeNode"

endlocal