//  Gant -- A Groovy build tool based on scripting Ant tasks
//
//  Copyright © 2006-7 Russel Winder <russel@russel.org.uk>
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

package org.codehaus.groovy.gant.tests ;

import java.io.ByteArrayOutputStream ;
import java.io.PrintStream ;
import groovy.util.GroovyTestCase ;

import gant.Gant ;

/**
 *  A Gant test case: Adds the required input stream manipulation features to avoid replication of code.
 *  Also prepare a new instance of Gant for each test.
 *
 *  @author Russel Winder
 *  @version $Revision$ $Date$
 */
public class GantTestCase extends GroovyTestCase {
  protected ByteArrayOutputStream output ;
  protected Gant gant ;
  protected void setUp ( ) {
    output = new ByteArrayOutputStream ( ) ;
    System.setOut ( new PrintStream ( output ) ) ;
    gant = new Gant ( ) ;
  }
}
