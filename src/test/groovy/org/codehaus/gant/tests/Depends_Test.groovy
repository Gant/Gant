//  Gant -- A Groovy build framework based on scripting Ant tasks.
//
//  Copyright © 2006-7 Russel Winder
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
    System.setIn ( new StringBufferInputStream ( '''
target ( noneDoit : '' ) { println ( 'done.' ) }
target ( noneDoA : '' ) { noneDoit ( ) }
target ( noneDoB : '' ) { noneDoit ( ) }
target ( noneDoC : '' ) { noneDoit ( ) }
target ( noneDoAll : '' ) { noneDoA ( ) ; noneDoB ( ) ; noneDoC ( ) }
''' ) )
    assertEquals ( 0 , gant.process ( [ '-f' , '-' , 'noneDoAll' ] as String[] ) )
    assertEquals ( '''done.
done.
done.
''' , output ) 
  }
  void testMixed ( ) {
    System.setIn ( new StringBufferInputStream ( '''
target ( mixedDoit : '' ) { println ( 'done.' ) }
target ( mixedDoA : '' ) { depends ( mixedDoit ) }
target ( mixedDoB : '' ) { mixedDoit ( ) }
target ( mixedDoC : '' ) { depends ( mixedDoit ) }
target ( mixedDoAll : '' ) { mixedDoA ( ) ; mixedDoB ( ) ; mixedDoC ( ) }
''' ) )
    assertEquals ( 0 , gant.process ( [ '-f' , '-' , 'mixedDoAll' ] as String[] ) )
    assertEquals ( '''done.
done.
''' , output ) 
  }
  void testAll ( ) {
    System.setIn ( new StringBufferInputStream ( '''
target ( allDoit : '' ) { println ( 'done.' ) }
target ( allDoA : '' ) { depends ( allDoit ) }
target ( allDoB : '' ) { depends ( allDoit ) }
target ( allDoC : '' ) { depends ( allDoit ) }
target ( allDoAll : '' ) { allDoA ( ) ; allDoB ( ) ; allDoC ( ) }
''' ) )
    assertEquals ( 0 , gant.process ( [ '-f' , '-' , 'allDoAll' ] as String[] ) )
    assertEquals ( 'done.\n' , output ) 
  }
  void testMultiple ( ) {
    System.setIn ( new StringBufferInputStream ( '''
target ( multipleDoit : '' ) { println ( 'done.' ) }
target ( multipleDoA : '' ) { depends ( multipleDoit ) }
target ( multipleDoB : '' ) { depends ( multipleDoit ) }
target ( multipleDoC : '' ) { depends ( multipleDoit ) }
target ( multipleDoAll : '' ) { depends ( multipleDoA , multipleDoB , multipleDoC ) }
''' ) )
    assertEquals ( 0 , gant.process ( [ '-f' , '-' , 'multipleDoAll' ] as String[] ) )
    assertEquals ( 'done.\n' , output ) 
  }
  void testList ( ) {
    System.setIn ( new StringBufferInputStream ( '''
target ( listDoit : '' ) { println ( 'done.' ) }
target ( listDoA : '' ) { depends ( listDoit ) }
target ( listDoB : '' ) { depends ( listDoit ) }
target ( listDoC : '' ) { depends ( listDoit ) }
target ( listDoAll : '' ) { depends ( [ listDoA , listDoB , listDoC ] ) }
''' ) )
    assertEquals ( 0 , gant.process ( [ '-f' , '-' , 'listDoAll' ] as String[] ) )
    assertEquals ( 'done.\n' , output ) 
  }
  void testNotClosure ( ) {
    System.setIn ( new StringBufferInputStream ( '''
datum = 1
target ( notClosure : '' ) { depends ( datum ) }
''' ) )
    assertEquals ( 13 , gant.process ( [ '-f' , '-' , 'notClosure' ] as String[] ) )
    assertEquals ( 'depends called with an argument (1) that is not appropriate.\n' , output )
  }
  void testNotListClosure ( ) {
    System.setIn ( new StringBufferInputStream ( '''
datum = 1
target ( notListClosure : '' ) { depends ( [ datum ] ) }
''' ) )
    assertEquals ( 13 , gant.process ( [ '-f' , '-' , 'notListClosure' ] as String[] ) )
    assertEquals ( 'depends called with List argument that contains an item (1) that is not appropriate.\n' , output )
  }
  void testOutOfOrder ( ) {
    System.setIn ( new StringBufferInputStream ( '''
target ( outOfOrderDoAll : '' ) { depends ( outOfOrderDoA , outOfOrderDoB , outOfOrderDoC ) }
target ( outOfOrderDoC : '' ) { depends ( outOfOrderDoit ) }
target ( outOfOrderDoB : '' ) { depends ( outOfOrderDoit ) }
target ( outOfOrderDoA : '' ) { depends ( outOfOrderDoit ) }
target ( outOfOrderDoit : '' ) { println ( 'done.' ) }
''' ) )
    assertEquals ( 0 , gant.process ( [ '-f' , '-' , 'outOfOrderDoAll' ] as String[] ) )
    assertEquals ( 'done.\n' , output )
  }
  void testOutOfOrderList ( ) {
    System.setIn ( new StringBufferInputStream ( '''
target ( outOfOrderListDoAll : '' ) { depends ( [ outOfOrderListDoA , outOfOrderListDoB , outOfOrderListDoC ] ) }
target ( outOfOrderListDoC : '' ) { depends ( outOfOrderListDoit ) }
target ( outOfOrderListDoB : '' ) { depends ( outOfOrderListDoit ) }
target ( outOfOrderListDoA : '' ) { depends ( outOfOrderListDoit ) }
target ( outOfOrderListDoit : '' ) { println ( 'done.' ) }
''' ) )
    assertEquals ( 0 , gant.process ( [ '-f' , '-' , 'outOfOrderListDoAll' ] as String[] ) )
    assertEquals ( 'done.\n' , output )
  }
  void testSameTargetAndFileName ( ) {
    //  Having a target of the same name as the script being compiled is fine until the target name is used in
    //  a depend.  At this point the class name not the name in the binding is picked up and all hell breaks
    //  loose.  Standard input is compiled as class standard_input.
    System.setIn ( new StringBufferInputStream ( '''
target ( standard_input , '' ) { System.err.println ( 'Standard Input' ) ; println ( 'done.' ) }
target ( startingPoint , '' ) { System.err.println ( 'StartingPoint' ) ; depends ( standard_input ) }
''' ) )
    assertEquals ( 2 , gant.process ( [ '-f' , '-' , 'startingPoint' ] as String[] ) )
    assertTrue ( output.startsWith ( 'Standard input, line 2 -- Error evaluating Gantfile: groovy.lang.MissingMethodException: No signature of method: gant.Gant$_closure1.doCall() is applicable for argument types:' ) )
  }
  void testStringParameter ( ) {
    System.setIn ( new StringBufferInputStream ( '''
target ( anotherTarget : '' ) { println ( 'done.' ) }
target ( stringParameter : '' ) { depends ( 'anotherTarget' ) }
''' ) )
    assertEquals ( 0 , gant.process ( [ '-f' , '-' , 'stringParameter' ] as String[] ) )
    assertEquals ( 'done.\n' , output )
  }
}
