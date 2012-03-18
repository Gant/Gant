//  Gant -- A Groovy way of scripting Ant tasks.
//
//  Copyright © 2009-10 Russel Winder
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
//
//  Author : Russel Winder <russel@winder.org.uk>

package  org.codehaus.gant.tests.bugs

import org.codehaus.gant.tests.GantTestCase

class GANT_108_Test extends GantTestCase {
  private testString = 'Hello.'
  private targetName = 'doit'
  private problemTargetBodyString = """
def writer = new StringWriter ( )
def xml = new MarkupBuilder ( writer )
xml.Configure { Set { println ( '${testString}' ) } }
println ( writer.toString ( ) )
"""
   private workingTargetBodyString = problemTargetBodyString.replace ( 'Set' , 'xml.Set' )
   private resultString = '''
<Configure>
  <Set />
</Configure>
'''

//  TODO : Enable the tests that show the GANT-108 problems.

  void X_test_inTargetProblem ( ) {
    script = 'import groovy.xml.MarkupBuilder ; target ( ' + targetName + ' : "" ) { ' + problemTargetBodyString + ' }'
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( '' , error )
    assertEquals ( resultString ( targetName , testString + resultString ) , output )
  }  
  void test_inTargetWorking ( ) {
    script = 'import groovy.xml.MarkupBuilder ; target ( ' + targetName + ' : "" ) { ' + workingTargetBodyString + ' }'
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( '' , error )
    assertEquals ( resultString ( targetName , testString + resultString ) , output )
  }  
  void X_test_inFunctionProblem ( ) {
    script = 'import groovy.xml.MarkupBuilder ; def doMarkup ( ) { ' + problemTargetBodyString + ' } ; target ( ' + targetName + ' : "" ) { doMarkup ( ) }'
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( '' , error )
    assertEquals ( resultString ( targetName , testString + resultString ) , output )
  }
  void test_inFunctionWorking ( ) {
    script = 'import groovy.xml.MarkupBuilder ; def doMarkup ( ) { ' + workingTargetBodyString + ' } ; target ( ' + targetName + ' : "" ) { doMarkup ( ) }'
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( '' , error )
    assertEquals ( resultString ( targetName , testString + resultString ) , output )
  }
  void X_test_inLocalClosureProblem ( ) {
    script = 'import groovy.xml.MarkupBuilder ; def doMarkup = { ' + problemTargetBodyString + ' } ; target ( ' + targetName + ' : "" ) { doMarkup ( ) }'
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( '' , error )
    assertEquals ( resultString ( targetName , testString + resultString ) , output )
  }
  void test_inLocalClosureWorking ( ) {
    script = 'import groovy.xml.MarkupBuilder ; def doMarkup = { ' + workingTargetBodyString + ' } ; target ( ' + targetName + ' : "" ) { doMarkup ( ) }'
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( '' , error )
    assertEquals ( resultString ( targetName , testString + resultString ) , output )
  }
  void X_test_inBindingClosureProblem ( ) {
    script = 'import groovy.xml.MarkupBuilder ; doMarkup = { ' + problemTargetBodyString + ' } ; target ( ' + targetName + ' : "" ) { doMarkup ( ) }'
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( '' , error )
    assertEquals ( resultString ( targetName , testString + resultString ) , output )
  }
  void test_inBindingClosureWorking ( ) {
    script = 'import groovy.xml.MarkupBuilder ; doMarkup = { ' + workingTargetBodyString + ' } ; target ( ' + targetName + ' : "" ) { doMarkup ( ) }'
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( '' , error )
    assertEquals ( resultString ( targetName , testString + resultString ) , output )
  }
  void test_evaluatedNeverWasAProblemWithProblem ( ) {
    script = 'evaluate ( """import groovy.xml.MarkupBuilder ; doMarkup = { ' + problemTargetBodyString + ' }""" ) ; target ( ' + targetName + ' : "" ) { doMarkup ( ) }'
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( '' , error )
    assertEquals ( resultString ( targetName , testString + resultString ) , output )
  }
  void test_evaluatedNeverWasAProblemWithWorking ( ) {
    script = 'evaluate ( """import groovy.xml.MarkupBuilder ; doMarkup = { ' + workingTargetBodyString + ' }""" ) ; target ( ' + targetName + ' : "" ) { doMarkup ( ) }'
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( '' , error )
    assertEquals ( resultString ( targetName , testString + resultString ) , output )
  }
}
