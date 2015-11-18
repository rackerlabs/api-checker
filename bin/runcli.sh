##
##  Runs the cli script $PROG
##

if [ ! $PROG ] ; then
    echo 'Missing $PROG environment variable. You should not call runcli.sh directly.'
    exit 1
fi

if [ ! $JAVA ] ; then
    JAVA=$(which java)
fi

command -v $JAVA >/dev/null 2>&1 || { echo $PROG ' requires $JAVA enviornment variable or java in the system path!'; exit 1; }

VERSION=$($JAVA -version 2>&1 | grep ' version' | awk '{ print substr($3, 2, length($3)-2); }')
JAVA_MINOR=$(echo $VERSION | tr "." " " | cut -d " " -f2)

if [ "$JAVA_MINOR" -lt "7" ]; then
  echo "$PROG requires a Java version of at least 7 to function."
  echo "Please install a JRE 1.7 or greater."
  exit 1
fi

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"

WTESTJAR=$DIR/cli/$PROG/target/$PROG*-with-*.jar

if [ -e $WTESTJAR ]; then
    $JAVA $JAVA_OPTS -jar $WTESTJAR "$@"
else
    echo "Couldn't find $WTESTJAR "
    echo -n "Perhaps the project has not been built, "
    echo "try building the project with mvn install."
fi
