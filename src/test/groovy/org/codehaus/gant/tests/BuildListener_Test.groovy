package org.codehaus.gant.tests
/**
 * @author Graeme Rocher
 * @since 1.6
 * 
 * Created: Dec 17, 2008
 */

public class BuildListener_Test extends GantTestCase {


  void testBuildListeners() {
    TestBuildListener listener = new TestBuildListener()
    gant.addBuildListener listener

    script = '''
target(main:"the main target") {
   doMore()
}
target(doMore:"another target") {
  foo = "bar"

  ant.echo "do stuff"
  ant.property name:"one", value:"two"
}
setDefaultTarget(main)
'''

    processTargets()

    def starts = listener.targetStarts
    assertEquals 3, starts.size()
    assertEquals 3, listener.targetEnds.size()

    assertEquals "default", starts[0].target.name
    assertEquals "main", starts[1].target.name
    assertEquals "doMore", starts[2].target.name

    assertEquals "bar", starts[2].binding.foo

    assertEquals 1, listener.buildStarts.size()
    assertEquals 1, listener.buildEnds.size()

    assertEquals 2, listener.taskStarts.size()
    assertEquals 2, listener.taskEnds.size()


    starts = listener.taskStarts

    assertEquals "echo", starts[0].task.taskName
    assertEquals "property", starts[1].task.taskName
  }
}
class TestBuildListener implements org.apache.tools.ant.BuildListener{

  def targetStarts = []
  def targetEnds = []
  def buildStarts = []
  def buildEnds = []
  def taskStarts = []
  def taskEnds = []

  public void buildStarted(org.apache.tools.ant.BuildEvent event) {
    buildStarts << event
  }

  public void buildFinished(org.apache.tools.ant.BuildEvent event) {
    buildEnds << event
  }

  public void targetStarted(org.apache.tools.ant.BuildEvent event) {
    targetStarts << event
  }

  public void targetFinished(org.apache.tools.ant.BuildEvent event) {
    targetEnds << event
  }

  public void taskStarted(org.apache.tools.ant.BuildEvent event) {
    taskStarts << event
  }

  public void taskFinished(org.apache.tools.ant.BuildEvent event) {
    taskEnds << event
  }

  public void messageLogged(org.apache.tools.ant.BuildEvent event) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

}