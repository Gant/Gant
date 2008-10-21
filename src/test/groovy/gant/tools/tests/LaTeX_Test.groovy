//  Gant -- A Groovy build framework based on scripting Ant tasks.
//
//  Copyright © 2007-8 Russel Winder
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
 *  @author Russel Winder <russel.winder@concertant.com>
 */
final class LaTeX_Test extends GantTestCase {
  def executablePresent = false
  public LaTeX_Test ( ) {
    try { executablePresent = Runtime.runtime.exec ( 'pdflatex -interaction=batchmode \\end' ).waitFor ( ) == 0 }
    catch ( Exception io ) { }
  }  
  def optionTestGantFile ( name , key ) { """
includeTool << gant.tools.LaTeX
target ( add${name}Option : "" ) {
  laTeX.add${name}Option ( "-blah" )
  println ( laTeX.environment[ "${key}Options" ] )
}
"""
  }
  def optionListTestGantFile ( name , key ) { """
includeTool << gant.tools.LaTeX
target ( add${name}OptionList : "" ) {
  laTeX.add${name}Option ( [ "-blah" , "--flobadob" ] )
  println ( laTeX.environment[ "${key}Options" ] )
}
"""
  }
  void testAddLaTeXOption ( ) {
    script = optionTestGantFile ( 'LaTeX' , 'latex' )
    assertEquals ( 0 , processCmdLineTargets ( 'addLaTeXOption' ) )
    assertEquals ( ( groovyMinorVersion > 5 ) ? '''[-interaction=nonstopmode, -halt-on-error, -blah]
''' : '''["-interaction=nonstopmode", "-halt-on-error", "-blah"]
''' , output ) 
  }
  void testAddLaTeXOptionList ( ) {
    script = optionListTestGantFile ( 'LaTeX' , 'latex' )
    assertEquals ( 0 , processCmdLineTargets ( 'addLaTeXOptionList' ) )
    assertEquals ( ( groovyMinorVersion > 5 ) ? '''[-interaction=nonstopmode, -halt-on-error, -blah, --flobadob]
''' : '''["-interaction=nonstopmode", "-halt-on-error", "-blah", "--flobadob"]
''' , output ) 
  }
  void testAddBibTeXOption ( ) {
    script = optionTestGantFile ( 'BibTeX' , 'bibtex' )
    assertEquals ( 0 , processCmdLineTargets ( 'addBibTeXOption' ) )
    assertEquals ( ( groovyMinorVersion > 5 ) ? '''[-blah]
''' : '''["-blah"]
''' , output ) 
  }
  void testAddBibTeXOptionList ( ) {
    script = optionListTestGantFile ( 'BibTeX' , 'bibtex' )
    assertEquals ( 0 , processCmdLineTargets ( 'addBibTeXOptionList' ) )
    assertEquals ( ( groovyMinorVersion > 5 ) ? '''[-blah, --flobadob]
''' : '''["-blah", "--flobadob"]
''' , output ) 
  }
  void testAddMakeindexOption ( ) {
    script = optionTestGantFile ( 'Makeindex' , 'makeindex' )
    assertEquals ( 0 , processCmdLineTargets ( 'addMakeindexOption' ) )
    assertEquals ( ( groovyMinorVersion > 5 ) ? '''[-blah]
''' : '''["-blah"]
''' , output ) 
  }
  void testAddMakeindexOptionList ( ) {
    script = optionListTestGantFile ( 'Makeindex' , 'makeindex' )
    assertEquals ( 0 , processCmdLineTargets ( 'addMakeindexOptionList' ) )
    assertEquals ( ( groovyMinorVersion > 5 ) ? '''[-blah, --flobadob]
''' : '''["-blah", "--flobadob"]
''' , output ) 
  }
  void testAddDvipsOption ( ) {
    script = optionTestGantFile ( 'Dvips' , 'dvips' )
    assertEquals ( 0 , processCmdLineTargets ( 'addDvipsOption' ) )
    assertEquals ( ( groovyMinorVersion > 5 ) ? '''[-blah]
''' : '''["-blah"]
''' , output ) 
  }
  void testAddDvipsOptionList ( ) {
    script = optionListTestGantFile ( 'Dvips' , 'dvips' )
    assertEquals ( 0 , processCmdLineTargets ( 'addDvipsOptionList' ) )
    assertEquals ( ( groovyMinorVersion > 5 ) ? '''[-blah, --flobadob]
''' : '''["-blah", "--flobadob"]
''' , output ) 
  }
  void testAddPs2pdfOption ( ) {
    script = optionTestGantFile ( 'Ps2pdf' , 'ps2pdf' )
    assertEquals ( 0 , processCmdLineTargets ( 'addPs2pdfOption' ) )
    assertEquals ( ( groovyMinorVersion > 5 ) ? '''[-blah]
''' : '''["-blah"]
''' , output ) 
  }
  void testAddPs2pdfOptionList ( ) {
    script = optionListTestGantFile ( 'Ps2pdf' , 'ps2pdf' )
    assertEquals ( 0 , processCmdLineTargets ( 'addPs2pdfOptionList' ) )
    assertEquals ( ( groovyMinorVersion > 5 ) ? '''[-blah, --flobadob]
''' : '''["-blah", "--flobadob"]
''' , output ) 
  }
  final buildScript = '''
includeTool << gant.tools.LaTeX
target ( "pdf" : "" ) { laTeX.generatePDF ( root : "TESTFILENAME" ) }
includeTargets << gant.targets.Clean
laTeX.intermediateExtensions.each { extension -> cleanPattern << '*' + extension }
'''
  void testEmptyFile ( ) {
    if ( executablePresent ) {
      def extension = '.ltx'
      def filename = File.createTempFile ( 'gantLaTeXTest_' , extension , new File ( '.' ) )
      script = buildScript.replace ( 'TESTFILENAME' , filename.name.replaceAll ( extension , '' ) )
      assertEquals ( 0 , processCmdLineTargets ( 'pdf' ) )
      assertTrue ( output.contains (
                                    ( groovyMinorVersion > 5 )
                                    ? '[execute] [pdflatex, -interaction=nonstopmode, -halt-on-error, gantLaTeXTest_'
                                    : '[execute] ["pdflatex", "-interaction=nonstopmode", "-halt-on-error", "gantLaTeXTest_' ) )
      assertTrue ( output.contains ( '!  ==> Fatal error occurred, no output PDF file produced!' ) )
      assertEquals ( 0 , processCmdLineTargets ( 'clean' ) )
      filename.delete ( )
    }
    else { System.err.println ( 'testEmptyFile not run since pdflatex executable is not avaialble.' ) }
  }

  void testInitialized ( ) {
    script = '''
includeTool ** gant.tools.LaTeX * [ latexOptions : 'flobadob' ]
target ( test : "" ) { println ( laTeX.environment['latexOptions'] ) }
'''
    assertEquals ( 0 , processCmdLineTargets ( 'test' ) )
    assertEquals (  ( groovyMinorVersion > 5 )
                    ? '[-interaction=nonstopmode, -halt-on-error, flobadob]\n'
                    : '["-interaction=nonstopmode", "-halt-on-error", "flobadob"]\n'
                    , output )
  }
}
