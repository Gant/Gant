package org.codehaus.gant.tests.bugs

import org.codehaus.gant.tests.GantTestCase

class GANT_32 extends GantTestCase {
  void testSingleFileFailsCorrectly ( ) {
    script = '''
target ( test : '' ) { foo }
def foo { badvariable }
'''
    assertEquals ( -2 , processTargets ( 'test' ) )
    assertEquals ( '''Error evaluating Gantfile: startup failed, standard_input: 3: unexpected token: foo @ line 3, column 5.
1 error

''' , output )
  }
  void testMultipleFilesFailsCorrectly ( ) {
    def file = File.createTempFile ( 'gant-' , '-GANT_32.groovy' )
    file.write ( '''target ( test : '' ) { foo }
def foo { badvariable }
''' )
    script = "includeTargets << new File ( '${file.path}' )"
    try { assertEquals ( -2 , processTargets ( 'test' ) ) }
    finally { file.delete ( ) }
    assertTrue ( output.startsWith ( 'Standard input, line 1 -- Error evaluating Gantfile: startup failed, ' ) )
    assertTrue ( output.endsWith ( ''': 2: unexpected token: foo @ line 2, column 5.
   def foo { badvariable }
       ^

1 error

''' ) )
  }
}
