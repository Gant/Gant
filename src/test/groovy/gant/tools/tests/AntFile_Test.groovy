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

package gant.tools.tests

import org.codehaus.gant.tests.GantTestCase

/**
 *  A test to ensure that the AntFile tool is not broken.
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
final class AntFile_Test extends GantTestCase {
  void testExecutable ( ) {
    def temporaryFile = File.createTempFile ( 'gant-antFile-' ,  '-executable' )
    temporaryFile.write ( '''
<project name="Gant Ant Use Test" default="execute">
  <target name="execute" description="Do something.">
    <echo message="Hello world."/>
  </target>
</project>
''' )
    script = """includeTool << gant.tools.AntFile
AntFile.includeTargets ( '${temporaryFile.path}' )
setDefaultTarget ( 'execute' )
"""
    assertEquals ( 0 , processTargets ( ) )
    assertEquals ( '''     [echo] Hello world.
''' , output )
    temporaryFile.delete ( )
  }
  void testListing ( ) {
    def temporaryFile = File.createTempFile ( 'gant-antFile-' ,  '-executable' )
    temporaryFile.write ( '''
<project name="Gant Ant Use Test" default="execute">
  <target name="execute" description="Do something.">
    <echo message="Hello world."/>
  </target>
</project>
''' )
    script = """includeTool << gant.tools.AntFile
AntFile.includeTargets ( '${temporaryFile.path}' )
setDefaultTarget ( 'execute' )
"""
    assertEquals ( 0 , gant.processArgs ( [ '-p' , '-f' , '-' ] as String[] ) )
    assertEquals ( '''
 execute  Do something.

Default target is execute.

''' , output )
    temporaryFile.delete ( )
  }
}
