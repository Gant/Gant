Gant -- A Groovy build framework based on scripting Ant tasks.

This is Gant a Groovy way of working with Ant tasks -- no more XML :-)

The method of installation depends on whether you have downloaded a
tarball or zipfile distribution or you have a Subversion store
checkout.

Distribution
------------

In order to install the Gant distribution you must have an
installation of Groovy.

The distributions contain a ready-made install directory hierarchy.
Untar the tarball or unzip the zipfile to the location where you want
the Gant installation to reside.  A directory with the name structured
gant-<version-number> will be created in the location specified for
the untar or unzip.  You might like to set up an environment variable
GANT_HOME set to the directory created by the untar or unzip.

The script $GANT_HOME/bin/gant for systems with a Posix shell, or
$GANT_HOME/bin/gant.bat on Windows are the mechanisms for launching a
Gant run.

Subversion Checkout
-------------------

Having checked out a Gant source tree, you will need to create a file
called local.build.properties containing a definition of the property
installDirectory.  This property defines the directory of the
installation, not the parent.  An example definition:

  installDirectory = ${user.home}/lib/JavaPackages/gant-${gantVersion}

Having created this file with installDirectory property, then to
install Gant for the first time, you need to either:

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

Russel Winder <russel.winder@concertant.com>
