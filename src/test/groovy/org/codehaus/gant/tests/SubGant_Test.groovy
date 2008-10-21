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

package org.codehaus.gant.tests

/**
 *  A test to ensure that creating a new Gant object and using works. 
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
final class SubGant_Test extends GantTestCase {
  final targetName = 'targetName'
  final resultMessage = 'Do thing.'
  File buildFile 
  public void setUp ( ) {
    super.setUp ( )
    buildFile = File.createTempFile ( 'gant_' , '_SubGant_Test' ) // Must ensure name is a valid Java class name.
  }
  public void tearDown ( ) {
    super.tearDown ( )
    buildFile.delete ( )
  }
  public void testSimple ( ) {
    def buildScript = """
def internalTarget = 'doTarget'
target ( ( internalTarget ) : '' ) { println ( '${resultMessage}' ) }
target ( '${targetName}' : '' ) {
  newthing = new gant.Gant ( )
  newthing.loadScript ( new File ( '${isWindows ? buildFile.path.replace ( '\\' , '\\\\' ) : buildFile.path}' ) )
  newthing.processTargets ( internalTarget )
}
"""
    buildFile.write ( buildScript )
    script = buildScript
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultMessage + '\n' , output )
  }
  public void testWithBinding ( ) {
    def buildScript = """
def internalTarget = 'doTarget'
target ( ( internalTarget ) : '' ) { println ( '${resultMessage}' ) }
target ( '${targetName}' : '' ) {
  newthing = new gant.Gant ( binding.clone ( ) )
  newthing.loadScript ( new File ( '${isWindows ? buildFile.path.replace ( '\\' , '\\\\' ) : buildFile.path}' ) )
  newthing.processTargets ( internalTarget )
}
"""
    buildFile.write ( buildScript )
    script = buildScript
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultMessage + '\n' , output )
  }
  /*
  public void testSettingBindingVariable ( ) {
    def buildScript = '''
target ( doOutput : '' ) {
  println ( 'flobadob = ' + flobadob )
}
target ( doSubGant : '' ) {
  def newBinding = binding.clone ( )
  newBinding.flobadob = 'weed'
  newthing = new gant.Gant ( 'build.gant' , newBinding )
  newthing.processCmdLineTargets ( 'doOutput' )
}
'''
    buildFile.write ( buildScript )
    script = buildScript
    assertEquals ( 0 , processCmdLineTargets ( 'doSubGant' ) )
    assertEquals ( 'flobadoc = weed\n' , output )
    assertEquals ( -2 , processCmdLineTargets ( 'doOutput' ) )
  }
  */
}
