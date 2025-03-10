@echo off
setlocal enabledelayedexpansion

:: go to app-server root directory
cd /d %~dp0..

:: create the log directory
if not exist logs mkdir logs

:: setup Java options
set JAVA_OPTIONS=%JAVA_OPTIONS% -Xmx512m

:: run Java
java --add-opens=java.base/java.lang=ALL-UNNAMED -jar start.jar %JAVA_OPTIONS%
