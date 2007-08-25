#! /usr/bin/env groovy

//  Gant -- A Groovy build tool based on scripting Ant tasks
//
//  Copyright © 2006-7 Russel Winder <russel@russel.org.uk>
//
//  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
//  compliance with the License. You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software distributed under the License is
//  distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
//  implied. See the License for the specific language governing permissions and limitations under the
//  License.
//
//  Author : Russel Winder <russel@russel.org.uk>
//  $Revision$
//  $Date$

//  This Groovy script is for installing the Gant bits and pieces in the situation where the person doing
//  the install chooses not to extract the zip or tarball directly into $GROOVY_HOME.

def ant = new AntBuilder ( )
def groovyHome =System.properties.'groovy.home'
ant.copy ( todir : groovyHome ) {
  fileset ( dir : '.' , includes : 'bin' + System.properties.'file.separator' + 'gant*' )
  fileset ( dir : '.' , includes : 'lib' + System.properties.'file.separator' + 'gant*.jar' )
  fileset ( dir : '.' , includes : 'lib' + System.properties.'file.separator' + 'ivy*.jar' )
  fileset ( dir : '.' , includes : 'lib' + System.properties.'file.separator' + 'maven*.jar' )
}
ant.chmod ( perm : 'a+x' ) {
  fileset ( dir : groovyHome + System.properties.'file.separator' + 'bin' , includes : 'gant*' )
}
