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

package gant.tools

/**
 *  A class providing methods for executing operating system commands.
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
final class Execute {
  private final Binding binding ;
  Execute ( final Binding binding ) { this.binding = binding ; }
  //
  //  Don't use the Elvis operator since that would mean the code would not compile in Groovy 1.0.
  //
  private void manageProcess ( final Process process , final Closure errProcessing , final Closure outProcessing , final Object command , final String tag ) {
    //  Command can either be a String or a List.
    binding.getVariable ( 'message' ) ( tag , command )
    def errThread = Thread.start { process.err.eachLine ( errProcessing ) }
    def inThread = Thread.start { process.in.eachLine ( outProcessing ) }
    errThread.join ( )
    inThread.join ( )
    process.waitFor ( )
  }
  /**
   *  Execute a command from the PATH.
   *
   *  Optional, keyword parameters: <code>outProcessing</code> is a <code>Closure</code> used to process
   *  lines from standard out; <code>errProcessing is a <code>Closure</code> used to process lines from
   *  standard error.
   *
   *  @parameter command the command as a single <code>String</code>.
   */
  void executable ( final Map keywordParameters = [:] , final String command ) {
    manageProcess ( command.execute ( ) ,
                    keywordParameters['errProcessing'] ? keywordParameters['errProcessing'] : { System.err.println ( it ) },
                    keywordParameters['outProcessing'] ? keywordParameters['outProcessing'] : { println ( it ) } ,
                    command ,
                    'execute' )
  }
  /**
   *  Execute a command from the PATH.
   *
   *  Optional, keyword parameters: <code>outProcessing</code> is a <code>Closure</code> used to process
   *  lines from standard out; <code>errProcessing</code> is a <code>Closure</code> used to process lines
   *  from standard error.
   *
   *  @parameter command the command as a  list of <code>String</code>s.
   */
  void executable ( final Map keywordParameters = [:] , final List command ) {
    manageProcess ( command.execute ( ) ,
                    keywordParameters['errProcessing'] ? keywordParameters['errProcessing'] : { System.err.println ( it ) },
                    keywordParameters['outProcessing'] ? keywordParameters['outProcessing'] : { println ( it ) } ,
                    command ,
                    'execute' )
  }
  /**
   *  Execute a command using a shell.
   *
   *  Optional, keyword parameters: <code>outProcessing</code> is a <code>Closure</code> used to process
   *  lines from standard out; <code>errProcessing</code> is a <code>Closure</code> used to process lines
   *  from standard error.
   *
   *  @parameter command the command as a single <code>String</code>.
   */
  void shell ( final Map keywordParameters = [:] , final String command ) {
    manageProcess ( [ 'sh' , '-c' , command ].execute ( ) ,
                    keywordParameters['errProcessing'] ? keywordParameters['errProcessing'] : { System.err.println ( it ) },
                    keywordParameters['outProcessing'] ? keywordParameters['outProcessing'] : { println ( it ) } ,
                    command ,
                    'shell' )
  }
}
