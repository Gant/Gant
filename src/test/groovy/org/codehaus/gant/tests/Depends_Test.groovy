//  Gant -- A Groovy way of scripting Ant tasks.
//
//  Copyright © 2006-10 Russel Winder
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
 *  A test for the depends processing, i.e. make sure the depends calls the method when appropriate and not
 *  when appropriate.
 *
 *  @author Russel Winder <russel@winder.org.uk>
 */
final class Depends_Test extends GantTestCase {
  final outputMessage = 'done.'
  final targetName = 'getOnWithIt'
  final outputFunction = 'outputFunction'
   final caseA = 'caseA'
   final caseB = 'caseB'
   final caseC = 'caseC'
  void testNone ( ) {
    script = """
target ( ${outputFunction} : '' ) { println ( '${outputMessage}' ) }
target ( ${caseA} : '' ) { ${outputFunction} ( ) }
target ( ${caseB} : '' ) { ${outputFunction} ( ) }
target ( ${caseC} : '' ) { ${outputFunction} ( ) }
target ( ${targetName} : '' ) { ${caseA} ( ) ; ${caseB} ( ) ; ${caseC} ( ) }
"""
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName ,
                                  resultString ( caseA , resultString ( outputFunction , outputMessage + '\n' ) )
                                  + resultString ( caseB , resultString ( outputFunction , outputMessage + '\n' ) )
                                  + resultString ( caseC , resultString ( outputFunction , outputMessage + '\n' ) )
                                  ) , output )
    assertEquals ( '' , error )
  }
  void testMixed ( ) {
    script = """
target ( ${outputFunction} : '' ) { println ( '${outputMessage}' ) }
target ( ${caseA} : '' ) { depends ( ${outputFunction} ) }
target ( ${caseB} : '' ) { ${outputFunction} ( ) }
target ( ${caseC} : '' ) { depends ( ${outputFunction} ) }
target ( ${targetName} : '' ) { ${caseA} ( ) ; ${caseB} ( ) ; ${caseC} ( ) }
"""
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , 
                                  resultString ( caseA , resultString ( outputFunction , outputMessage + '\n' ) )
                                  + resultString ( caseB , resultString ( outputFunction , outputMessage + '\n' ) )
                                  + resultString ( caseC , '' )
                                  ) , output ) 
    assertEquals ( '' , error )
  }
  void testAll ( ) {
    script = """
target ( ${outputFunction} : '' ) { println ( '${outputMessage}' ) }
target ( ${caseA} : '' ) { depends ( ${outputFunction} ) }
target ( ${caseB} : '' ) { depends ( ${outputFunction} ) }
target ( ${caseC} : '' ) { depends ( ${outputFunction} ) }
target ( ${targetName} : '' ) { ${caseA} ( ) ; ${caseB} ( ) ; ${caseC} ( ) }
"""
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName ,
                                  resultString ( caseA , resultString ( outputFunction , outputMessage + '\n' ) )
                                  + resultString ( caseB , '' )
                                  + resultString ( caseC , '' )
                                  ) , output ) 
    assertEquals ( '' , error )
  }
  void testMultiple ( ) {
    script = """
target ( ${outputFunction} : '' ) { println ( '${outputMessage}' ) }
target ( ${caseA} : '' ) { depends ( ${outputFunction} ) }
target ( ${caseB} : '' ) { depends ( ${outputFunction} ) }
target ( ${caseC} : '' ) { depends ( ${outputFunction} ) }
target ( ${targetName} : '' ) { depends ( ${caseA} , ${caseB} , ${caseC} ) }
"""
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName ,
                                  resultString ( caseA , resultString ( outputFunction , outputMessage + '\n' ) )
                                  + resultString ( caseB , '' )
                                  + resultString ( caseC , '' )
                                  ) , output ) 
    assertEquals ( '' , error )
  }
  void testList ( ) {
    script = """
target ( ${outputFunction} : '' ) { println ( '${outputMessage}' ) }
target ( ${caseA} : '' ) { depends ( ${outputFunction} ) }
target ( ${caseB} : '' ) { depends ( ${outputFunction} ) }
target ( ${caseC} : '' ) { depends ( ${outputFunction} ) }
target ( ${targetName} : '' ) { depends ( [ ${caseA} , ${caseB} , ${caseC} ] ) }
"""
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName ,
                                  resultString ( caseA , resultString ( outputFunction , outputMessage + '\n' ) )
                                  + resultString ( caseB , '' )
                                  + resultString ( caseC , '' )
                                  ) , output )
    assertEquals ( '' , error )
  }
  void testNotClosure ( ) {
    script = """
datum = 1
target ( ${targetName} : '' ) { depends ( datum ) }
"""
    assertEquals ( -13 , processCmdLineTargets ( targetName ) )
    assertEquals ( targetName + ':\n' , output )
    assertEquals ( 'java.lang.RuntimeException: depends called with an argument (1) that is not a known target or list of targets.\n' , error )
  }
  void testNotListClosure ( ) {
    script = """
datum = 1
target ( ${targetName} : '' ) { depends ( [ datum ] ) }
"""
    assertEquals ( -13 , processCmdLineTargets ( targetName ) )
    assertEquals ( targetName + ':\n' , output )
    assertEquals ( 'java.lang.RuntimeException: depends called with an argument (1) that is not a known target or list of targets.\n' , error )
  }
  void testOutOfOrder ( ) {
    script = """
target ( ${targetName} : '' ) { depends ( ${caseA} , ${caseB} , ${caseC} ) }
target ( ${caseC} : '' ) { depends ( ${outputFunction} ) }
target ( ${caseB} : '' ) { depends ( ${outputFunction} ) }
target ( ${caseA} : '' ) { depends ( ${outputFunction} ) }
target ( ${outputFunction} : '' ) { println ( '${outputMessage}' ) }
"""
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName ,
                                  resultString ( caseA , resultString ( outputFunction , outputMessage + '\n' ) )
                                  + resultString ( caseB , '' )
                                  + resultString ( caseC , '' )
                                  ) , output )
    assertEquals ( '' , error )
  }
  void testOutOfOrderList ( ) {
    script = """
target ( ${targetName} : '' ) { depends ( [ ${caseA} , ${caseB} , ${caseC} ] ) }
target ( ${caseC} : '' ) { depends ( ${outputFunction} ) }
target ( ${caseB} : '' ) { depends ( ${outputFunction} ) }
target ( ${caseA} : '' ) { depends ( ${outputFunction} ) }
target ( ${outputFunction} : '' ) { println ( '${outputMessage}' ) }
"""
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName ,
                                  resultString ( caseA , resultString ( outputFunction , outputMessage + '\n' ) )
                                  + resultString ( caseB , '' )
                                  + resultString ( caseC , '' )
                                  ) , output )
    assertEquals ( '' , error )
  }
  void testSameTargetAndFileName ( ) {
    //  Having a target of the same name as the script being compiled is fine until the target name is used in
    //  a depend.  At this point the class name not the name in the binding is picked up and all hell breaks
    //  loose.  Standard input is compiled as class standard_input.
    script = """
target ( standard_input , '' ) { println ( '${outputMessage}' ) }
target ( ${targetName} , '' ) { depends ( standard_input ) }
"""
    assertEquals ( -4 , processCmdLineTargets ( targetName ) )
    assertTrue ( error.startsWith ( 'Standard input, line 2 -- Error evaluating Gantfile: No signature of method: ' ) )
  }
  void testStringParameter ( ) {
    script = """
target ( ${caseA} : '' ) { println ( '${outputMessage}' ) }
target ( ${targetName} : '' ) { depends ( '${caseA}' ) }
"""
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , resultString ( caseA , outputMessage + '\n' ) ) , output )
    assertEquals ( '' , error )
  }
  void testStringListParameter ( ) {
    script = """
target ( ${caseA} : '' ) { println ( '${outputMessage}' ) }
target ( ${caseB} : '' ) { println ( '${outputMessage}' ) }
target ( ${targetName} : '' ) { depends ( [ '${caseA}' , '${caseB}' ] ) }
"""
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , 
                                  resultString ( caseA , outputMessage + '\n' )
                                  + resultString ( caseB , outputMessage + '\n' )
                                  ) , output )
    assertEquals ( '' , error )
  }
  void testMixedListParameter ( ) {
    script = """
target ( ${caseA} : '' ) { println ( '${outputMessage}' ) }
target ( ${caseB} : '' ) { println ( '${outputMessage}' ) }
target ( ${targetName} : '' ) { depends ( [ ${caseA} , '${caseB}' ] ) }
"""
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , 
                                  resultString ( caseA , outputMessage + '\n' )
                                  + resultString ( caseB , outputMessage + '\n' )
                                  ) , output )
    assertEquals ( '' , error )
  }
  void testCircularDependency ( ) {
    //  Should this actually fail? cf. GANT-9.  Current view is that it is fine as is.
    script = '''
target ( A : '' ) { depends ( B ) ; println ( 'A' ) }
target ( B : '' ) { depends ( C )  ; println ( 'B' ) }
target ( C : '' ) { depends ( A )  ; println ( 'C' ) }
'''
    assertEquals ( 0 , processCmdLineTargets ( 'A' ) )
    assertEquals ( resultString ( 'A' ,
                                  resultString ( 'B' , 
                                                 resultString ( 'C' ,
                                                                resultString ( 'A' , 'A\n' )
                                                                + 'C\n' )
                                                 + 'B\n' )
                                  + 'A\n' ) , output )
    assertEquals ( '' , error )
  }
  void testMultipleIndependentTargets ( ) {
    script = """
target ( ${caseA} : '' ) { println ( '${caseA}' ) }
target ( ${caseB} : '' ) { println ( '${caseB}' ) }
"""
    assertEquals ( 0 , processCmdLineTargets ( [ caseA , caseB ] ) )
    assertEquals ( resultString ( caseA , caseA + '\n' ) + resultString ( caseB , caseB + '\n' ) , output )
    assertEquals ( '' , error )
  }
  void testEmbeddedDepend ( ) {
    script = """
target ( ${caseA} : '' ) { println ( '${outputMessage}' ) }
target ( ${targetName} : '' ) { (0..3).each { depends ( ${caseA} ) } }
"""
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , resultString ( caseA , outputMessage + '\n' ) ) , output )
    assertEquals ( '' , error )
  }
  //  cf. GANT-26
  void testMultipleDependentTargets ( ) {
    script = """
target ( ${caseA} : '' ) {
  depends ( ${caseB} )
  println ( '${caseA}' )
}
target ( ${caseB} : '' ) { println ( '${caseB}' ) }
"""
    assertEquals ( 0 , processCmdLineTargets ( [ caseA , caseB ] ) )
    assertEquals ( resultString ( caseA , resultString ( caseB , caseB + '\n' ) + caseA + '\n' ) + resultString ( caseB , caseB + '\n' ) , output )
    assertEquals ( '' , error )
  }
}
