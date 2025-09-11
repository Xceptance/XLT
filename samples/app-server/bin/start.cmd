@echo off
setlocal enabledelayedexpansion

:: go to app-server root directory
cd /d %~dp0..

:: create the log directory
if not exist logs mkdir logs

:: setup Java options
set JAVA_OPTIONS=%JAVA_OPTIONS% -Xmx512m
set JAVA_OPTIONS=%JAVA_OPTIONS% --add-opens=java.base/java.lang=ALL-UNNAMED

:: run Java
java %JAVA_OPTIONS% -jar start.jar
