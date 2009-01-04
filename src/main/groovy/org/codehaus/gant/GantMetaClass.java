//  Gant -- A Groovy way of scripting Ant tasks.
//
//  Copyright © 2006-9 Russel Winder
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
import java.util.List ;
import java.util.Set ;

import groovy.lang.Closure ;
import groovy.lang.DelegatingMetaClass ;
import groovy.lang.GString ;
import groovy.lang.MetaClass ;
import groovy.lang.MissingMethodException ;
import groovy.lang.MissingPropertyException ;
import groovy.lang.Tuple ;

import org.codehaus.groovy.runtime.MetaClassHelper ;

import org.apache.tools.ant.BuildException ;

/**
 *  This class is the metaclass used for target <code>Closure</code>s, any enclosed <code>Closures</code>,
 *  and the Gant script itself.
 *
 *  <p>This metaclass deals with <code>depends</code> method calls and redirects unknown method calls to the
 *  instance of <code>GantBuilder</code>.  To process the <code>depends</code> all closures from the
 *  binding called during execution of the Gant specification must be logged so that when a depends happens
 *  the full closure call history is available.</p>
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
public class GantMetaClass extends DelegatingMetaClass {
  /**
   *  The set of all targets that have been called.  This is a global variable shared by all instances of
   *  <code>GantMetaClass</code>.
   *
   *  <p>TODO : This code is a long way from thread safe, and so it needs fixing.  Should this variable be
   *  moved to the GantState class, which is the class that holds other bits of the internal shared state?
   *  Should a different data structure be used, one that is a bit more thread safe?  Arguably it is
   *  reasonable for this to be a synchronized object.</p>
   */
  private static final Set<Closure> methodsInvoked = new HashSet<Closure> ( ) ;
  /**
   *  The binding (aka global shared state) that is being used.
   */
  private final GantBinding binding ;
  /*
   */
  public GantMetaClass ( final MetaClass metaClass , final GantBinding binding ) {
    super ( metaClass ) ;
    this.binding = binding ;
  }
  /**
   *  Execute a <code>Closure</code> only if it hasn't been executed previously.  Record the execution if it
   *  is executed.  Only used for processing a <code>depends</code> call.
   *
   *  @param closure The <code>Closure</code> to potentially call.
   *  @return the result of the <code>Closure</code> call, or <code>null</code> if the closure was not
   *  called.
   */
  private Object processClosure ( final Closure closure ) {
    if ( ! methodsInvoked.contains ( closure ) ) {
      methodsInvoked.add ( closure ) ;
      return closure.call ( ) ;
    }
    return null ;
  }
  /**
   *  Process the argument to a <code>depends</code> call.  If the parameter is a <code>Closure</code> just
   *  process it. If it is a <code>String</code> then do a lookup for the <code>Closure</code> in the
   *  binding, and if found process it.
   *
   *  @param argument The argument.
   *  @return The result of the <code>Closure</code>.
   */
  private Object processArgument ( final Object argument ) {
    Object returnObject ;
    if ( argument instanceof Closure ) { returnObject = processClosure ( (Closure) argument ) ; }
    else {
      final String errorReport = "depends called with an argument (" + argument + ") that is not a known target or list of targets." ;
      Object theArgument = argument ;
      if ( theArgument instanceof GString ) { theArgument = theArgument.toString ( ) ; }
      if ( theArgument instanceof String ) {
        final Object entry = binding.getVariable ( (String) theArgument ) ;
        if ( ( entry != null ) && ( entry instanceof Closure ) ) { returnObject = processClosure ( (Closure) entry ) ; }
        else { throw new RuntimeException ( errorReport ) ; }
      }
      else { throw new RuntimeException ( errorReport ) ; }
    }
    return returnObject ;
  }
  /**
   *  Invokes a method on the given object with the given name and arguments. The <code>MetaClass</code>
   *  will attempt to pick the best method for the given name and arguments. If a method cannot be invoked a
   *  <code>MissingMethodException</code> will be thrown.
   *
   *  @see MissingMethodException
   *  @param object The instance on which the method is invoked.
   *  @param methodName The name of the method.
   *  @param arguments The arguments to the method.
   *  @return The return value of the method which is <code>null</code> if the return type is
   *  <code>void</code>.
   */
  @Override public Object invokeMethod ( final Object object , final String methodName , final Object[] arguments ) {
    Object returnObject = null ;
    if ( methodName.equals ( "depends" ) ) {
      for ( final Object argument : arguments ) {
        if ( argument instanceof List ) {
          for ( final Object item : (List<?>) argument ) { returnObject = processArgument ( item ) ; }
        }
        else { returnObject = processArgument ( argument ) ; }
      }
    }
    else {
      try {
        returnObject = super.invokeMethod ( object , methodName , arguments ) ;
        try {
          final Closure closure = (Closure) binding.getVariable ( methodName ) ;
          if ( closure != null ) { methodsInvoked.add ( closure ) ; }
        }
        catch ( final MissingPropertyException mpe ) { /* Purposefully empty */ }
      }
      catch ( final MissingMethodException mme ) {
        try { returnObject = ( (GantBuilder) ( binding.getVariable ( "ant" ) ) ).invokeMethod ( methodName , arguments ) ; }
        catch ( final BuildException be ) { throw mme ; }
      }
    }
    return returnObject ;
  }
  /**
   *  Invokes a method on the given object, with the given name and single argument.
   *
   *  @see #invokeMethod(Object, String, Object[])
   *  @param object The Object to invoke the method on
   *  @param methodName The name of the method
   *  @param arguments The argument to the method
   *  @return The return value of the method which is null if the return type is void
   */
  @Override public Object invokeMethod ( final Object object , final String methodName , final Object arguments ) {
    if ( arguments == null ) { return invokeMethod ( object , methodName , MetaClassHelper.EMPTY_ARRAY ) ; }
    else if ( arguments instanceof Tuple ) { return invokeMethod ( object , methodName , ( (Tuple) arguments ).toArray ( ) ) ; }
    else if ( arguments instanceof Object[] ) { return invokeMethod ( object , methodName , (Object[]) arguments ) ; }
    else { return invokeMethod ( object , methodName , new Object[] { arguments } ) ; }
  }
  /**
   *  Invoke the given method.
   *
   *  @param name the name of the method to call
   *  @param args the arguments to use for the method call
   *  @return the result of invoking the method
   */
  @Override public Object invokeMethod ( final String name , final Object args ) {
    return invokeMethod ( this , name , args ) ;
  }
  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  //  Groovy 1.5.x is Java 1.4 and as at 2008-07-10 Groovy 1.6.x groovy.lang.MetaClass and
  //  groovy.lang.GroovyObject have not been marked up for Java 5 generics.  Because there is a problem that
  //  blah(Class<?>) does not override blah(Class) -- at least according to Eclipse -- leave the Class type
  //  without a type parameter and suffer the compilation warning.
  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  /**
   *  Invoke a method on the given receiver for the specified arguments. The sender is the class that
   *  invoked the method on the object.  Attempt to establish the method to invoke based on the name and
   *  arguments provided.
   *
   *  <p>The <code>isCallToSuper</code> and <code>fromInsideClass</code> help the Groovy runtime perform
   *  optimizations on the call to go directly to the superclass if necessary.</p>
   *
   *  @param sender The <code>java.lang.Class</code> instance that invoked the method.
   *  @param receiver The object which the method was invoked on.
   *  @param methodName The name of the method.
   *  @param arguments The arguments to the method.
   *  @param isCallToSuper Whether the method is a call to a superclass method.
   *  @param fromInsideClass Whether the call was invoked from the inside or the outside of the class.
   *  @return The return value of the method
   */
  @Override public Object invokeMethod ( final Class sender , final Object receiver , final String methodName , final Object[] arguments, final boolean isCallToSuper, final boolean fromInsideClass ) {
    return invokeMethod ( receiver , methodName , arguments ) ;
  }
}
