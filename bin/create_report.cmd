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
set JAVA_OPTIONS=%JAVA_OPTIONS% -XX:+UseStringDeduplication
set JAVA_OPTIONS=%JAVA_OPTIONS% -Dcom.xceptance.xlt.home="%XLT_HOME%"
set JAVA_OPTIONS=%JAVA_OPTIONS% -Dlog4j2.configurationFile="%XLT_CONFIG_DIR%\reportgenerator.properties"
rem set JAVA_OPTIONS=%JAVA_OPTIONS% -agentlib:jdwp=transport=dt_socket,address=localhost:6666,server=y,suspend=n
set JAVA_OPTIONS=%JAVA_OPTIONS% -cp "%CLASSPATH%"

:: append options to suppress illegal access warnings for Java 9+
set PACKAGES=java.base/java.lang.reflect java.base/java.text java.base/java.util java.desktop/java.awt.font
for %%p in (%PACKAGES%) do set JAVA_OPTIONS=!JAVA_OPTIONS! --add-opens=%%p=ALL-UNNAMED
set JAVA_OPTIONS=%JAVA_OPTIONS% -XX:+IgnoreUnrecognizedVMOptions
rem set JAVA_OPTIONS=%JAVA_OPTIONS% --illegal-access=debug

:: run Java
java %JAVA_OPTIONS% com.xceptance.xlt.report.ReportGeneratorMain %*
