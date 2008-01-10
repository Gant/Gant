//  Gant -- A Groovy build framework based on scripting Ant tasks.
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
    assertEquals ( 0 , processTargets ( 'initialize' ) )
    assertEquals ( '' , output ) 
  }
  void testCompileTarget ( ) {
    script = """
includeTargets << gant.targets.Maven
"""
    assertEquals ( 0 , processTargets ( 'compile' ) )
    assertEquals ( '''  [groovyc] No sources to compile
''' , output ) 
  }


  void testPackageNoGroupIdLeftShift ( ) {
    script = """
includeTargets << gant.targets.Maven
"""
    assertEquals ( 13 , processTargets ( 'package' ) )
    assertEquals ( '''Maven.groupId must be set to achieve target package.
''' , output ) 
  }
  void testPackageNoGroupIdPower ( ) {
    script = """
includeTargets ** gant.targets.Maven * [ : ]
"""
    assertEquals ( 13 , processTargets ( 'package' ) )
    assertEquals ( '''Maven.groupId must be set to achieve target package.
''' , output ) 
  }
  void testPackageNoArtifactIdLeftShift ( ) {
    script = """
includeTargets << gant.targets.Maven
Maven.groupId = 'flob'
"""
    assertEquals ( 13 , processTargets ( 'package' ) )
    assertEquals ( '''Maven.artifactId must be set to achieve target package.
''' , output ) 
  }
  void testPackageNoArtifactIdPower ( ) {
    script = """
includeTargets ** gant.targets.Maven * [ groupId : 'flob' ]
"""
    assertEquals ( 13 , processTargets ( 'package' ) )
    assertEquals ( '''Maven.artifactId must be set to achieve target package.
''' , output ) 
  }
  void testPackageVersionLeftShift ( ) {
    script = """
includeTargets << gant.targets.Maven
Maven.groupId = 'flob'
Maven.artifactId = 'adob'
"""
    assertEquals ( 13 , processTargets ( 'package' ) )
    assertEquals ( '''Maven.version must be set to achieve target package.
''' , output ) 
  }
  void testPackageVersionPower ( ) {
    script = """
includeTargets ** gant.targets.Maven * [ groupId : 'flob' , artifactId : 'adob' ]
"""
    assertEquals ( 13 , processTargets ( 'package' ) )
    assertEquals ( '''Maven.version must be set to achieve target package.
''' , output ) 
  }

  void testBindingPropertyIsReadOnlyLeftShift ( ) {
    script = """
includeTargets << gant.targets.Maven
Maven.binding = new Binding ( )
"""
    assertEquals ( 2 , processTargets ( 'initialize' ) )
    assertEquals ( '''Standard input, line 3 -- Error evaluating Gantfile: Cannot amend the property binding.
''' , output ) 
  }
  

  void testBindingPropertyIsReadOnlyPower ( ) {
    script = """
includeTargets ** gant.targets.Maven * [ binding : new Binding ( ) ]
""" 
    assertEquals ( 2 , processTargets ( 'initialize' ) )
    assertEquals ( '''Standard input, line 2 -- Error evaluating Gantfile: Cannot amend the property binding.
''' , output ) 
  }
  






}
