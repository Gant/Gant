//  Gant -- A Groovy build framework based on scripting Ant tasks.
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
 *  A test to ensure that the target specification works. 
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
final class Targets_Test extends GantTestCase {
  final result = 'OK.'
  void testNoDescription ( ) {
    script = "target ( noDescription : '' ) { print ( '${result}' ) }"
    assertEquals ( 0 , processTargets ( 'noDescription' ) )
    assertEquals ( result , output ) 
  }
  void testWithDescription ( ) {
    script = "target ( withDescription : 'Blah blah' ) { print ( '${result}' ) }"
    assertEquals ( 0 , processTargets ( 'withDescription' ) )
    assertEquals ( result , output ) 
  }
  void testEmptyMap ( ) {
    script = "target ( [ : ] ) { print ( '${result}' ) }"
    assertEquals ( -2 , processTargets ( 'withDescription' ) )
    assertEquals ( 'Standard input, line 1 -- Error evaluating Gantfile: Target specified without a name.\n' , output ) 
  }
  void testMultipleEntries ( ) {
    script = "target ( fred : '' , debbie : '' ) { print ( '${result}' ) }"
    assertEquals ( -2 , processTargets ( 'withDescription' ) )
    assertEquals ( 'Standard input, line 1 -- Error evaluating Gantfile: Target specified with multiple names.\n' , output ) 
  }
  void testOverwriting ( ) {
    //
    //  TODO: Fix the problem of overwriting targets.  Until changed, creating a new symbol in the binding
    //  using a target overwrites the old symbol.  This is clearly wrong behaviour and needs amending.
    //
    script = '''
target ( hello : '' ) { println ( 'Hello 1' ) }
target ( hello : '' ) { println ( 'Hello 2' ) }
'''
    System.err.println ( 'testOverwriting: This test is wrong -- it shows that target overwriting is supported.' )
    assertEquals ( 0 , processTargets ( 'hello' ) )
    assertEquals ( 'Hello 2\n' , output )
  }
  void testForbidRedefinitionOfTarget ( ) {
    script = '''
target ( test : '' ) { }
target = 10
'''
    assertEquals ( -2 , processTargets ( 'test' ) )
    assertEquals ( 'Standard input, line 3 -- Error evaluating Gantfile: Cannot redefine symbol target\n' , output )
  }
  void testStringParameter ( ) {
    script = "target ( 'string' ) { print ( '${result}' ) }"
    assertEquals ( -2 , processTargets ( 'string' ) )
    assertTrue ( output.startsWith ( 'Standard input, line 1 -- Error evaluating Gantfile: No signature of method: org.codehaus.gant.GantBinding$_initializeGantBinding_closure' ) ) 
  }
  void testStringSequenceParameter ( ) {
    script = "target ( 'key' , 'description' ) { print ( '${result}' ) }"
    assertEquals ( -2 , processTargets ( 'key' ) )
    assertTrue ( output.startsWith ( 'Standard input, line 1 -- Error evaluating Gantfile: No signature of method: org.codehaus.gant.GantBinding$_initializeGantBinding_closure' ) ) 
  }
  void testMissingTargetInScriptExplicitTarget ( ) {
    script = 'setDefaultTarget ( blah )'
    assertEquals ( -2 , processTargets ( 'blah' ) )
    assertEquals ( 'Standard input, line 1 -- Error evaluating Gantfile: No such property: blah for class: standard_input\n' , output )
  }
  void testMissingTargetInScriptDefaultTarget ( ) {
    script = 'setDefaultTarget ( blah )'
    assertEquals ( -2 , processTargets ( ) )
    assertEquals ( 'Standard input, line 1 -- Error evaluating Gantfile: No such property: blah for class: standard_input\n' , output )
  }
  void testFaultyScript ( ) {
    script = 'XXXXX : YYYYY ->'
    assertEquals ( -2 , processTargets ( ) )
    assertEquals ( '''Error evaluating Gantfile: startup failed, standard_input: 1: unexpected token: -> @ line 1, column 15.
1 error

''' , output )
  }

  //  Tests resulting from GANT-45.

  final theTarget = 'stuff'
  final testScript = """
target ( ${theTarget} : '' ) { println ( home ) }
setDefaultTarget ( ${theTarget} )
"""
  final expectedOutput = ( ( groovyMinorVersion < 6 ) ? '' : 'Standard input, line 2 -- ' ) + 'Error evaluating Gantfile: No such property: home for class: standard_input\n'
  void test_GANT_45_MessageBugDefaultTarget ( ) {
    script = testScript
    assertEquals ( -12 , processTargets ( ) )
    assertEquals ( expectedOutput , output )
  }
  void test_GANT_45_MessageBugExplicitTarget ( ) {
    script = testScript
    assertEquals ( -11 , processTargets ( theTarget ) )
    assertEquals ( expectedOutput , output )
  }
  
  //  Tests resulting from GANT-55 -- GString as aparameter to depends call causes problems.

  void test_GANT_55_originalFailure ( ) {
    def targetName = 'test'
    script = '''
def tag = "''' + targetName + '''"
target ( "${tag}.profile" : "Profile for $tag" ) { println "Profile for $tag" }
target ( "${tag}.compile" : "Compile for $tag" ) {
  depends ( "${tag}.profile" )
  println "Compile for $tag"
}'''
    /*
     *  The original behaviour:
     *
    assertEquals ( -13 , processTargets ( 'test.compile' ) )
    assertEquals ( 'depends called with an argument (test.profile) that is not a known target or list of targets.\n' , output )
    *
    *  Is now fixed:
    */
    assertEquals ( 0 , processTargets ( 'test.compile' ) )
    assertEquals ( """Profile for ${targetName}
Compile for ${targetName}
""" , output )
  }
  void test_GANT_55_originalFixed ( ) {
    def targetName = 'test'
    script = '''
def tag = "''' + targetName + '''"
target ( "${tag}.profile" : "Profile for $tag" ) { println "Profile for $tag" }
target ( "${tag}.compile" : "Compile for $tag" ) {
  depends ( '' + "${tag}.profile" )
  println "Compile for $tag"
}'''
    assertEquals ( 0 , processTargets ( 'test.compile' ) )
    assertEquals ( """Profile for ${targetName}
Compile for ${targetName}
""" , output )
  }

  void testGStringWorkingAsATargetName ( ) {
    def targetName = 'fred'
    script = '''
def name = "''' + targetName + '''"
target ( "${name}" : '' ) {
  println ( "${name}" )
}
'''
    assertEquals ( 0 , processTargets ( targetName ) )
    assertEquals ( targetName + '\n' , output )
  }
}
