//  Gant -- A Groovy way of scripting Ant tasks.
//
//  Copyright © 2008-9 Russel Winder
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

package org.codehaus.gant.tests.bugs

import org.codehaus.gant.tests.GantTestCase

class GANT_58_Test extends GantTestCase {
  void testSingleFileFailsCorrectly ( ) {
    def file = File.createTempFile ( 'gant_' , '_GANT_58_Test.groovy' )
    file.write ( '''
def a = 1
def b = 0
def c = a / b
''' )
    script = """
includeTargets << new File ( '${escapeWindowsPath ( file.path )}' )
target ( 'default' , '' ) { }
"""
    try {
      assertEquals ( -4 , processCmdLineTargets ( ) )
      assertEquals ( "Standard input, line 2 -- Error evaluating Gantfile: ${file.path}, line 4 -- java.lang.ArithmeticException: / by zero\n" , output )
    }
    finally { file.delete ( ) }
  }
}
