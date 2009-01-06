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

class GANT_32_Test extends GantTestCase {
  void testSingleFileFailsCorrectly ( ) {
    script = '''
target ( test : '' ) { foo }
def foo { badvariable }
'''
    assertEquals ( -2 , processCmdLineTargets( 'test' ) )
    assertEquals ( '''Error evaluating Gantfile: startup failed, standard_input: 3: unexpected token: foo @ line 3, column 5.
1 error

''' , output )
  }
  void testMultipleFilesFailsCorrectly ( ) {
    def file = File.createTempFile ( 'gant-' , '-GANT_32.groovy' )
    file.write ( '''target ( test : '' ) { foo }
def foo { badvariable }
''' )
    script = "includeTargets << new File ( '${escapeWindowsPath ( file.path )}' )"
    try { assertEquals ( -4 , processCmdLineTargets ( 'test' ) ) }
    finally { file.delete ( ) }
    assertTrue ( output.startsWith ( 'Standard input, line 1 -- Error evaluating Gantfile: org.codehaus.groovy.control.MultipleCompilationErrorsException: startup failed, ' ) )
    assertTrue ( output.endsWith ( '''GANT_32.groovy: 2: unexpected token: foo @ line 2, column 5.
   def foo { badvariable }
       ^

1 error

''' ) )
  }
}
