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

/**
 *  A test to ensure that Gant objects are garbage collected appropriately.
 *
 *  <p>Original idea for the test from GANT-33, due to Peter Ledbrook.</p>
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
final class GANT_33_Test extends GantTestCase {
  private final buildScript =  '''
function = { -> }
target ( main : 'simpleTest' ) {
  println ( 'Main target executing...' )
  function ( )
}
'''
  private final scriptTemplate = '''
import gant.Gant
import java.lang.ref.PhantomReference
import java.lang.ref.ReferenceQueue
def refQueue = new ReferenceQueue ( )
def phantomRefs = new HashSet ( )
output = [ ] // Must be in the binding.
Thread.startDaemon {
  while ( true ) {
    def obj = refQueue.remove ( )
    if ( obj != null ) {
      output << obj.toString ( )
      phantomRefs.remove ( obj )
    }
  }
}
def buildScript = '__BUILDSCRIPT_PATH__'
def target = 'main'
def gant = __CREATE_GANT__
def refA = new PhantomReference ( gant , refQueue )
phantomRefs << refA
output << refA.toString ( )
__PROCESS_TARGET__
System.gc ( )
gant = __CREATE_GANT__
def refB = new PhantomReference ( gant , refQueue )
phantomRefs << refB
output << refB.toString ( )
__PROCESS_TARGET__
System.gc ( )
Thread.sleep ( 500 ) //  Give time for the reference queue monitor to report in.
'''
  private File buildScriptFile
  void setUp ( ) {
    super.setUp ( ) 
    buildScriptFile = File.createTempFile ( 'gant_' , '_GarbageCollect_Test' )
    buildScriptFile.write ( buildScript )
  }
  void tearDown ( ) {
    buildScriptFile.delete ( )
  }
  //////////////////////////////////////////////////////////////////////////////////////////////
  //  On Windows the string returned by createTempFile must have \ reprocessed before being used for other
  //  purposes.
  //////////////////////////////////////////////////////////////////////////////////////////////
  void testCorrectCollection ( ) {
    //  Creates two Gant instances, one of which should be garbage collected, so the result of execution is
    //  a list of 3 items, the addresses of the two created objects and the address of the collected object
    //  -- which should be the same as the address of the first created object.
    def binding = new Binding ( output : '' )
    def groovyShell = new GroovyShell ( binding )
    groovyShell.evaluate (
                          scriptTemplate
                          .replace ( '__BUILDSCRIPT_PATH__' , ( isWindows ? buildScriptFile.path.replace ( '\\' , '\\\\' ) : buildScriptFile.path ) )
                          .replace ( '__CREATE_GANT__' , 'new Gant ( buildScript )' )
                          .replace ( '__PROCESS_TARGET__' , 'gant.processTargets ( target )' )
                          )
    assertEquals ( 3 , binding.output.size ( ) )
    assertEquals ( binding.output[0] , binding.output[2] )
  }
  void testNoCollection ( ) {
    //  Creates two Gant instances neither of which are garbage collected.
    def binding = new Binding ( output : '' )
    def groovyShell = new GroovyShell ( binding )
    groovyShell.evaluate (
                          scriptTemplate
                          .replace ( '__BUILDSCRIPT_PATH__' , ( isWindows ? buildScriptFile.path.replace ( '\\' , '\\\\' ) : buildScriptFile.path ) )
                          .replace ( '__CREATE_GANT__' , 'new Gant ( )' )
                          .replace ( '__PROCESS_TARGET__' , 'gant.processArgs ( [ "-f" , new File ( buildScript ).absolutePath , "-c" , target ] as String[] )' )
                          )
    assertEquals ( 2 , binding.output.size ( ) )
  }
}
