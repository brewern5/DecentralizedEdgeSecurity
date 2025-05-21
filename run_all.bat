@echo off

setlocal

REM Base directory = where this script is located
set BASEDIR=%~dp0

REM Compile server
cd /d %BASEDIR%\Dev\Server
javac --release 17 Edge_Server\EdgeServer.java

REM Compile module
cd /d %BASEDIR%\Dev\Module
javac --release 17 Edge_Module\EdgeModule.java

REM Run both in separate terminals
cd /d %BASEDIR%\
start cmd /k "cd /d Dev\Server && java -cp . Edge_Server.EdgeServer"
start cmd /k "cd /d Dev\Module && java -cp . Edge_Module.EdgeModule"

endlocal