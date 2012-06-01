# Introduction

Gant is a lightweight dependency programming framework for Groovy and Java systems.

Gant was originally created as a build framework: [Groovy](http://groovy.codehaus.org) has AntBuilder and
Gant was a framework built around it, created so as to be able to avoid using XML with Ant. Groovy makes a
much better build specification language than XML; Gant gives access to all the Ant tasks using Groovy. So
successful was this model that Gant was forked to create the official Groovy Front-End to Ant that is now an
Ant standard feature.

Experimentation with Gant showed though that the computational model at the heart of Gant was not going to
allow for a fully fledged build framework.  Thus was [Gradle](http://www.gradle.org) born.  Gradle is now
the standard Groovy-based build framework.  Even Gant, which originally used Gant for its build, now uses
Gradle for its build.

Gant is an integral part of the [Grails](http://www.grails.org) Web application framework.

Gant is the basis for the [GINT](https://studio.plugins.atlassian.com/wiki/display/GINT/Home) integration
testing framework.

# Overview

Gant is a tool for scripting Ant tasks using Groovy instead of XML to specify the logic. A Gant
specification is a Groovy script and so can bring all the power of Groovy to bear directly, something not
possible with Ant scripts. Whilst it might be seen as a competitor to Ant, Gant uses Ant tasks for many of
the actions, so Gant is really an alternative way of doing things using Ant, but using a programming
language rather than XML to specify the rules.

Here is an example Gant script:

    includeTargets << gant.targets.Clean
    cleanPattern << [ '**/*~' ,  '**/*.bak' ]
    cleanDirectory << 'build'

    target ( stuff : 'A target to do some stuff.' ) {
      println ( 'Stuff' )
      depends ( clean )
      echo ( message : 'A default message from Ant.' )
      otherStuff ( )
    }

    target ( otherStuff : 'A target to do some other stuff' ) {
      println ( 'OtherStuff' )
      echo ( message : 'Another message from Ant.' )
      clean ( )
    }

    setDefaultTarget ( stuff )
    
In this script there are two targets, `stuff` and `otherStuff` -- the default target for this build is
designated as stuff and is the target run when Gant is executed from the command line with no target as
parameter.

Targets are closures so they can be called as functions, in which case they are executed as you expect, or
they can be dependencies to other targets by being parameters to the depends function, in which case they
are executed if an only if they have not been executed already in this run. (There is a page with some more
information on Targets.)

You may be wondering about the stuff at the beginning of the script. Gant has two ways of using pre-built
sub-scripts, either textual inclusion of another Gant script or the inclusion of a pre-compiled class. The
example here shows the latter -- the class `gant.targets.Clean` is a class that provides simple clean
capabilities.

The default name for the Gant script is build.gant, in the same way that the default for an Ant script in
build.xml.

Gant provides a way of finding what the documented targets are:

> |> gant -p  
> clean       Action the cleaning.  
> clobber     Action the clobbering.  Do the cleaning first.  
> otherStuff  A target to do some other stuff.  
> stuff       A target to do some stuff.  
>  
> Default target is stuff.  
> |>  

The messages on this output are exactly the strings associated with the target name in the introduction to the target.

# The Source

The Gant mainline source is in a [Git repository](https://github.com/Gant/Gant) held on GitHub. Feel free to
fork, amend and send in pull requests. The master branch is currently both the 1.9.x series maintenance
branch and the development branch. All development should though happen on feature branches until accepted
and merged into master..

The Git repository held at Codehaus is an administrative mirror and shouldn't really be the repository to
fork in order to work on Gant development.

# Support

Although GitHub is used for the Gant source, [Codehaus](http://www.codehaus.org) is used for the
[website](http://gant.codehaus.org), wiki, [issue tracker](http://jira.codehaus.org/browse/GANT) and
[continuous integration](http://bamboo.ci.codehaus.org/browse/GANT).

# Licence

Gant is licenced under ASL 2.0.
