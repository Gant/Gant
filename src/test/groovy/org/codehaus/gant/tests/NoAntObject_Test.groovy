//  Gant -- A Groovy build framework based on scripting Ant tasks.
//
//  Copyright © 2007-8 Russel Winder
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
 *  A test to ensure that calling an Ant task without the Ant object works as required.
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
final class NoAntObject_Test extends GantTestCase {
  void testEchoAttribute ( ) {
    script = 'target ( test : "" ) { echo ( message : "Hello." ) } '
    assertEquals ( 0 , processTargets ( 'test' ) )
    assertEquals ( '''     [echo] Hello.
''' , output ) 
  }
  void testEchoText ( ) {
    script = 'target ( test : "" ) { echo { "Hello." } } '
    assertEquals ( 0 , processTargets ( 'test' ) )
    assertEquals ( '' , output ) 
  }
  void testEchoMixed ( ) {
    script = 'target ( test : "" ) { echo ( message : "Hello" ) { " World." } } '
    assertEquals ( 0 , processTargets ( 'test' ) )
    assertEquals ( '''     [echo] Hello
''' , output ) 
  }
  //  cf. GANT-10
  void testWithAntReferenceScriptLevel ( ) {
    script = '''
ant.echo ( message : "Hello" )
target ( test : '' ) { ant.echo ( message : "World" ) }
'''
    assertEquals ( 0 , processTargets ( 'test' ) )
    assertEquals ( '''     [echo] Hello
     [echo] World
''' , output ) 
  }
  /*
  void testWithoutAntReferenceScriptLevel ( ) {
    script = '''
echo ( message : "Hello" )
target ( test : '' ) { echo ( message : "World" ) }
'''
    //assertEquals ( 0 , processTargets ( 'test' ) )
    System.err.println ( processTargets ( 'test' ) )
    assertEquals ( '''     [echo] Hello
     [echo] World
''' , output ) 
  }
  */
  void testWithAntReferenceInClosure ( ) {
    script = '''
target ( test : '' ) {
  ( 0..3 ).each { ant.echo ( message : "Hello World!" ) }
}
'''
    assertEquals ( 0 , processTargets ( 'test' ) )
    assertEquals ( '''     [echo] Hello World!
     [echo] Hello World!
     [echo] Hello World!
     [echo] Hello World!
''' , output ) 
  }
  void testWithoutAntReferenceInClosure ( ) {
    script = '''target ( test : '' ) { ( 0..3 ).each { echo ( message : "Hello World!" ) } }'''
    assertEquals ( 0 , processTargets ( 'test' ) )
    assertEquals ( '''     [echo] Hello World!
     [echo] Hello World!
     [echo] Hello World!
     [echo] Hello World!
''' , output ) 
  }
  void testWithoutAntReferenceInMapClosure ( ) {
    script = '''target ( test : '' ) { [ a : 'A' , b : 'B' ].each { key , value -> echo ( message : "${key}:${value}" ) } }'''
    assertEquals ( 0 , processTargets ( 'test' ) )
    assertEquals ( '''     [echo] a:A
     [echo] b:B
''' , output ) 
  }
  void testClosureWithAnt ( ) {
    script = '''
closure = { ant.echo ( message : "Hello World!" ) }
target ( test : '' ) { ( 0..3 ).each closure }
'''
    assertEquals ( 0 , processTargets ( 'test' ) )
    assertEquals ( '''     [echo] Hello World!
     [echo] Hello World!
     [echo] Hello World!
     [echo] Hello World!
''' , output ) 
  }
  void testClosureWithoutAntWithExplictMetaClassSetting ( ) {
    script = '''
closure = { echo ( message : "Hello World!" ) }
closure.metaClass = new org.codehaus.gant.GantMetaClass ( closure.metaClass , binding )
target ( test : '' ) { ( 0..3 ).each closure }
'''
    assertEquals ( 0 , processTargets ( 'test' ) )
    assertEquals ( '''     [echo] Hello World!
     [echo] Hello World!
     [echo] Hello World!
     [echo] Hello World!
''' , output ) 
  }
  /*
  void testClosureWithoutAnt ( ) {
    script = '''
closure = { echo ( message : "Hello World!" ) }
target ( test : '' ) { ( 0..3 ).each closure }
'''
    //assertEquals ( 0 , processTargets ( 'test' ) )
    System.err.println ( processTargets ( 'test' ) )
    assertEquals ( '''     [echo] Hello World!
     [echo] Hello World!
     [echo] Hello World!
     [echo] Hello World!
''' , output ) 
  }
  */
}
