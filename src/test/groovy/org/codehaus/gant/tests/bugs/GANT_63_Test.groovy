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

package org.codehaus.gant.tests.bugs

import org.codehaus.gant.tests.GantTestCase

class GANT_63_Test extends GantTestCase {
  void testExceptionFailsCorrectly ( ) {
    script = '''
target ( main : '' ) {
  def f = new File ( 'blahblahblahblahblah' )
  println ( 'before' )
  f.eachDir { println ( it ) }
  println ( 'after' )
}
setDefaultTarget ( main )
'''
    assertEquals ( -13 , processCmdLineTargets ( ) )
    assertTrue ( output.startsWith ( '''before
java.io.FileNotFoundException: ''' ) )
    assertTrue ( output.endsWith ( 'blahblahblahblahblah\n' ) )
  }
}
