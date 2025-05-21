@echo off

REM Compile server
cd /d f:\Github\DecentralizedEdgeSecurity\Dev\Server
javac Edge_Server\EdgeServer.java

REM Compile module
cd /d f:\Github\DecentralizedEdgeSecurity\Dev\Module
javac Edge_Module\EdgeModule.java

REM Run both in separate terminals
cd /d f:\Github\DecentralizedEdgeSecurity
start cmd /k "cd /d Dev\Server && java -cp . Edge_Server.EdgeServer"
start cmd /k "cd /d Dev\Module && java -cp . Edge_Module.EdgeModule"