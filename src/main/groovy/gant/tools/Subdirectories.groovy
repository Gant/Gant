//  Gant -- A Groovy way of scripting Ant tasks.
//
//  Copyright © 2006-9,2010 Russel Winder
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
import org.codehaus.gant.GantState

/**
 *  Provides methods for executing processes in all subdirectories of the working directory.
 *
 *  @author Russel Winder <russel@russel.org.uk>
 */
final class Subdirectories {
  private final GantBinding binding ;
  /**
   *  Constructor for the "includeTool <<" usage.
   *
   *  @param binding The <code>GantBinding</code> to bind to.
   */
  Subdirectories ( final GantBinding binding ) { this.binding = binding ; }
  /**
   *  Constructor for the "includeTool **" usage.  It is assumed that the <code>Map</code> entry provides a
   *  filename or a list of filenames of Ant XML files to load. 
   *
   *  @param binding The <code>GantBinding</code> to bind to.
   *  @param map The <code>Map</code> of initialization parameters.
   */
  Subdirectories ( final GantBinding binding , final Map<String,String> map ) { this.binding = binding ; }
  /**
   *  Run a shell command in a named directory.
   *
   *  @param command The shell command to execute.
   *  @param directory Path of the directory in which to execute the shell command.
   */
  void runSubprocess ( final String command , final File directory ) {
    binding.ant.project.log ( "\n============ ${directory} ================" , GantState.VERBOSE )
    def process = command.execute ( null , directory )
    if ( GantState.verbosity >= GantState.NORMAL ) {
      ( new InputStreamReader ( process.err ) ).eachLine { line -> System.err.println ( line ) }
      ( new InputStreamReader ( process.in ) ).eachLine { line -> println ( line ) }
    }
    process.waitFor ( )
  }
  /**
   *  Run a shell command in all the subdirectories of this one.
   *
   *  @param command The shell command to execute.
   */
  void forAllSubdirectoriesRun ( final String command ) {
    ( new File ( '.' ) ).eachDir { directory -> runSubprocess ( command , directory ) }
  }
  /**
   *  Execute an Ant target in all the subdirectories of this one.
   *
   *  @param target The target to execute.
   */
  void forAllSubdirectoriesAnt ( final String target ) { forAllSubdirectoriesRun ( 'ant ' + target ) }
  /**
   *  Execute a Gant target in all the subdirectories of this one.
   *
   *  @param target The target to execute.
   */
  void forAllSubdirectoriesGant ( final String target ) { forAllSubdirectoriesRun ( 'gant ' + target ) }  
}
