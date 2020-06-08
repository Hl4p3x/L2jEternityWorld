@echo off
title Register Game Server
java -Djava.util.logging.config.file=console.ini -cp ./../libs/*;EWLogin.jar l2e.tools.gsregistering.BaseGameServerRegister -c
pause