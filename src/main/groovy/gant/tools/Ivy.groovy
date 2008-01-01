//  Gant -- A Groovy build framework based on scripting Ant tasks.
//
//  Copyright © 2006-8 Russel Winder
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

package gant.tools

/**
 *  A class to provide support for using Ivy.  Assumes the ivy jar file is in $GROOVY_HOME/lib.
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
final class Ivy {
  private final Binding binding ;
  private final classpathRef = 'ivy.class.path'
  Ivy ( final Binding binding ) {
    this.binding = binding
    binding.Ant.path ( id : classpathRef ) { binding.Ant.fileset ( dir : System.properties.'groovy.home' + System.properties.'file.separator' + 'lib' , includes : 'ivy*.jar' ) }
    binding.Ant.taskdef ( resource : 'org/apache/ivy/ant/antlib.xml' , classpathref : classpathRef )
  }
  //  To save having to maintain lists of the functions available, simply redirect all method calls to the Ant object.
  def invokeMethod ( String name , args ) { binding.Ant.invokeMethod ( name , args ) }
}
