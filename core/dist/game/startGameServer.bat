@echo off
title Eternity-World Game Server
:start
echo Starting Game Server.
echo.
REM -------------------------------------
REM Default parameters for a basic server.
java -Dfile.encoding=UTF-8 -Djava.util.logging.manager=l2e.util.L2LogManager -Xms1024m -Xmx1024m -cp ./../libs/*;EternityWorld.jar l2e.gameserver.GameServer
REM
REM If you have a big server and lots of memory, you could experiment for example with
REM java -server -Xmx1536m -Xms1024m -Xmn512m -XX:PermSize=256m -XX:SurvivorRatio=8 -Xnoclassgc -XX:+AggressiveOpts
REM -------------------------------------
if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end
:restart
echo.
echo Admin Restart ...
echo.
goto start
:error
echo.
echo Server terminated abnormally
echo.
:end
echo.
echo server terminated
echo.
pause