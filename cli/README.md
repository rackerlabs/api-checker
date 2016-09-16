# Command Line Utilities

The following includes a number of utilities that are used for testing
and debugging API Checker.

The best way to use these utilities is to build the entire project:

````shell
mvn install
````

Then, on a unix-like system, simply add the ```bin``` directory to
your path.

Each utility compiles into a self contained jar with all external
dependencies.  This means that in a non-unix system you can simply
launch these using ```java -jar```.

For example you can launch wadl2checker by simply calling:

````shell
    java -jar cli/wadl2checker/target/wadl2checker*-with-dependencies.jar
````

There are a number of advantages to using the scripts in the ```bin```
directory however. The biggest advantage is
[nailgun](http://www.martiansoftware.com/nailgun/) integration which
significantly improves performance between runs.

# CLI Utilities

There are 3 CLI utilities:

1. wadl2checker
2. wadl2dot
3. wadltest

All utilities understand the ```--help``` argument which will display
detailed info regarding their usage.

There are two utilities related to nailgun:

1. nailgun-client
2. nailgun-server

These utilities are used by the scripts in ```bin``` directory to
speed up the performance of the cli utilities above. See nailgun
integration below for more details.

## wadl2checker

The ```wadl2checker``` utility can turn a WADL into checker format —
an internal format used by API Checker to represent the validation
rules that must be enforced.  Checker format is useful for debugging
API Checker, but it is also useful because checker format can be
loaded directly by API Checker.  For really large WADLs this has the
potential for significantly improving load time.  API Checker expects
checker format whenever it sees a file with the ```.checker```
extension.

It is important to note that checker format is an internal format, it
can change without warning between different versions API Checker —
even if those versions are deemed backwards compatible.  There is no
guarantee that the file will be interpreted correctly when versions
doesn't match exactly.  Because of this you should never treat a
checker file as an integration format (that's what WADL is for), if
you need to improve load performance treat checker format as an
internal cached representation, and make sure you regenerate the file
every time you upgrade API Checker. API Checker always displays a
warning when a checker file is loaded in a version it wasn't generated
from.


## wadl2dot

The ```wadl2dot``` utility is used to turn a WADL into a
[graphviz](http://www.graphviz.org/) dot file. The graph in the dot
file represents the internal state machine used by API Checker to
validate the API. This is useful for debugging. 

## wadltest

The ```wadltest``` utility launches API Checker in a simple server
application.  The source of ```wadltest``` serves as a great example
of how API Checker can be used.  Additionally, the tool can be used to
test how a WADL will be interpreted by API Checker.

When you run wadltest on a wadl, you'll see something like this:

````
 ╒═════════════════════════════════════════════════════════════════════════════════════════════════════════╕
 │ API Checker WADLTest 2.0.2                                                                              │
 ╞═════════════════════════════════════════════════════════════════════════════════════════════════════════╡
 │                                                                                                         │
 │ Running validator Test_Validator                                                                        │
 │                                                                                                         │
 │ Port: 9191                                                                                              │
 │ WADL Input: file:/Users/jorgew/projects/api-checker/core/src/test/resources/wadl/sharedXPath.wadl       │
 │ Dot File: /var/folders/lh/w4sgz94j1y3704_gl6h16b_c0000gq/T/chk4023450315132795486.dot                   │
 │                                                                                                         │
 │ The service should return a 200 response if the request                                                 │
 │ validates against the WADL, it will return a 4xx code                                                   │
 │ with an appropriate message otherwise.                                                                  │
 │                                                                                                         │
 │ You can pass an 'echoContent' query paramater to the                                                    │
 │ request to have the service echo the body of the request                                                │
 │ in the response.                                                                                        │
 │                                                                                                         │
 │ You can pass a 'respType' query paramater to the                                                        │
 │ request to set the ContentType of the response to the value                                             │
 │ of that parameter.                                                                                      │
 │                                                                                                         │
 ╘═════════════════════════════════════════════════════════════════════════════════════════════════════════╛
````

Notice that the service will return a 200 if an API request is
accepted by the WADL and an appropriate error message if it is not.

# Nailgun integration

If you launch the command line utilities above from the scripts in the
```bin``` directory, then nailgun will be used to improve the
performance of the utilities significantly between runs.

Nailgun works by standing up a server to handle CLI requests.  Runs of
the CLI utilities improve in performance because the JVM does not need
to be started between runs, internal data structures can be cached
between runs, and the Hotspot compiler is allowed to do its
optimization work.  This means that runs that otherwise take multiple
seconds to execute can be reduced to mere milliseconds.

Nailgun works by standing up a nailgun server which listens for
commands on a specific port (by default 2113).  There is also a
nailgun client: a small utility written in C which launches requests
to the nailgun server and is responsible for redirecting I/O,
environment variables, working directory, etc to give the appearance
that the CLI utility was launched locally.

The nailgun server is launched automatically when a cli utility is
run, and continues to run, in the background waiting for new CLI
requests, for a fixed length of time (by default 2 hours).  The server
will silently quit after the time expires.  The server will also quit
if its jar file is modified in any way.  This is used to handle the
case where API Checker is being debugged and therefore is
recompiled. Because of this a clean way to stop the server is to
simply :

````shell
touch cli/nailgun-server/target/*.jar
````

The following environment variables may be used to change how (or if)
nailgun will be used:

1. ```NO_CHECKER_NAILGUN``` : if set nailgun will never be used.  The
scripts in ```bin``` will simply use ```java -jar``` to launch the CLI
utilities.
2. ```CHECKER_NAILGUN_PORT``` : The port that the nailgun server will
listen to.  By default the port is 2113.
3. ```CHECKER_NAILGUN_HOST``` : The host to listen to.  By default
localhost.
4. ```CHECKER_NAILGUN_DURATION``` : The amount of time the server is
allowed to run before it is shutdown. The duration should be specified
in [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601) format. By
default the duration is ```PT2H``` (2 hours).

