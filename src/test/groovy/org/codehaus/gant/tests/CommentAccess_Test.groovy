//  Gant -- A Groovy way of scripting Ant tasks.
//
//  Copyright © 2007–2010, 2013  Russel Winder
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

/**
 *  A test to ensure access to the comment in a task works correctly.
 *
 *  @author Russel Winder <russel@winder.org.uk>
 */
final class CommentAccess_Test extends GantTestCase {
  void testcommentAccess() {
    final targetName = 'commentAccess'
    final success = 'Success.'
    script = """
theComment = 'Some comment.'
target(${targetName}: theComment) {
 //  This is old-style and should be deprecated.
 assert commentAccess_description == theComment
 assert it.description == theComment
 println('${success}')
}
"""
    assertEquals(0, processCmdLineTargets('commentAccess'))
    assertEquals(resultString(targetName, success + '\n'), output)
    assertEquals('', error)
  }
}
