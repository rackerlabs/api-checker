# Breaking Changes #

## Breaking Changes 1.x.x → 2.x.x ##

The following are the breaking changes you may encounter between 1.x.x
and 2.x.x of api-checker.

### Repeating Attribute now Honored by Header Parameters ###

The ```repeating``` attribute is now now honored by api-checker for
header parameters. This means that if you want a header to accept
multiple values you must set ```repeating="true"```, the default is
false which means that, by default, the header should expect 1 and
only 1 value.

Note that a repeating header values in a request can be specified in
a number of ways…

1. By passing a comma separated string of values:

   ````
       X-TEST-HEADER:  value1, value2, value3
   ````

1. By specifying multiple headers with the same name:

   ````
       X-TEST-HEADER: value1
       X-TEST-HEADER: value2
       X-TEST-HEADER: value3
   ````

1. Or by combining both approaches

   ````
      X-TEST-HEADER: value1, value2
      X-TEST-HEADER: value3
   ````
   
#### Example WADL ####

````xml
   <application xmlns="http://wadl.dev.java.net/2009/02"
                xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <resources base="https://test.api.openstack.com">
        <resource path="/a/b">
            <param name="X-TEST" style="header" required="true" type="xsd:int"/>
            <param name="X-TEST2" style="header" repeating="true"
                   required="true" type="xsd:int"/>
            <param name="X-TEST3" style="header" repeating="false"
                   required="true" type="xsd:int"/>
            <method name="GET"/>
        </resource>
      </resources>
   </application>
````

#### Changes in Behavior ####

In 1.x.x, all headers were considered repeating, the ```repeating```
attribute is ignored. This means that the following would validate.

````
    X-TEST:  1, 2, 3
    X-TEST2: 4, 5, 6
    X-TEST3: 7
    X-TEST3: 8
````

In 2.x.x the request would fail for two reasons:

1. ```X-TEST``` would not validate because the header is not repeating
   (remember the default is that ```repeating='false'```) and the string
   ```"1, 2, 3"``` does not validate according to the type of the header
   value which is ```xsd:int```. The comma is not a special character for
   non-repeating headers so api-checker looks at the whole content of the
   header value when validating the header.
   
   This is the correct behavior, many non-repeating headers in HTTP
   use a comma within a single header value. The ```User-Agent```
   header is a good example.
   
1. ```X-TEST3``` would not validate because ```repeating='false'```
   and there are two X-TEST3 headers.


### All Header Values Must Now Match All Header Types ###

#### Example WADL ####

````xml
   <application xmlns="http://wadl.dev.java.net/2009/02"
                xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <resources base="https://test.api.openstack.com">
        <resource path="/a/b">
            <param name="X-TEST" style="header" repeating="true"
                   required="true" type="xsd:int"/>
            <param name="X-TEST" style="header" repeating="true"
                   required="true" type="xsd:date"/>
            <param name="X-TEST" style="header" repeating="true"
                   required="true" type="xsd:string" fixed="foo"/>
            <method name="GET"/>
        </resource>
      </resources>
   </application>
````

In the example above X-TEST is a repeating header who's values may be
an integer, a date, or the string ```"foo"```

### Changes in Behavior ###

In 1.x.x, as long as any header value matched any of the types, the
header would pass validation.  For example:

````
    X-TEST: 7, baz, biz
````

would pass because ```7``` validates against ```xsd:int```.  In 2.x.x,
the header above would fail because values ```baz``` and ```biz``` do
not match against any of the header types.  However, the following
header would pass validation, because all header values match.

````
   X-TEST: 7, 2001-01-01, foo
````

You can use the new ```rax:anyMatch``` extension to revert to the old
1.x.x behavior.

````xml
   <application xmlns="http://wadl.dev.java.net/2009/02"
                xmlns:rax="http://docs.rackspace.com/api"
                xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <resources base="https://test.api.openstack.com">
        <resource path="/a/b">
            <param name="X-TEST" style="header" repeating="true"
                   required="true" type="xsd:int" rax:anyMatch="true"/>
            <param name="X-TEST" style="header" repeating="true"
                   required="true" type="xsd:date" rax:anyMatch="true"/>
            <param name="X-TEST" style="header" repeating="true"
                   required="true" type="xsd:string" fixed="foo"
                   rax:anyMatch="true"/>
            <method name="GET"/>
        </resource>
      </resources>
   </application>
````

Given the WADL above, 1.x.x and 2.x.x would exhibit the same
behavior.  Note that ```rax:anyMatch``` must be applied to all header
parameters.  If it's only applied to some of them the behavior is
different.   For example:

````xml
   <application xmlns="http://wadl.dev.java.net/2009/02"
                xmlns:rax="http://docs.rackspace.com/api"
                xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <resources base="https://test.api.openstack.com">
        <resource path="/a/b">
            <param name="X-TEST" style="header" repeating="true"
                   required="true" type="xsd:int" rax:anyMatch="true"/>
            <param name="X-TEST" style="header" repeating="true"
                   required="true" type="xsd:date"/>
            <param name="X-TEST" style="header" repeating="true"
                   required="true" type="xsd:string" fixed="foo"/>
            <method name="GET"/>
        </resource>
      </resources>
   </application>
````

In 2.x.x the following would validate correctly:

````
    X-TEST: 7, baz, biz
````

because the X-TEST header with an ```xsd:int``` matching would be
treated as an anyMatch.  However the following would fail…


````
    X-TEST: 2001-01-01, baz, biz
````

…because a value matches ```xsd:date``` but ```baz``` and ```biz``` do
not also match ```xsd:date``` or the value ```"foo"```.

### Changes in Behavior on rax:captureHeader, rax:message, rax:code ###

Not specifying the same values for ```rax:captureHeader```,
```rax:message```, and ```rax:code``` with header parameters of the
same name produced undefined behavior in 1.x.x and continues to
produce undefined behavior in 2.x.x. That said, the actual behavior
between 1.x.x and 2.x.x is now slightly different and it's best not to
rely on it.

For example:

````xml
   <application xmlns="http://wadl.dev.java.net/2009/02"
                xmlns:rax="http://docs.rackspace.com/api"
                xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <resources base="https://test.api.openstack.com">
        <resource path="/a/b">
            <param name="X-TEST" style="header" repeating="true"
                   required="true" type="xsd:int" rax:code="400"/>
            <param name="X-TEST" style="header" repeating="true"
                   required="true" type="xsd:date" rax:code="401"/>
            <param name="X-TEST" style="header" repeating="true"
                   required="true" type="xsd:string" fixed="foo"
                   rax:code="402"/>
            <method name="GET"/>
        </resource>
      </resources>
   </application>
````

Would produce produce an error code of 400, 401, or 402. Which error
code actually gets returned depends on how api-checker is configured
and what the input value is.  It's best to be consistent in the values
of these extensions as below.

````xml
   <application xmlns="http://wadl.dev.java.net/2009/02"
                xmlns:rax="http://docs.rackspace.com/api"
                xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <resources base="https://test.api.openstack.com">
        <resource path="/a/b">
            <param name="X-TEST" style="header" repeating="true"
                   required="true" type="xsd:int" rax:code="401"/>
            <param name="X-TEST" style="header" repeating="true"
                   required="true" type="xsd:date" rax:code="401"/>
            <param name="X-TEST" style="header" repeating="true"
                   required="true" type="xsd:string" fixed="foo"
                   rax:code="401"/>
            <method name="GET"/>
        </resource>
      </resources>
   </application>
````

In the above case, you'll get an error code of 401 regardless of how
api-checker is configured.
