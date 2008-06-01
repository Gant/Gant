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
import groovy.lang.GroovyObject ;
import groovy.lang.GroovySystem ;
import groovy.lang.MetaClass ;
import groovy.lang.MetaMethod ;
import groovy.lang.MetaProperty ;
import groovy.lang.MissingMethodException ;
import groovy.lang.MissingPropertyException ;
import groovy.lang.Tuple ;

import org.codehaus.groovy.ast.ClassNode ;
import org.codehaus.groovy.runtime.InvokerHelper ;
import org.codehaus.groovy.runtime.MetaClassHelper ;

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
class GantMetaClass implements MetaClass , GroovyObject {
  /**
   *  The metaclass that this metaclass is a proxy of.
   */
  private final MetaClass delegate ;
  /**
   *  The set of all targets that have been called.  This is a global variable shared by all instances of
   *  <code>GantMetaClass</code>.
   *
   *  <p>FIXME: This code is a long way from thread safe, and so it needs fixing.  Should this variable be
   *  moved to the GantState class, which is the class that holds other bits of the internal shared state?
   *  Should a different data structure be used, one that is a bit more thread safe?  Arguably it is
   *  reasonable for this to be a synchronized object.</p>
   */
  private final static HashSet<Closure> methodsInvoked = new HashSet<Closure> ( ) ;
  /**
   *  The binding (aka global shared state) that is being used.
   */
  private final Binding binding ;
  /*
   */
  public GantMetaClass ( final MetaClass metaClass , final Binding binding ) {
    this.delegate = metaClass ;
    this.binding = binding ;
  }
 
  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  ////  Method required of a GroovyObject

  /**
   *  Invoke the given method.
   *
   *  @param name the name of the method to call
   *  @param args the arguments to use for the method call
   *  @return the result of invoking the method
   */
  public Object invokeMethod ( final String name ,  final Object args ) {
    try { return getMetaClass ( ).invokeMethod ( this , name , args ) ; }
    catch ( final MissingMethodException mme ) { 
      if ( delegate instanceof GroovyObject ) { return ( (GroovyObject) delegate ).invokeMethod ( name , args ) ; }
      else { throw mme ; }
    }
  }
  /**
   *  Retrieve a property value.
   *
   *  @param propertyName the name of the property of interest
   *  @return the given property
   */
  public Object getProperty ( final String propertyName ) {
    try { return getMetaClass ( ).getProperty ( this , propertyName ) ; }
    catch ( final MissingPropertyException mpe ) {
      if ( delegate instanceof GroovyObject ) { return ( (GroovyObject) delegate ).getProperty ( propertyName ) ; }
      else { throw mpe ; }  
    }
  }
  /**
   * Sets the given property to the new value.
   *
   * @param propertyName the name of the property of interest
   * @param newValue     the new value for the property
   */
  public void setProperty ( final String propertyName , final Object newValue ) {
    try { getMetaClass ( ).setProperty ( this , propertyName , newValue ) ; }
    catch ( final MissingPropertyException mpe ) {
      if ( delegate instanceof GroovyObject ) { ( (GroovyObject) delegate ).setProperty ( propertyName , newValue ) ; }
      else { throw mpe ; }
    }
  }
  /**
   *  Return the metaclass of this object.
   *
   * @return the metaclass of this object.
   */
  public MetaClass getMetaClass ( ) { return InvokerHelper.getMetaClass ( this ) ; }
  /**
   *  Set a new metaclass for this object.  This method always throws a
   *  <code>UnsupportedOperationException</code>.
   *
   *  @param metaclass the new metaclass.
   *  @throws UnsupportedOperationException
   */
  public void setMetaClass ( final MetaClass metaClass ) { throw new UnsupportedOperationException ( ) ; }
  
  ////  End of GroovyObject methods.
  //////////////////////////////////////////////////////////////////////////////////////////////////////////

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  //// Methods required of  a MetaClass.

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
  public Object invokeMethod ( final Class sender , final Object receiver , final String methodName , final Object[] arguments , final boolean isCallToSuper , final boolean fromInsideClass ) {
    return invokeMethod ( receiver , methodName , arguments ) ;
  }
  /**
   *  Retrieve a property on the given receiver for the specified arguments. The sender is the class that is
   *  requesting the property from the object.  Attempt to establish the method to invoke based on the name
   *  and arguments provided.
   *
   * <p>The <code>isCallToSuper</code> and <code>fromInsideClass</code> help the Groovy runtime perform
   * optimizations on the call to go directly to the superclass if necessary.</p>
   *
   * @param sender The <code>java.lang.Class</code> instance that requested the property.
   * @param receiver The <code>Object</code> which the property is being retrieved from.
   * @param property The name of the property.
   * @param isCallToSuper Whether the call is to a superclass property.
   * @param fromInsideClass ??
   * @return The property value.
   */
  public Object getProperty ( final Class sender , final Object receiver , final String property , final boolean isCallToSuper , final boolean fromInsideClass ) {
    return delegate.getProperty ( sender , receiver , property , isCallToSuper , fromInsideClass ) ;
  }
  /**
   *  Set the value of a property on the given receiver. The sender is the class that is setting the
   *  property.  Attempt to establish the method to invoke based on the name and arguments provided.
   *
   * <p>The <code>isCallToSuper</code> and <code>fromInsideClass</code> help the Groovy runtime perform
   * optimizations on the call to go directly to the superclass if necessary.</p>
   *
   * @param sender The java.lang.Class instance that is mutating the property
   * @param receiver The Object which the property is being set on
   * @param property The name of the property
   * @param value The new value of the property to set
   * @param isCallToSuper Whether the call is to a super class property
   * @param fromInsideClass ??
   */
  public void setProperty ( final Class sender , final Object receiver , final String property , final Object value , final boolean isCallToSuper , final boolean fromInsideClass ) {
    delegate.setProperty ( sender , receiver , property , value , isCallToSuper , fromInsideClass ) ;
  }
  /**
   *  Attempt to invoke the <code>methodMissing</code> method otherwise throw a
   *  <code>MissingMethodException</code>.
   *
   *  @see groovy.lang.MissingMethodException
   *  @param instance The instance to invoke <code>methodMissing</code> on.
   *  @param methodName The name of the method.
   *  @param arguments The arguments to the method.
   *  @return The results of <code>methodMissing</code> or throws <code>MissingMethodException</code>.
   */
  public Object invokeMissingMethod ( final Object instance , final String methodName , final Object[] arguments ) {
    return delegate.invokeMissingMethod ( instance , methodName , arguments ) ;
  }
  /**
   *  Attempt to invoke the <code>propertyMissing</code> method otherwise throw a
   *  <code>MissingPropertyException</code>.
   *
   * @param instance The instance of the class.
   * @param propertyName The name of the property.
   * @param optionalValue The value of the property which could be null in the case of a getter.
   * @param isGetter Whether the missing property event was the result of a getter or a setter.
   * @return The result of the <code>propertyMissing </code>method or throws <code>MissingPropertyException</code>.
   */
  public Object invokeMissingProperty ( final Object instance , final String propertyName , final Object optionalValue,  final boolean isGetter ) {
    return delegate.invokeMissingProperty ( instance , propertyName , optionalValue , isGetter ) ;
  }
  /**
   * Retrieve the value of an attribute (field). This method is to support the Groovy runtime and not for general client API usage.
   *
   * @param sender The class of the object that requested the attribute.
   * @param receiver The instance.
   * @param messageName The name of the attribute.
   * @param useSuper Whether to look-up on the super class or not.
   * @return The attribute value.
   */
  public Object getAttribute ( final Class sender , final Object receiver , final String messageName , final boolean useSuper ) {
    return delegate.getAttribute ( sender , receiver , messageName , useSuper ) ;
  }
  /**
   * Set the value of an attribute (field). This method is to support the Groovy runtime and not for general client API usage.
   *
   * @param sender The class of the object that requested the attribute.
   * @param receiver The instance.
   * @param messageName The name of the attribute.
   * @param messageValue The value of the attribute.
   * @param useSuper Whether to look-up on the super class or not.
   * @param fromInsideClass Whether the call happened from the inside or the outside of a class.
   */
  public void setAttribute ( final Class sender , final Object receiver , final String messageName , final Object messageValue , final boolean useSuper , final boolean fromInsideClass ) {
    delegate.setAttribute ( sender , receiver , messageName , messageValue , useSuper , fromInsideClass ) ;
  }
  /**
   *  Complete the initialization process. After this method is called no methods should be added to the
   *  metaclass.  Invocation of methods or access to fields/properties is forbidden unless this method is
   *  called. This method should contain any initialization code, taking a longer time to complete. An
   *  example is the creation of the <code>Reflector</code>. It is suggested to synchronize this method.
   */
  public void initialize ( ) { delegate.initialize ( ) ; }
  /**
   *  Retrieve a list of <code>MetaProperty</code> instances that the <code>MetaClass</code> has.
   *
   *  @see MetaProperty
   *  @return A list of <code>MetaProperty</code> instances
   */
  public List<MetaProperty> getProperties ( ) { return delegate.getProperties ( ) ; }
  /**
   *  Retrieve a list of <code>MetaMethods</code> held by the class.
   *
   *  @return A list of <code>MetaMethods</code>
   */
  public List<MetaMethod> getMethods ( ) { return delegate.getMethods ( ) ; }
  /**
   *  Return a reference to the original AST for the <ciode>MetaClass</code> if it is available at runtime.
   *
   *  @return The original AST or <code>null</code> if it cannot be returned.
   */
  public ClassNode getClassNode ( ) { return delegate.getClassNode ( ) ; }
  /**
   *  Return a list of <code>MetaMethod</code> instances held by this class.
   *
   *  @return A list of <code>MetaMethod</code> instances.
   */
  public List<MetaMethod> getMetaMethods ( ) { return delegate.getMetaMethods ( ) ; }
  /**
   *  Internal method to support Groovy runtime. Not for client usage.
   *
   *  @param numberOfConstructors The number of constructors.
   *  @param arguments The arguments.
   *  @return selected index.
   */
  public int selectConstructorAndTransformArguments ( final int numberOfConstructors , final Object[] arguments ) {
    return delegate.selectConstructorAndTransformArguments ( numberOfConstructors , arguments ) ;
  }
  /**
   *  Selects a method by name and argument classes. This method does not search for an exact match, it
   *  searches for a compatible method. For this the method selection mechanism is used as provided by the
   *  implementation of this <code>MetaClass</code>. <code>pickMethod</code> may or may not be used during
   *  the method selection process when invoking a method.  There is no warranty for that.
   *
   * @param methodName the name of the method to pick.
   * @param arguments the method arguments.
   * @return a matching <code>MetaMethod</code> or <code>null</code>.
   * @throws GroovyRuntimeException if there is more than one matching method.
   */
  public MetaMethod pickMethod ( final String methodName , final Class[] arguments ) {
    return delegate.pickMethod ( methodName , arguments ) ;
  }
  
  ////  End of MetaClass methods.
  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  //// Methods required of a MetaObjectProtocol object that are not explicitly mentioned for GroovyObject
  //// and MetaClass.
  
  ////  getPropeties and getMethods have been defined above as tehy are required for MetaClass.

  /*
   * Obtain a list of all meta properties available on this meta class
   *
   * @see groovy.lang.MetaBeanProperty
   * @return A list of MetaBeanProperty instances
   */
  //List getProperties();
  /*
   * Obtain a list of all the meta methods available on this meta class
   *
   * @see groovy.lang.MetaMethod
   * @return A list of MetaMethod instances
   */
  //List getMethods();
  
  /**
   *  Return an object satisfying Groovy truth if the implementing <code>MetaClass</code> responds to a
   *  method with the given name and arguments types.
   *
   *  <p>Note that this method's return value is based on realized methods and does not take into account
   *  objects or classes that implement <code>invokeMethod</code> or <code>methodMissing</code>.</p>
   *
   *  <p>This method is "safe" in that it will always return a value and never throw an exception.</p>
   *
   * @param obj The object to inspect.
   * @param name The name of the method of interest.
   * @param argTypes The argument types to match against.
   * @return A <code>List</code> of <code>MetaMethod</code>s matching the argument types which will be empty
   * if no matching methods exist.
   */
  public List<MetaMethod> respondsTo ( final Object object , final String name , final Object[] argTypes ) {
    return delegate.respondsTo ( object , name , argTypes ) ;
  }
  /**
   *  Return an object satisfying Groovy truth if the implementing <code>MetaClass</code> responds to a
   *  method with the given name regardless of arguments. In other words this method will return for
   *  <code>foo ( )</code> and <code>foo ( String )</code>.
   *
   *  <p>Note that this method's return value is based on realized methods and does not take into account
   *  objects or classes that implement <code>invokeMethod</code> or <code>methodMissing</code>.</p>
   *
   *  <p>This method is "safe" in that it will always return a value and never throw an exception.</p>
   *
   * @param obj The object to inspect.
   * @param name The name of the method of interest.
   * @return A <code>List</code> of <code>MetaMethod</code>s which will be empty if no methods with the
   * given name exist.
   */
  public List<MetaMethod> respondsTo ( final Object object , final String name ) {
    return delegate.respondsTo ( object , name ) ;
  }
  /**
   *  Return <code>true</code> if the implementing <code>MetaClass</code> has a property of the given name.
   *
   *  <p>Note that this method will only return <code>true</code> for realized properties and does not take
   *  into account implementation of <code>getProperty</code> or <code>propertyMissing</code>.
   *
   *  @param obj The object to inspect.
   *  @param name The name of the property.
   *  @return The MetaProperty or null if it doesn't exist.
   */
  public MetaProperty hasProperty ( final Object object , final String name ) {
    return delegate.hasProperty ( object , name ) ;
  }
  /**
   *  Return a <code>MetaProperty</code> for the given name or <code>null</code> if it doesn't exist.
   *
   *  @param name The name of the <code>MetaProperty</code>
   *  @return A <code>MetaProperty</code> or <code>null</code>.
   */
  public MetaProperty getMetaProperty ( final String name ) {
    return delegate.getMetaProperty ( name ) ;
  }
  /**
   *  Retrieve a static <code>MetaMethod</code> for the given name and argument values, using the types of
   *  the arguments to establish the chosen <code>MetaMethod</code>.
   *
   *  @param name The name of the <code>MetaMethod</code>.
   *  @param args The argument types.
   *  @return A <code>MetaMethod</code> or <code>null</code> if it doesn't exist.
   */
  public MetaMethod getStaticMetaMethod ( final String name , final Object[] args ) {
    return delegate.getStaticMetaMethod ( name , args ) ;
  }
  /**
   *  Retrieve a MetaMethod instance for the given name and argument values, using the types of the
   *  argument values to establish the chosen MetaMethod
   *
   * @param name The name of the MetaMethod
   * @param args The argument types
   * @return A MetaMethod or null if it doesn't exist
   */
  public MetaMethod getMetaMethod ( final String name , final Object[] args ) {
    return delegate.getMetaMethod ( name , args ) ;
  }
  /**
   *  Retrieve the <code>Class</code> of the instance to which this <code>MetaClass</code> instance is
   *  attached.
   *
   * @return The <code>Class</code> instance.
   */
  public Class getTheClass ( ) { return delegate.getTheClass ( ) ; }
  /**
   *  Invoke a constructor for the given arguments. The <code>MetaClass</code> will attempt to pick the best
   *  argument which matches the types of the objects passed within the arguments array.
   *
   *  @param arguments The arguments to the constructor
   *  @return An instance of the <code>Class</code> to which this <code>MetaObjectProtocol</code> object
   *  applies.
   */
  public Object invokeConstructor ( final Object[] arguments) {
    return delegate.invokeConstructor ( arguments ) ;
  }
  /**
   *  Invokes a method on the given object with the given name and arguments. The <code>MetaClass</code>
   *  will attempt to pick the best method for the given name and arguments. If a method cannot be invoked a
   *  <code>MissingMethodException</code> will be thrown.
   *
   *  @see groovy.lang.MissingMethodException
   *  @param object The instance on which the method is invoked.
   *  @param methodName The name of the method.
   *  @param arguments The arguments to the method.
   *  @return The return value of the method which is <code>null</code> if the return type is
   *  <code>void</code>.
   */
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
      /*
      for ( Object arg : arguments ) {
        if ( arg instanceof Closure ) {
          final Closure closure = (Closure) arg ;
           if ( ! ( closure.getMetaClass ( ) instanceof GantMetaClass ) ) {
             System.err.println ( "Setting metaclass on " + arg + " which had class " + closure.getClass ( ) + " and metaclass " + closure.getMetaClass ( ) ) ;
             closure.setMetaClass ( new GantMetaClass ( closure.getMetaClass ( ) , binding ) ) ;
           }
        }
      }
      */
      //////////////////////////////////////////////////////////////////////////////////////////////////////

      try {
        returnObject = delegate.invokeMethod ( object , methodName , arguments ) ;
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
  /**
   *  Invokes a method on the given object, with the given name and single argument.
   *
   *  @see #invokeMethod(Object, String, Object[])
   *  @param object The Object to invoke the method on
   *  @param methodName The name of the method
   *  @param arguments The argument to the method
   *  @return The return value of the method which is null if the return type is void
   */
  public Object invokeMethod ( final Object object , final String methodName , final Object arguments ) {
    if ( arguments == null ) { return invokeMethod ( object , methodName , MetaClassHelper.EMPTY_ARRAY ) ; }
    else if ( arguments instanceof Tuple ) { return invokeMethod ( object , methodName , ( (Tuple) arguments ).toArray ( ) ) ; }
    else if ( arguments instanceof Object[] ) { return invokeMethod ( object , methodName , (Object[]) arguments ) ; }
    else { return invokeMethod ( object , methodName , new Object[] { arguments } ) ; }
  }
  /**
   *  Invoke a static method on the given object with the given name and arguments.
   *
   *  <p> The Object can either be an instance of the class that this
   *  MetaObjectProtocol instance applies to or the java.lang.Class instance itself. If a method cannot be invoked
   *  a MissingMethodException is will be thrown</p>
   *
   *  @see groovy.lang.MissingMethodException
   *  @param object An instance of the class returned by the getTheClass() method or the class itself
   *  @param methodName The name of the method
   *  @param arguments The arguments to the method
   *  @return The return value of the method which is null if the return type is void
   */
  public Object invokeStaticMethod ( final Object object , final String methodName , final Object[] arguments ) {
    return delegate.invokeStaticMethod ( object , methodName , arguments ) ;
  }
  /**
   *  Retrieve a property of an instance of the class returned by the <code>getTheClass</code> method.
   *
   *  <p>What this means is largely down to the <code>MetaClass</code> implementation, however the default
   *  case would result in an attempt to invoke a JavaBean getter, or if no such getter exists a public
   *  field of the instance.</p>
   *
   *  @see MetaClassImpl
   *  @param object An instance of the class returned by the <code>getTheClass</code> method.
   *  @param property The name of the property to retrieve the value for.
   *  @return The property's value.
   */
  public Object getProperty ( final Object object , final String property ) {
    return delegate.getProperty ( object , property ) ;
  }
  /**
   *  Sets a property of an instance of the class returned by the <ocde>getTheClass</code> method.
   *
   *  <p>What this means is largely down to the <code>MetaClass</code> implementation, however the default
   *  case would result in an attempt to invoke a JavaBean setter, or if no such setter exists to set a
   *  public field of the instance.</p>
   *
   *  @see MetaClassImpl
   *  @param object An instance of the class returned by the <code>getTheClass</code> method.
   *  @param property The name of the property to set.
   *  @param newValue The new value of the property.
   */
  public void setProperty ( final Object object , final String property , final Object newValue ) {
    delegate.setProperty ( object , property , newValue ) ;
  }
  /**
   *  Retrieves an attribute of an instance of the class returned by the <code>getTheClass</code> method.
   *
   *  <p>What this means is largely down to the <code>MetaClass</code> implementation, however the default
   *  case would result in attempt to read a field of the instance.</p>
   *
   *  @see MetaClassImpl
   *  @param object An instance of the class returned by the <code>getTheClass</code> method.
   *  @param attribute The name of the attribute to retrieve the value for.
   *  @return The attribute value.
   */
  public Object getAttribute ( final Object object , final String attribute )  {
    return delegate.getAttribute ( object , attribute ) ;
  }
  /**
   *  Sets an attribute of an instance of the class returned by the <code>getTheClass</code> method.
   *
   *  <p>What this means is largely down to the <code>MetaClass</code> implementation, however the default
   *  case would result in an attempt to set a field of the instance.</p>
   *
   *  @see MetaClassImpl
   *  @param object An instance of the class returned by the <code>getTheClass</code> method.
   *  @param attribute The name of the attribute to set.
   *  @param newValue The new value of the attribute.
   */
  public void setAttribute ( final Object object , final String attribute , final Object newValue ) {
    delegate.setAttribute ( object , attribute , newValue ) ;
  }
  
  ////  End of MetaObjectProtocol methods.
  //////////////////////////////////////////////////////////////////////////////////////////////////////////

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
   *  @param argument
   *  @return The result of the <code>Closure</code>.
   */
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
}
