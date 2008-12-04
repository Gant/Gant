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
 *  A test to ensure that the Clean targets are not broken.
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
final class Clean_Test extends GantTestCase {
  void testCleanDirectoryString ( ) {
    script = """
includeTargets << gant.targets.Clean
cleanDirectory << 'target'
target ( testClean : '' ) { println ( cleanDirectory ) }
"""
    assertEquals ( 0 , processCmdLineTargets ( 'testClean' ) )
    assertEquals (  ( groovyMinorVersion > 5 ) ? '''[target]
''' : '''["target"]
''' , output ) 
  }
  void testCleanDirectoryList ( ) {
    script = """
includeTargets << gant.targets.Clean
cleanDirectory << [ 'target_a' , 'target_b' ]
target ( testClean : '' ) { println ( cleanDirectory ) }
""" 
    assertEquals ( 0 , processCmdLineTargets ( 'testClean' ) ) 
    assertEquals ( ( groovyMinorVersion > 5 ) ? '''[[target_a, target_b]]
''' :  '''[["target_a", "target_b"]]
''' , output ) 
  }
  void testCleanPatternString ( ) {
    script = """
includeTargets << gant.targets.Clean
cleanPattern << '**/*~'
target ( testClean : '' ) {println ( cleanPattern ) }
"""
    assertEquals ( 0 , processCmdLineTargets ( 'testClean' ) )
    assertEquals ( ( groovyMinorVersion > 5 ) ? '''[**/*~]
''' : '''["**/*~"]
''' , output ) 
  }
  void testCleanPatternList ( ) {
    script = """
includeTargets << gant.targets.Clean
cleanPattern << [ '**/*~' , '**/*.bak' ]
target ( testClean : '' ) { println ( cleanPattern ) }
"""
    assertEquals ( 0 , processCmdLineTargets ( 'testClean' ) )
    assertEquals ( ( groovyMinorVersion > 5 ) ? '''[[**/*~, **/*.bak]]
''' : '''[["**/*~", "**/*.bak"]]
''' , output ) 
  }
  void testClobberDirectoryString ( ) {
    script = """
includeTargets << gant.targets.Clean
clobberDirectory << 'target'
target ( testClobber : '' ) { println ( clobberDirectory ) }
"""
    assertEquals ( 0 , processCmdLineTargets ( 'testClobber' ) )
    assertEquals ( ( groovyMinorVersion > 5 ) ? '''[target]
''' : '''["target"]
''' , output ) 
  }
  void testClobberDirectoryList ( ) {
    script = """
includeTargets << gant.targets.Clean
clobberDirectory << [ 'target_a' , 'target_b' ]
target ( testClobber : '' ) { println ( clobberDirectory ) }
"""
    assertEquals ( 0 , processCmdLineTargets ( 'testClobber' ) )
    assertEquals ( ( groovyMinorVersion > 5 ) ? '''[[target_a, target_b]]
''' : '''[["target_a", "target_b"]]
''' , output ) 
  }
  void testClobberPatternString ( ) {
    script = """
includeTargets << gant.targets.Clean
clobberPattern << '**/*~'
target ( testClobber : '' ) {
  println ( clobberPattern )
}
"""
    assertEquals ( 0 , processCmdLineTargets ( 'testClobber' ) )
    assertEquals ( ( groovyMinorVersion > 5 ) ? '''[**/*~]
''' : '''["**/*~"]
''' , output ) 
  }
  void testClobberPatternList ( ) {
    script = """
includeTargets << gant.targets.Clean
clobberPattern << [ '**/*~' , '**/*.bak' ]
target ( testClobber : '' ) { println ( clobberPattern ) }
"""
    assertEquals ( 0 , processCmdLineTargets ( 'testClobber' ) )
    assertEquals ( ( groovyMinorVersion > 5 ) ? '''[[**/*~, **/*.bak]]
''' : '''[["**/*~", "**/*.bak"]]
''' , output ) 
  }
  void testParameterizedInclude ( ) {
   script = """
includeTargets ** gant.targets.Clean * [ cleanDirectory : 'target' ]
target ( testClean : '' ) { println ( cleanDirectory ) }
"""
    assertEquals ( 0 , processCmdLineTargets ( 'testClean' ) )
    assertEquals (  ( groovyMinorVersion > 5 ) ? '''[target]
''' : '''["target"]
''' , output ) 
  }
}
