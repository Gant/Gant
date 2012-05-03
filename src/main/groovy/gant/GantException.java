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

//  Peter's original extended RuntimeException.  GANT-111 introduced the problem that this cauises problems
//  for usage with Ant tasks -- where the exception really needs to be a descendent of
//  org.apache.tools.ant.BuildException.  Making this change should not affect stand alone activity.  Thanks
//  to Eric Van Dewoestine for providing this change.

import org.apache.tools.ant.BuildException ;

/**
 *  Generic Gant exception.
 *
 *  @author Peter Ledbrook
 */
public class GantException extends /*RuntimeException*/ BuildException {
  public static final long serialVersionUID = 1 ;
  public GantException ( ) { super( ) ; }
  public GantException ( final String msg ) { super ( msg ) ; }
  public GantException ( final Exception e ) { super ( e ) ; }
  public GantException ( final String msg , final Exception e ) { super ( msg, e ) ; }
}
