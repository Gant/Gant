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

class GANT_4_Test extends GantTestCase {
  final theScript = '''
target ( target1 : 'This has no properties.' ) {
	println "Target One"
}
target ( target2 : 'with command line properties' ) {
	println "Target Two"
	println "p1=${p1}"
}
target ( 'default' : 'The default target.' ) { 
	println "Default Target"
}
'''
  void testDefaultTarget ( ) {
    script = theScript
    assertEquals ( 0 , processTargets ( ) )
    assertEquals ( 'Default Target\n' , output )
  }
  void testTarget1 ( ) {
    script = theScript
    assertEquals ( 0 , processTargets ( 'target1' ) )
    assertEquals ( 'Target One\n' , output )
  }
  void testTarget2 ( ) {
    script = theScript
    assertEquals ( -11 , processTargets ( 'target2' ) )
    assertEquals ( '''Target Two
Standard input, line 7 -- Error evaluating Gantfile: No such property: p1 for class: standard_input
''' , output )
  }
  void testDefaultTargetCommandLine ( ) {
    script = theScript
    assertEquals ( 0 , gant.processArgs ( [ '-f' , '-' ] as String[] ) )
    assertEquals ( 'Default Target\n' , output )
  }
  void testTarget1CommandLine ( ) {
    script = theScript
    assertEquals ( 0 , gant.processArgs ( [ '-f' , '-' , 'target1' ] as String[] ) )
    assertEquals ( 'Target One\n' , output )
  }
  void testTarget2CommandLine ( ) {
    script = theScript
    assertEquals ( -11 , gant.processArgs ( [ '-f' , '-' , 'target2' ] as String[] ) )
    assertEquals ( '''Target Two
Standard input, line 7 -- Error evaluating Gantfile: No such property: p1 for class: standard_input
''' , output )
  }
  void testTarget2CommandLineWithDefinitionNoSpace ( ) {
    script = theScript
    assertEquals ( 0 , gant.processArgs ( [ '-Dp1=MyVal' , '-f' , '-' , 'target2' ] as String[] ) )
    assertEquals ( '''Target Two
p1=MyVal
''' , output )
  }
  void testTarget2CommandLineWithDefinitionWithSpace ( ) {
    script = theScript
    assertEquals ( 0 , gant.processArgs ( [ '-D' , 'p1=MyVal' , '-f' , '-' , 'target2' ] as String[] ) )
    assertEquals ( '''Target Two
p1=MyVal
''' , output )
  }
}
