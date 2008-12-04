//  Gant -- A Groovy way of scripting Ant tasks.
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
 *  A test to ensure that the return value of Gant is reasonable.
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
final class ReturnValue_Test extends GantTestCase {
  void testMissingMethodInDefaultTarget ( ) {
    script = '''
target ( 'default' : '' ) { blah ( ) }
'''
    assertEquals ( -13 , processCmdLineTargets ( ) )
  }
  void testMissingMethodInNonDefaultTarget ( ) {
    script = '''
target ( doit : '' ) { blah ( ) }
'''
    assertEquals ( -13 , processCmdLineTargets ( 'doit' ) )
  }
  void testMissingPropertyInDefaultTarget ( ) {
    script = '''
target ( 'default' : '' ) { x = blah }
'''
    assertEquals ( -12 , processCmdLineTargets ( ) )
  }
  void testMissingPropertyInNonDefaultTarget ( ) {
    script = '''
target ( doit : '' ) { x = blah }
'''
    assertEquals ( -11 , processCmdLineTargets ( 'doit' ) )
  }
  void testExplicitReturnCodeInDefaultTarget ( ) {
    def code = 27
    script = """
target ( 'default' : '' ) { ${code} }
"""
    assertEquals ( code , processCmdLineTargets ( ) )
  }
  void testExplicitReturnCodeInNonDefaultTarget ( ) {
    def code = 28
    script = """
target ( doit : '' ) { ${code} }
"""
    assertEquals ( code , processCmdLineTargets ( 'doit' ) )
  }
  void testScriptCompilationError ( ) {
    script = '''
this is definitely not a legal script.
'''
    assertEquals ( -2 , processCmdLineTargets ( ) )
  }
  void testCannotFindScript ( ) {
    assertEquals ( -3 , ( new gant.Gant ( ) ).processArgs ( [ '-f' , 'blah_blah_blah_blah' ] as String[] ) )
  }
}
