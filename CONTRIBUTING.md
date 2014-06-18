Guidelines for Contributing
====
Contributions from the community are essential in keeping uPortal (any Open Source project really) strong and successful.  While we try to keep requirements for contributing to a minimum, there are a few guidelines we ask that you mind.

## Individual Contributor License Agreement

uPortal is an Apereo project.  Apereo requires that contributions be through Individual Contributor License Agreements.  If you wish to make a contribution, you must complete an Individual Contributor License Agreement.  It is through this agreement that you are licensing your contribution to Apereo so that Apereo can then license it to others under uPortal's open source software license.

You can learn more about [Apereo licensing generally][], the [contributor licensing agreements specifically][], and get the actual [Individual Contributor License Agreement form][], as linked.

## Getting Started
If you are just getting started with Git, GitHub and/or contributing to uPortal via GitHub there are a few pre-requisite steps.

* Make sure you have a [Jasig Jira account](https://issues.jasig.org)
* Make sure you have a [GitHub account](https://github.com/signup/free)
* [Fork](http://help.github.com/fork-a-repo) the uPortal repository.  As discussed in the linked page, this also includes:
    * [Set](https://help.github.com/articles/set-up-git) up your local git install
    * Clone your fork


## Create the working (topic) branch
Create a "topic" branch on which you will work.  The convention is to name the branch using the JIRA issue key.  If there is not already a Jira issue covering the work you want to do, create one.  Assuming you will be working from the master branch and working on the Jira UP-123 : 

    git checkout -b UP-123 master

For a more in-depth description of the git workflow check out the
[uPortal Git Workflow](https://wiki.jasig.org/display/UPC/Git+Workflow+for+Non-Committers)


## Code
Do yo thang!

## Commit

* Make commits of logical units.
* Be sure to use the JIRA issue key in the commit message.  This is how Jira will pick up the related commits and display them on the Jira issue.
* Make sure you have added the necessary tests for your changes.
* Run _all_ the tests to assure nothing else was accidentally broken.

_Prior to commiting, if you want to pull in the latest upstream changes  (highly appreciated btw), please use rebasing rather than merging.  Merging creates "merge commits" that really muck up the project timeline._

## Submit
* You submitted a [Contributor License Agreement][], right?  Seriously, Apereo cannot accept your contribution other than under a Contributor License Agreement.
* Push your changes to a topic branch in your fork of the repository.
* Initiate a [pull request](http://help.github.com/send-pull-requests/)
* Update the Jira issue, adding a comment inclusing a link to the created pull request

[Apereo licensing generally]: http://www.apereo.org/licensing
[contributor licensing agreements specifically]: http://www.apereo.org/licensing/agreements
[Contributor License Agreement]: http://www.apereo.org/licensing/agreements
[Individual Contributor License Agreement form]: http://www.apereo.org/sites/default/files/licensing/apereo-icla.pdf
