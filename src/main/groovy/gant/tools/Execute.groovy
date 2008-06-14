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

import org.codehaus.gant.GantBinding

/**
 *  Provides methods for executing operating system commands ensuring that the pipes are flushed and
 *  so the execution cannot block on full pipes.
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
final class Execute {
  private final GantBinding binding ;
  /**
   *  Constructor for the "includeTool <<" usage.
   *
   *  @param binding The <code>GantBinding</code> to bind to.
   */
  Execute ( final GantBinding binding ) { this.binding = binding ; }
   /**
   *  Constructor for the "includeTool **" usage.
   *
   *  @param binding The <code>GantBinding</code> to bind to.
   *  @param map The <code>Map</code> of initialization parameters.  Currently ignored.
    */
  Execute ( final GantBinding binding , final Map map ) { this.binding = binding ; }
 /**
   *  Handle the output and error streams from the already initializaed and started process to ensure the
   *  buffers are never filled, and block waiting termination of the process.
   *
   *  @param process the executing process.
   *  @param errProcessing the <code>Closure</code> to process the error stream.
   *  @param outProcessing the <code>Closure</code> to process the output stream.
   *  @param command the command used to start the executing process.
   *  @param tag the tag to print.
   *  @return the return code of the process.
   */
  private manageProcess ( final Process process , final Closure errProcessing , final Closure outProcessing , final Object command , final String tag ) {
    //  Command can either be a String or a List.
    binding.getVariable ( 'message' ) ( tag , command )
    def errThread = Thread.start { ( new InputStreamReader ( process.err ) ).eachLine ( errProcessing ) }
    def inThread = Thread.start { ( new InputStreamReader ( process.in ) ).eachLine ( outProcessing ) }
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
   *  @param command the command as a single <code>String</code>.
   *  @return the return code of the process.
   */
  def executable ( final Map keywordParameters = [:] , final String command ) {
    manageProcess ( command.execute ( ) ,
                    keywordParameters['errProcessing'] ?: { System.err.println ( it ) },
                    keywordParameters['outProcessing'] ?: { println ( it ) } ,
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
   *  @param command the command as a  list of <code>String</code>s.
   *  @return the return code of the process.
   */
  def executable ( final Map keywordParameters = [:] , final List command ) {
    manageProcess ( command.execute ( ) ,
                    keywordParameters['errProcessing'] ?: { System.err.println ( it ) },
                    keywordParameters['outProcessing'] ?: { println ( it ) } ,
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
   *  @param command the command as a single <code>String</code>.
   *  @return the return code of the process.
   */
  def shell ( final Map keywordParameters = [:] , final String command ) {
    final String osName = System.getProperty ( "os.name" )
    final boolean isWindows = ( osName.length ( ) > 6 ) ? osName.substring ( 0 , 7 ).equals ( "Windows" ) : false
    final commandArray = isWindows ? [ 'cmd' , '/c' , command ] : [ 'sh' , '-c' , command ]
    manageProcess ( commandArray.execute ( ) ,
                    keywordParameters['errProcessing'] ?: { System.err.println ( it ) },
                    keywordParameters['outProcessing'] ?: { println ( it ) } ,
                    command ,
                    'shell' )
  }
}
