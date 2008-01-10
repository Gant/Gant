//  Gant -- A Groovy build framework based on scripting Ant tasks.
//
//  Copyright © 2008 Russel Winder
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

package org.codehaus.gant.ant ;

import java.io.File ;

import org.apache.tools.ant.BuildException ;
import org.apache.tools.ant.Task ;

/**
 *  Execute a Gant script.
 *
 * <p>This Ant task provides a Gant capability for continuous integration systems that do not directly
 * support Gant but only Ant.</p>
 *
 * @author Russel Winder
 */
public class Gant extends Task {
  /**
   * Gantfile to load, default is build.gant.
   */
  private String file = "build.gant" ;
  /**
   * Target to achieve.
   */
  private String target = "" ;
  /**
   *  Set the name of the Gantfile.
   *
   *  @param file The Gantfile.
   */
  public void setFile ( final String file ) { this.file = file ; }
  /**
   *  Set the target to be run.
   *
   *  @param target The target.
   */
  public void setTarget ( final String target ) { this.target = target ; }
  /**
   * Load the file and then execute it
   */
  public void execute ( ) throws BuildException {
    if ( ! ( new File ( file ) ).exists ( ) ) { throw new BuildException ( "Gantfile does not exist." , getLocation ( ) ) ; }
    final gant.Gant gant = new gant.Gant ( file ) ;
    final int returnCode ;
    if ( target.equals ( "" ) ) { returnCode = gant.processTargets ( ) ; }
    else { returnCode = gant.processTargets ( target ) ; }
    if ( returnCode != 0 ) { throw new BuildException ( "Gant execution failed with return code " + Integer.toString ( returnCode ) + "." , getLocation ( ) ) ; }
  }
}
