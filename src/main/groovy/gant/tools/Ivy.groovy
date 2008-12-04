//  Gant -- A Groovy way of scripting Ant tasks.
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

import org.codehaus.gant.GantBinding

/**
 *  Provide support for using Ivy.  This simply redirects all method calls to the standard
 * <code>GantBuilder</code> instance, which in turn selects the method from the Ivy jar. 
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
final class Ivy {
  private final GantBinding binding ;
  private final ivyURI = 'antlib:org.apache.ivy.ant'
  /**
   *  Constructor to support "includeTool <<" usage.  Assumes that an Ivy jar is already in the classpath.
   *  The standard Gant installation includes an Ivy jar and it is automatically included in the classpath.
   *
   *  @param binding The <code>GantBinding</code> to bind to.
   */
  Ivy ( final GantBinding binding ) {
    this.binding = binding
    binding.ant.taskdef ( resource : 'org/apache/ivy/ant/antlib.xml' , uri : ivyURI )
  }
 /**
   *  Constructor to support "includeTool **" usage.  By default assumes that an Ivy jar is already in the
   *  classpath.  The standard Gant installation includes an Ivy jar and it is automatically included in the
   *  classpath.  However the <code>ivyJarPath</code> field can be set to allow explicit specification of
   *  the location of the Ivy jar.
   *
   *  @param binding The <code>GantBinding</code> to bind to.
   *  @param map The <code>Map</code> of parameters for intialization.
   */
  Ivy ( final GantBinding binding , final Map map ) {
    this.binding = binding
    if ( map.containsKey ( 'ivyJarPath' ) ) {
      final classpathId = 'ivy.class.path'
      binding.ant.path ( id : classpathId ) { binding.ant.fileset ( dir : map.ivyJarPath , includes : 'ivy*.jar' ) }
      binding.ant.taskdef ( resource : 'org/apache/ivy/ant/antlib.xml' , uri : ivyURI , classpathref : classpathId )
    }
    else {
      binding.ant.taskdef ( resource : 'org/apache/ivy/ant/antlib.xml' , uri : ivyURI )
    }
  }
  //  To save having to maintain lists of the functions available, simply redirect all method calls to the GantBuilder object.
  def invokeMethod ( String name , args ) { binding.ant.invokeMethod ( ivyURI + ':' + name , args ) }
}
