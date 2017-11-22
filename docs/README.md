<link rel="stylesheet" href="css/reset_image_size.css">

# uPortal Documentation

**NOTE:** This area is a work-in-progress. It is intended to serve as primary
documentation for uPortal version 5.0 (expected mid-2017) and above.
Documentation for earlier versions of uPortal is available in
[Confluence](https://wiki.jasig.org). See _Documentation for Previous Releases_
below.

<table border="0">
  <tr>
    <td>
      <a href="https://www.w3.org/TR/WCAG20/">
        <img src="https://www.w3.org/WAI/wcag2AA-blue-v.svg" alt="WCAG 2 AA Badge">
      </a>
      <a href="http://issuestats.com/github/Jasig/uPortal">
        <img src="http://issuestats.com/github/Jasig/uPortal/badge/pr" alt="Issue Stats">
      </a>
      <a href="https://google.github.io/styleguide/javaguide.html">
        <img src="https://img.shields.io/badge/code_style-Google-green.svg?style=flat" alt="Google Code Style">
      </a>
      <a href="https://github.com/search?q=topic%3Auportal+topic%3Asoffit&type=Repositories">
        <img src="https://img.shields.io/badge/discover-soffits-blue.svg?style=flat" alt="Discover Soffits">
      </a>
      <a href="https://github.com/search?q=topic%3Auportal+topic%3Aportlet&type=Repositories">
        <img src="https://img.shields.io/badge/discover-portlets-blue.svg?style=flat" alt="Discover Portlets">
      </a>
      <br>
      <table>
        <tr>
          <th>
            Version
          </th>
          <th>
            Linux
          </th>
          <th>
            Windows
          </th>
          <th>
            Coverage
          </th>
          <th>
            Dependencies
          </th>
        </tr>
        <tr>
          <td>
            <a href="https://github.com/Jasig/uPortal/tree/master">
              uPortal 5
            </a>
          </td>
          <td>
            <a href="https://travis-ci.org/Jasig/uPortal">
              <img src="https://travis-ci.org/Jasig/uPortal.svg?branch=master" alt="Linux Build Status">
            </a>
          </td>
          <td>
            <a href="https://ci.appveyor.com/project/drewwills/uportal/branch/master">
              <img src="https://ci.appveyor.com/api/projects/status/8t95sjt090mf62dh/branch/master?svg=true" alt="Windows Build Status">
            </a>
          </td>
          <td>
            <a href="https://coveralls.io/github/Jasig/uPortal?branch=master">
              <img src="https://coveralls.io/repos/github/Jasig/uPortal/badge.svg?branch=master" alt="Coverage Status">
            </a>
          </td>
          <td>
            <a href='https://www.versioneye.com/user/projects/59e525762de28c000f9188ae'>
              <img src='https://www.versioneye.com/user/projects/59e525762de28c000f9188ae/badge.svg?style=flat-square' alt="Dependency Status" />
            </a>
          </td>
        </tr>
        <tr>
          <td>
            <a href="https://github.com/Jasig/uPortal/tree/rel-4-3-patches">
              uPortal 4
            </a>
          </td>
          <td>
            <a href="https://travis-ci.org/Jasig/uPortal">
              <img src="https://travis-ci.org/Jasig/uPortal.svg?branch=rel-4-3-patches" alt="Linux Build Status">
            </a>
          </td>
          <td>
            <a href="https://ci.appveyor.com/project/drewwills/uportal/branch/rel-4-3-patches">
              <img src="https://ci.appveyor.com/api/projects/status/8t95sjt090mf62dh/branch/rel-4-3-patches?svg=true" alt="Windows Build Status">
            </a>
          </td>
          <td>
            <a href="https://coveralls.io/github/Jasig/uPortal?branch=rel-4-3-patches">
              <img src="https://coveralls.io/repos/github/Jasig/uPortal/badge.svg?branch=rel-4-3-patches" alt="Coverage Status">
            </a>
          </td>
          <td>
            N/A
          </td>
        </tr>
      </table>
    </td>
    <td>
      <table>
        <tr>
          <th>
            Get Involved
          </th>
          <th>
            Outlet
          </th>
        </tr>
        <tr>
          <td>
            Report an Issue
          </td>
          <td>
            <a href="https://issues.jasig.org/browse/UP">
              <img src="https://img.shields.io/badge/issue_tacker-Jira-green.svg?style=flat" alt="Issue Tracker">
            </a>
          </td>
        </tr>
        <tr>
          <td>
            Request a feature
          </td>
          <td>
            <a href="https://issues.jasig.org/browse/UP">
              <img src="https://img.shields.io/badge/issue_tacker-Jira-green.svg?style=flat" alt="Issue Tracker">
            </a>
          </td>
        </tr>
        <tr>
          <td>
            Contribute Code
          </td>
          <td>
            <a href="CONTRIBUTING.md">
              <img src="https://img.shields.io/badge/contributing-guide-green.svg?style=flat" alt="Contributing Guide">
            </a>
          </td>
        </tr>
        <tr>
          <td>
            Join the Conversation
          </td>
          <td>
            <a href="https://groups.google.com/a/apereo.org/forum/#!forum/uportal-user">
              <img src="https://img.shields.io/badge/uPortal-user-green.svg?style=flat" alt="uPortal user mailing list">
            </a>
            <br>
            <a href="https://groups.google.com/a/apereo.org/forum/#!forum/uportal-dev">
              <img src="https://img.shields.io/badge/uPortal-dev-blue.svg?style=flat" alt="uPortal developer mailing list">
            </a>
            <br>
            <a href="https://apereo.slack.com">
              <img src="https://img.shields.io/badge/chat-on_slack-E01765.svg?style=flat" alt="chat on slack">
            </a>
            <br>
            <a href="https://twitter.com/uPortal">
              <img src="https://img.shields.io/twitter/follow/uPortal.svg?style=social&amp;label=Follow" alt="Twitter Follow">
            </a>
          </td>
        </tr>
      </table>
    </td>
  </tr>
</table>

## Sections

* [Building / Deploying uPortal](building-and-deploying-uportal.md)
* [Implementing uPortal](implement/README.md)
* [uPortal System Administration](sysadmin/README.md)
* [Developer's Guide](developer/README.md)
* [Supported web browsers](SUPPORTED_BROWSERS.md)
* [Accessibility](ACCESSIBILITY.md)
* [Project Committers](COMMITTERS.md)

## External Links

* [Apereo Foundation Home](https://www.apereo.org/)
* [uPortal Wiki](https://wiki.jasig.org/display/UPC/Home)

## Documentation for Previous Releases

* [uPortal 4.3](https://wiki.jasig.org/display/UPM43/Home)
* [uPortal 4.2](https://wiki.jasig.org/display/UPM42/Home)
* [uPortal 4.1](https://wiki.jasig.org/display/UPM41/Home)
* [uPortal 4.0](https://wiki.jasig.org/display/UPM40/Home)
* [uPortal 3.2](https://wiki.jasig.org/display/UPM32/Home)
* [uPortal 3.1](https://wiki.jasig.org/display/UPM31/Home)
* [uPortal 3.0](https://wiki.jasig.org/display/UPM30/Home)
