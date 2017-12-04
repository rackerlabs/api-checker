# Releases #
## In Progress Work ##
1. Added support for ```rax:representation```, this works like ```wadl:representation``` in that it can make assertions about XML, JSON representations. With ```rax:representation```, the representation may be embedded in another representation (JSON in XML), or in a header.

## Release 2.5.1 (2017-10-24) ##
1. Fixed a bug where the removeDups opt sometimes created checkers with missing states.

## Release 2.5.0 (2017-09-25) ##
1. Fixed a bug where method labels were sometimes lost when the rax:roles was used.
1. Clean up : better handling of error states in optimization stages.
1. New configuration option ```preserveMethodLabels``` ensures that method labels are always kept in the state machine.

## Release 2.4.1 (2017-09-01) ##
1. Fixed a regression where the remove dups optimization was always dropping step labels.

## Release 2.4.0 (2017-08-28) ##
1. Updated Dependencies
   1. saxon: 9.7.0-15 → 9.8.0-4
      1. There is no longer a need for a Saxon EE License to use XSLT 3.0
      1. XSLT 2.0 processor is no longer available, XSLT 3.0 processor will be used for XSLT 2.0 code which ensures a high level of backwards compatibility.
      1. XSLT 1.0 backward compatibility is no longer supported unless you have a Saxon EE license, if you don't have license switch your XSLT engine to be Xalan or XalanC.
   1. wadl-tools: 1.0.36 → 1.0.37
1. Clean up : join optimization stages rewritten to execute more efficiently; compile time speedups of 3-4x have been observed in complex setups.
   1. Since XSLT 3.0 no longer requires a Saxon EE License, the XPath join optimization now produces XSLT 3.0 code when using Saxon HE.

## Release 2.3.0 (2017-08-08) ##
1. Fixed a bug where rax:roles were not masked correctly if default headers were set.
1. Added support for ```rax:captureHeader```, this allows setting XPath 3.1 paths at the method request, representation, resource, and resources
   level to allow capturing a header with the result of the XPath. The XPaths can simultaneously introspect headers, uri, methods, and body content (xml or json).
1. Fixed a bug where the priority of URL_FAIL states was set too high and this caused mislabeled 404 errors when rax:roles masked was enabled.
1. Added support for `X-Relevant-Roles`, a header which will be added to the request any time `rax:roles` are being used.
   `X-Relevant-Roles` are the values of the `X-Roles` header which match a `rax:roles` value for the resource being requested.
   In other words, `X-Relevant-Roles` are the user roles which granted access to the resource.
   Note that the `rax:captureHeader` feature must be enabled for the `X-Relevant-Roles` header to be populated.
1. Clean up : code for optimization stages is now generated in smaller templates to avoid method size restrictions and allow for bytecode generation.

## Release 2.2.1 (2017-05-24) ##
1. Fixed a bug where we were not correctly setting the classloader.

## Release 2.2.0 (2017-05-12) ##
1. Updated Dependencies
   1. saxon: 9.7.0-8 → 9.7.0-15
   1. wadl-tools: 1.0.33 → 1.0.36
      1. Fixed a bug where under rare circumstances a WADL was generated with two attributes with the same name.
      1. Fixed a bug where extensions under the resources element were being lost
   1. io.dropwizard.metrics:metrics-core: 3.1.2 → 3.2.0
1. Clean up : Complete rewrite of WADLCheckerBuilder and WADLDotBuilder
   1. We now use Saxon API instead of JAX-P XSLT API, this allows finer grained control over options and cleaner code.
   1. Interact with wadl-tools via XSLT integration rather than Scala integration, again better options and control over execution.
   1. Lazy XSLT / XSD compilation : We now compile a style sheet or an XSD only when it needs to be used - no startup cost for unused features.
   1. Instrumented pipeline : Compiling the project with ```-Dchecker.timeFunctions``` will cause timing information on each step of the pipeline to print to ```stderr``` at run time.
   1. WADLDotBuilder has always only worked with ```StreamResult```s (other ```Result``` types failed) this is now made explicit with the interface.
1. Added support for ```rax:assert```, this allows setting XPath 3.1 assertions at the method request, representation, resource, and resources level.
   These assertions can simultaneously introspect headers, uri, methods, and body content (xml or json).
1. Adjusted the priority of content type error steps to ensure content type errors are reported correctly.
1. Added support for checking metedata changes on ```PUT``` operation in the ```rax:metdata``` extension.

## Release 2.1.1 (2017-01-27) ##
1. Fixed a bug where a resource file needed by checker-util was kept in checker-core

## Release 2.1.0 (2017-01-09) ##
1. Clean up : Move reusable utility classes into a separate project (checker-util).

## Release 2.0.3 (2016-11-18) ##
1. Clean up : Convert ```Throwable``` to more restrictive ```RuntimeException```

## Release 2.0.2 (2016-10-17) ##
1. Updated Dependencies
   1. saxon: 9.5.1-8 → 9.7.0-8
   1. wadl-tools: 1.0.32 → 1.0.33
   1. jackson-databind: 2.2.3 → 2.8.1
1. Added ```rax:authenticatedBy``` extension, the extension works exactly like ```rax:roles``` but allows the specification of an authentication method (```PASSCODE```, ```PASSWORD```, etc) rather than a role.
1. XPath 3.1 is now supported in ```plain``` parameters.
1. It is now possible to specify ```plain``` parameters on JSON media types via XPath 3.1. The variables ```$_``` or ```$body``` can be used to specify the content body.
1. The config item ```xsdEngine``` now applies when validating checker format, so that SaxonEE can be used to pre-validate a state machine before loading it.
1. Clean up: We now compile XSLs one time at startup instead of every time a Validator is created.
1. Integrated with [Nailgun](http://www.martiansoftware.com/nailgun/) to speed up startup performance of CLI utilities.
1. Fixed a bug where header parameters were incorrectly treated as case sensitive in a WADL.
1. Fixed a bug where header quality (and other parameters) were preventing matches of header values.
1. Fixed a bug where multiple ```wadl:doc``` elements on a single method were causing the processing of a WADL to fail.
1. Fixed a bug where metadata extension (```rax:metadata```) was inadvertently introducing invalid WADLs in the pipeline.

## Release 2.0.1 (2016-08-06) ##
1. Fixed a bug where a call to getInputStream was not invalidating cached XML/JSON.
1. Fixed a bug where comments and spaces were not properly maintained in XML bodies when the XML was processed.
1. Fixed a bug where including a new line in an URL caused validation to fail.
1. Fixed a bug where an error in parsing XML or JSON resulted in the request input stream being closed.
1. Fixed a bug where the ```fixed``` attribute in a template parameter was not being taken into account.
1. API checker now follows RFC 7234 recommendation of adding a Warning header whenever it modifies a request body.  The feature can be disabled with the ```enableWarnHeaders``` configuration option.

## Release 2.0.0 (2016-05-24) ##
This release introduces breaking changes. Refer to [BREAKING.md](BREAKING.md) for details.

1. Updated Dependencies
   1. scala: 2.10.3 → 2.11.7
   1. saxon: 9.4.0.9 → 9.5.1-8
   1. scala-test: 2.0 → 2.2.6
   1. metrics: 2.2.0 (Yammer) → 3.1.2 (DropWizard/CodaHale)
   1. argot: 1.0.1  → 1.0.4
1. Breaking Change: The ```repeating``` attribute is now honored correctly by header parameters. New default is ```repeating="false"```.
1. Breaking Change: If there are multiple repeating headers parameters of the same name then *all* header values must correctly validate
   against *all* header parameters.
1. Added support for the rax:anyMatch extension. On a repeating header
   parameter adding ```rax:anyMatch="true"``` means that the header correctly validates if *any* header vaule validates. This was the default
   behavior for header parameters before this version.
1. The wadltest cli utility now allows setting Content-Type of a sample response by passing it in a ```respType``` query parameter.
1. Clean up: Minor cleanup of code to address static analysis findings and remove deprication warnings.
1. Fixed a bug where a missing item in the priority map did not error out correctly.

## Release 1.1.4 (2016-01-01) ##
1. Added support for default values in required headers.
1. Added a new CLI tool (wadltest) that runs api-checker on command for manual testing, this replaces the sample filter and filter-test-app.
1. Added start-up scripts for CLI utilities. Placing bin in the system path in *nix environments should allow easy running of CLI tools.
1. Fixed a bug where header parameters with the same name, but no fixed value, were not handled correctly.
1. Clean up: The -v/--validate option was removed from wadl2checker in favor of the -D/--dont-validate used by the other CLI utilities.
   This inverts the default behavior in wadl2checker, but normalizes it across all the CLI utilities to validate by default.
1. Clean up: Cleaned the code base of nasty tab characters.
1. Clean up: Brought back an ignored test that was disabled because of a previous bug with wadl-tools.
1. Updated wadl-tools: 1.0.31 → 1.0.32. This addressed a number of bugs affecting this project:
   1. Fixed a bug where multiple slashes // on the root resource (/) with a method was creating empty resources.
   1. Fixed a bug where a WADL with no resources was not handled correctly.


## Release 1.1.3 (2015-11-10) ##
1. Added missing xsd-grammar-transform (g) option to wadl2checker.
1. Added the ability to disable checker validation in wadl2dot.
1. Added support for the rax:metadata extension. The rax:metadata extension can be configured
   on a WADL as an attribute at the resource level. It is always enabled when rax:roles is enabled.
1. Added support for spaces in role names in the rax:roles extension by means of non-breaking space (NBSP) characters.
1. Clean up: The DateUtils class was converted from Java to Scala.
1. Clean up: Cleaner Scala code when handling optional values through out the project.
1. Clean up: Removed outdated graphs from the project.
1. Fixed a bug where CLI utilities were not displaying the correct version.
1. Fixed a bug where an incorrect error message was given when a resource had multiple methods of the same type.
1. Fixed a bug where default checker values were not correctly filled in where checker validation was turned off.
1. Fixed a bug in checker validation where under certain circumstances an optional METHOD_FAIL state was marked as required.
1. Updated wadl-tools: 1.0.29 → 1.0.31.  This addressed a number of bugs affecting this project
   1. Fixed a bug where multiple slashes // in a resource path caused the creation of empty resources.
   1. Fixed a bug where WADL extensions were not correctly propagated to child resources.
   1. WADL-Tools is now aware of the rax:roles extension and can correctly merge multiple rax:roles attributes into one as part of the normalization process.


## Release 1.1.2 (2015-07-20) ##
1. Added the ability for the DelegationHandler to have the default component name of ```api-checker``` overridden.
This will allow multiple API-Checker instances to be utilized in a single application and have them be differentiated in the logs.

## Release 1.1.1 (2015-05-22) ##
1. Added the new ApiCoverageHandler to log the path taken by a request to a logger named ```api-coverage-logger```.
1. Fixed a bug where the Wadl2Checker and Wadl2Dot CLI utilities were accidentally placed in the test source tree. 

## Release 1.1.0 (2015-04-15) ##
1. Classes have been repackaged into different packages.
This breaks backwards compatibility for classes related to Result and Step
2. Added support for rax:captureHeader extension.  If added to a wadl param, it will put the value
into the header specified by the rax:captureHeader value.
3. Added an alias for rax:captureHeader="X-Device-Id" called rax:device=true
4. Fixed a bug where rax:roles were not masked on non-string header checks
5. Performance improvements to XSL stages of the checker builder

## Release 1.0.22 (2015-03-17) ##

1. Added step context for adding headers via the context.
1. Added inStep method to handlers to take an action to the step context between steps.
1. Added MethodLabelHandler that will check the step and apply the label if its a method to a header in the step context.
1. Enhancements to validation of checker XML to check that error states are correctly assigned.
1. Fixed an issue where HEADER_ANY and HEADERXSD_ANY did not have error states correctly assigned.

## Release 1.0.21 (2015-01-19) ##

1. We now explicitly select a class loader when loading XML factories.
2. Updated wadl-tools: 1.0.28 → 1.0.29.

## Release 1.0.20 (2014-12-18) ##

1. Checker format can now be loaded directly by a validator. The
checker document must end with a ```.checker``` extension or the query
component of the systemID must contain ```checker=true```.
1. Fixed an issue where dependencies are listed more than one time in
checker metadata.
1. Fixed an issue where having a header check next to rax:roles would
break raxRolesMask.
1. Updated to use log4j 2 in test and CLI utilities.
1. Updated http delegation library: 2.0.0 → 4.0.0.

## Release 1.0.19 (2014-11-06) ##

1. Checker format now contains metadata including:
   1. The creator
   1. The creation time
   1. The version of api-checker
   1. The user
   1. Dependencies that were used to build the checker
   1. Configuration options
1. Added support for Delegation Handler which provides info on
errors via headers without actually rejecting the request.
1. Minor cleanup to POMs.
1. Fixed a bug where method IDs are not always properly generated.
1. Updated wadl-tools: 1.0.25 → 1.0.28.
1. Updated saxon: 9.4.0.6 → 9.4.0.9.

## Release 1.0.18 (2014-07-01) ##

1. Fixed bug in raxRolesMask where illegal step IDs were generated in the
case where a role name contained a non-alphanumeric character.
1. Fixed bug where a malformed URI resulted in a 500 error code instead of
400.
1. Fixed a potential bug where an NPE would result in the rare but
plausible case where a container does not provide access to an HTTP
header.
1. Fixed bug where single mismatch method and URI errors contain the
same priority as multiple mismatch errors.
1. HTTP servlet request mocks now handle HTTP headers in a case
insensitive manner.
1. Licensed code under Apache License version 2.0.

## Release 1.0.17 (2014-05-25) ##

1. New strategy when deciding error message that involves longest path
from Start and a priority value based on step type.
1. Fixed bug in xpath join optimization where WELL_XML steps were not
always correctly handled.
1. Better logging using slf4j.
1. Fixed bug where Saxon 9.3 was added as a dependency in addition to Saxon 9.4.
1. Transition from maven-assembly-plugin to maven-shade-plugin when
creating single jar cli utils.
1. Updated wadl-tools: 1.0.22 → 1.0.25.

## Release 1.0.16 (2014-02-10) ##

1. Added raxRolesMask. Normally api-checker will return 403s when
authorization is denied.  When the raxRolesMask feature is enabled,
404s or 405s will be returned instead to hide the fact that failure is
due to lack of privileges.
1. The removeDups optimization can now correctly handle METHOD\_FAIL
and URL\_FAIL steps with no match.
1. Fixed bug where the instrumented handler was not counting steps
correctly.
1. Fixed a potential bug where METHOD\_FAIL and URL\_FAIL messages
could be confused with one another in the removeDups optimization.
1. Fixed a bug where the next attribute is not taken into account in
the header join optimization.
1. Fixed a bug in the prune step transform where checker elements may
not be copied correctly.
1. Updated wadl-tools: 1.0.21 → 1.0.22

## Release 1.0.15 (2014-01-14) ##

1. Added preserveRequestBody. Normally api-checker will simply stream the request
body through the state machine for validation. This works fine for a single validator,
but if multiple validators are used this will cause unexpected errors. preserveRequestBody
will add an extra step in the state machine to 'save' the request body in a buffer
so as validators further down the line can read it. Turning this feature on will add an extra
step in the state machine to capture the request body.
1. Updated XPath join optimization to be able to join adjacent mergable XSLs.
1. Updated wadl-tools: 1.0.20 → 1.0.21
1. Fixed issue: rax:roles in method references were not handled correctly. 
1. Enable implicit conversions in core tests. Removes warnings during compilation.

## Release 1.0.14 (2013-12-12) ##

1. Fixed URI decoding and handling of '+' in URL path segment
1. Better handling of exceptions in validator and checker-builder
1. Ensure JSON schema errors are concise
1. Fixed and enhanced printing paths for multifail results
1. Addressed Compilation Warnings on Scala 2.10

## Release 1.0.13 (2013-11-11) ##

1. Updated Dependencies
   1. scala: 2.9.3 → 2.10.3
   1. scala-test: 1.9.1 → 2.0
   1. wadl-tools: 1.0.15  → 1.0.20
   1. argot: 0.3.5  → 1.0.1

## Release 1.0.12 (2013-10-23) ##

1. Added support for rax:roles extension.  rax:roles extension can be configured
on a WADL as an attribute at the resource or method level.  A request is checked
for authorization if the the rax roles check is enabled.  If enabled, and the 
requested resource or method has a rax:roles defined, the request must have a 
header value of X-Roles with a header value that matches one of the defined values
in rax:roles.

## Release 1.0.11 (2013-10-14) ##

1. Added utility class for headers.  Modified request wrapper to not split
headers on commas by default.
1. Updated Dependencies
   1. jackson-databind: 2.1.5 → 2.2.3
   1. json-schema-validator: 2.1.6 → 2.1.7

## Release 1.0.10 (2013-08-16) ##

1. Initial support for JSON Schema. Currently a single JSON schema can
be associated with the WADL. Validation is applied to all JSON media
type requests unless a resource or representation is marked with rax:ignoreJSONSchema="true"
1. Fixed a bug where a representation without a mediatype was being
processed as if it were an XML media type
1. WADL-Tools updated to 1.0.15 which addresses some issues when
handling namespaces in X-PATHs

## Release 1.0.9 (2013-07-08) ##

1. Configuration has new XSD-Engine setting which makes clear which XSD
validator should be used
1. JSON is now processed with Jackson processor
1. WADL-Tools updated to 1.0.14 which reduces noise on console when
processing a WADL

## Release 1.0.8 (2013-04-29) ##

1. Updated Dependecies
   1. scala: 2.9.1 → 2.9.3
   1. scala-test: 1.6.1 → 1.9.1
   1. scala maven plugin: 2.15.1 → 3.1.3
   1. xerces: 2.12.0-rax → 2.12.1-rax
   1. wadl-tools: 1.0.12  → 1.0.13
   1. jetty: 6.1.22 → 8.1.1.20120215
1. There is now a priority when interpreting multi-fail results
1. Resolved bug where NPE is possible if XSD types are specifed in a
WADL without a grammar

## Release 1.0.7 (2013-03-20) ##

1. Updated WADL Tools to 1.0.12 to fix issue where some resource types
   were dropped during WADL normalization
1. Added destroy method in validator to do general cleanup
1. rax:message and rax:code extensions can now apply to Headers

## Release 1.0.6 (2013-03-14) ##

1. Allow header is now set on 405 error as required by HTTP RFC
1. Resolved bug where error states were not always handled correctly
   by removed dups optimization
1. Resolved bug where results were not converted correctly to strings


## Release 1.0.5 (2013-02-27) ##

1. Updated Saxon 9.4.0.4 -> 9.4.0.6
1. All RAX extensions are now enabled by default
1. Test framework is now exported as a jar, so you can use api-checker
   in JUnit tests
1. Resolved Bug here XPath pool was not taking namespace context into account
1. Resolved Bug where HREFs in a WADL artifacts (XSDs, XSLs) were not
   handled correctly when using relative paths
1. Resolved Bug where preprocess extension was forcing a well-formness check
1. Resolved Bug where preprocess extension was losing data when the join dups
   optimization was enabled
1. Extend join-dups optimization to work with headers with a fixed value
1. Plain parameters can now have a customizable error response code set
1. Increased the amount of memory needed to compile core

## Release 1.0.4 (2013-01-28) ##

1. Resolved a number of bugs dealing with the joinDups optimization
1. Cleaned up all optimizations, it is now possible to share code
   between optimization stages
1. Added rax:message extension which allows for friendlier error
   messages when validation for WADL plain parameters fails
1. Added friendly error messages to checker XSD assertions

## Release 1.0.3 (2013-01-21) ##

1. JMX Instrumentation of the Validator
1. Provides overall stats on validator
1. Visibility on resource pools
1. JMX access to DOT and XML representations of the state machine
1. Validators can now be assigned names so they can be identified via JMX
1. JMX handler (instrumented handler) which provides additional JMX info
1. Most frequently occurring errors
1. Stats on each validator state
1. Extend Header checks to support multiple header values
1. Extend Header checks to support fixed header values
1. Fix bug where resource with a path "/" is not correctly handled

## Release 1.0.2 (2012-11-13) ##

1. Integrate with WADL Tools 1.0.9
1. Media Type ranges (for example text/* and \*/\*)  are now supported
1. Media Type parameters (application/xml;version=2) are now ignored when validating
1. Media Type charset parameter is now supported (application/xml; charset=UTF-16)
1. Wadl2Checker CLI Util now supports xpath-version argument
1. Required Header checks can now be enforced on request or representation 
1. Added support for ignore XSD extension -- to avoid performing XSD checks on request or representation 
1. Ensure that relative paths always work in CLI utilities
1. Ensure that validation **does not** require a SAXON license

## Release 1.0.1 (2012-07-30) ##

1. Create saxon schema factory directly instead of relying on System property
1. Killed outdated samples directory
1. Slight optimization when checking XML well formness stop parsing as soon as first error is detected.
1. Now support embedded XSLT in preproc extension in addition to href
1. New optimization joinXPathChecks  -- this merges XML well formness stages with multiple xpath stages
1. Update to latest version of Saxon 9.4 (9.4.0.4)

## Release 1.0.0 (2012-07-23) ##

1. URI checks, including template parameters with types defined by XSD
1. Method checks
1. Content-Type checks
1. Well formed checks for both JSON and XML
1. Root element checks in XML
1. Required plain parameter checks in XML (assertions using XPath 1.0/2.0)
1. Full XSD 1.1 validation of content backed by Xerces or Saxon
1. Preprocess extension: clean up/transform content before validation
1. Correctly handle cases where versioning is not done via URI
1. Correct error handling for missed checks (404 for URI, 405 for
   method etc) most checks have human readable messages:  Found (server)
   expected (servers | images | flavors)
1. Flexible configuration (any check can be enabled/disabled)
