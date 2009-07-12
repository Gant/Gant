//  Gant -- A Groovy way of scripting Ant tasks.
//
//  Copyright  © 2008 Graeme Rocher
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

package org.codehaus.gant.tests

import org.apache.tools.ant.BuildEvent
import org.apache.tools.ant.BuildListener

/**
 * @author Graeme Rocher
 * @since 1.6
 * 
 * Created: 2008-12-17
 */
public class BuildListener_Test extends GantTestCase {
  void testBuildListeners ( ) {
    DummyBuildListener listener = new DummyBuildListener ( )
    gant.addBuildListener ( listener )
    script = '''
target ( main : "The main target." ) { doMore ( ) }
target ( doMore : "Another target.") {
  foo = "bar"
  ant.echo "do stuff"
  ant.property name:"one", value:"two"
}
setDefaultTarget main
'''
    processTargets ( )
    def starts = listener.targetStarts
    assertEquals 2, starts.size ( )
    assertEquals 2, listener.targetEnds.size ( )
    assertEquals "main", starts[0].target.name
    assertEquals "doMore", starts[1].target.name
    assertEquals "bar", starts[1].binding.foo
    assertEquals 1, listener.buildStarts.size ( )
    assertEquals 1, listener.buildEnds.size ( )
    assertEquals 2, listener.taskStarts.size ( )
    assertEquals 2, listener.taskEnds.size ( )
    starts = listener.taskStarts
    assertEquals "echo", starts[0].task.taskName
    assertEquals "property", starts[1].task.taskName
  }
}

class DummyBuildListener implements BuildListener {
  def targetStarts = [ ]
  def targetEnds = [ ]
  def buildStarts = [ ]
  def buildEnds = [ ]
  def taskStarts = [ ]
  def taskEnds = [ ]
  public void buildStarted ( final BuildEvent event ) { buildStarts << event }
  public void buildFinished ( final BuildEvent event ) { buildEnds << event }
  public void targetStarted ( final BuildEvent event ) { targetStarts << event }
  public void targetFinished ( final BuildEvent event ) { targetEnds << event }
  public void taskStarted ( final BuildEvent event ) { taskStarts << event }
  public void taskFinished ( final BuildEvent event ) { taskEnds << event }
  public void messageLogged ( final BuildEvent event ) {
    //To change body of implemented methods use File | Settings | File Templates.
  }
}
