#!/bin/sh

# setup basic paths 
CWD=`pwd`
cd "`dirname "$0"`/.."
XLT_HOME=`pwd`
cd "$CWD"
export XLT_HOME

if [ -z "$XLT_CONFIG_DIR" ]; then
    XLT_CONFIG_DIR=$XLT_HOME/config
    export XLT_CONFIG_DIR
fi

# setup Java class path
CLASSPATH="$XLT_HOME"/target/classes:"$XLT_HOME"/lib/*

# setup other Java options
JAVA_OPTIONS=
JAVA_OPTIONS="$JAVA_OPTIONS -Xmx1g"
#JAVA_OPTIONS="$JAVA_OPTIONS -Xmx4g"
#JAVA_OPTIONS="$JAVA_OPTIONS -XX:+UseG1GC -XX:+UseStringDeduplication"
JAVA_OPTIONS="$JAVA_OPTIONS -Dcom.xceptance.xlt.home=\"$XLT_HOME\""
JAVA_OPTIONS="$JAVA_OPTIONS -Dlog4j.configuration=\"file:$XLT_CONFIG_DIR/reportgenerator.properties\""
JAVA_OPTIONS="$JAVA_OPTIONS -Djava.awt.headless=true"
#JAVA_OPTIONS="$JAVA_OPTIONS -agentlib:jdwp=transport=dt_socket,address=localhost:6666,server=y,suspend=n"
#JAVA_OPTIONS="$JAVA_OPTIONS -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints -XX:FlightRecorderOptions=stackdepth=1024"
#JAVA_OPTIONS="$JAVA_OPTIONS -XX:+UnlockDiagnosticVMOptions -XX:+PrintCompilation -XX:+PrintInlining"
#JAVA_OPTIONS="$JAVA_OPTIONS -XX:MaxInlineSize=128 -XX:FreqInlineSize=1024"

JAVA_OPTIONS="$JAVA_OPTIONS -cp \"$CLASSPATH\""

# append options to suppress illegal access warnings for Java 9+
PACKAGES="java.base/java.lang.reflect java.base/java.text java.base/java.util java.desktop/java.awt.font"
for p in $PACKAGES; do JAVA_OPTIONS="$JAVA_OPTIONS --add-opens=$p=ALL-UNNAMED"; done
JAVA_OPTIONS="$JAVA_OPTIONS -XX:+IgnoreUnrecognizedVMOptions" 
#JAVA_OPTIONS="$JAVA_OPTIONS --illegal-access=debug"

# run Java
CMD="java $JAVA_OPTIONS com.xceptance.xlt.report.ReportGeneratorMain"
ARGS=""
I=1
while [ $I -le $# ]; do
    eval x=\${$I}
    ARGS="$ARGS \"$x\""
    I=$((I+1))
done
eval $CMD "$ARGS"
