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
    assertEquals ( 0 , processCmdLineTargets ( ) )
    assertEquals ( resultString ( 'default' , 'Default Target\n' ) , output )
    assertEquals ( '' , error )
  }
  void testTarget1 ( ) {
    final targetName = 'target1'
    script = theScript
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , 'Target One\n' ) , output )
    assertEquals ( '' , error )
  }
  void testTarget2 ( ) {
    final targetName = 'target2'
    script = theScript
    assertEquals ( -11 , processCmdLineTargets ( targetName ) ) 
    assertEquals ( "${targetName}:\nTarget Two\n" , output )
    assertEquals ( "Standard input, line 7 -- Error evaluating Gantfile: No such property: p1 for class: standard_input\n" , error )
  }
  void testDefaultTargetCommandLine ( ) {
    script = theScript
    assertEquals ( 0 , gant.processArgs ( [ '-f' , '-' ] as String[] ) )
    assertEquals ( resultString ( 'default' , 'Default Target\n' ) , output )
    assertEquals ( '' , error )
  }
  void testTarget1CommandLine ( ) {
    final targetName = 'target1'
    script = theScript
    assertEquals ( 0 , gant.processArgs ( [ '-f' , '-' , targetName ] as String[] ) )
    assertEquals ( resultString ( targetName , 'Target One\n' ) , output )
    assertEquals ( '' , error )
  }
  void testTarget2CommandLine ( ) {
    final targetName = 'target2'
    script = theScript
    assertEquals ( -11 , gant.processArgs ( [ '-f' , '-' , targetName ] as String[] ) )
    assertEquals ( "${targetName}:\nTarget Two\n" , output )
    assertEquals ( "Standard input, line 7 -- Error evaluating Gantfile: No such property: p1 for class: standard_input\n" , error )
  }
  void testTarget2CommandLineWithDefinitionNoSpace ( ) {
    final targetName = 'target2'
    script = theScript
    assertEquals ( 0 , gant.processArgs ( [ '-Dp1=MyVal' , '-f' , '-' , targetName ] as String[] ) )
    assertEquals (resultString ( targetName ,  '''Target Two
p1=MyVal
''' ) , output )
    assertEquals ( '' , error )
  }
  void testTarget2CommandLineWithDefinitionWithSpace ( ) {
    final targetName = 'target2'
    script = theScript
    assertEquals ( 0 , gant.processArgs ( [ '-D' , 'p1=MyVal' , '-f' , '-' , targetName ] as String[] ) )
    assertEquals ( resultString ( targetName , '''Target Two
p1=MyVal
''' ) , output )
    assertEquals ( '' , error )
  }
}
