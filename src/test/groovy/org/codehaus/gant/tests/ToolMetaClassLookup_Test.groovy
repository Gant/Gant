//  Gant -- A Groovy way of scripting Ant tasks.
//
//  Copyright © 2006-10 Russel Winder
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

package org.codehaus.gant.tests

import org.codehaus.gant.GantBuilder
import org.codehaus.gant.GantState

/**
 *  A test to ensure that the target listing works. 
 *
 *  @author Russel Winder <russel@russel.org.uk>
 */
final class ToolMetaClassLookup_Test extends GantTestCase {
  private final something = 'something'
  private final subdirectory = new File ( 'aSubdirectoryOfTheCurrentOneThatIsUnlikelyToExist' )
  private final gantBuilder = new GantBuilder ( ) ; {
    gantBuilder.logger.setMessageOutputLevel ( GantState.SILENT )
  }
  private final message = 'yes'
  void setUp ( ) {
    super.setUp ( )
    if ( subdirectory.exists ( ) ) { fail ( 'The name "' + directory.name + '" is in use.' ) }
    gantBuilder.mkdir ( dir : subdirectory.name )
    def command = ( isWindows ? 'cmd /c echo ' : 'echo ' ) + message
    script = """
includeTool << gant.tools.Subdirectories
target ( ${something} : '' ) { subdirectories.runSubprocess ( '${command}' , new File ( '${subdirectory.name}' ) ) }
setDefaultTarget ( ${something} )
"""
  }
  void tearDown ( ) { gantBuilder.delete ( dir : subdirectory.name , quiet : 'true' ) }

  void testDefault ( ) {
    assertEquals ( 0 , processCmdLineTargets ( ) )
    assertEquals ( resultString ( something , message + '\n' ) , output )
    assertEquals ( '' , error )
  }
  void testTargetNotPresent ( ) {
    final targetName = 'blah'
    assertEquals ( -11 , processCmdLineTargets ( targetName ) )
    assertEquals ( '' , output )
    assertEquals ( "Target ${targetName} does not exist.\n" , error ) 
  }
  void testSomething ( ) {
    assertEquals ( 0 , processCmdLineTargets ( something ) )
    assertEquals ( resultString ( something , message + '\n' ) , output ) 
    assertEquals ( '' , error )
  }
}
