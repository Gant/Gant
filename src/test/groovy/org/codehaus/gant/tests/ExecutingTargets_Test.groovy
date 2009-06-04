//  Gant -- A Groovy way of scripting Ant tasks.
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
 *  A test to ensure that the target executing works. 
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
final class ExecutingTargets_Test extends GantTestCase {
  final coreScript = '''
target ( something : "Do something." ) { }
target ( somethingElse : "Do something else." ) { }
'''
  void testSomethingArgs ( ) {
    script = coreScript
    assertEquals ( 0 , gant.processArgs ( [ '-f' ,  '-' , 'something' ] as String[] ) )
    assertEquals ( '' , output ) 
  }
  void testSomethingTargets ( ) {
    script = coreScript
    assertEquals ( 0 , processCmdLineTargets ( 'something' ) )
    assertEquals ( '' , output ) 
  }
  void testCleanAndSomethingArgs ( ) {
    script = 'includeTargets << gant.targets.Clean\n' + coreScript
    assertEquals ( 0 , gant.processArgs ( [ '-f' ,  '-' , 'clean' , 'something' ] as String[] ) )
    assertEquals ( '' , output ) 
  }
  void testCleanAndSomethingTargets ( ) {
    script = 'includeTargets << gant.targets.Clean\n' + coreScript
    assertEquals ( 0 , processCmdLineTargets ( [ 'clean' , 'something' ] ) )
    assertEquals ( '' , output ) 
  }

 //  GANT-44 asks for targets to have access to the command line target list so that it can be processed in targets.

  void testTargetsListIsAccessbileAnChangeable ( ) {
    script = '''
target ( testing : '' ) {
  assert targets.class == ArrayList
  assert targets.size ( ) == 3
  assert targets[0] == 'testing'
  assert targets[1] == 'one'
  assert targets[2] == 'two'
  def x = targets.remove ( 1 )
  assert x == 'one'
  assert targets.size ( ) == 2
  assert targets[0] == 'testing'
  assert targets[1] == 'two'
}
'''
    assertEquals ( -11 , processCmdLineTargets ( [ 'testing' , 'one' , 'two' ] ) )
    assertEquals ( 'Target two does not exist.\n' , output )
  }
  
  //  GANT-81 requires that the target finalize is called in all circumstances if it is present.  If it
  //  contains dependencies then they are ignored.

  void testFinalizeIsCalledNormally ( ) {
    def testingMessage =  'testing called'
    def finalizeMessage = 'finalize called'
    script = """
target ( testing : '' ) { println ( '${testingMessage}' ) }
target ( finalize : '' ) { println ( '${finalizeMessage}' ) }
"""
    assertEquals ( 0 , processCmdLineTargets ( 'testing' ) )
    assertEquals ( """${testingMessage}
${finalizeMessage}
""" , output )
  }
  void testFinalizeIsCalledOnAnException ( ) {
    def testingMessage =  'testing forcibly failed'
    def finalizeMessage = 'finalize called'
    script = """
target ( testing : '' ) { throw new RuntimeException ( '${testingMessage}' ) }
target ( finalize : '' ) { println ( '${finalizeMessage}' ) }
"""
    assertEquals ( -13 , processCmdLineTargets ( 'testing' ) )
    assertEquals ( """${finalizeMessage}
java.lang.RuntimeException: ${testingMessage}
""" , output )
  }
  
}
