//  Gant -- A Groovy build tool based on scripting Ant tasks
//
//  Copyright © 2006 Russel Winder <russel@russel.org.uk>
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

package org.codehaus.gant ;

/**
 *  A class to hold the global shared state for a run of Gant.  This is needed because parts of Gant are
 *  written in Java and parts in Groovy and it is not possible to compile them all at the same time.  All
 *  references to Groovy classes must be avoided in the Java classes so that the Java can be compiled and
 *  then the Groovy compiled.  This class contains things that should be in the <code>Gant</code> class but
 *  cannot be.
 *
 *  @author Russel Winder <russel@russel.org.uk>
 *  @version $Revision$ $Date$
 */
class GantState {
  public final static int SILENT = 0 , QUIET = 1 , NORMAL = 2 , VERBOSE = 3 ;
  static int verbosity = NORMAL ;
  static boolean dryRun = false ;
}
