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

import org.codehaus.gant.GantBinding
import org.codehaus.gant.GantState

/**
 *  A class providing methods for executing processes in all subdirectories of the working directory
 *  for use in Gant scripts.  This is not really a target but a target support method.
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
final class Subdirectories {
  private final GantBinding binding ;
  Subdirectories ( final GantBinding binding ) { this.binding = binding ; }
  void runSubprocess ( final String command , final File directory ) {
    if ( GantState.verbosity > GantState.NORMAL ) { println "\n============ ${directory} ================" }
    //  If we allowed ourselves Java SE 5.0 then we could use ProcessBuilder but we restrict ourselves to Java 1.4.
    //def process = ( new ProcessBuilder ( [ 'sh' , '-c' , command ] )).directory ( directory ).start ( )
    //
    //  Groovy 1.0 RC-01 cannot deal with null in the first parameter.
    //def process = command.execute ( null , directory )
    def process = command.execute ( [ ] , directory )
    if ( GantState.verbosity > GantState.QUIET ) {
      ( new InputStreamReader ( process.err ) ).eachLine { line -> System.err.println ( line ) }
      ( new InputStreamReader ( process.in ) ).eachLine { line -> println ( line ) }
    }
    process.waitFor ( )
  }
  void forAllSubdirectoriesRun ( final String command ) {
    ( new File ( '.' ) ).eachDir { directory -> runSubprocess ( command , directory ) }
  }
  void forAllSubdirectoriesAnt ( final String target ) { forAllSubdirectoriesRun ( 'ant ' + target ) }
  void forAllSubdirectoriesGant ( final String target ) { forAllSubdirectoriesRun ( 'gant ' + target ) }  
}
