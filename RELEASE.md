# Releases #

## Release 1.0.5 (2013-02-27) ##

1. Updated Saxon 9.4.0.4 -> 9.4.0.6
1. All RAX extensions are now enabled by default
1. Test framework is not exported as a jar, so you can use api-checker
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
1. Media Type ranges (for example text/* and */*)  are now supported
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
