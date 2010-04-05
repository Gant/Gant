//  Gant -- A Groovy way of scripting Ant tasks.
//
//  Copyright © 2008-10 Russel Winder
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
 *  @author Russel Winder <russel@russel.org.uk>
 */
final class SubGant_Test extends GantTestCase {
  private final targetName = 'targetName'
  private final internalTarget = 'doTarget'
  private final resultMessage = 'Do thing.'
  private File buildFile
  public void setUp ( ) {
    super.setUp ( )
    buildFile = File.createTempFile ( 'gant_' , '_SubGant_Test' ) // Must ensure name is a valid Java class name.
  }
  public void tearDown ( ) {
    super.tearDown ( )
    buildFile.delete ( )
  }
  public void testSimple ( ) {
    final buildScript = """
target ( ${internalTarget} : '' ) { println ( '${resultMessage}' ) }
target ( ${targetName} : '' ) {
  subGant = new gant.Gant ( )
  subGant.loadScript ( new File ( '${escapeWindowsPath ( buildFile.path )}' ) )
  subGant.processTargets ( '${internalTarget}' )
}
"""
    buildFile.write ( buildScript )
    script = buildScript
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , resultString ( internalTarget , resultMessage + '\n' ) ) , output )
  }
  public void testWithBinding ( ) {
    final buildScript = """
target ( ${internalTarget} : '' ) { println ( '${resultMessage}' ) }
target ( ${targetName} : '' ) {
  subGant = new gant.Gant ( binding.clone ( ) )
  subGant.loadScript ( new File ( '${escapeWindowsPath ( buildFile.path )}' ) )
  subGant.processTargets ( '${internalTarget}' )
}
"""
    buildFile.write ( buildScript )
    script = buildScript
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , resultString ( internalTarget , resultMessage + '\n' ) ) , output )
  }
  public void testSettingBindingVariable ( ) {
    final flobadob = 'flobadob'
    final weed = 'weed'
    final buildScript = """
target ( ${internalTarget} : '' ) { println ( '${flobadob} = ' + ${flobadob} ) }
target ( ${targetName} : '' ) {
  def newBinding = binding.clone ( )
  newBinding.${flobadob} = '${weed}'
  subGant = new gant.Gant ( newBinding )
  subGant.loadScript ( new File ( '${escapeWindowsPath ( buildFile.path )}' ) )
  subGant.processTargets ( '${internalTarget}' )
}
"""
    buildFile.write ( buildScript )
    script = buildScript
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , resultString ( internalTarget , flobadob + ' = ' + weed + '\n' ) ) , output )
  }
}
