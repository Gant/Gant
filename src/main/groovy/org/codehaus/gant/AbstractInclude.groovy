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

package org.codehaus.gant

/**
 *  This class is for code sharing between classes doing include activity.
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
abstract class AbstractInclude {
  protected binding
  protected AbstractInclude ( binding ) { this.binding = binding }
  protected createInstance ( Class theClass ) {
    try { return theClass.getConstructor ( Binding ).newInstance ( [ binding ] as Object[] ) }
    catch ( NoSuchMethodException nsme ) { throw new RuntimeException ( 'Could not initialize ' + theClass.name , nsme ) }
  }
  protected createInstance ( Class theClass , Map keywordParameters ) {
    try { return theClass.getConstructor ( Binding , Map ).newInstance ( [ binding , keywordParameters ] as Object[] ) }
    catch ( NoSuchMethodException nsme ) { throw new RuntimeException ( 'Could not initialize ' + theClass.name , nsme ) }
  }
  private Class attemptRead ( File file , boolean asClass ) {
    if ( asClass ) { return binding.groovyShell.evaluate ( file.text + " ; return ${file.name.replace('.groovy', '' )}" ) }
    binding.groovyShell.evaluate ( file )
    null
  }
  protected Class readFile ( File file , boolean asClass = false ) {
    try { return attemptRead ( file , asClass ) }
    catch ( FileNotFoundException fnfe ) {
      for ( directory in binding.gantLib ) {
        def possible = new File ( directory , file.name )
        if ( possible.isFile ( ) && possible.canRead ( ) ) { return attemptRead ( possible , asClass ) }
      }
      throw fnfe
    }
  }
}
