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
 *  A test for the depends processing, i.e. make sure the depends calls the method when appropriate and not
 *  when appropriate.
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
final class Depends_Test extends GantTestCase {
  void testNone ( ) {
    script = '''
target ( noneDoit : '' ) { println ( 'done.' ) }
target ( noneDoA : '' ) { noneDoit ( ) }
target ( noneDoB : '' ) { noneDoit ( ) }
target ( noneDoC : '' ) { noneDoit ( ) }
target ( noneDoAll : '' ) { noneDoA ( ) ; noneDoB ( ) ; noneDoC ( ) }
'''
    assertEquals ( 0 , processTargets ( 'noneDoAll' ) )
    assertEquals ( '''done.
done.
done.
''' , output ) 
  }
  void testMixed ( ) {
    script = '''
target ( mixedDoit : '' ) { println ( 'done.' ) }
target ( mixedDoA : '' ) { depends ( mixedDoit ) }
target ( mixedDoB : '' ) { mixedDoit ( ) }
target ( mixedDoC : '' ) { depends ( mixedDoit ) }
target ( mixedDoAll : '' ) { mixedDoA ( ) ; mixedDoB ( ) ; mixedDoC ( ) }
'''
    assertEquals ( 0 , processTargets ( 'mixedDoAll' ) )
    assertEquals ( '''done.
done.
''' , output ) 
  }
  void testAll ( ) {
    script = '''
target ( allDoit : '' ) { println ( 'done.' ) }
target ( allDoA : '' ) { depends ( allDoit ) }
target ( allDoB : '' ) { depends ( allDoit ) }
target ( allDoC : '' ) { depends ( allDoit ) }
target ( allDoAll : '' ) { allDoA ( ) ; allDoB ( ) ; allDoC ( ) }
'''
    assertEquals ( 0 , processTargets ( 'allDoAll' ) )
    assertEquals ( 'done.\n' , output ) 
  }
  void testMultiple ( ) {
    script = '''
target ( multipleDoit : '' ) { println ( 'done.' ) }
target ( multipleDoA : '' ) { depends ( multipleDoit ) }
target ( multipleDoB : '' ) { depends ( multipleDoit ) }
target ( multipleDoC : '' ) { depends ( multipleDoit ) }
target ( multipleDoAll : '' ) { depends ( multipleDoA , multipleDoB , multipleDoC ) }
'''
    assertEquals ( 0 , processTargets ( 'multipleDoAll' ) )
    assertEquals ( 'done.\n' , output ) 
  }
  void testList ( ) {
    script = '''
target ( listDoit : '' ) { println ( 'done.' ) }
target ( listDoA : '' ) { depends ( listDoit ) }
target ( listDoB : '' ) { depends ( listDoit ) }
target ( listDoC : '' ) { depends ( listDoit ) }
target ( listDoAll : '' ) { depends ( [ listDoA , listDoB , listDoC ] ) }
'''
    assertEquals ( 0 , processTargets ( 'listDoAll' ) )
    assertEquals ( 'done.\n' , output ) 
  }
  void testNotClosure ( ) {
    script = '''
datum = 1
target ( notClosure : '' ) { depends ( datum ) }
'''
    assertEquals ( -13 , processTargets ( 'notClosure' ) )
    assertEquals ( 'depends called with an argument (1) that is not a known target or list of targets.\n' , output )
  }
  void testNotListClosure ( ) {
    script = '''
datum = 1
target ( notListClosure : '' ) { depends ( [ datum ] ) }
'''
    assertEquals ( -13 , processTargets ( 'notListClosure' ) )
    assertEquals ( 'depends called with an argument (1) that is not a known target or list of targets.\n' , output )
  }
  void testOutOfOrder ( ) {
    script = '''
target ( outOfOrderDoAll : '' ) { depends ( outOfOrderDoA , outOfOrderDoB , outOfOrderDoC ) }
target ( outOfOrderDoC : '' ) { depends ( outOfOrderDoit ) }
target ( outOfOrderDoB : '' ) { depends ( outOfOrderDoit ) }
target ( outOfOrderDoA : '' ) { depends ( outOfOrderDoit ) }
target ( outOfOrderDoit : '' ) { println ( 'done.' ) }
'''
    assertEquals ( 0 , processTargets ( 'outOfOrderDoAll' ) )
    assertEquals ( 'done.\n' , output )
  }
  void testOutOfOrderList ( ) {
    script = '''
target ( outOfOrderListDoAll : '' ) { depends ( [ outOfOrderListDoA , outOfOrderListDoB , outOfOrderListDoC ] ) }
target ( outOfOrderListDoC : '' ) { depends ( outOfOrderListDoit ) }
target ( outOfOrderListDoB : '' ) { depends ( outOfOrderListDoit ) }
target ( outOfOrderListDoA : '' ) { depends ( outOfOrderListDoit ) }
target ( outOfOrderListDoit : '' ) { println ( 'done.' ) }
'''
    assertEquals ( 0 , processTargets ( 'outOfOrderListDoAll' ) )
    assertEquals ( 'done.\n' , output )
  }
  void testSameTargetAndFileName ( ) {
    //  Having a target of the same name as the script being compiled is fine until the target name is used in
    //  a depend.  At this point the class name not the name in the binding is picked up and all hell breaks
    //  loose.  Standard input is compiled as class standard_input.
    script = '''
target ( standard_input , '' ) { println ( 'done.' ) }
target ( startingPoint , '' ) { depends ( standard_input ) }
'''
    assertEquals ( -2 , processTargets ( 'startingPoint' ) )
    assertTrue ( output.startsWith ( 'Standard input, line 2 -- Error evaluating Gantfile: No signature of method: ' ) )
  }
  void testStringParameter ( ) {
    script = '''
target ( anotherTarget : '' ) { println ( 'done.' ) }
target ( stringParameter : '' ) { depends ( 'anotherTarget' ) }
'''
    assertEquals ( 0 , processTargets ( 'stringParameter' ) )
    assertEquals ( 'done.\n' , output )
  }
  void testStringListParameter ( ) {
    script = '''
target ( aTarget : '' ) { println ( 'done.' ) }
target ( anotherTarget : '' ) { println ( 'done.' ) }
target ( stringListParameter : '' ) { depends ( [ 'aTarget' , 'anotherTarget' ] ) }
'''
    assertEquals ( 0 , processTargets ( 'stringListParameter' ) )
    assertEquals ( 'done.\ndone.\n' , output )
  }
  void testMixedListParameter ( ) {
    script = '''
target ( aTarget : '' ) { println ( 'done.' ) }
target ( anotherTarget : '' ) { println ( 'done.' ) }
target ( mixedListParameter : '' ) { depends ( [ aTarget , 'anotherTarget' ] ) }
'''
    assertEquals ( 0 , processTargets ( 'mixedListParameter' ) )
    assertEquals ( 'done.\ndone.\n' , output )
  }
  void testCircularDependency ( ) {
    //  Should this actually fail? cf. GANT-9.  Current view is that it is fine as is.
    script = '''
target ( A : '' ) { depends ( B ) ; println ( 'A' ) }
target ( B : '' ) { depends ( C )  ; println ( 'B' ) }
target ( C : '' ) { depends ( A )  ; println ( 'C' ) }
'''
    assertEquals ( 0 , processTargets ( 'A' ) )    
    assertEquals ( '''A
C
B
A
''' , output )    
  }
  void testMultipleIndependentTargets ( ) {
    script = '''
target ( one : 'One Target' ) { println 'Running one...' }
target ( two : 'Two Target' ) { println 'Running two...' }
'''
    assertEquals ( 0 , processTargets ( [ 'one' , 'two' ] ) )
    assertEquals ( '''Running one...
Running two...
''' , output )
  }
  void testEmbeddedDepend ( ) {
    script = '''
target ( targetA : '' ) { println ( 'done.' ) }
target ( targetB : '' ) { (0..3).each { depends ( targetA ) } }
'''
    assertEquals ( 0 , processTargets ( 'targetB' ) )
    assertEquals ( 'done.\n' , output )
  }
  //  cf. GANT-26
  void testMultipleDependentTargets ( ) {
    script = '''
target ( one : 'One Target' ) {
  depends ( two )
  println 'Running one...'
}
target ( two : 'Two Target' ) { println 'Running two...' }
'''
    assertEquals ( 0 , processTargets ( [ 'one' , 'two' ] ) )
    assertEquals ( '''Running two...
Running one...
Running two...
''' , output )
  }
}
