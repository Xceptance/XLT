@echo off
setlocal enabledelayedexpansion

:: setup basic paths
set AGENT_HOME=%CD%
set AGENT_CONFIG_DIR=%AGENT_HOME%\config
cd /d %~dp0\..
set XLT_HOME=%CD%
cd /d %AGENT_HOME%

:: setup Java class path
set CP_PATCHES=%AGENT_HOME%\patches\classes;%AGENT_HOME%\patches\lib\*
set CP_XLT=%XLT_HOME%\target\classes;%XLT_HOME%\lib\*
set CP_STD=%AGENT_HOME%\classes;%AGENT_HOME%\lib\*
set CP_MVN=%AGENT_HOME%\target\classes;%AGENT_HOME%\target\test-classes;%AGENT_HOME%\target\dependency\*
set CP_GRD=%AGENT_HOME%\build\classes\java\main;%AGENT_HOME%\build\classes\kotlin\main;%AGENT_HOME%\build\resources\main;%AGENT_HOME%\build\classes\java\test;%AGENT_HOME%\build\classes\kotlin\test;%AGENT_HOME%\build\resources\test;%AGENT_HOME%\build\dependency\*
set CP_ECL=%AGENT_HOME%\bin
set CLASSPATH=%CP_PATCHES%;%CP_XLT%;%CP_STD%;%CP_MVN%;%CP_GRD%;%CP_ECL%

:: setup other Java options
set JAVA_OPTIONS=
rem set JAVA_OPTIONS=%JAVA_OPTIONS% -Djava.endorsed.dirs="%XLT_HOME%"
set JAVA_OPTIONS=%JAVA_OPTIONS% -Dcom.xceptance.xlt.home="%XLT_HOME%"
set JAVA_OPTIONS=%JAVA_OPTIONS% -Dcom.xceptance.xlt.agent.home="%AGENT_HOME%"
set JAVA_OPTIONS=%JAVA_OPTIONS% -Dlog4j2.configurationFile="%AGENT_CONFIG_DIR%\log4j2.properties","%AGENT_CONFIG_DIR%\log4j2.xml"
set JAVA_OPTIONS=%JAVA_OPTIONS% -Dorg.apache.xml.dtm.DTMManager=org.apache.xml.dtm.ref.DTMManagerDefault
rem set JAVA_OPTIONS=%JAVA_OPTIONS% -agentlib:jdwp=transport=dt_socket,address=localhost:6666,server=y,suspend=n
set JAVA_OPTIONS=%JAVA_OPTIONS% -cp "%CLASSPATH%"

:: append options to suppress illegal access warnings for Java 9+
set PACKAGES=java.base/java.lang java.base/java.lang.reflect java.base/java.net java.base/java.text java.base/java.util java.desktop/java.awt.font
for %%p in (%PACKAGES%) do set JAVA_OPTIONS=!JAVA_OPTIONS! --add-opens=%%p=ALL-UNNAMED
set JAVA_OPTIONS=%JAVA_OPTIONS% -XX:+IgnoreUnrecognizedVMOptions
rem set JAVA_OPTIONS=%JAVA_OPTIONS% --illegal-access=debug

:: append custom Java options
set JVM_CFG_FILE=%AGENT_CONFIG_DIR%\jvmargs.cfg

if exist "%JVM_CFG_FILE%" (
    for /f "eol=# delims=" %%o in ('type "%JVM_CFG_FILE%"') do set JAVA_OPTIONS=!JAVA_OPTIONS! %%o
)

:: run Java
echo java %JAVA_OPTIONS% com.xceptance.xlt.agent.Main %* > results\agentCmdLine
java %JAVA_OPTIONS% com.xceptance.xlt.agent.AgentMain %*
