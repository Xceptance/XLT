@echo off
setlocal enabledelayedexpansion

:: setup basic paths
set CWD=%CD%
cd /d %~dp0\..
set XLT_HOME=%CD%
cd /d %CWD%

if not defined XLT_CONFIG_DIR set XLT_CONFIG_DIR=%XLT_HOME%\config

:: setup Java class path
set CLASSPATH=%XLT_HOME%\target\classes;%XLT_HOME%\lib\*

:: setup other Java options
set JAVA_OPTIONS=
set JAVA_OPTIONS=%JAVA_OPTIONS% -Xmx1g
set JAVA_OPTIONS=%JAVA_OPTIONS% -Dcom.xceptance.xlt.home="%XLT_HOME%"
set JAVA_OPTIONS=%JAVA_OPTIONS% -Dlog4j.configuration="file:%XLT_CONFIG_DIR%\trendreportgenerator.properties"
rem set JAVA_OPTIONS=%JAVA_OPTIONS% -agentlib:jdwp=transport=dt_socket,address=localhost:6666,server=y,suspend=n
set JAVA_OPTIONS=%JAVA_OPTIONS% -cp "%CLASSPATH%"

:: run Java
java %JAVA_OPTIONS% com.xceptance.xlt.report.trendreport.TrendReportGeneratorMain %*
