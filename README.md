## API Checker ##

The API Checker library creates a Java servlet filter that supports the validation of a ServletRequest using a state
machine that is built from a normalized WADL.  There are many checks that can be performed during validation of a request,
and these checks can be enabled or disabled during the configuration of a validator.

CLI utilities exist to support the ability to convert a normalized WADL to the CheckerFormat, and from CheckerFormat
to WADL.

The API Checker library has the following phases to create a validator given a WADL:

    Phase 1: Normalize the WADL
    Phase 2: WADL to CheckerFormat (custom XML format)
    Phase 3: CheckerFormat to State Machine

### Phase 1: Normalize the WADL ###

A WADL may have external resources (XSDs, other WADL references) that are needed in order to produce a complete state
machine to support the validation of a request.  The normalize phase will resolve all external resources and generate a
single wadl file that can then be used by Phase 2.

> Development Tips
>
> To manually normalize a WADL to support testing of phase 2:
>
>    1. Clone the wadl-tools repository
>        $ git clone git@github.com:rackerlabs/wadl-tools.git
>    2. Normalize your WADL using the wadltools utilities (cloned from github)
>        $ wadl-tools/bin/normalizeWadl.sh -w {location of your wadl file} -f tree


### Phase 2: WADL to CheckerFormat ###

checker-builder.scala: WADLCheckerBuilder

Transforms normalized WADL using a number of stylesheets into the CheckerFormat (XML).  Configuration
flags influence the checks that are added to the generated CheckerFormat XML.  Stylesheets used by WADLCheckerBuilder
include: raxRoles.xsl, builder.xsl

CLI utility: Wadl2Checker

### Phase 3: CheckerFormat to State Machine ###

dot-builder.scala: WADLDotBuilder

Create the state machine from the CheckerFormat.  The state machine is then used by the validator to validate requests.
State machine is created from CheckerFormat XML using the checker2dot stylesheet.

CLI utility: Wadl2Dot

## Getting Started ##

1. Saxon License: obtain one and setup your SAXON_HOME environment variable
2. Oxygen XML Editor: allows easier development and troubleshooting of XML Stylesheets
3. wadl-tools used by api-checker to normalize a WADL.  Includes useful CLI utilities to perform the normalization
   step
4. Review the TODO.org file for a historical checklist of implemented features.
5. Review the RELEASE.md for release notes

### Getting Started: Saxon License ###

The default maven test phase will run all tests, including tests that require a SAXON license.  To exclude tests that
don't require a saxon license, use the -Pxerces-only maven cmdline setting.

To properly execute all tests in this source repository, do the following:
    1. Obtain a Saxon-PE license and save it in a local file named saxon-license.lic
    2. Export a SAXON_HOME environment variable that contains the path to the directory where you saved your saxon-license.lic file
