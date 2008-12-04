//  Gant -- A Groovy way of scripting Ant tasks.
//
//  Copyright © 2008 Peter Ledbrook 
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

package gant

/**
 *  Thrown when an undefined property is referenced during target execution.
 *
 *  @author Peter Ledbrook 
 */
class TargetMissingPropertyException extends GantException {
  TargetMissingPropertyException ( ) { super ( ) }
  TargetMissingPropertyException ( String msg ) { super ( msg ) }
  TargetMissingPropertyException ( MissingPropertyException e ) { super ( e ) }
  TargetMissingPropertyException ( String msg , MissingPropertyException e ) { super ( msg , e ) }
}
