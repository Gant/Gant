//  Gant -- A Groovy way of scripting Ant tasks.
//
//  Copyright © 2007-8 Russel Winder
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
    assertEquals ( '' , output )
  }
  void testCompileTargetInDirectoryOtherThanTheCurrentBuildDirectory ( ) {
    final antBuilder = new AntBuilder ( )
    final compileDirectory = 'target_forMavenTest'
    //  Ensure the directory does not exist to protect against a failed test run leaving a version
    //  in place -- which causes the test to fail inappropriately.
    final file = new File ( compileDirectory )
    if ( file.exists ( ) ) {
      if ( file.isDirectory ( ) ) { antBuilder.delete ( dir : compileDirectory ) }
      else { antBuilder.delete ( file : compileDirectory ) }
    }
    script = """
includeTargets ** gant.targets.Maven * [
    targetPath : '${compileDirectory}'
]
"""
    assertEquals ( 0 , processCmdLineTargets ( 'compile' ) )
    assertTrue ( output.startsWith( '    [mkdir] Created dir:' ) )
    assertTrue ( output.contains( ' [groovyc] Compiling' ) )
    assertTrue ( file.isDirectory ( ) )
    antBuilder.delete ( dir : compileDirectory )
    assertFalse ( file.exists ( ) )
  }
  void testPackageNoGroupIdLeftShift ( ) {
    script = """
includeTargets << gant.targets.Maven
"""
    assertEquals ( -13 , processCmdLineTargets ( 'package' ) )
    assertEquals ( '''java.lang.RuntimeException: maven.groupId must be set to achieve target package.
''' , output )
  }
  void testPackageNoGroupIdPower ( ) {
    script = """
includeTargets ** gant.targets.Maven * [ : ]
"""
    assertEquals ( -13 , processCmdLineTargets ( 'package' ) )
    assertEquals ( '''java.lang.RuntimeException: maven.groupId must be set to achieve target package.
''' , output )
  }
  void testPackageNoArtifactIdLeftShift ( ) {
    script = """
includeTargets << gant.targets.Maven
maven.groupId = 'flob'
"""
    assertEquals ( -13 , processCmdLineTargets ( 'package' ) )
    assertEquals ( '''java.lang.RuntimeException: maven.artifactId must be set to achieve target package.
''' , output )
  }
  void testPackageNoArtifactIdPower ( ) {
    script = """
includeTargets ** gant.targets.Maven * [ groupId : 'flob' ]
"""
    assertEquals ( -13 , processCmdLineTargets ( 'package' ) )
    assertEquals ( '''java.lang.RuntimeException: maven.artifactId must be set to achieve target package.
''' , output )
  }
  void testPackageVersionLeftShift ( ) {
    script = """
includeTargets << gant.targets.Maven
maven.groupId = 'flob'
maven.artifactId = 'adob'
"""
    assertEquals ( -13 , processCmdLineTargets ( 'package' ) )
    assertEquals ( '''java.lang.RuntimeException: maven.version must be set to achieve target package.
''' , output )
  }
  void testPackageVersionPower ( ) {
    script = """
includeTargets ** gant.targets.Maven * [ groupId : 'flob' , artifactId : 'adob' ]
"""
    assertEquals ( -13 , processCmdLineTargets ( 'package' ) )
    assertEquals ( '''java.lang.RuntimeException: maven.version must be set to achieve target package.
''' , output )
  }
  void testBindingPropertyIsReadOnlyLeftShift ( ) {
    script = """
includeTargets << gant.targets.Maven
maven.binding = new Binding ( )
"""
    assertEquals ( -2 , processCmdLineTargets ( 'initialize' ) )
    assertEquals ( '''Standard input, line 3 -- Error evaluating Gantfile: Cannot amend the property binding.
''' , output )
  }
  void testBindingPropertyIsReadOnlyPower ( ) {
    script = """
includeTargets ** gant.targets.Maven * [ binding : new Binding ( ) ]
"""
    assertEquals ( -2 , processCmdLineTargets ( 'initialize' ) )
    assertEquals ( '''Standard input, line 2 -- Error evaluating Gantfile: Cannot amend the property binding.
''' , output )
  }
  void testAdditionalTarget ( ) {
    script = '''
includeTargets << gant.targets.Maven
target ( sayHello : '' ) { println ( 'Hello.' ) }
'''
    assertEquals ( 0 , processCmdLineTargets ( 'sayHello' ) )
    assertEquals ( 'Hello.\n' , output )
  }
  void testAdditionalTargetError ( ) {
    script = '''
includeTargets << gant.targets.Maven
target ( sayHello , '' ) { println ( 'Hello.' ) }
'''
    assertEquals ( -2 , processCmdLineTargets ( 'sayHello' ) )
    assertEquals ( 'Standard input, line 3 -- Error evaluating Gantfile: No such property: sayHello for class: standard_input\n' , output )
  }
}
