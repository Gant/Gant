//  Gant -- A Groovy build framework based on scripting Ant tasks.
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
 *  Throw when an undefined target is invoked.
 *
 *  @author Peter Ledbrook 
 */
class MissingTargetException extends GantException {
  MissingTargetException ( ) { super ( ) }
  MissingTargetException ( String msg ) { super ( msg ) }
  MissingTargetException ( Exception e ) { super ( e ) }
  MissingTargetException ( String msg , Exception e ) { super ( msg , e ) }
}
