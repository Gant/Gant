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

package gant.targets

/**
 *  A class to provide clean and clobber actions for Gant build scripts.  Maintains separate lists of
 *  Ant pattern specifications and directory names for clean and for clobber.  The lists are used as the
 *  specifications when the clean or clobber methods are called.
 *
 *  @author Russel Winder <russel@russel.org.uk>
 *  @version $Revision$ $Date$
 */
final class Clean {
  private Binding binding
   private performPatternAction ( List l ) {
    if ( l.size ( ) > 0 ) {
      binding.Ant.delete ( quiet : 'false' ) {
        fileset ( dir : '.' , includes : l.flatten ( ).join ( ',' ) , defaultexcludes : 'false' )
      }
    }
  }
  private performDirectoryAction ( List l ) {
    l.flatten ( ).each { item -> binding.Ant.delete ( dir : item , quiet : 'false' ) }
  }
  Clean ( Binding binding ) {
    this.binding = binding
    binding.cleanPattern = [ ]
    binding.cleanDirectory = [ ]
    binding.task.call ( clean : 'Action the cleaning.' ) {
      performPatternAction ( binding.cleanPattern )
      performDirectoryAction ( binding.cleanDirectory )
    }
    binding.clobberPattern = [ ]
    binding.clobberDirectory = [ ]
    binding.task.call ( clobber : 'Action the clobbering.  Do the cleaning first.' ) {
      depends ( binding.clean )
      performPatternAction ( binding.clobberPattern )
      performDirectoryAction ( binding.clobberDirectory )
    }
  }
}
