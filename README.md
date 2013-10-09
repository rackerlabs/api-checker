## Saxon License ##

The default maven test phase will run all tests, including tests that require a SAXON license.  To exclude tests that
don't require a saxon license, use the -Pxerces-only maven cmdline setting.

To properly execute all tests in this source repository, do the following:
    1. Obtain a Saxon-PE license and save it in a local file named saxon-license.lic
    2. Export a SAXON_HOME environment variable that contains the path to the directory where you saved your saxon-license.lic file
