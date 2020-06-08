@echo off
title SQL Account Manager
java -Djava.util.logging.config.file=console.ini -cp ./../libs/*;EWLogin.jar l2e.tools.accountmanager.SQLAccountManager
if %errorlevel% == 0 (
echo.
echo Execution successful
echo.
) else (
echo.
echo An error has occurred while running the L2J Account Manager!
echo.
echo Possible reasons for this to happen:
echo.
echo - Missing .jar files or ../libs directory.
echo - MySQL server not running or incorrect MySQL settings:
echo    check ./config/loginserver.properties
echo - Wrong data types or values out of range were provided:
echo    specify correct values for each required field
echo.
)
pause