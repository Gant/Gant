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
  private final ivyURI = 'antlib:org.apache.ivy.ant'
  private final classpathRef = 'ivy.class.path'
  private String ivyJarPath = null
  Ivy ( final Binding binding ) {
    this.binding = binding
    if ( System.properties.'groovy.home' ) {
      ivyJarPath = System.properties.'groovy.home' + System.properties.'file.separator' + 'lib'
    }
    else {
      throw new RuntimeException ( 'groovy.home property not set, and cannot guess location of Ivy jar.' )
    }
    constructIvyTask ( )
  }
  Ivy ( final Binding binding , final Map map ) {
    this.binding = binding
    if ( map.containsKey ( 'ivyJarPath' ) ) { ivyJarPath = map.ivyJarPath }
    else {
      if ( System.properties.'groovy.home' ) {
        ivyJarPath = System.properties.'groovy.home' + System.properties.'file.separator' + 'lib'
      }
      else {
        throw new RuntimeException ( 'Neither ivyJarPath or groovy.home set, and cannot guess location of Ivy jar.' )
      }
    }
    constructIvyTask ( )
  }
  private void constructIvyTask ( ) {
    binding.Ant.path ( id : classpathRef ) { binding.Ant.fileset ( dir : ivyJarPath , includes : 'ivy*.jar' ) }
    binding.Ant.taskdef ( resource : 'org/apache/ivy/ant/antlib.xml' , uri : ivyURI , classpathref : classpathRef )
  }
  //  To save having to maintain lists of the functions available, simply redirect all method calls to the Ant object.
  def invokeMethod ( String name , args ) { binding.Ant.invokeMethod ( ivyURI + ':' + name , args ) }
}
