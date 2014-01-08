# Releases #

## Release 1.0.14 (2013-12-12) ##

1. Added preserveRequestBody. Normally api-checker will simply stream the request
body through the state machine for validation. This works fine for a single validator,
but if multiple validators are used this will cause unexpected errors. preserveRequestBody
will add an extra step in the state machine to 'save' the request body in a buffer
so as validators further down the line can read it. Turning this feature on will cause
a slight performance hit as it adds a step in the state machine.
1. Updated XPath join optimization to be able to join adjacent mergable XSLs.

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
