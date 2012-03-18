//  Gant -- A Groovy way of scripting Ant tasks.
//
//  Copyright © 2007-10 Russel Winder
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
 *  @author Russel Winder <russel@winder.org.uk>
 */
final class NoAntObject_Test extends GantTestCase {
  private final targetName = 'targetName'
  private final message = 'Hello'
  private final followUp = ' World'
  private final replicationCount = 4
  private String resultMessage = resultString ( targetName , "     [echo] ${message}\n" )
  private String resultReplicated = resultString ( targetName , "     [echo] ${message}\n" * replicationCount )
  void testEchoAttribute ( ) {
    script = "target ( ${targetName} : '' ) { echo ( message : '${message}' ) }"
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultMessage , output ) 
  }
  void testEchoText ( ) {
    script = "target ( ${targetName} : '' ) { echo { '${message}' } }"
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , '' ) , output ) 
  }
  void testEchoMixed ( ) {
    script = "target ( ${targetName} : '' ) { echo ( message : '${message}' ) { ' ${followUp}' } }"
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultMessage , output ) 
  }
  //  cf. GANT-10
  void testWithAntReferenceScriptLevel ( ) {
    script = """
ant.echo ( message : '${message}' )
target ( ${targetName} : '' ) { ant.echo ( message : '${followUp}' ) }
"""
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( "     [echo] ${message}\n" + resultString ( targetName , "     [echo] ${followUp}\n" ) , output ) 
  }
  void testWithoutAntReferenceScriptLevel ( ) {
    script = """
ant.echo ( message : '${message}' )
target ( ${targetName} : '' ) { echo ( message : '${followUp}' ) }
"""
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals (  "     [echo] ${message}\n" + resultString ( targetName , "     [echo] ${followUp}\n" ) , output ) 
  }
  void testWithAntReferenceInClosure ( ) {
    script = "target ( ${targetName} : '' ) { ( 0..<${replicationCount} ).each { ant.echo ( message : '${message}' ) } }"
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultReplicated , output ) 
  }
  void testWithoutAntReferenceInClosure ( ) {
    script = "target ( ${targetName} : '' ) { ( 0..<${replicationCount} ).each { echo ( message : '${message}' ) } }"
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultReplicated , output )
  }
  void testWithoutAntReferenceInMapClosure ( ) {
    script = "target ( ${targetName} : '' ) { [ a : 'A' , b : 'B' ].each { key , value -> echo ( message : \"\${key}:\${value}\" ) } }"
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , '''     [echo] a:A
     [echo] b:B
''' ) , output ) 
  }
  void testClosureWithAnt ( ) {
    script = """
closure = { ant.echo ( message : '${message}' ) }
target ( ${targetName} : '' ) { ( 0..<${replicationCount} ).each closure }
"""
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultReplicated , output )
  }
  void testClosureWithoutAntWithExplictMetaClassSetting ( ) {
    script = """
closure = { echo ( message : '${message}' ) }
closure.metaClass = new org.codehaus.gant.GantMetaClass ( closure.metaClass , binding )
target ( ${targetName} : '' ) { ( 0..<${replicationCount} ).each closure }
"""
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultReplicated , output )
  }
  void testClosureWithoutAnt ( ) {
    script = """
closure = { echo ( message : '${message}' ) }
target ( ${targetName} : '' ) { ( 0..<${replicationCount} ).each closure }
"""
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultReplicated , output )
  }
}
