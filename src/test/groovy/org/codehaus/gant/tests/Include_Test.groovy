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
    script = toolBuildScriptClass
    assertEquals ( 0 , processTargets ( ) )
    assertEquals ( 'flobbed.\n' , output ) 
  }
  void testToolDefaultFile ( ) {
    script = toolBuildScriptFile
    assertEquals ( 0 , processTargets ( ) )
    assertEquals ( 'flobbed.\n' , output ) 
  }
  void testToolDefaultString ( ) {
    script = toolBuildScriptString
    assertEquals ( 0 , processTargets ( ) )
    assertEquals ( 'flobbed.\n' , output ) 
  }
  void testToolFlobClass ( ) {
    script = toolBuildScriptClass
    assertEquals ( 11 , processTargets ( 'flob') )
    assertEquals ( 'Target flob does not exist.\n' , output ) 
  }
  void testToolFlobFile ( ) {
    script = toolBuildScriptFile
    assertEquals ( 11 , processTargets ( 'flob') )
    assertEquals ( 'Target flob does not exist.\n' , output ) 
  }
  void testToolFlobString ( ) {
    script = toolBuildScriptString
    assertEquals ( 11 , processTargets ( 'flob') )
    assertEquals ( 'Target flob does not exist.\n' , output ) 
  }
  void testToolBurbleClass ( ) {
    script = toolBuildScriptClass
    assertEquals ( 11 , processTargets ( 'burble') )
    assertEquals ( 'Target burble does not exist.\n' , output ) 
  }
  void testToolBurbleFile ( ) {
    script = toolBuildScriptFile
    assertEquals ( 11 , processTargets ( 'burble') )
    assertEquals ( 'Target burble does not exist.\n' , output ) 
  }
  void testToolBurbleString ( ) {
    script = toolBuildScriptString
    assertEquals ( 11 , processTargets ( 'burble') )
    assertEquals ( 'Target burble does not exist.\n' , output ) 
  }
  void testToolSomethingClass ( ) {
    script = toolBuildScriptClass
    assertEquals ( 0 , processTargets ( 'something') )
    assertEquals ( 'flobbed.\n' , output ) 
  }
  void testToolSomethingFile ( ) {
    script = toolBuildScriptFile
    assertEquals ( 0 , processTargets ( 'something') )
    assertEquals ( 'flobbed.\n' , output ) 
  }
  void testToolSomethingString ( ) {
    script = toolBuildScriptString
    assertEquals ( 0 , processTargets ( 'something') )
    assertEquals ( 'flobbed.\n' , output ) 
  }
  void testToolClassNoFile ( ) {
    script = toolBuildScriptFile.replace ( toolClassFilePath , nonExistentFilePath )
    assertEquals ( 2 , processTargets ( 'flob') )
    assertEquals ( 'Standard input, line 1 -- Error evaluating Gantfile: ' + nonExistentFilePath + ' (No such file or directory)\n' , output )
  }
  void testTargetsDefaultClassClass ( ) {
    script = targetsBuildClassClass
    assertEquals ( 0 , processTargets ( ) )
    assertEquals ( 'flobbed.\n' , output ) 
  }
  /*
  void testTargetsDefaultClassFile ( ) {
    script = targetsBuildClassFile
    assertEquals ( 0 , processTargets ( ) )
    assertEquals ( 'flobbed.\n' , output ) 
  }
  void testTargetsDefaultClassString ( ) {
    script = targetsBuildClassString
    assertEquals ( 0 , processTargets ( ) )
    assertEquals ( 'flobbed.\n' , output ) 
  }
  */
  void testTargetsFlobClassClass ( ) {
    script = targetsBuildClassClass
    assertEquals ( 0 , processTargets ( 'flob') )
    assertEquals ( 'flobbed.\n' , output ) 
  }
  /*
  void testTargetsFlobClassFile ( ) {
    script = targetsBuildClassFile
    assertEquals ( 0 , processTargets ( 'flob') )
    assertEquals ( 'flobbed.\n' , output ) 
  }
  void testTargetsFlobClassString ( ) {
    script = targetsBuildClassString
    assertEquals ( 0 , processTargets ( 'flob') )
    assertEquals ( 'flobbed.\n' , output ) 
  }
  */
  void testTargetsBurbleClassClass ( ) {
    script = targetsBuildClassClass
    assertEquals ( 11 , processTargets ( 'burble') )
    assertEquals ( 'Target burble does not exist.\n' , output ) 
  }
  /*
  void testTargetsBurbleClassFile ( ) {
    script = targetsBuildClassFile
    assertEquals ( 11 , processTargets ( 'burble') )
    assertEquals ( 'Target burble does not exist.\n' , output ) 
  }
  void testTargetsBurbleClassString ( ) {
    script = targetsBuildClassString
    assertEquals ( 11 , processTargets ( 'burble') )
    assertEquals ( 'Target burble does not exist.\n' , output ) 
  }
  */
  void testTargetsSomethingClassClass ( ) {
    script = targetsBuildClassClass
    assertEquals ( 0 , processTargets ( 'something') )
    assertEquals ( 'flobbed.\n' , output ) 
  }
  /*
  void testTargetsSomethingClassFile ( ) {
    script = targetsBuildClassFile
    assertEquals ( 0 , processTargets ( 'something') )
    assertEquals ( 'flobbed.\n' , output ) 
  }
  void testTargetsSomethingClassString ( ) {
    script = targetsBuildClassString
    assertEquals ( 0 , processTargets ( 'something') )
    assertEquals ( 'flobbed.\n' , output ) 
  }
  */
  void testTargetsClassNoFile ( ) {
    script = targetsBuildClassFile.replace ( targetsClassFilePath , nonExistentFilePath )
    assertEquals ( 2 , processTargets ( 'flob') )
    //  This is a weird message, should be better than this.
    assertEquals ( 'Standard input, line 1 -- Error evaluating Gantfile: ' + nonExistentFilePath + ' (' + nonExistentFilePath + ')\n' , output )
  }
  /*
  void testTargetsDefaultScriptClass ( ) {
    script = targetsBuildScriptClass
    assertEquals ( 0 , processTargets ( ) )
    assertEquals ( 'flobbed.\n' , output ) 
  }
  */
  void testTargetsDefaultScriptFile ( ) {
    script = targetsBuildScriptFile
    assertEquals ( 0 , processTargets ( ) )
    assertEquals ( 'flobbed.\n' , output ) 
  }
  void testTargetsDefaultScriptString ( ) {
    script = targetsBuildScriptString
    assertEquals ( 0 , processTargets ( ) )
    assertEquals ( 'flobbed.\n' , output ) 
  }
  /*
  void testTargetsFlobScriptClass ( ) {
    script = targetsBuildScriptClass
    assertEquals ( 0 , processTargets ( 'flob') )
    assertEquals ( 'flobbed.\n' , output ) 
  }
  */
  void testTargetsFlobScriptFile ( ) {
    script = targetsBuildScriptFile
    assertEquals ( 0 , processTargets ( 'flob') )
    assertEquals ( 'flobbed.\n' , output ) 
  }
  void testTargetsFlobScriptString ( ) {
    script = targetsBuildScriptString
    assertEquals ( 0 , processTargets ( 'flob') )
    assertEquals ( 'flobbed.\n' , output ) 
  }
  /*
  void testTargetsBurbleScriptClass ( ) {
    script = targetsBuildScriptClass
    assertEquals ( 11 , processTargets ( 'burble') )
    assertEquals ( 'Target burble does not exist.\n' , output ) 
  }
  */
  void testTargetsBurbleScriptFile ( ) {
    script = targetsBuildScriptFile
    assertEquals ( 11 , processTargets ( 'burble') )
    assertEquals ( 'Target burble does not exist.\n' , output ) 
  }
  void testTargetsBurbleScriptString ( ) {
    script = targetsBuildScriptString
    assertEquals ( 11 , processTargets ( 'burble') )
    assertEquals ( 'Target burble does not exist.\n' , output ) 
  }
  /*
  void testTargetsSomethingScriptClass ( ) {
    script = targetsBuildScriptClass
    assertEquals ( 11 , processTargets ( 'something') )
    assertEquals ( 'flobbed.\n' , output ) 
  }
  */
  void testTargetsSomethingScriptFile ( ) {
    script = targetsBuildScriptFile
    assertEquals ( 0 , processTargets ( 'something') )
    assertEquals ( 'flobbed.\n' , output ) 
  }
  void testTargetsSomethingScriptString ( ) {
    script = targetsBuildScriptString
    assertEquals ( 0 , processTargets ( 'something') )
    assertEquals ( 'flobbed.\n' , output ) 
  }
  void testTargetsScriptNoFile ( ) {
    script = targetsBuildScriptFile.replace ( targetsScriptFilePath , nonExistentFilePath )
    assertEquals ( 2 , processTargets ( 'flob') )
    //  This is a weird message, should be better than this.
    assertEquals ( 'Standard input, line 1 -- Error evaluating Gantfile: ' + nonExistentFilePath + ' (' + nonExistentFilePath + ')\n' , output )
  }

  ////////  Test multiple include of the same targets.

  void testTargetsMultipleIncludeDefaultScriptFile ( ) {
    script = "includeTargets <<  new File ( '${targetsScriptFilePath}' )\n" + targetsBuildScriptFile
    assertEquals ( 0 , processTargets ( ) )
    assertEquals ( 'flobbed.\n' , output )
  }
  void testTargetsMultipleIncludeDefaultScriptString ( ) {
    script = "includeTargets <<  '''${targetsScriptText}'''\n" + targetsBuildScriptString
    assertEquals ( 0 , processTargets ( ) )
    assertEquals ( 'flobbed.\n' , output )
  }
  void testTargetsMultipleIncludeFlobScriptFile ( ) {
    script = "includeTargets <<  new File ( '${targetsScriptFilePath}' )\n" + targetsBuildScriptFile
    assertEquals ( 0 , processTargets ( 'flob' ) )
    assertEquals ( 'flobbed.\n' , output )
  }
  void testTargetsMultipleIncludeFlobScriptString ( ) {
    script = "includeTargets <<  '''${targetsScriptText}'''\n" + targetsBuildScriptString
    assertEquals ( 0 , processTargets ( 'flob' ) )
    assertEquals ( 'flobbed.\n' , output )
  }
  void testTargetsMultipleIncludeBurbleScriptFile ( ) {
    script = "includeTargets <<  new File ( '${targetsScriptFilePath}' )\n" + targetsBuildScriptFile
    assertEquals ( 11 , processTargets ( 'burble') )
    assertEquals ( 'Target burble does not exist.\n' , output ) 
  }
  void testTargetsMultipleIncludeBurbleScriptString ( ) {
    script = "includeTargets <<  '''${targetsScriptText}'''\n" + targetsBuildScriptString
    assertEquals ( 11 , processTargets ( 'burble') )
    assertEquals ( 'Target burble does not exist.\n' , output ) 
  }
  void testTargetsMultipleIncludeSomethingScriptFile ( ) {
    script = "includeTargets <<  new File ( '${targetsScriptFilePath}' )\n" + targetsBuildScriptFile
    assertEquals ( 0 , processTargets ( 'flob' ) )
    assertEquals ( 'flobbed.\n' , output )
  }
  void testTargetsMultipleIncludeSomethingScriptString ( ) {
    script = "includeTargets <<  '''${targetsScriptText}'''\n" + targetsBuildScriptString
    assertEquals ( 0 , processTargets ( 'flob' ) )
    assertEquals ( 'flobbed.\n' , output )
  }

  void testUsingParameterConstructor ( ) {
    def theToolClassName = 'TheTool'
    def theToolClassText = """class ${theToolClassName} {
  ${theToolClassName} ( Binding binding , Map map ) { }
  def flob ( ) { println ( 'flobbed.' ) }
}"""
    script = """includeTool ** groovyShell.evaluate ( '''${theToolClassText} ; return ${theToolClassName}''' ) * [ flob : 'adob' , foo : 'bar' ]
target ( something : '' ) { ${theToolClassName}.flob ( ) }
"""
    assertEquals ( 0 , processTargets ( 'something' ) )    
    assertEquals ( 'flobbed.\n' , output )
  }
  
}
