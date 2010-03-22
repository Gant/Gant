//  Gant -- A Groovy way of scripting Ant tasks.
//
//  Copyright © 2007-9 Russel Winder
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

package gant.targets.tests

import org.codehaus.gant.GantBuilder
import org.codehaus.gant.GantState

import org.codehaus.gant.tests.GantTestCase

/**
 *  A test to ensure that the Maven targets are not broken.
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
final class Maven_Test extends GantTestCase {
  void testLoadingTargets ( ) {
    script = """
includeTargets << gant.targets.Maven
"""
    assertEquals ( 0 , processCmdLineTargets ( 'initialize' ) )
    assertEquals ( 'initialize:\n' + exitMarker + 'initialize\n' , output )
    assertEquals ( '' , error )
  }
  void testCompileTargetInDirectoryOtherThanTheCurrentBuildDirectory ( ) {
    final mavenTargetSetTestDirectory = new File ( 'mavenTargetsSetTest' )
    final sourceDirectory = new File ( mavenTargetSetTestDirectory , 'src' )
    final javaFileDirectory = new File ( sourceDirectory , 'main/java' )
    final targetDirectory = new File ( mavenTargetSetTestDirectory , 'target' )
    final compiledClassesDirectory = new File ( targetDirectory , 'classes' )
    final root = 'hello'
    final gantBuilder = new GantBuilder ( )
    gantBuilder.logger.setMessageOutputLevel ( GantState.SILENT )
    gantBuilder.delete ( dir : mavenTargetSetTestDirectory.path )
    gantBuilder.mkdir ( dir : javaFileDirectory.path )
    ( new File ( javaFileDirectory , root + '.java' ) ).write ( "class ${root} { }" )
    final targetName = 'compile'
    script = """
includeTargets ** gant.targets.Maven * [
    sourcePath : '${sourceDirectory.path}' ,
    targetPath : '${targetDirectory.path}'
]
"""
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , resultString ( 'initialize' , '' ) + """    [mkdir] Created dir: ${compiledClassesDirectory.absolutePath}
    [javac] : warning: 'includeantruntime' was not set, defaulting to build.sysclasspath=last; set to false for repeatable builds
    [javac] Compiling 1 source file to ${compiledClassesDirectory.absolutePath}
""" ) , output )
    assertTrue ( ( new File ( compiledClassesDirectory , root + '.class' ) ).isFile ( ) )
    assertEquals ( '' , error )
    gantBuilder.delete ( dir : mavenTargetSetTestDirectory.path )
    assertFalse ( mavenTargetSetTestDirectory.exists ( ) )
  }
  void testPackageNoGroupIdLeftShift ( ) {
    final targetName = 'package'
    script = """
includeTargets << gant.targets.Maven
"""
    assertEquals ( -13 , processCmdLineTargets ( targetName ) )
    assertEquals ( targetName + ':\n' , output )
    assertEquals ( 'java.lang.RuntimeException: maven.groupId must be set to achieve target package.\n' , error )
  }
  void testPackageNoGroupIdPower ( ) {
    def targetName = 'package'
    script = """
includeTargets ** gant.targets.Maven * [ : ]
"""
    assertEquals ( -13 , processCmdLineTargets ( targetName ) )
    assertEquals ( targetName + ':\n' , output )
    assertEquals ( 'java.lang.RuntimeException: maven.groupId must be set to achieve target package.\n' , error )
  }
  void testPackageNoArtifactIdLeftShift ( ) {
    final targetName = 'package'
    script = """
includeTargets << gant.targets.Maven
maven.groupId = 'flob'
"""
    assertEquals ( -13 , processCmdLineTargets ( targetName ) )
    assertEquals ( targetName + ':\n' , output )
    assertEquals ( 'java.lang.RuntimeException: maven.artifactId must be set to achieve target package.\n' , error )
  }
  void testPackageNoArtifactIdPower ( ) {
    def targetName = 'package'
    script = """
includeTargets ** gant.targets.Maven * [ groupId : 'flob' ]
"""
    assertEquals ( -13 , processCmdLineTargets ( targetName ) )
    assertEquals ( targetName + ':\n' , output )
    assertEquals ( 'java.lang.RuntimeException: maven.artifactId must be set to achieve target package.\n' , error )
  }
  void testPackageVersionLeftShift ( ) {
    final targetName = 'package'
    script = """
includeTargets << gant.targets.Maven
maven.groupId = 'flob'
maven.artifactId = 'adob'
"""
    assertEquals ( -13 , processCmdLineTargets ( targetName ) )
    assertEquals ( targetName + ':\n' , output )
    assertEquals ( 'java.lang.RuntimeException: maven.version must be set to achieve target package.\n' , error )
  }
  void testPackageVersionPower ( ) {
    final targetName = 'package'
    script = """
includeTargets ** gant.targets.Maven * [ groupId : 'flob' , artifactId : 'adob' ]
"""
    assertEquals ( -13 , processCmdLineTargets ( targetName ) )
    assertEquals ( targetName + ':\n' , output )
    assertEquals ( 'java.lang.RuntimeException: maven.version must be set to achieve target package.\n' , error )
  }
  void testBindingPropertyIsReadOnlyLeftShift ( ) {
    script = """
includeTargets << gant.targets.Maven
maven.binding = new Binding ( )
"""
    assertEquals ( -4 , processCmdLineTargets ( 'initialize' ) )
    assertEquals ( '' , output )
    assertEquals ( 'Standard input, line 3 -- Error evaluating Gantfile: Cannot amend the property binding.\n' , error )
  }
  void testBindingPropertyIsReadOnlyPower ( ) {
    script = """
includeTargets ** gant.targets.Maven * [ binding : new Binding ( ) ]
"""
    assertEquals ( -4 , processCmdLineTargets ( 'initialize' ) )
    assertEquals ( '' , output )
    assertEquals ( 'Standard input, line 2 -- Error evaluating Gantfile: Cannot amend the property binding.\n' , error )
  }
  void testAdditionalTarget ( ) {
    final targetName = 'sayHello'
    script = """
includeTargets << gant.targets.Maven
target ( ${targetName} : '' ) { println ( 'Hello.' ) }
"""
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , 'Hello.\n' ) , output )
    assertEquals ( '' , error )
  }
  void testAdditionalTargetError ( ) {
    final targetName = 'sayHello'
    script = """
includeTargets << gant.targets.Maven
target ( ${targetName} , '' ) { println ( 'Hello.' ) }
"""
    assertEquals ( -4 , processCmdLineTargets ( targetName ) )
    assertEquals ( '' , output )
    assertEquals ( 'Standard input, line 3 -- Error evaluating Gantfile: No such property: sayHello for class: standard_input\n' , error )
  }
}
