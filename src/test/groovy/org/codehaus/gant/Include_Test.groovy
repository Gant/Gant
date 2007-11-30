//  Gant -- A Groovy build framework based on scripting Ant tasks.
//
//  Copyright © 2006-7 Russel Winder
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

////////////////////////////////////////////////////////////////////////////////////////////////////
//  NB Commented out tests are ones that it is not certain should be supported.
////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 *  A test to ensure that the various include mechanisms work as they should.
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
final class Include_Test extends GantTestCase {
  def toolClassName = 'ToolClass'
  def toolClassFilePath = "/tmp/${toolClassName}.groovy"
  def toolClassText =  """
class ${toolClassName} {
  def ${toolClassName} ( Binding binding ) { }
  def flob ( ) { println ( 'flobbed.' ) }
}
"""
  def toolBuildScriptBase =  """
target ( something : '' ) { ${toolClassName}.flob ( ) }
target ( 'default' : '' ) { something ( ) }
"""
  def toolBuildScriptClass =  "includeTool <<  groovyShell.evaluate ( '''${toolClassText} ; return ${toolClassName}''' )\n" + toolBuildScriptBase
  def toolBuildScriptFile =  "includeTool <<  new File ( '${toolClassFilePath}' )\n" + toolBuildScriptBase
  def toolBuildScriptString =  "includeTool <<  '''${toolClassText}'''\n" + toolBuildScriptBase
  def targetsScriptFilePath = '/tmp/targets.gant'
  def targetsScriptText =  '''
target ( flob : '' ) { println ( 'flobbed.' ) }
''' 
  def targetsClassName = 'TargetsClass'
  def targetsClassFilePath = "/tmp/${targetsClassName}.groovy"
  def targetsClassText =  """
class ${targetsClassName} {
  def ${targetsClassName} ( Binding binding ) {
    binding.target.call ( flob : '' ) { println ( 'flobbed.' ) }
  }
}
"""
  def targetsBuildScriptBase =  """
target ( something : '' ) { flob ( ) }
target ( 'default' : '' ) { something ( ) }
"""
  def targetsBuildScriptClass =  "includeTargets <<  groovyShell.evaluate ( '''${targetsScriptText} ; return ${targetsClassName}''' , ${targetsClassName} )\n" + targetsBuildScriptBase
  def targetsBuildScriptFile =  "includeTargets <<  new File ( '${targetsScriptFilePath}' )\n" + targetsBuildScriptBase
  def targetsBuildScriptString =  "includeTargets <<  '''${targetsScriptText}'''\n" + targetsBuildScriptBase
  def targetsBuildClassClass =  "includeTargets <<  groovyShell.evaluate ( '''${targetsClassText} ; return ${targetsClassName}''' )\n" + targetsBuildScriptBase
  def targetsBuildClassFile =  "includeTargets <<  new File ( '${targetsClassFilePath}' )\n" + targetsBuildScriptBase
  def targetsBuildClassString =  "includeTargets <<  '''${targetsClassText}'''\n" + targetsBuildScriptBase
  def nonExistentFilePath = '/tmp/tmp/tmp'
  Include_Test ( ) {
    ( new File ( toolClassFilePath ) ).write( toolClassText )
    ( new File ( targetsScriptFilePath ) ).write( targetsScriptText )
    ( new File ( targetsClassFilePath ) ).write( targetsClassText )
  }
  void testToolDefaultClass ( ) {
    System.setIn ( new StringBufferInputStream ( toolBuildScriptClass ) )
    assertEquals ( 0 , gant.process ( [ '-f' ,  '-'  ] as String[] ) )
    assertEquals ( 'flobbed.\n' , output.toString ( ) ) 
  }
  void testToolDefaultFile ( ) {
    System.setIn ( new StringBufferInputStream ( toolBuildScriptFile ) )
    assertEquals ( 0 , gant.process ( [ '-f' ,  '-'  ] as String[] ) )
    assertEquals ( 'flobbed.\n' , output.toString ( ) ) 
  }
  void testToolDefaultString ( ) {
    System.setIn ( new StringBufferInputStream ( toolBuildScriptString ) )
    assertEquals ( 0 , gant.process ( [ '-f' ,  '-'  ] as String[] ) )
    assertEquals ( 'flobbed.\n' , output.toString ( ) ) 
  }
  void testToolFlobClass ( ) {
    System.setIn ( new StringBufferInputStream ( toolBuildScriptClass ) )
    assertEquals ( 1 , gant.process ( [ '-f' ,  '-'  , 'flob'] as String[] ) )
    assertEquals ( 'Target flob does not exist.\n' , output.toString ( ) ) 
  }
  void testToolFlobFile ( ) {
    System.setIn ( new StringBufferInputStream ( toolBuildScriptFile ) )
    assertEquals ( 1 , gant.process ( [ '-f' ,  '-'  , 'flob'] as String[] ) )
    assertEquals ( 'Target flob does not exist.\n' , output.toString ( ) ) 
  }
  void testToolFlobString ( ) {
    System.setIn ( new StringBufferInputStream ( toolBuildScriptString ) )
    assertEquals ( 1 , gant.process ( [ '-f' ,  '-'  , 'flob'] as String[] ) )
    assertEquals ( 'Target flob does not exist.\n' , output.toString ( ) ) 
  }
  void testToolBurbleClass ( ) {
    System.setIn ( new StringBufferInputStream ( toolBuildScriptClass ) )
    assertEquals ( 1 , gant.process ( [ '-f' ,  '-'  , 'burble'] as String[] ) )
    assertEquals ( 'Target burble does not exist.\n' , output.toString ( ) ) 
  }
  void testToolBurbleFile ( ) {
    System.setIn ( new StringBufferInputStream ( toolBuildScriptFile ) )
    assertEquals ( 1 , gant.process ( [ '-f' ,  '-'  , 'burble'] as String[] ) )
    assertEquals ( 'Target burble does not exist.\n' , output.toString ( ) ) 
  }
  void testToolBurbleString ( ) {
    System.setIn ( new StringBufferInputStream ( toolBuildScriptString ) )
    assertEquals ( 1 , gant.process ( [ '-f' ,  '-'  , 'burble'] as String[] ) )
    assertEquals ( 'Target burble does not exist.\n' , output.toString ( ) ) 
  }
  void testToolSomethingClass ( ) {
    System.setIn ( new StringBufferInputStream ( toolBuildScriptClass ) )
    assertEquals ( 0 , gant.process ( [ '-f' ,  '-'  , 'something'] as String[] ) )
    assertEquals ( 'flobbed.\n' , output.toString ( ) ) 
  }
  void testToolSomethingFile ( ) {
    System.setIn ( new StringBufferInputStream ( toolBuildScriptFile ) )
    assertEquals ( 0 , gant.process ( [ '-f' ,  '-'  , 'something'] as String[] ) )
    assertEquals ( 'flobbed.\n' , output.toString ( ) ) 
  }
  void testToolSomethingString ( ) {
    System.setIn ( new StringBufferInputStream ( toolBuildScriptString ) )
    assertEquals ( 0 , gant.process ( [ '-f' ,  '-'  , 'something'] as String[] ) )
    assertEquals ( 'flobbed.\n' , output.toString ( ) ) 
  }
  void testToolClassNoFile ( ) {
    System.setIn ( new StringBufferInputStream ( toolBuildScriptFile.replace ( toolClassFilePath , nonExistentFilePath ) ) )
    try { assertEquals ( 1 , gant.process ( [ '-f' ,  '-'  , 'flob'] as String[] ) ) }
    catch ( FileNotFoundException fnfe ) { return }
    fail ( 'Should have got a FileNotFoundException but didn\'t.' )
  }
  void testTargetsDefaultClassClass ( ) {
    System.setIn ( new StringBufferInputStream ( targetsBuildClassClass ) )
    assertEquals ( 0 , gant.process ( [ '-f' ,  '-'  ] as String[] ) )
    assertEquals ( 'flobbed.\n' , output.toString ( ) ) 
  }
  /*
  void testTargetsDefaultClassFile ( ) {
    System.setIn ( new StringBufferInputStream ( targetsBuildClassFile ) )
    assertEquals ( 0 , gant.process ( [ '-f' ,  '-'  ] as String[] ) )
    assertEquals ( 'flobbed.\n' , output.toString ( ) ) 
  }
  void testTargetsDefaultClassString ( ) {
    System.setIn ( new StringBufferInputStream ( targetsBuildClassString ) )
    assertEquals ( 0 , gant.process ( [ '-f' ,  '-'  ] as String[] ) )
    assertEquals ( 'flobbed.\n' , output.toString ( ) ) 
  }
  */
  void testTargetsFlobClassClass ( ) {
    System.setIn ( new StringBufferInputStream ( targetsBuildClassClass ) )
    assertEquals ( 0 , gant.process ( [ '-f' ,  '-'  , 'flob'] as String[] ) )
    assertEquals ( 'flobbed.\n' , output.toString ( ) ) 
  }
  /*
  void testTargetsFlobClassFile ( ) {
    System.setIn ( new StringBufferInputStream ( targetsBuildClassFile ) )
    assertEquals ( 0 , gant.process ( [ '-f' ,  '-'  , 'flob'] as String[] ) )
    assertEquals ( 'flobbed.\n' , output.toString ( ) ) 
  }
  void testTargetsFlobClassString ( ) {
    System.setIn ( new StringBufferInputStream ( targetsBuildClassString ) )
    assertEquals ( 0 , gant.process ( [ '-f' ,  '-'  , 'flob'] as String[] ) )
    assertEquals ( 'flobbed.\n' , output.toString ( ) ) 
  }
  */
  void testTargetsBurbleClassClass ( ) {
    System.setIn ( new StringBufferInputStream ( targetsBuildClassClass ) )
    assertEquals ( 1 , gant.process ( [ '-f' ,  '-'  , 'burble'] as String[] ) )
    assertEquals ( 'Target burble does not exist.\n' , output.toString ( ) ) 
  }
  /*
  void testTargetsBurbleClassFile ( ) {
    System.setIn ( new StringBufferInputStream ( targetsBuildClassFile ) )
    assertEquals ( 1 , gant.process ( [ '-f' ,  '-'  , 'burble'] as String[] ) )
    assertEquals ( 'Target burble does not exist.\n' , output.toString ( ) ) 
  }
  void testTargetsBurbleClassString ( ) {
    System.setIn ( new StringBufferInputStream ( targetsBuildClassString ) )
    assertEquals ( 1 , gant.process ( [ '-f' ,  '-'  , 'burble'] as String[] ) )
    assertEquals ( 'Target burble does not exist.\n' , output.toString ( ) ) 
  }
  */
  void testTargetsSomethingClassClass ( ) {
    System.setIn ( new StringBufferInputStream ( targetsBuildClassClass ) )
    assertEquals ( 0 , gant.process ( [ '-f' ,  '-'  , 'something'] as String[] ) )
    assertEquals ( 'flobbed.\n' , output.toString ( ) ) 
  }
  /*
  void testTargetsSomethingClassFile ( ) {
    System.setIn ( new StringBufferInputStream ( targetsBuildClassFile ) )
    assertEquals ( 0 , gant.process ( [ '-f' ,  '-'  , 'something'] as String[] ) )
    assertEquals ( 'flobbed.\n' , output.toString ( ) ) 
  }
  void testTargetsSomethingClassString ( ) {
    System.setIn ( new StringBufferInputStream ( targetsBuildClassString ) )
    assertEquals ( 0 , gant.process ( [ '-f' ,  '-'  , 'something'] as String[] ) )
    assertEquals ( 'flobbed.\n' , output.toString ( ) ) 
  }
  */
  void testTargetsClassNoFile ( ) {
    System.setIn ( new StringBufferInputStream ( targetsBuildClassFile.replace ( targetsClassFilePath , nonExistentFilePath ) ) )
    try { assertEquals ( 1 , gant.process ( [ '-f' ,  '-'  , 'flob'] as String[] ) ) }
    catch ( FileNotFoundException fnfe ) { return }
    fail ( 'Should have got a FileNotFoundException but didn\'t.' )
  }
  /*
  void testTargetsDefaultScriptClass ( ) {
    System.setIn ( new StringBufferInputStream ( targetsBuildScriptClass ) )
    assertEquals ( 0 , gant.process ( [ '-f' ,  '-'  ] as String[] ) )
    assertEquals ( 'flobbed.\n' , output.toString ( ) ) 
  }
  */
  void testTargetsDefaultScriptFile ( ) {
    System.setIn ( new StringBufferInputStream ( targetsBuildScriptFile ) )
    assertEquals ( 0 , gant.process ( [ '-f' ,  '-'  ] as String[] ) )
    assertEquals ( 'flobbed.\n' , output.toString ( ) ) 
  }
  void testTargetsDefaultScriptString ( ) {
    System.setIn ( new StringBufferInputStream ( targetsBuildScriptString ) )
    assertEquals ( 0 , gant.process ( [ '-f' ,  '-'  ] as String[] ) )
    assertEquals ( 'flobbed.\n' , output.toString ( ) ) 
  }
  /*
  void testTargetsFlobScriptClass ( ) {
    System.setIn ( new StringBufferInputStream ( targetsBuildScriptClass ) )
    assertEquals ( 0 , gant.process ( [ '-f' ,  '-'  , 'flob'] as String[] ) )
    assertEquals ( 'flobbed.\n' , output.toString ( ) ) 
  }
  */
  void testTargetsFlobScriptFile ( ) {
    System.setIn ( new StringBufferInputStream ( targetsBuildScriptFile ) )
    assertEquals ( 0 , gant.process ( [ '-f' ,  '-'  , 'flob'] as String[] ) )
    assertEquals ( 'flobbed.\n' , output.toString ( ) ) 
  }
  void testTargetsFlobScriptString ( ) {
    System.setIn ( new StringBufferInputStream ( targetsBuildScriptString ) )
    assertEquals ( 0 , gant.process ( [ '-f' ,  '-'  , 'flob'] as String[] ) )
    assertEquals ( 'flobbed.\n' , output.toString ( ) ) 
  }
  /*
  void testTargetsBurbleScriptClass ( ) {
    System.setIn ( new StringBufferInputStream ( targetsBuildScriptClass ) )
    assertEquals ( 1 , gant.process ( [ '-f' ,  '-'  , 'burble'] as String[] ) )
    assertEquals ( 'Target burble does not exist.\n' , output.toString ( ) ) 
  }
  */
  void testTargetsBurbleScriptFile ( ) {
    System.setIn ( new StringBufferInputStream ( targetsBuildScriptFile ) )
    assertEquals ( 1 , gant.process ( [ '-f' ,  '-'  , 'burble'] as String[] ) )
    assertEquals ( 'Target burble does not exist.\n' , output.toString ( ) ) 
  }
  void testTargetsBurbleScriptString ( ) {
    System.setIn ( new StringBufferInputStream ( targetsBuildScriptString ) )
    assertEquals ( 1 , gant.process ( [ '-f' ,  '-'  , 'burble'] as String[] ) )
    assertEquals ( 'Target burble does not exist.\n' , output.toString ( ) ) 
  }
  /*
  void testTargetsSomethingScriptClass ( ) {
    System.setIn ( new StringBufferInputStream ( targetsBuildScriptClass ) )
    assertEquals ( 1 , gant.process ( [ '-f' ,  '-'  , 'something'] as String[] ) )
    assertEquals ( 'flobbed.\n' , output.toString ( ) ) 
  }
  */
  void testTargetsSomethingScriptFile ( ) {
    System.setIn ( new StringBufferInputStream ( targetsBuildScriptFile ) )
    assertEquals ( 0 , gant.process ( [ '-f' ,  '-'  , 'something'] as String[] ) )
    assertEquals ( 'flobbed.\n' , output.toString ( ) ) 
  }
  void testTargetsSomethingScriptString ( ) {
    System.setIn ( new StringBufferInputStream ( targetsBuildScriptString ) )
    assertEquals ( 0 , gant.process ( [ '-f' ,  '-'  , 'something'] as String[] ) )
    assertEquals ( 'flobbed.\n' , output.toString ( ) ) 
  }
  void testTargetsScriptNoFile ( ) {
    System.setIn ( new StringBufferInputStream ( targetsBuildScriptFile.replace ( targetsScriptFilePath , '/tmp/tmp/tmp' ) ) )
    try { assertEquals ( 1 , gant.process ( [ '-f' ,  '-'  , 'flob'] as String[] ) ) }
    catch ( FileNotFoundException fnfe ) { return }
    fail ( 'Should have got a FileNotFoundException but didn\'t.' )
  }

  ////////  Test multiple include of the same targets.

  void testTargetsMultipleIncludeDefaultScriptFile ( ) {
    System.setIn ( new StringBufferInputStream ( "includeTargets <<  new File ( '${targetsScriptFilePath}' )\n" + targetsBuildScriptFile ) )
    assertEquals ( 0 , gant.process ( [ '-f' ,  '-'  ] as String[] ) )
    assertEquals ( 'flobbed.\n' , output.toString ( ) )
  }
  void testTargetsMultipleIncludeDefaultScriptString ( ) {
    System.setIn ( new StringBufferInputStream ( "includeTargets <<  '''${targetsScriptText}'''\n" + targetsBuildScriptString ) )
    assertEquals ( 0 , gant.process ( [ '-f' ,  '-'  ] as String[] ) )
    assertEquals ( 'flobbed.\n' , output.toString ( ) )
  }
  void testTargetsMultipleIncludeFlobScriptFile ( ) {
    System.setIn ( new StringBufferInputStream ( "includeTargets <<  new File ( '${targetsScriptFilePath}' )\n" + targetsBuildScriptFile ) )
    assertEquals ( 0 , gant.process ( [ '-f' ,  '-'  , 'flob' ] as String[] ) )
    assertEquals ( 'flobbed.\n' , output.toString ( ) )
  }
  void testTargetsMultipleIncludeFlobScriptString ( ) {
    System.setIn ( new StringBufferInputStream ( "includeTargets <<  '''${targetsScriptText}'''\n" + targetsBuildScriptString ) )
    assertEquals ( 0 , gant.process ( [ '-f' ,  '-'  , 'flob' ] as String[] ) )
    assertEquals ( 'flobbed.\n' , output.toString ( ) )
  }
  void testTargetsMultipleIncludeBurbleScriptFile ( ) {
    System.setIn ( new StringBufferInputStream ( "includeTargets <<  new File ( '${targetsScriptFilePath}' )\n" + targetsBuildScriptFile ) )
    assertEquals ( 1 , gant.process ( [ '-f' ,  '-'  , 'burble'] as String[] ) )
    assertEquals ( 'Target burble does not exist.\n' , output.toString ( ) ) 
  }
  void testTargetsMultipleIncludeBurbleScriptString ( ) {
    System.setIn ( new StringBufferInputStream ( "includeTargets <<  '''${targetsScriptText}'''\n" + targetsBuildScriptString ) )
    assertEquals ( 1 , gant.process ( [ '-f' ,  '-'  , 'burble'] as String[] ) )
    assertEquals ( 'Target burble does not exist.\n' , output.toString ( ) ) 
  }
  void testTargetsMultipleIncludeSomethingScriptFile ( ) {
    System.setIn ( new StringBufferInputStream ( "includeTargets <<  new File ( '${targetsScriptFilePath}' )\n" + targetsBuildScriptFile ) )
    assertEquals ( 0 , gant.process ( [ '-f' ,  '-'  , 'flob' ] as String[] ) )
    assertEquals ( 'flobbed.\n' , output.toString ( ) )
  }
  void testTargetsMultipleIncludeSomethingScriptString ( ) {
    System.setIn ( new StringBufferInputStream ( "includeTargets <<  '''${targetsScriptText}'''\n" + targetsBuildScriptString ) )
    assertEquals ( 0 , gant.process ( [ '-f' ,  '-'  , 'flob' ] as String[] ) )
    assertEquals ( 'flobbed.\n' , output.toString ( ) )
  }
}
