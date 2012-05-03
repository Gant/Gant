//  Gant -- A Groovy way of scripting Ant tasks.
//
//  Copyright © 2008,2012 Peter Ledbrook
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

package gant ;

import groovy.lang.MissingPropertyException ;

/**
 *  Thrown when an undefined property is referenced during target execution.
 *
 *  @author Peter Ledbrook 
 */
public class TargetMissingPropertyException extends GantException {
  public static final long serialVersionUID = 1 ;
  public TargetMissingPropertyException ( ) { super ( ) ; }
  public TargetMissingPropertyException ( final String msg ) { super ( msg ) ; }
  public TargetMissingPropertyException ( final MissingPropertyException e ) { super ( e ) ; }
  public TargetMissingPropertyException ( final String msg , final MissingPropertyException e ) { super ( msg , e ) ; }
}
