//  Gant -- A Groovy build framework based on scripting Ant tasks.
//
//  Copyright © 2007 Russel Winder
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

package gant.tools

import org.codehaus.gant.GantState

/**
 *  A class providing methods support for processing LaTeX.
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
class LaTeX {
  protected final binding
  protected final executor
  public LaTeX ( final Binding binding ) {
    this.binding = binding
    executor = new Execute ( binding ) 
  }
  public final ltxExtension = '.ltx'
  public final texExtension = '.tex'
  public final dviExtension = '.dvi'
  public final epsExtension = '.eps'
  public final pdfExtension = '.pdf'
  public final psExtension = '.ps'
  public final auxExtension = '.aux'
  public final bblExtension = '.bbl'
  public final blgExtension = '.blg'
  public final idxExtension = '.idx'
  public final ilgExtension = '.ilg'
  public final indExtension = '.ind'
  public final logExtension = '.log'
  public final tocExtension = '.toc'
  public final pdfBookMarkExtension = '.out'
   //  As at r5438 super fails to work and so we cannot make this final, we have to make it accesible to subclasses.
  public intermediateExtensions = [
    auxExtension , dviExtension , logExtension , tocExtension ,
    bblExtension , blgExtension ,
    idxExtension , ilgExtension , indExtension ,
    pdfBookMarkExtension
    ]
  public environment = [
    latexCommand : 'pdflatex' ,
    latexOptions : [ ] ,
    bibtexCommand : 'bibtex' ,
    bibtexOptions : [ ] ,
    makeindexCommand : 'makeindex' ,
    makeindexOptions : [ ] ,
    dvipsCommand : 'dvips' ,
    dvipsOptions : [ ] ,
    ps2pdfCommand : 'ps2pdf' ,
    ps2pdfOptions : [ ] ,
    root : '' ,
    dependents : [ ]
    ]
  private void addOption ( key , option ) {
    if ( option instanceof List ) { environment[ key ] += option }
    else { environment[ key ] << option }
  }
  public void addLaTeXOption ( option ) { addOption ( 'latexOptions' , option ) }
  public void addBibTeXOption ( option ) { addOption ( 'bibtexOptions' , option ) }
  public void addMakeindexOption ( option ) { addOption ( 'makeindexOptions' , option ) }
  public void addDvipsOption ( option ) { addOption ( 'dvipsOptions' , option ) }
  public void addPs2pdfOption ( option ) { addOption ( 'ps2pdfOptions' , option ) }
  public void addOptions ( Map keywordOptions ) { keywordOptions.each { key , value -> addOption ( key , value ) } }
  private void executeLaTeX ( ) {
    def root = environment.root
    def sourceName = root + ltxExtension
    def sourceFile = new File ( sourceName )
    if ( ! sourceFile.exists ( ) ) {
      sourceFile = new File ( sourceName = root + texExtension )
      if ( ! sourceFile.exists ( ) ) { throw new FileNotFoundException ( "Neither ${root}.ltx or ${root}.tex exist." ) }
    }
    def targetExtension = environment.latexCommand == 'pdflatex' ? pdfExtension : dviExtension
    def targetName = root + targetExtension
    def targetFile = new File ( targetName )
    def needToUpdate = false
    if ( ! targetFile.exists ( ) ) { needToUpdate = true }
    else {
      ( environment.dependents + [ sourceName ] ).each { dependent ->
        if ( ! ( dependent instanceof File ) ) { dependent = new File ( dependent ) }
        if ( dependent.lastModified ( ) > targetFile.lastModified ( ) ) { needToUpdate = true }
      }
    }
    if ( needToUpdate ) {
      def latexAction = [ environment.latexCommand , *environment.latexOptions , sourceName ]
      def runLaTeX = { executor.executable ( latexAction ) }
      def conditionallyRunLaTeX = {
        def rerun = new File ( root + logExtension ).text =~ /(Warning:.*Rerun|Warning:.*undefined citations)/
        if ( rerun ) { runLaTeX ( ) }
        rerun
      }
      runLaTeX ( )
      def currentDirectory = new File ( '.' )
      def bibTeXRun = false
      currentDirectory.eachFileMatch ( ~/.*.aux/ ) { auxFile ->
        if ( auxFile.text =~ 'bibdata' ) {
          executor.executable ( [ environment.bibtexCommand , *environment.bibtexOptions , auxFile.name ] )
          bibTeXRun = true
        }
      }
      if ( bibTeXRun ) {
        runLaTeX ( )
        if ( conditionallyRunLaTeX ( ) ) { conditionallyRunLaTeX ( ) }
      }
      def makeindexRun = false
      currentDirectory.eachFileMatch ( ~/.*.idx/ ) { idxFIle ->
        executor.executable ( [ environment.bibtexCommand , *environment.bibtexOptions , idxFile.name ] )
        makeindexRun = true
      }
      if ( makeindexRun ) { runLaTeX ( ) }
      runLaTeX ( )
      if ( conditionallyRunLaTeX ( ) ) {
        if ( ! conditionallyRunLaTeX ( ) ) {
          throw new RuntimeException ( '#### Something SERIOUSLY Wrong. ###' )
        }
      }
    }
  }
  public void generatePDF ( Map arguments ) {
    arguments.each { key , value -> environment[ key ] = value }
    environment.latexCommand = 'pdflatex'
    executeLaTeX ( )
  }
  public void generatePS ( Map arguments ) {
    arguments.each { key , value -> environment[ key ] = value }
    environment.latexCommand = 'latex'
    executeLaTeX ( )
    def dviFile = new File ( environment.root + dviExtension )
    def psFile = new File ( environment.root + psExtension )
    if ( ( ! psFile.exists ( ) ) || ( dviFile.lastModified ( ) > psFile.lastModified ( ) ) ) {
      executor.executable ( [ environment.dvipsCommand , * environment.dvipsOptions , '-o' , psFile.name , dviFile.name ] )
    }
  }
}
