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

/**
 *  A Gant test case: Adds the required input stream manipulation features to avoid replication of code.
 *  Also prepare a new instance of Gant for each test.
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
public abstract class GantTestCase extends GroovyTestCase {
  private ByteArrayOutputStream output ;
  private PrintStream savedOut ;
  protected Gant gant ;
  protected String script ;
  protected void setUp ( ) {
    savedOut = System.out ;
    output = new ByteArrayOutputStream ( ) ;
    System.setOut ( new PrintStream ( output ) ) ;
    gant = new Gant ( "-" ) ;
    script = "" ;
  }
  protected void tearDown ( ) { System.setOut ( savedOut ) ; }
  protected void setScript ( final String script ) { System.setIn ( new ByteArrayInputStream ( script.getBytes ( ) ) ) ; }
  protected int processTargets ( ) { return gant.processTargets ( ) ; }
  protected int processTargets ( final String s ) { return gant.processTargets ( s ) ; }
  protected int processTargets ( final List<String> l ) { return gant.processTargets ( l ) ; }  
  protected String getOutput ( ) { return output.toString ( ).replace ( "\r" , "" ) ; }
}
