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

package org.codehaus.gant

/**
 *  An instance of this class is provided to each Gant script for including targets.  Targets can be
 *  provided by Gant (sub)scripts or Groovy or Java classes.
 *
 *  @author Russel Winder <russel.winder@concertant.com> 
 *  @author Graeme Rocher <graeme.rocher@gmail.com>        
 */
class IncludeTargets extends AbstractInclude {
  IncludeTargets ( Binding binding ) { super ( binding ) }
  def leftShift ( File file ) { 
    def className = file.name
    if ( ! ( className in loadedClasses ) ) {
      if ( binding.cacheEnabled ) {
        className = className.replaceAll ( /\./ , '_' )
        binding.loadClassFromCache.call ( className , file.lastModified ( ) , file )
      }   
      else { readFile ( file ) }
      loadedClasses << className
    }
    this 		
  }
  def leftShift ( String s ) { binding.groovyShell.evaluate ( s ) ; this }
}
