# uPortal

<table><tr>
<td>

[![WCAG 2 AA Badge](https://www.w3.org/WAI/wcag2AA-blue-v.svg)](#accessible)
[![Issue Stats](http://issuestats.com/github/Jasig/uPortal/badge/pr)](http://issuestats.com/github/Jasig/uPortal)
[![Google Code Style](https://img.shields.io/badge/code_style-Google-green.svg?style=flat)](https://google.github.io/styleguide/javaguide.html)

| Version | Linux | Windows | Coverage |
| - | - | - | - |
| [uPortal 5](https://github.com/Jasig/uPortal/tree/master) | [![Linux Build Status](https://travis-ci.org/Jasig/uPortal.svg?branch=master)](https://travis-ci.org/Jasig/uPortal) | [![Windows Build Status](https://ci.appveyor.com/api/projects/status/8t95sjt090mf62dh/branch/master?svg=true)](https://ci.appveyor.com/project/drewwills/uportal/branch/master) | [![Coverage Status](https://coveralls.io/repos/github/Jasig/uPortal/badge.svg?branch=master)](https://coveralls.io/github/Jasig/uPortal?branch=master) |
| [uPortal 4](https://github.com/Jasig/uPortal/tree/rel-4-3-patches) | [![Linux Build Status](https://travis-ci.org/Jasig/uPortal.svg?branch=rel-4-3-patches)](https://travis-ci.org/Jasig/uPortal) | [![Windows Build Status](https://ci.appveyor.com/api/projects/status/8t95sjt090mf62dh/branch/rel-4-3-patches?svg=true)](https://ci.appveyor.com/project/drewwills/uportal/branch/rel-4-3-patches) | [![Coverage Status](https://coveralls.io/repos/github/Jasig/uPortal/badge.svg?branch=rel-4-3-patches)](https://coveralls.io/github/Jasig/uPortal?branch=rel-4-3-patches) |

</td>
<td>

| Get Involved | Outlet |
| - | - |
| Report an Issue | [![Issue Tracker](https://img.shields.io/badge/issue_tacker-Jira-green.svg?style=flat)](https://issues.jasig.org/browse/UP) |
| Request a feature | [![Issue Tracker](https://img.shields.io/badge/issue_tacker-Jira-green.svg?style=flat)](https://issues.jasig.org/browse/UP) |
| Contibute Code | [![Contributing Guide](https://img.shields.io/badge/contributing-guide-green.svg?style=flat)](CONTRIBUTING.md)
| Join the Conversation | [![Gitter](https://badges.gitter.im/Jasig/uPortal.svg)](https://gitter.im/Jasig/uPortal?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge) <br> [![uPortal on IRC](https://img.shields.io/badge/IRC-%23jasig--uportal-1e72ff.svg?style=flat)](https://www.irccloud.com/invite?channel=%23jasig-uportal&amp;hostname=irc.freenode.net&amp;port=6697&amp;ssl=1) <br> [![Twitter Follow](https://img.shields.io/twitter/follow/uPortal.svg?style=social&label=Follow)](https://twitter.com/uPortal) |

</td>
</tr></table>

## About

uPortal is the leading open source enterprise portal framework built by and for
the higher education community. uPortal continues to evolve through contributions
from its global community and is supported by resources, grants, donations, and
memberships fees from academic institutions, commercial affiliates, and non-profit
foundations. uPortal is built on open standards-based technologies such as Java
and XML, and enables easy, standards-based integration with authentication and
security infrastructures, single sign-on secure access, campus applications,
web-based content, and end user customization. uPortal can easily integrate with
other enterprise systems and can be customized for specific local needs.

### Forever Free!

You may [download uPortal](https://github.com/Jasig/uPortal/releases) and use it
on your site at no cost. Unlike our competitors, uPortal is 100% free open source
software managed by [Apereo](https://www.apereo.org/content/about). Our community
has access to all releases of the uPortal software with absolutely no costs. We
welcome [contributions from our community](https://github.com/Jasig/uPortal/graphs/contributors)
of all types and sizes.

### Accessible

uPortal strives to conform with [Web Content Accessibility Guidelines Version 2.0](https://www.w3.org/TR/WCAG20/) Level AA.
The most recent accessibility audit results can be seen in [UP-4735](https://issues.jasig.org/browse/UP-4735).

## Help and Support

The [uportal-user@apereo.org](https://wiki.jasig.org/display/JSG/uportal-user)
email address is the best place to go with questions related to configuring or
deploying uPortal.

The [uPortal manual](#manual) is a collaborative resource which has more detailed documentation for
each uPortal release.

### Manual

Additional information about uPortal is available in the Manual.

*   [uPortal 5.0 Manual](https://jasig.github.io/uPortal)
*   [uPortal 4.3 Manual](https://wiki.jasig.org/display/UPM43/Home)
*   [uPortal 4.2 Manual](https://wiki.jasig.org/display/UPM42/Home)
*   [uPortal 4.1 Manual](https://wiki.jasig.org/display/UPM41/Home)
*   [uPortal 4.0 Manual](https://wiki.jasig.org/display/UPM40/Home)
*   [uPortal 3.2 Manual](https://wiki.jasig.org/display/UPM32/Home)
*   [uPortal 3.1 Manual](https://wiki.jasig.org/display/UPM31/Home)
*   [uPortal 3.0 Manual](https://wiki.jasig.org/display/UPM30/Home)

## Requirements

*   JDK 1.8 - The JRE alone is NOT sufficient, a full JDK is required
*   Servlet 3.1 Container - [Tomcat](https://tomcat.apache.org/) 8.0 is required.  (NOTE:  Tomcat 7 may continue to workin the early 4.3 period.  We reserve the right to use leverage Servlet 3.1, JSP 2.3, EL 3.0 and Web Socket 1.1)  There some configuration changes that must be made for Tomcat which are documented in the [uPortal manual](https://wiki.jasig.org/display/UPM42/Installing+Tomcat).
*   [Maven](https://maven.apache.org/) 3.2.2 or later
*   [Ant](https://ant.apache.org/) 1.8.2 or 1.9.3 or later.

## Building and Deploying

uPortal uses Maven for its project configuration and build system. An Ant
*build.xml* is also provided which handles the initialization and deployment
related tasks. As a uPortal deployer you will likely only ever need to use the
Ant tasks. Ant 1.8.2 or 1.9.3 or later is required

### Ant tasks

For a full list of ant tasks run `ant -p`

*   **hsql** - Starts a HSQL database instance. The default uPortal configuration points
to this database and it can be used for portal development.
*   **initportal** - Runs the 'deploy-ear' and 'init-db' ant targets, should be the first
and only task run when setting up a new uPortal instance **WARNING**: This runs 'init-db'
which **DROPS** and re-creates the uPortal database
*   **deploy-ear** - Ensures the latest changes have been compiled and packaged then
deploys uPortal, shared libraries and all packaged portlets to the container
*   **initdb** - Sets up the uPortal database. **DROPS ALL EXISTING** uPortal tables
re-creates them and populates them with the default uPortal data **WARNING**: This DROPS
and re-creates the uPortal database
*   **deploy-war** - Ensures the latest uPortal changes have been compiled and packaged
then deploys the uPortal WAR to the container.
*   **deployPortletApp** - Deploys the specified portlet application to the container.
This is the required process to deploy any portlet to a uPortal instance.

``` shell
ant deployPortletApp -DportletApp=/path/to/portlet.war
```

## Other Notes

### Initial Configuration

To deploy uPortal you must set the server.home variable in the
build.properties file to the instance of Tomcat you want to deploy to.


### Build approach

The approach here is that there is a generic *pom.xml* and *build.xml* that you
should not have to edit, alongside a *build.properties* that you absolutely must
edit to reflect your local configuration. Edit build.properties to reflect such
configuration as where your Tomcat is, what context you would like uPortal to
be deployed as, etc.


### Initial Deployment

You must run the initportal target before uPortal is started the first time.
This target will take care of compiling, deploying, database population and
other initial tasks. Running initportal again is similar to hitting a reset
button on the portal. Any saved configuration in the portal is lost and a clean
version of the portal is configured.

### Logging

The */uportal-war/src/main/resources/logback.xml* Logback configuration
file will end up on the classpath for Logback to find. You'll
need to either change that configuration then run deploy-war. You can configure
the logging level, where the file should be, or even choose a different logging
approach.

### Database configuration

Database connection information is read from */uportal-war/src/main/resources/properties/rdbm.properties*,
but is normally configured in *filters/{environment.name}.properties*.
