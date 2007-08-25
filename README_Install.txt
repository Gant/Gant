Gant -- A Groovy build tool based on scripting Ant tasks

This is Gant a Groovy way of working with Ant tasks -- no more XML :-)

In order to install and use Gant you must have Groovy installed and
the environment variable GROOVY_HOME must be set to the location of a
Groovy installation.  This location is where Gant will be installed:
the gant/gant.bat scripts are installed in the Groovy bin directory
and the gant-<version-number>.jar file in the Groovy lib directory.

The method of installation depends on whether you have downloaded a
tarball or zipfile distribution or you have a Subversion store
checkout.

Distribution
------------

Untar the tarball or unzip the zipfile.  This creates a directory with
all the files.  cd into the directory and issue the command:

    groovy bin/install.groovy

assuming the command groovy is in your path.  If it is not then you
will have to give the path to the groovy executable.

Subversion Checkout
-------------------

To install Gant for the first time, you need to either:

--  install Gant from a distribution as above and the type "gant
    install" which will overwrite the distribution install with the
    Subversion checkout install; or

--  assuming you have an Ant installation, type "ant install" which
    will do everything necessary.


To install a new build of Gant where one is installed already, you
can, of course (!), simply do "gant install".

Contact
-------

If you have any problems using Gant, or have any ideas for
improvements, please contact me.

Russel Winder <russel@russel.org.uk>

$Revision$
$Date$
