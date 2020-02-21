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
JAVA_OPTIONS="$JAVA_OPTIONS -Dcom.xceptance.xlt.home=\"$XLT_HOME\""
JAVA_OPTIONS="$JAVA_OPTIONS -Dlog4j.configuration=\"file:$XLT_CONFIG_DIR/mastercontroller.properties\""
JAVA_OPTIONS="$JAVA_OPTIONS -Djava.awt.headless=true"
#JAVA_OPTIONS="$JAVA_OPTIONS -agentlib:jdwp=transport=dt_socket,address=localhost:6666,server=y,suspend=n"
JAVA_OPTIONS="$JAVA_OPTIONS -cp \"$CLASSPATH\""

# fix #35 (in case we run the embedded agent controller)
JAVA_OPTIONS="$JAVA_OPTIONS -Djava.security.egd=file:/dev/./urandom"

# append options to suppress illegal access warnings for Java 9+
PACKAGES="java.base/java.lang java.base/java.lang.reflect java.base/java.text java.base/java.util java.desktop/java.awt.font"
for p in $PACKAGES; do JAVA_OPTIONS="$JAVA_OPTIONS --add-opens=$p=ALL-UNNAMED"; done
JAVA_OPTIONS="$JAVA_OPTIONS -XX:+IgnoreUnrecognizedVMOptions" 
#JAVA_OPTIONS="$JAVA_OPTIONS --illegal-access=debug"

# run Java
CMD="java $JAVA_OPTIONS com.xceptance.xlt.mastercontroller.Main"
ARGS=""
I=1
while [ $I -le $# ]; do
    eval x=\${$I}
    ARGS="$ARGS \"$x\""
    I=$((I+1))
done
eval $CMD "$ARGS"
