//  Gant -- A Groovy build framework based on scripting Ant tasks.
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

//  Developed from the bug report by Phil Swenson.

class GANT_45_Test extends GantTestCase {
  final theTarget = 'stuff'
  final testScript = """
target ( ${theTarget} : '' ) { println ( home ) }
setDefaultTarget ( ${theTarget} )
"""
  final expectedOutput = 'Standard input, line 2 -- Error evaluating Gantfile: No such property: home for class: standard_input\n'
  void testMMEMessageBugDefaultTarget ( ) {
    script = testScript
    assertEquals ( -12 , processTargets ( ) )
    assertEquals ( expectedOutput , output )
  }
  void testMMEMessageBugExplicitTarget ( ) {
    script = testScript
    assertEquals ( -11 , processTargets ( theTarget ) )
    assertEquals ( expectedOutput , output )
  }
}
