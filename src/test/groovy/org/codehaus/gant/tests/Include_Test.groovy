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

/**
 *  A test to ensure that the various include mechanisms work as they should.
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
final class Include_Test extends GantTestCase {

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  ////  NB Instance initializers do not work properly in Groovy.  This means that fields that depend on the
  ////  name of the temporary file must be initialized in the constructor.  Remember, variables in GStrings
  ////  are bound at definition time even though expression execution only occurs at use time.  This means
  ////  any GString depending on the name of the temporary directory must also be initialized in the
  ////  constructor to avoid having variable bound to null.
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final temporaryDirectory
  private final toolClassName = 'ToolClass'
  private final toolBindingName = 'toolClass'
  private final toolClassFilePath
  private final toolClassText =  """
class ${toolClassName} {
  ${toolClassName} ( Binding binding ) { }
  void flob ( ) { println ( 'flobbed.' ) }
}
"""
  private final toolBuildScriptBase =  """
target ( something : '' ) { ${toolBindingName}.flob ( ) }
target ( 'default' : '' ) { something ( ) }
"""
  private final toolBuildScriptClass =  "includeTool <<  groovyShell.evaluate ( '''${toolClassText} ; return ${toolClassName}''' )\n" + toolBuildScriptBase
  private final toolBuildScriptFile
  private final toolBuildScriptString =  "includeTool <<  '''${toolClassText}'''\n" + toolBuildScriptBase
  private final targetsScriptFilePath
  private final targetsScriptText =  '''
target ( flob : '' ) { println ( 'flobbed.' ) }
''' 
  private final targetsClassName = 'TargetsClass'
  private final targetsClassFilePath
  private final targetsClassText =  """
class ${targetsClassName} {
  ${targetsClassName} ( Binding binding ) { binding.target.call ( flob : '' ) { println ( 'flobbed.' ) } }
}
"""
  private final targetsBuildScriptBase =  """
target ( something : '' ) { flob ( ) }
target ( 'default' : '' ) { something ( ) }
"""
  private final targetsBuildScriptClass =  "includeTargets <<  groovyShell.evaluate ( '''${targetsScriptText} ; return ${targetsClassName}''' , ${targetsClassName} )\n" + targetsBuildScriptBase
  private final targetsBuildScriptFile
  private final targetsBuildScriptString =  "includeTargets <<  '''${targetsScriptText}'''\n" + targetsBuildScriptBase
  private final targetsBuildClassClass =  "includeTargets <<  groovyShell.evaluate ( '''${targetsClassText} ; return ${targetsClassName}''' )\n" + targetsBuildScriptBase
  private final targetsBuildClassFile
  private final targetsBuildClassString =  "includeTargets <<  '''${targetsClassText}'''\n" + targetsBuildScriptBase
  private final nonExistentFilePath
  Include_Test ( ) {
    temporaryDirectory = File.createTempFile ( 'gant-includeTest-' ,  '-directory' )
    toolClassFilePath = temporaryDirectory.path + System.properties.'file.separator' + toolClassName + '.groovy'
    toolBuildScriptFile =  "includeTool <<  new File ( '${toolClassFilePath}' )\n" + toolBuildScriptBase
    targetsScriptFilePath = temporaryDirectory.path + System.properties.'file.separator' + 'targets.gant'
    targetsClassFilePath = temporaryDirectory.path + System.properties.'file.separator' + targetsClassName + '.groovy'
    targetsBuildScriptFile =  "includeTargets <<  new File ( '${targetsScriptFilePath}' )\n" + targetsBuildScriptBase
    targetsBuildClassFile =  "includeTargets <<  new File ( '${targetsClassFilePath}' )\n" + targetsBuildScriptBase
    nonExistentFilePath = temporaryDirectory.path + ( System.properties.'file.separator' + 'tmp' ) * 3
  }  
  void setUp ( ) {
    super.setUp ( )
    temporaryDirectory.delete ( )
    temporaryDirectory.mkdirs ( )
    ( new File ( toolClassFilePath ) ).write ( toolClassText )
    ( new File ( targetsScriptFilePath ) ).write ( targetsScriptText )
    ( new File ( targetsClassFilePath ) ).write ( targetsClassText )
  }
  void tearDown ( ) {
    ( new File ( toolClassFilePath ) ).delete ( )
    ( new File ( targetsScriptFilePath ) ).delete ( )
    ( new File ( targetsClassFilePath ) ).delete ( )
    temporaryDirectory.delete ( )
    super.tearDown ( )
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
  void testTargetsDefaultClassFile ( ) {
    script = targetsBuildClassFile
    assertEquals ( 2 , processTargets ( ) )
    assertEquals ( "Standard input, line 1 -- Error evaluating Gantfile: ${targetsClassName}\n" , output ) 
  }
  void testTargetsDefaultClassString ( ) {
    script = targetsBuildClassString
    assertEquals ( 2 , processTargets ( ) )
    assertEquals ( "Standard input, line 1 -- Error evaluating Gantfile: ${targetsClassName}\n" , output ) 
  }
  void testTargetsFlobClassClass ( ) {
    script = targetsBuildClassClass
    assertEquals ( 0 , processTargets ( 'flob') )
    assertEquals ( 'flobbed.\n' , output ) 
  }
  void testTargetsFlobClassFile ( ) {
    script = targetsBuildClassFile
    assertEquals ( 2 , processTargets ( 'flob') )
    assertEquals ( "Standard input, line 1 -- Error evaluating Gantfile: ${targetsClassName}\n" , output ) 
  }
  void testTargetsFlobClassString ( ) {
    script = targetsBuildClassString
    assertEquals ( 2 , processTargets ( 'flob') )
    assertEquals ( "Standard input, line 1 -- Error evaluating Gantfile: ${targetsClassName}\n" , output ) 
  }
  void testTargetsBurbleClassClass ( ) {
    script = targetsBuildClassClass
    assertEquals ( 11 , processTargets ( 'burble' ) )
    assertEquals ( 'Target burble does not exist.\n' , output ) 
  }
  void testTargetsBurbleClassFile ( ) {
    script = targetsBuildClassFile
    assertEquals ( 2 , processTargets ( 'burble') )
    assertEquals ( "Standard input, line 1 -- Error evaluating Gantfile: ${targetsClassName}\n" , output ) 
  }
  void testTargetsBurbleClassString ( ) {
    script = targetsBuildClassString
    assertEquals ( 2 , processTargets ( 'burble') )
    assertEquals ( "Standard input, line 1 -- Error evaluating Gantfile: ${targetsClassName}\n" , output ) 
  }
  void testTargetsSomethingClassClass ( ) {
    script = targetsBuildClassClass
    assertEquals ( 0 , processTargets ( 'something') )
    assertEquals ( 'flobbed.\n' , output ) 
  }
  void testTargetsSomethingClassFile ( ) {
    script = targetsBuildClassFile
    assertEquals ( 2 , processTargets ( 'something') )
    assertEquals ( "Standard input, line 1 -- Error evaluating Gantfile: ${targetsClassName}\n", output ) 
  }
  void testTargetsSomethingClassString ( ) {
    script = targetsBuildClassString
    assertEquals ( 2 , processTargets ( 'something') )
    assertEquals ( "Standard input, line 1 -- Error evaluating Gantfile: ${targetsClassName}\n", output ) 
  }
  void testTargetsClassNoFile ( ) {
    script = targetsBuildClassFile.replace ( targetsClassFilePath , nonExistentFilePath )
    assertEquals ( 2 , processTargets ( 'flob') )
    //  This is a weird message, should be better than this.
    assertEquals ( 'Standard input, line 1 -- Error evaluating Gantfile: ' + nonExistentFilePath + ' (' + nonExistentFilePath + ')\n' , output )
  }
  void testTargetsDefaultScriptClass ( ) {
    script = targetsBuildScriptClass
    assertEquals ( 2 , processTargets ( ) )
    assertEquals ( "Standard input, line 6 -- Error evaluating Gantfile: No such property: ${targetsClassName} for class: standard_input\n" , output ) 
  }
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
  void testTargetsFlobScriptClass ( ) {
    script = targetsBuildScriptClass
    assertEquals ( 2 , processTargets ( 'flob') )
    assertEquals ( "Standard input, line 6 -- Error evaluating Gantfile: No such property: ${targetsClassName} for class: standard_input\n" , output ) 
  }
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
  void testTargetsBurbleScriptClass ( ) {
    script = targetsBuildScriptClass
    assertEquals ( 2 , processTargets ( 'burble') )
    assertEquals ( "Standard input, line 6 -- Error evaluating Gantfile: No such property: ${targetsClassName} for class: standard_input\n" , output ) 
  }
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
  void testTargetsSomethingScriptClass ( ) {
    script = targetsBuildScriptClass
    assertEquals ( 2 , processTargets ( 'something') )
    assertEquals ( "Standard input, line 6 -- Error evaluating Gantfile: No such property: ${targetsClassName} for class: standard_input\n" , output ) 
  }
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
    def theToolBindingName = 'theTool'
    def theToolClassText = """
class ${theToolClassName} {
  ${theToolClassName} ( Binding binding , Map map ) { }
  void flob ( ) { println ( 'flobbed.' ) }
}
"""
    script = """
includeTool ** groovyShell.evaluate ( '''${theToolClassText} ; return ${theToolClassName}''' ) * [ flob : 'adob' , foo : 'bar' ]
target ( something : '' ) { ${theToolBindingName}.flob ( ) }
"""
    assertEquals ( 0 , processTargets ( 'something' ) )    
    assertEquals ( 'flobbed.\n' , output )
  }
  //  cf. GANT-29
  void testInlineToolClass ( ) {
    script = '''
class SampleTool {
  private final Map properties = [name : '' ]
  SampleTool ( Binding binding ) { properties.binding = binding }
  def getProperty ( String name ) { properties[name] }
  void setProperty ( String name , value ) { properties[name] = value }  
}
includeTool << SampleTool
target ( doit : '' ) {
  sampleTool.name = 'name'
  println ( sampleTool.name )
}
'''
    assertEquals ( 0 , processTargets ( 'doit' ) )
    assertEquals ( 'name\n' , output )
  }
  
}
