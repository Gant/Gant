//  Gant -- A Groovy way of scripting Ant tasks.
//
//  Copyright © 2011 Russel Winder
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

/**
 *  At some point during the 1.9.4 release cycle there was a regression of behaviour critical to Grails.
 *
 *  <p>Original idea for the test due to Jeff Brown.</p>
 *
 *  @author Russel Winder <russel@winder.org.uk>
 */
final class Regression_1_9_4_Test extends GantTestCase {
  final theMessage = 'The Default Target Is Running...'
  final expectedOutput = theMessage + '\n'
  final expectedDecoratedOutput = "default:\n${expectedOutput}${exitMarker}default\n"
  final groovyScript = """
target ( default : 'some default target' ) {
    println ( '${theMessage}' )
}
"""
  final groovyProgramTemplate = """
import gant.Gant
doNothingClosure = { }
gant = new Gant ( )
gant.loadScript ('''
${groovyScript}
''' )
gant.prepareTargets ( )
__ITEM__
gant.executeTargets ( )
"""
  void testForExpectedBehaviourOfBaseProgram ( ) {
    final groovyShell = new GroovyShell ( )
    groovyShell.evaluate ( groovyProgramTemplate.replace ( '__ITEM__' , '' ) )
    assertEquals ( expectedDecoratedOutput , output )
  }
  void testForPresenceOfTheRegression ( ) {
    final groovyProgram = groovyProgramTemplate.replace ( '__ITEM__' , '''
gant.setAllPerTargetPostHooks ( doNothingClosure )
gant.setAllPerTargetPreHooks ( doNothingClosure )
''')
    final groovyShell = new GroovyShell ( )
    groovyShell.evaluate ( groovyProgram )
    assertEquals ( expectedOutput , output )
  }
  void testForCorrectBehaviourOfScript ( ) {
    script = groovyScript
    assertEquals ( 0 , processTargets ( ) )
    assertEquals ( expectedDecoratedOutput , output )
  }

  final switchOffHooks = """
setAllPerTargetPreHooks ( { } )
setAllPerTargetPostHooks ( { } )
"""
  void testForExpectedBehaviourOfPreAmendedScript ( ) {
    script = switchOffHooks + groovyScript
    assertEquals ( 0 , processTargets ( ) )
    assertEquals ( expectedDecoratedOutput , output )
  }
  void testForExpectedBehaviourOfPostAmendedScript ( ) {
    script = groovyScript + switchOffHooks
    assertEquals ( 0 , processTargets ( ) )
    assertEquals ( expectedOutput , output )
  }
}
