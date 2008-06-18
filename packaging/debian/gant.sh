#!/bin/sh

#  Gant -- A Groovy build framework based on scripting Ant tasks.
#
#  Copyright © 2008 Russel Winder
#
#  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
#  compliance with the License. You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software distributed under the License is
#  distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
#  implied. See the License for the specific language governing permissions and limitations under the
#  License.
#
#  Author : Russel Winder <russel.winder@concertant.com>

#  Gant initiation script for Debian and Ubuntu.  Relies on there being an installation of Groovy and of
#  Ant.

GANT_HOME=/usr/share/gant
GROOVY_HOME=/usr/share/groovy
ANT_HOME=/usr/share/ant

GROOVY_APP_NAME=Gant
GROOVY_CONF="$GANT_HOME/conf/gant-starter.conf"

. "$GROOVY_HOME/bin/startGroovy"

JAVA_OPTS="$JAVA_OPTS -Dgant.home=$GANT_HOME -Dant.home=$ANT_HOME"

startGroovy gant.Gant "$@"
