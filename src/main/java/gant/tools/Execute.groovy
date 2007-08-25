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

package gant.tools

/**
 *  A class providing methods for executing operating system commands.
 *
 *  @author Russel Winder <russel@russel.org.uk>
 *  @version $Revision: 4214 $ $Date: 2006-11-13 08:59:52 +0000 (Mon, 13 Nov 2006) $
 */
final class Execute {
  private final Binding binding ;
  Execute ( final Binding binding ) { this.binding = binding ; }
  /**
   *  Execute a command from the PATH.
   */
  void executable ( final String command , final Closure closure = { println it } ) {
    binding.getVariable ( 'message' ) ( 'execute' , command )
    def process = command.execute ( )
    process.in.eachLine ( closure )
    process.waitFor ( )
  }
  /**
   *  Execute a command from the PATH.
   */
  void executable ( final List command , final Closure closure = { println it } ) {
    binding.getVariable ( 'message' ) ( 'execute' , command )
    def process = command.execute ( )
    process.in.eachLine ( closure )
    process.waitFor ( )
  }
  /**
   *  Execute a UNIX-style shell.
   */
  void shell ( final String command , final Closure closure = { println it } ) {
    binding.getVariable ( 'message' ) ( 'shell' , command )
    def process = [ 'sh' , '-c' , command ].execute ( )
    process.in.eachLine ( closure )
    process.waitFor ( )
  }
}
