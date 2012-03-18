//  Gant -- A Groovy way of scripting Ant tasks.
//
//  Copyright © 2007-10 Russel Winder
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

package gant.tools.tests

import org.codehaus.gant.tests.GantTestCase

/**
 *  A test to ensure that the LaTeX tool is not broken.
 *
 *  @author Russel Winder <russel@winder.org.uk>
 */
final class LaTeX_Test extends GantTestCase {
  def executablePresent = false
  public LaTeX_Test ( ) {
    try { executablePresent = Runtime.runtime.exec ( 'pdflatex -interaction=batchmode \\end' ).waitFor ( ) == 0 }
    catch ( Exception io ) { }
  }  
  def optionTestGantFile ( name , key ) { """
includeTool << gant.tools.LaTeX
target ( add${name}Option : '' ) {
  laTeX.add${name}Option ( '-blah' )
  println ( laTeX.environment[ "${key}Options" ] )
}
"""
  }
  def optionListTestGantFile ( name , key ) { """
includeTool << gant.tools.LaTeX
target ( add${name}OptionList : '' ) {
  laTeX.add${name}Option ( [ '-blah' , '--flobadob' ] )
  println ( laTeX.environment[ "${key}Options" ] )
}
"""
  }

  void testAddLaTeXOption ( ) {
    final targetName = 'addLaTeXOption'
    script = optionTestGantFile ( 'LaTeX' , 'latex' )
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , '[-interaction=nonstopmode, -halt-on-error, -blah]\n' ) , output ) 
    assertEquals ( '' , error )
  }
  void testAddLaTeXOptionList ( ) {
    final targetName = 'addLaTeXOptionList'
    script = optionListTestGantFile ( 'LaTeX' , 'latex' )
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , '[-interaction=nonstopmode, -halt-on-error, -blah, --flobadob]\n' ) , output ) 
    assertEquals ( '' , error )
  }
  void testAddBibTeXOption ( ) {
    final targetName = 'addBibTeXOption'
    script = optionTestGantFile ( 'BibTeX' , 'bibtex' )
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , '[-blah]\n' ) , output ) 
    assertEquals ( '' , error )
  }
  void testAddBibTeXOptionList ( ) {
    final targetName = 'addBibTeXOptionList'
    script = optionListTestGantFile ( 'BibTeX' , 'bibtex' )
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , '[-blah, --flobadob]\n' ) , output ) 
    assertEquals ( '' , error )
  }
  void testAddMakeindexOption ( ) {
    final targetName = 'addMakeindexOption'
    script = optionTestGantFile ( 'Makeindex' , 'makeindex' )
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , '[-blah]\n' ) , output ) 
    assertEquals ( '' , error )
  }
  void testAddMakeindexOptionList ( ) {
    final targetName = 'addMakeindexOptionList'
    script = optionListTestGantFile ( 'Makeindex' , 'makeindex' )
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , '[-blah, --flobadob]\n' ) , output ) 
    assertEquals ( '' , error )
  }
  void testAddDvipsOption ( ) {
    final targetName = 'addDvipsOption'
    script = optionTestGantFile ( 'Dvips' , 'dvips' )
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , '[-blah]\n' ) , output ) 
    assertEquals ( '' , error )
  }
  void testAddDvipsOptionList ( ) {
    final targetName = 'addDvipsOptionList'
    script = optionListTestGantFile ( 'Dvips' , 'dvips' )
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , '[-blah, --flobadob]\n' ) , output ) 
    assertEquals ( '' , error )
  }
  void testAddPs2pdfOption ( ) {
    final targetName = 'addPs2pdfOption'
    script = optionTestGantFile ( 'Ps2pdf' , 'ps2pdf' )
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , '[-blah]\n' ) , output ) 
    assertEquals ( '' , error )
  }
  void testAddPs2pdfOptionList ( ) {
    final targetName = 'addPs2pdfOptionList'
    script = optionListTestGantFile ( 'Ps2pdf' , 'ps2pdf' )
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , '[-blah, --flobadob]\n' ) , output ) 
    assertEquals ( '' , error )
  }
  void testEmptyFile ( ) {
    final buildScript = '''
includeTool << gant.tools.LaTeX
target ( "pdf" : "" ) { laTeX.generatePDF ( root : "TESTFILENAME" ) }
includeTargets << gant.targets.Clean
laTeX.intermediateExtensions.each { extension -> cleanPattern << '*' + extension }
'''
    if ( executablePresent ) {
      final extension = '.ltx'
      final filename = File.createTempFile ( 'gantLaTeXTest_' , extension , new File ( '.' ) )
      script = buildScript.replace ( 'TESTFILENAME' , filename.name.replaceAll ( extension , '' ) )
      assertEquals ( 0 , processCmdLineTargets ( 'pdf' ) )
      assertTrue ( output.contains ( '[execute] [pdflatex, -interaction=nonstopmode, -halt-on-error, gantLaTeXTest_' ) )
      assertTrue ( output.contains ( '!  ==> Fatal error occurred, no output PDF file produced!' ) )
      assertEquals ( '' , error )
      assertEquals ( 0 , processCmdLineTargets ( 'clean' ) )
      filename.delete ( )
    }
    else { System.err.println ( 'testEmptyFile not run since pdflatex executable is not available.' ) }
  }
  void testInitialized ( ) {
    final targetName = 'test'
    script = """
includeTool ** gant.tools.LaTeX * [ latexOptions : 'flobadob' ]
target ( ${targetName} : "" ) { println ( laTeX.environment['latexOptions'] ) }
"""
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , '[-interaction=nonstopmode, -halt-on-error, flobadob]\n' ) , output )
    assertEquals ( '' , error )
  }
}
