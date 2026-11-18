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
set JAVA_OPTIONS=%JAVA_OPTIONS% -Xmx4g
set JAVA_OPTIONS=%JAVA_OPTIONS% -Dcom.xceptance.xlt.home="%XLT_HOME%"
set JAVA_OPTIONS=%JAVA_OPTIONS% -Dlogback.configurationFile="%XLT_CONFIG_DIR%\trendreportgenerator-logback.xml"
rem set JAVA_OPTIONS=%JAVA_OPTIONS% -agentlib:jdwp=transport=dt_socket,address=localhost:6666,server=y,suspend=n
set JAVA_OPTIONS=%JAVA_OPTIONS% -cp "%CLASSPATH%"

:: workaround for Java4Shell issue (GH#730)
set JAVA_OPTIONS=%JAVA_OPTIONS% -XX:-UseBASE64Intrinsics

:: run Java
java %JAVA_OPTIONS% com.xceptance.xlt.report.trendreport.TrendReportGeneratorMain %*
