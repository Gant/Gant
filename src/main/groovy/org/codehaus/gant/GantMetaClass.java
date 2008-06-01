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

import java.util.HashSet ;
import java.util.Iterator ;
import java.util.List ;

import groovy.lang.Binding ;
import groovy.lang.Closure ;
import groovy.lang.DelegatingMetaClass ;
import groovy.lang.MissingMethodException ;
import groovy.lang.MissingPropertyException ;

import groovy.lang.GroovySystem ;
import groovy.lang.MetaClass ;

/**
 *  This class is the metaclass used for target <code>Closure</code>s, any enclosed <code>Closures</code>,
 *  and the Gant script itself.
 *
 *  <p>This metaclass deals with <code>depends</code> method calls and redirects unknown method calls to the
 *  instance of <code>GantBuilder</code>.  To process the <codce>depends</code> all closures from the
 *  binding called during execution of the Gant specification must be logged so that when a depends happens
 *  the full closure call history is available.</p>
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
class GantMetaClass extends DelegatingMetaClass {
  //private final static HashSet<Closure> methodsInvoked = new HashSet<Closure> ( ) ;
  private final static HashSet methodsInvoked = new HashSet ( ) ;
  private final Binding binding ;
  public GantMetaClass ( final MetaClass metaClass , final Binding binding ) {
    super ( metaClass ) ;
    this.binding = binding ;
  }
  private Object processClosure ( final Closure closure ) {
    if ( ! methodsInvoked.contains ( closure ) ) {
      methodsInvoked.add ( closure ) ;         
      return closure.call ( ) ;
    }
    return null ;
  }
  private Object processArgument ( final Object argument ) {
    Object returnObject = null ;
    final String errorReport = "depends called with an argument (" + argument + ") that is not a known target or list of targets." ;
    if ( argument instanceof Closure ) { returnObject = processClosure ( (Closure) argument ) ; }
    else if ( argument instanceof String ) {
      final Object entry = binding.getVariable ( (String) argument ) ;
      if ( ( entry != null ) && ( entry instanceof Closure ) ) { returnObject = processClosure ( (Closure) entry ) ; }
      else { throw new RuntimeException ( errorReport ) ; }
    }
    else { throw new RuntimeException ( errorReport ) ; }
    return returnObject ;
  }
  public Object invokeMethod ( final Object object , final String methodName , final Object[] arguments ) {
    Object returnObject = null ;
    if ( methodName.equals ( "depends" ) ) {
      for ( int i = 0 ; i < arguments.length ; ++i ) {
        if ( arguments[i] instanceof List ) {
          Iterator<Object> iterator = ( (List) arguments[i] ).iterator ( ) ;
          while ( iterator.hasNext ( ) ) { returnObject = processArgument ( iterator.next ( ) ) ; }
        }
        else { returnObject = processArgument ( arguments[i] ) ; }
      }
    }
    else {

      //////////////////////////////////////////////////////////////////////////////////////////////////////
      //  Ensure that we have a GantMetaClass on every closure object so that we guarantee that the
      //  GantBuilder object is included in the search path.  Unfortunately, even though this changes the
      //  metaclass on some Closures, it appears to make no difference whatsoever :-(

      for ( Object arg : arguments ) {
        if ( arg instanceof Closure ) {
          final Closure closure = (Closure) arg ;
           if ( ! ( closure.getMetaClass ( ) instanceof GantMetaClass ) ) {
             System.err.println ( "Setting metaclass on " + arg + " which had class " + closure.getClass ( ) + " and metaclass " + closure.getMetaClass ( ) ) ;
             closure.setMetaClass ( new GantMetaClass ( closure.getMetaClass ( ) , binding ) ) ;
           }
        }
      }
      //////////////////////////////////////////////////////////////////////////////////////////////////////

      try {
        returnObject = super.invokeMethod ( object , methodName , arguments ) ;
        try {
          final Closure closure = (Closure) binding.getVariable ( methodName ) ;
          if ( closure != null ) { methodsInvoked.add ( closure ) ; }
        }
        catch ( final MissingPropertyException mpe ) { }
      }
      catch ( final MissingMethodException mme ) {
        returnObject = ( (GantBuilder) ( binding.getVariable ( "ant" ) ) ).invokeMethod ( methodName , arguments ) ;
      }
    }
    return returnObject ;
  }
  //  Added this one due to change of the metaclass system of r5077 and r5078.
  public Object invokeMethod ( final Class sender , final Object receiver , final String methodName , final Object[] arguments, final boolean isCallToSuper, final boolean fromInsideClass) {
    return invokeMethod ( receiver , methodName , arguments ) ;
  }
}
