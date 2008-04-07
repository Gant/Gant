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

package gant.tools

import org.apache.tools.ant.ProjectHelper

/**
 *  A class providing methods for including Ant XML files and setting up the targets as Gant targets.
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
final class AntFile {
  private final Binding binding ;
  AntFile ( final Binding binding ) { this.binding = binding }
  AntFile ( final Binding binding , Map map ) {
    this.binding = binding
    includeTargets ( map.filename )
  }
  void includeTargets ( String fileName ) {
    ProjectHelper.configureProject ( binding.ant.project , new File ( fileName ) )
    binding.ant.project.targets.each { key , value ->
      assert key == value.name
      binding.setProperty ( key , { value.execute ( ) } )
      if ( value.description ) { binding.targetDescriptions.put ( key , value.description ) }
    }
  }
}
