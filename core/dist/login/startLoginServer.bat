@echo off
title Eternity-World Login Server
:start
echo Starting Login Server.
echo.
java -Xms128m -Xmx128m  -cp ./../libs/*;EWLogin.jar l2e.loginserver.L2LoginServer
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
