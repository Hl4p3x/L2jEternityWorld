#!/bin/sh
java -Djava.util.logging.config.file=console.ini -cp ./../libs/*:EWLogin.jar l2e.tools.accountmanager.SQLAccountManager
