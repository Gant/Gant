#!/bin/sh

#  Gant -- A Groovy way of scripting Ant tasks.

#  Copyright © 2006-7,2009 Russel Winder

#  Gant initiation script for Linux and UNIX

#  Use -cp or -classpath just as in java to use a custom classpath

DIRNAME=`dirname "$0"`
. "$DIRNAME/startGroovy"

startGroovy gant.Gant "$@"
