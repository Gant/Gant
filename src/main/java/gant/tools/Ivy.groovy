//  Gant -- A Groovy build tool based on scripting Ant tasks
//
//  Copyright © 2006 Russel Winder <russel@russel.org.uk>
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
 *  A class to provide support for using Ivy.  Assumes the ivy jar files are in $GROOVY_HOME.
 *
 *  @author Russel Winder <russel@russel.org.uk>
 *  @version $Revision$ $Date$
 */
final class Ivy {
  private final Binding binding ;
  private final classpath = 'ivy.class.path'
  Ivy ( final Binding binding ) {
    this.binding = binding ;
    /*
     *  This is what we want to do:

    binding.Ant.path ( id : classpath ) { fileset ( dir : System.getenv ( ).GROOVY_HOME , includes : 'ivy*.jar' ) }

    *  but this causes real hassles when using JDK versions prior to 1.5.  But we know that groovy.home is a
    *  property in the AntBuilder so just use that.
    */
    binding.Ant.path ( id : classpath ) { fileset ( dir : binding.Ant.project.properties.'groovy.home' , includes : 'ivy*.jar' ) }
    binding.Ant.taskdef ( resource : 'fr/jayasoft/ivy/ant/antlib.xml' , classpathref : classpath )
  }
  void cachepath ( map ) { binding.Ant.cachepath ( map ) }
  void configure ( map ) { binding.Ant.configure ( map ) }
  void publish ( map ) { binding.Ant.publish ( map ) }
  void report ( map ) { binding.Ant.report ( map ) }
  void resolve ( map ) { binding.Ant.resolve ( map ) }
  void retrieve ( map ) { binding.Ant.retrieve ( map ) }
}
