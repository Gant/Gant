//  Gant -- A Groovy way of scripting Ant tasks.
//
//  Copyright © 2006-9 Russel Winder
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
  ////  constructor to avoid having the variable bound to null.
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final File temporaryDirectory
  private final toolClassName = 'ToolClass'
  private final toolBindingName = 'toolClass'
  private final String toolClassFilePath
  private final toolClassText =  """
import org.codehaus.gant.GantBinding
class ${toolClassName} {
  ${toolClassName} ( GantBinding binding ) { }
  void flob ( ) { println ( 'flobbed.' ) }
}
"""
  private final toolBuildScriptBase =  """
target ( something : '' ) { ${toolBindingName}.flob ( ) }
target ( 'default' : '' ) { something ( ) }
"""
  private final toolBuildScriptClass =  "includeTool <<  groovyShell.evaluate ( '''${toolClassText} ; return ${toolClassName}''' )\n" + toolBuildScriptBase
  private final String toolBuildScriptFile
  private final toolBuildScriptString =  "includeTool <<  '''${toolClassText}'''\n" + toolBuildScriptBase
  private final String targetsScriptFilePath
  private final targetsScriptText =  '''
target ( flob : '' ) { println ( 'flobbed.' ) }
'''
  private final targetsClassName = 'TargetsClass'
  private final String targetsClassFilePath
  private final targetsClassText =  """
import org.codehaus.gant.GantBinding
class ${targetsClassName} {
  ${targetsClassName} ( GantBinding binding ) { binding.target.call ( flob : '' ) { println ( 'flobbed.' ) } }
}
"""
  private final targetsBuildScriptBase =  """
target ( something : '' ) { flob ( ) }
target ( 'default' : '' ) { something ( ) }
"""
  //  Source containing just a class and not a complete script must be turned into a script that
  //  instantiates the class.  Test both correct and errorful behaviour
  private final targetsBuildScriptClass =  "includeTargets <<  groovyShell.evaluate ( '''${targetsScriptText} ; return ${targetsClassName}''' , '${targetsClassName}' )\n" + targetsBuildScriptBase
  private final targetsErrorBuildScriptClass =  "includeTargets <<  groovyShell.evaluate ( '''${targetsScriptText}''' , '${targetsClassName}' )\n" + targetsBuildScriptBase
  private final resultErrorEvaluatingScript = 'Standard input, line 1 -- Error evaluating Gantfile: ' + ( ( ( groovyMinorVersion < 6 ) && ( groovyBugFixVersion < 8 ) ) ? 'null' : "Cannot get property 'class' on null object" ) + '\n'
  private final String targetsBuildScriptFile
  private final targetsBuildScriptString =  "includeTargets <<  '''${targetsScriptText}'''\n" + targetsBuildScriptBase
  private final targetsBuildClassClass =  "includeTargets <<  groovyShell.evaluate ( '''${targetsClassText} ; return ${targetsClassName}''' )\n" + targetsBuildScriptBase
  //  Source containing just a class and not a complete script must be turned into a script that
  //  instantiates the class.  Test both correct and errorful behaviour
  private final String targetsBuildClassFile
  private final targetsBuildClassString =  "includeTargets <<  '''${targetsClassText} ; binding.classInstanceVariable = new ${targetsClassName} ( binding )'''\n" + targetsBuildScriptBase
  private final String targetsErrorBuildClassFile
  private final targetsErrorBuildClassString =  "includeTargets <<  '''${targetsClassText}'''\n" + targetsBuildScriptBase
  private final resultErrorEvaluatingClass = "Standard input, line 1 -- Error evaluating Gantfile: java.lang.InstantiationException: ${targetsClassName}\n"
  private final String nonExistentFilePath
  private final resultFlobbed = 'flobbed.\n'
  Include_Test ( ) {
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////  createTempFile delivers a File object that delivers a string for the path that is platform
    ////  specific.  Cannot use // to delimit the strings in the Gant script being created since / is the
    ////  file separator on most OSs.  Have to do something to avoid problems on Windows since '' strings
    ////  still interpret \.  Fortunately Windows will accept / as the path separator, so transform all \ to
    ////  / in all cases.
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    temporaryDirectory = File.createTempFile ( 'gant-includeTest-' ,  '-directory' )
    def temporaryDirectoryPath = isWindows ? temporaryDirectory.path.replaceAll ( '\\\\' , '/' ) : temporaryDirectory.path
    toolClassFilePath = temporaryDirectoryPath + '/' + toolClassName + '.groovy'
    toolBuildScriptFile =  "includeTool <<  new File ( '${toolClassFilePath}' )\n" + toolBuildScriptBase
    targetsScriptFilePath = temporaryDirectoryPath +'/targets.gant'
    targetsClassFilePath = temporaryDirectoryPath + '/' + targetsClassName + '.groovy'
    targetsBuildScriptFile =  "includeTargets <<  new File ( '${targetsScriptFilePath}' )\n" + targetsBuildScriptBase
    //  Files containing source code that is a class rahter than a script must be treated as a tool.
    targetsBuildClassFile =  "includeTool <<  new File ( '${targetsClassFilePath}' )\n" + targetsBuildScriptBase
    targetsErrorBuildClassFile =  "includeTargets <<  new File ( '${targetsClassFilePath}' )\n" + targetsBuildScriptBase
    nonExistentFilePath = temporaryDirectoryPath + '/tmp' * 3
  }
  private resultTargetDoesNotExist ( String target ) { 'Target ' + target + ' does not exist.\n' }
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
    assertEquals ( 0 , processCmdLineTargets ( ) )
    assertEquals ( resultFlobbed , output )
  }
  void testToolDefaultFile ( ) {
    script = toolBuildScriptFile
    assertEquals ( 0 , processCmdLineTargets ( ) )
    assertEquals ( resultFlobbed , output )
  }
  void testToolDefaultString ( ) {
    script = toolBuildScriptString
    assertEquals ( 0 , processCmdLineTargets ( ) )
    assertEquals ( resultFlobbed , output )
  }
  void testToolFlobClass ( ) {
    def target = 'flob'
    script = toolBuildScriptClass
    assertEquals ( -11 , processCmdLineTargets ( target ) )
    assertEquals ( resultTargetDoesNotExist ( target ) , output )
  }
  void testToolFlobFile ( ) {
    def target = 'flob'
    script = toolBuildScriptFile
    assertEquals ( -11 , processCmdLineTargets ( target ) )
    assertEquals ( resultTargetDoesNotExist ( target ) , output )
  }
  void testToolFlobString ( ) {
    def target = 'flob'
    script = toolBuildScriptString
    assertEquals ( -11 , processCmdLineTargets ( target ) )
    assertEquals ( resultTargetDoesNotExist ( target ) , output )
  }
  void testToolBurbleClass ( ) {
    def target = 'burble'
    script = toolBuildScriptClass
    assertEquals ( -11 , processCmdLineTargets ( target ) )
    assertEquals ( resultTargetDoesNotExist ( target ) , output )
  }
  void testToolBurbleFile ( ) {
    def target = 'burble'
    script = toolBuildScriptFile
    assertEquals ( -11 , processCmdLineTargets ( target ) )
    assertEquals ( resultTargetDoesNotExist ( target ) , output )
  }
  void testToolBurbleString ( ) {
    def target = 'burble'
    script = toolBuildScriptString
    assertEquals ( -11 , processCmdLineTargets ( target ) )
    assertEquals ( resultTargetDoesNotExist ( target ) , output )
  }
  void testToolSomethingClass ( ) {
    script = toolBuildScriptClass
    assertEquals ( 0 , processCmdLineTargets ( 'something' ) )
    assertEquals ( resultFlobbed , output )
  }
  void testToolSomethingFile ( ) {
    script = toolBuildScriptFile
    assertEquals ( 0 , processCmdLineTargets ( 'something' ) )
    assertEquals ( resultFlobbed , output )
  }
  void testToolSomethingString ( ) {
    script = toolBuildScriptString
    assertEquals ( 0 , processCmdLineTargets ( 'something' ) )
    assertEquals ( resultFlobbed , output )
  }
  void testToolClassNoFile ( ) {
    script = toolBuildScriptFile.replace ( toolClassFilePath , nonExistentFilePath )
    def errorMessage = 'Standard input, line 1 -- Error evaluating Gantfile: java.io.FileNotFoundException: '
    if ( isWindows ) { errorMessage += nonExistentFilePath.replaceAll ( '/' , '\\\\' ) + ' (The system cannot find the path specified)\n' }
    else { errorMessage += nonExistentFilePath + ' (No such file or directory)\n' }
    assertEquals ( -4 , processCmdLineTargets ( 'flob' ) )
    assertEquals ( errorMessage , output )
  }
  void testTargetsDefaultClassClass ( ) {
    script = targetsBuildClassClass
    assertEquals ( 0 , processCmdLineTargets ( ) )
    assertEquals ( resultFlobbed , output )
  }
  void testTargetsDefaultClassFile ( ) {
    script = targetsBuildClassFile
    assertEquals ( 0 , processCmdLineTargets ( ) )
    assertEquals ( resultFlobbed , output )
  }
  void testErrorTargetsDefaultClassFile ( ) {
    script = targetsErrorBuildClassFile
    assertEquals ( -4 , processCmdLineTargets ( ) )
    assertEquals ( resultErrorEvaluatingClass , output )
  }
  void testTargetsDefaultClassString ( ) {
    script = targetsBuildClassString
    assertEquals ( 0 , processCmdLineTargets ( ) )
    assertEquals ( resultFlobbed , output )
  }
  void testErrorTargetsDefaultClassString ( ) {
    script = targetsErrorBuildClassString
    assertEquals ( -4 , processCmdLineTargets ( ) )
    assertEquals ( resultErrorEvaluatingClass , output )
  }
  void testTargetsFlobClassClass ( ) {
    script = targetsBuildClassClass
    assertEquals ( 0 , processCmdLineTargets ( 'flob' ) )
    assertEquals ( resultFlobbed , output )
  }
  void testTargetsFlobClassFile ( ) {
    script = targetsBuildClassFile
    assertEquals ( 0 , processCmdLineTargets ( 'flob' ) )
    assertEquals ( resultFlobbed , output )
  }
  void testErrorTargetsFlobClassFile ( ) {
    script = targetsErrorBuildClassFile
    assertEquals ( -4 , processCmdLineTargets ( 'flob' ) )
    assertEquals ( resultErrorEvaluatingClass , output )
  }
  void testTargetsFlobClassString ( ) {
    script = targetsBuildClassString
    assertEquals ( 0 , processCmdLineTargets ( 'flob' ) )
    assertEquals ( resultFlobbed , output )
  }
  void testErrorTargetsFlobClassString ( ) {
    script = targetsErrorBuildClassString
    assertEquals ( -4 , processCmdLineTargets ( 'flob' ) )
    assertEquals ( resultErrorEvaluatingClass , output )
  }
  void testTargetsBurbleClassClass ( ) {
    def target = 'burble'
    script = targetsBuildClassClass
    assertEquals ( -11 , processCmdLineTargets ( target ) )
    assertEquals ( resultTargetDoesNotExist ( target ) , output )
  }
  void testTargetsBurbleClassFile ( ) {
    def target = 'burble'
    script = targetsBuildClassFile
    assertEquals ( -11 , processCmdLineTargets ( target ) )
    assertEquals ( resultTargetDoesNotExist ( target ) , output )
  }
  void testErrorTargetsBurbleClassFile ( ) {
    script = targetsErrorBuildClassFile
    assertEquals ( -4 , processCmdLineTargets ( 'burble' ) )
    assertEquals ( resultErrorEvaluatingClass , output )
  }
  void testTargetsBurbleClassString ( ) {
    def target = 'burble'
    script = targetsBuildClassString
    assertEquals ( -11 , processCmdLineTargets ( target ) )
    assertEquals ( resultTargetDoesNotExist ( target ) , output )
  }
  void testErrorTargetsBurbleClassString ( ) {
    script = targetsErrorBuildClassString
    assertEquals ( -4 , processCmdLineTargets ( 'burble') )
    assertEquals ( resultErrorEvaluatingClass , output )
  }
  void testTargetsSomethingClassClass ( ) {
    script = targetsBuildClassClass
    assertEquals ( 0 , processCmdLineTargets ( 'something' ) )
    assertEquals ( resultFlobbed , output )
  }
  void testTargetsSomethingClassFile ( ) {
    script = targetsBuildClassFile
    assertEquals ( 0 , processCmdLineTargets ( 'something') )
    assertEquals ( resultFlobbed , output )
  }
  void testErrorTargetsSomethingClassFile ( ) {
    script = targetsErrorBuildClassFile
    assertEquals ( -4 , processCmdLineTargets ( 'something' ) )
    assertEquals ( resultErrorEvaluatingClass , output )
  }
  void testTargetsSomethingClassString ( ) {
    script = targetsBuildClassString
    assertEquals ( 0 , processCmdLineTargets ( 'something' ) )
    assertEquals ( resultFlobbed , output )
  }
  void testErrorTargetsSomethingClassString ( ) {
    script = targetsErrorBuildClassString
    assertEquals ( -4 , processCmdLineTargets ( 'something' ) )
    assertEquals ( resultErrorEvaluatingClass , output )
  }
  void testTargetsClassNoFile ( ) {
    script = targetsBuildClassFile.replace ( targetsClassFilePath , nonExistentFilePath )
    assertEquals ( -4 , processCmdLineTargets ( 'flob' ) )
    //  The returned string is platform dependent and dependent on whether NFS is used to mount stores, or
    //  even RAID.  We therefore choose not to check the output to avoid having large numbers of cases.
  }
  void testTargetsDefaultScriptClass ( ) {
    script = targetsBuildScriptClass
    assertEquals ( 0 , processCmdLineTargets ( ) )
    assertEquals ( resultFlobbed , output )
  }
  void testErrorTargetsDefaultScriptClass ( ) {
    script = targetsErrorBuildScriptClass
    assertEquals ( -4 , processCmdLineTargets ( ) )
    assertEquals ( resultErrorEvaluatingScript , output )
  }
  void testTargetsDefaultScriptFile ( ) {
    script = targetsBuildScriptFile
    assertEquals ( 0 , processCmdLineTargets ( ) )
    assertEquals ( resultFlobbed , output )
  }
  void testTargetsDefaultScriptString ( ) {
    script = targetsBuildScriptString
    assertEquals ( 0 , processCmdLineTargets ( ) )
    assertEquals ( resultFlobbed , output )
  }
  void testTargetsFlobScriptClass ( ) {
    script = targetsBuildScriptClass
    assertEquals ( 0 , processCmdLineTargets ( 'flob' ) )
    assertEquals ( resultFlobbed , output )
  }
  void testErrorTargetsFlobScriptClass ( ) {
    script = targetsErrorBuildScriptClass
    assertEquals ( -4 , processCmdLineTargets ( 'flob' ) )
    assertEquals ( resultErrorEvaluatingScript , output )
  }
  void testTargetsFlobScriptFile ( ) {
    script = targetsBuildScriptFile
    assertEquals ( 0 , processCmdLineTargets ( 'flob' ) )
    assertEquals ( resultFlobbed , output )
  }
  void testTargetsFlobScriptString ( ) {
    script = targetsBuildScriptString
    assertEquals ( 0 , processCmdLineTargets ( 'flob' ) )
    assertEquals ( resultFlobbed , output )
  }
  void testTargetsBurbleScriptClass ( ) {
    def target = 'burble'
    script = targetsBuildScriptClass
    assertEquals ( -11 , processCmdLineTargets ( target ) )
    assertEquals ( resultTargetDoesNotExist ( target ) , output )
  }
  void testErrorTargetsBurbleScriptClass ( ) {
    script = targetsErrorBuildScriptClass
    assertEquals ( -4 , processCmdLineTargets ( 'burble' ) )
    assertEquals ( resultErrorEvaluatingScript , output )
  }
  void testTargetsBurbleScriptFile ( ) {
    def target = 'burble'
    script = targetsBuildScriptFile
    assertEquals ( -11 , processCmdLineTargets ( target ) )
    assertEquals ( resultTargetDoesNotExist ( target ) , output )
  }
  void testTargetsBurbleScriptString ( ) {
    def target = 'burble'
    script = targetsBuildScriptString
    assertEquals ( -11 , processCmdLineTargets ( target ) )
    assertEquals ( resultTargetDoesNotExist ( target ) , output )
  }
  void testTargetsSomethingScriptClass ( ) {
    script = targetsBuildScriptClass
    assertEquals ( 0 , processCmdLineTargets ( 'something' ) )
    assertEquals ( resultFlobbed , output )
  }
  void testErrorTargetsSomethingScriptClass ( ) {
    script = targetsErrorBuildScriptClass
    assertEquals ( -4 , processCmdLineTargets ( 'something' ) )
    assertEquals ( resultErrorEvaluatingScript , output )
  }
  void testTargetsSomethingScriptFile ( ) {
    script = targetsBuildScriptFile
    assertEquals ( 0 , processCmdLineTargets ( 'something' ) )
    assertEquals ( resultFlobbed , output )
  }
  void testTargetsSomethingScriptString ( ) {
    script = targetsBuildScriptString
    assertEquals ( 0 , processCmdLineTargets ( 'something' ) )
    assertEquals ( resultFlobbed , output )
  }
  void testTargetsScriptNoFile ( ) {
    script = targetsBuildScriptFile.replace ( targetsScriptFilePath , nonExistentFilePath )
    assertEquals ( -4 , processCmdLineTargets ( 'flob' ) )
    //  The returned string is platform dependent and dependent on whether NFS is used to mount stores, or
    //  even RAID.  We therefore choose not to check the output to avoid having large numbers of cases.
  }

  ////////  Test multiple include of the same targets.

  void testTargetsMultipleIncludeDefaultScriptFile ( ) {
    script = "includeTargets <<  new File ( '${targetsScriptFilePath}' )\n" + targetsBuildScriptFile
    assertEquals ( 0 , processCmdLineTargets ( ) )
    assertEquals ( resultFlobbed , output )
  }
  void testTargetsMultipleIncludeDefaultScriptString ( ) {
    script = "includeTargets <<  '''${targetsScriptText}'''\n" + targetsBuildScriptString
    assertEquals ( 0 , processCmdLineTargets ( ) )
    assertEquals ( resultFlobbed , output )
  }
  void testTargetsMultipleIncludeFlobScriptFile ( ) {
    script = "includeTargets <<  new File ( '${targetsScriptFilePath}' )\n" + targetsBuildScriptFile
    assertEquals ( 0 , processCmdLineTargets ( 'flob' ) )
    assertEquals ( resultFlobbed , output )
  }
  void testTargetsMultipleIncludeFlobScriptString ( ) {
    script = "includeTargets <<  '''${targetsScriptText}'''\n" + targetsBuildScriptString
    assertEquals ( 0 , processCmdLineTargets ( 'flob' ) )
    assertEquals ( resultFlobbed , output )
  }
  void testTargetsMultipleIncludeBurbleScriptFile ( ) {
    def target = 'burble'
    script = "includeTargets <<  new File ( '${targetsScriptFilePath}' )\n" + targetsBuildScriptFile
    assertEquals ( -11 , processCmdLineTargets ( target ) )
    assertEquals ( resultTargetDoesNotExist ( target ) , output )
  }
  void testTargetsMultipleIncludeBurbleScriptString ( ) {
    def target = 'burble'
    script = "includeTargets <<  '''${targetsScriptText}'''\n" + targetsBuildScriptString
    assertEquals ( -11 , processCmdLineTargets ( target ) )
    assertEquals ( resultTargetDoesNotExist ( target ) , output )
  }
  void testTargetsMultipleIncludeSomethingScriptFile ( ) {
    script = "includeTargets <<  new File ( '${targetsScriptFilePath}' )\n" + targetsBuildScriptFile
    assertEquals ( 0 , processCmdLineTargets ( 'flob' ) )
    assertEquals ( resultFlobbed , output )
  }
  void testTargetsMultipleIncludeSomethingScriptString ( ) {
    script = "includeTargets <<  '''${targetsScriptText}'''\n" + targetsBuildScriptString
    assertEquals ( 0 , processCmdLineTargets ( 'flob' ) )
    assertEquals ( resultFlobbed , output )
  }
  void testUsingParameterConstructor ( ) {
    def theToolClassName = 'TheTool'
    def theToolBindingName = 'theTool'
    def theToolClassText = """
import org.codehaus.gant.GantBinding
class ${theToolClassName} {
  ${theToolClassName} ( GantBinding binding , Map map ) { }
  void flob ( ) { println ( 'flobbed.' ) }
}
"""
    script = """
includeTool ** groovyShell.evaluate ( '''${theToolClassText} ; return ${theToolClassName}''' ) * [ flob : 'adob' , foo : 'bar' ]
target ( something : '' ) { ${theToolBindingName}.flob ( ) }
"""
    assertEquals ( 0 , processCmdLineTargets ( 'something' ) )
    assertEquals ( resultFlobbed , output )
  }
  //  cf. GANT-29
  void testInlineToolClass ( ) {
    script = '''
import org.codehaus.gant.GantBinding
class SampleTool {
  private final Map properties = [name : '' ]
  SampleTool ( GantBinding binding ) { properties.binding = binding }
  def getProperty ( String name ) { properties[name] }
  void setProperty ( String name , value ) { properties[name] = value }
}
includeTool << SampleTool
target ( doit : '' ) {
  sampleTool.name = 'name'
  println ( sampleTool.name )
}
'''
    assertEquals ( 0 , processCmdLineTargets ( 'doit' ) )
    assertEquals ( 'name\n' , output )
  }

  //  Make sure that errors are correctly trapped.

  void testErrorPowerNoMultiply ( ) {
    // ** without * is effectively a no-op due to the way things are processed.
    script = '''
includeTargets ** gant.targets.Clean
target ( test : '' ) { }
'''
    assertEquals ( 0 , processCmdLineTargets ( 'test' ) )
    assertEquals ( '' , output )
  }
  void testErrorNoPower ( ) {
    // * instead of ** is an error because of the type of the right hand parameter.
    script = '''
includeTargets * gant.targets.Clean
target ( test : '' ) { }
'''
    assertEquals ( -4 , processCmdLineTargets ( 'test' ) )
    assertEquals ( 'Standard input, line 2 -- Error evaluating Gantfile: No signature of method: org.codehaus.gant.IncludeTargets.multiply() is applicable for argument types: (java.lang.Class) values: ' + ( ( groovyMinorVersion < 6 ) ? '{class gant.targets.Clean}' : '[class gant.targets.Clean]' ) + '\n' , output )
  }
  void testErrorNullPower ( ) {
    script = '''
includeTargets ** null * [ ]
target ( test : '' ) { }
'''
    assertEquals ( -4 , processCmdLineTargets ( 'test' ) )
    assertEquals ( 'Standard input, line 2 -- Error evaluating Gantfile: wrong number of arguments\n' , output )
  }

  //  This test provided by Peter Ledbrook in order to test the change to Gant to allow Script objects to be
  //  used to initialize a Gant object.

  //  TODO :  There needs to be more testing of this feature.

  void testIncludeCompiledScript ( ) {
    def script = '''
testVar = 'Test'
target ( aTarget : '' ) {
  println ( 'Tested.' )
}
'''
    def gcl = new GroovyClassLoader ( )
    def clazz = gcl.parseClass ( script )
    def binding = new org.codehaus.gant.GantBinding ( )
    def includeTargets = new org.codehaus.gant.IncludeTargets ( binding )
    includeTargets << clazz
    assertEquals ( 'Test', binding.testVar )
    assertNotNull ( binding.aTarget )
  }

}
