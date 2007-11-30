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

package org.codehaus.gant ;

import java.util.Iterator ;
import java.util.Map ;

import groovy.lang.Closure ;
import groovy.util.AntBuilder ;

/**
 *  This class is a sub-class of <code>AntBuilder</code> to provide dry-run capability and to deal with all
 *  the verbosity issues.
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
public class GantBuilder extends AntBuilder {
  public Object invokeMethod ( final String name , final Object arguments ) {
    if ( GantState.dryRun ) {
      if ( GantState.verbosity > GantState.SILENT ) {
        int padding = 9 - name.length ( ) ;
        if ( padding < 0 ) { padding = 0 ; }
        System.out.print ( "         ".substring ( 0 , padding ) + "[" + name + "] ") ;
        final Object[] args = (Object[]) arguments ;
        if ( args[0] instanceof Map ) {
          final Iterator i = ( (Map) args[0] ).entrySet ( ).iterator ( ) ;
          while ( i.hasNext ( ) ) {
            final Map.Entry e = (Map.Entry) i.next ( ) ;
            System.out.print ( e.getKey ( ) + " : '" + e.getValue ( ) + "'" ) ;
            if ( i.hasNext ( ) ) { System.out.print ( " , " ) ; }
          }
          System.out.println ( ) ;
          if ( args.length == 2 ) { ( (Closure) args[1] ).call ( ) ; }
        }
        else if ( args[0] instanceof Closure ) { System.out.println ( ) ; ( (Closure) args[0] ).call ( ) ; }
        else { throw new RuntimeException ( "Unexpected type of parameter to method " + name ) ; }
      }
      return null ;
    }
    return super.invokeMethod ( name , arguments ) ;
  }
}
