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

package org.codehaus.gant ;

import java.util.Iterator ;
import java.util.Map ;

import groovy.lang.Closure ;
import groovy.util.AntBuilder ;

import org.apache.tools.ant.Project ;

/**
 *  This class is a sub-class of <code>AntBuilder</code> to provide extra capabilities.  In particular, a
 *  dry-run capability, and the implementation of various levels of verbosity.
 *
 *  <p>If execution is from a command line Gant or call from a Groovy script then the class loader for all
 *  objects is a single instance of <code>org.codehaus.groovy.tools.RootLoader</code>, which already has Ant
 *  and Groovy jars in the classpath.  If however execution is from an Ant execution via the Gant Ant Task,
 *  then the classloader for the instance is an instance of <code>org.apache.tools.ant.AntClassLoader</code>
 *  with Ant and Groovy jars on the classpath BUT the class loader for the
 *  <code>org.apache.tools.ant.Project</code> instance is a simple <code>java.net.URLClassLoader</code> and
 *  does not have the necessary jars on the classpath.  When using Ant, the Ant jar has been loaded before
 *  the Groovy aspects of the classpath have been set up.  So we must allow for a specialized constructor
 *  taking a preprepared <code>org.apache.tools.ant.Project</code> to handle this situation. </p>
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
public class GantBuilder extends AntBuilder {
  public GantBuilder ( ) { }
  public GantBuilder ( final Project project ) { super ( project ) ; }
  public Object invokeMethod ( final String name , final Object arguments ) {
    if ( GantState.dryRun ) {
      if ( GantState.verbosity > GantState.SILENT ) {
        int padding = 9 - name.length ( ) ;
        if ( padding < 0 ) { padding = 0 ; }
        System.out.print ( "         ".substring ( 0 , padding ) + "[" + name + "] ") ;
        final Object[] args = (Object[]) arguments ;
        if ( args[0] instanceof Map ) {
          final Iterator<Map.Entry> i = ( (Map) args[0] ).entrySet ( ).iterator ( ) ;
          while ( i.hasNext ( ) ) {
            final Map.Entry e = i.next ( ) ;
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
