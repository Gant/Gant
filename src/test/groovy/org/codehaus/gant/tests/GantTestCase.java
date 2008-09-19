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

package org.codehaus.gant.tests ;

import java.util.List ;

import java.io.ByteArrayInputStream ;
import java.io.ByteArrayOutputStream ;
import java.io.PrintStream ;

import groovy.util.GroovyTestCase ;

import gant.Gant ;

import org.codehaus.gant.GantState ;

import org.codehaus.groovy.runtime.InvokerHelper ;

/**
 *  A Gant test case: Adds the required input stream manipulation features to avoid replication of code.
 *  Also prepare a new instance of Gant for each test.
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
public abstract class GantTestCase extends GroovyTestCase {
  public static final int groovyMajorVersion ;
  public static final int groovyMinorVersion ;
  static {
    final String[] version =  InvokerHelper.getVersion ( ).split ( "[.-]" , 3 ) ;
    groovyMajorVersion = Integer.parseInt ( version[0] ) ;
    groovyMinorVersion = Integer.parseInt ( version[1] ) ;
  }
  public static final boolean isWindows ;
  static {
    final String osName = System.getProperty ( "os.name" ) ;
    isWindows = ( osName.length ( ) > 6 ) && osName.substring ( 0 , 7 ).equals ( "Windows" ) ;
  }
  private ByteArrayOutputStream output ;
  private PrintStream savedOut ;
  protected Gant gant ;
  protected String script ;
  @Override protected void setUp ( ) throws Exception {
    super.setUp ( ) ;
    savedOut = System.out ;
    output = new ByteArrayOutputStream ( ) ;
    System.setOut ( new PrintStream ( output ) ) ;
    gant = new Gant ( "-" ) ;
    script = "" ;
    //
    //  If the JUnit is run with fork mode 'perTest' then we do not have to worry about the static state.
    //  However, when the fork mode is 'perBatch' or 'once' then we have to ensure that the static state
    //  is reset to the normal state.
    //
    GantState.verbosity = GantState.NORMAL ;
    GantState.dryRun = false ;
  }
  @Override protected void tearDown ( ) throws Exception {
    System.setOut ( savedOut ) ;
    super.tearDown ( ) ;
  }
  protected void setScript ( final String script ) { System.setIn ( new ByteArrayInputStream ( script.getBytes ( ) ) ) ; }
  protected Integer processTargets ( ) { return (Integer) gant.processTargets ( ) ; } // IntelliJ IDEA thinks processTargets returns an Object.
  protected Integer processTargets ( final String s ) { return (Integer) gant.processTargets ( s ) ; } // IntelliJ IDEA thinks processTargets returns an Object.
  protected Integer processTargets ( final List<String> l ) { return (Integer) gant.processTargets ( l ) ; } // IntelliJ IDEA thinks processTargets returns an Object.
  protected String getOutput ( ) { return output.toString ( ).replace ( "\r" , "" ) ; }
}
