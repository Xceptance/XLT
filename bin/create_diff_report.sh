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
JAVA_OPTIONS="$JAVA_OPTIONS -Xmx4g"
JAVA_OPTIONS="$JAVA_OPTIONS -Dcom.xceptance.xlt.home=\"$XLT_HOME\""
JAVA_OPTIONS="$JAVA_OPTIONS -Dlog4j2.configurationFile=\"$XLT_CONFIG_DIR/diffreportgenerator.properties\""
JAVA_OPTIONS="$JAVA_OPTIONS -Djava.awt.headless=true"
#JAVA_OPTIONS="$JAVA_OPTIONS -agentlib:jdwp=transport=dt_socket,address=localhost:6666,server=y,suspend=n"
JAVA_OPTIONS="$JAVA_OPTIONS -cp \"$CLASSPATH\""

# run Java
CMD="java $JAVA_OPTIONS com.xceptance.xlt.report.diffreport.DiffReportGeneratorMain"
ARGS=""
I=1
while [ $I -le $# ]; do
    eval x=\${$I}
    ARGS="$ARGS \"$x\""
    I=$((I+1))
done
eval $CMD "$ARGS"
