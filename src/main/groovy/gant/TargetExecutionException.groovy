package gant

/**
 *  Thrown when there is an error running a script.
 */
class TargetExecutionException extends GantException {
  TargetExecutionException ( ) { super ( ) }
  TargetExecutionException ( String msg ) { super ( msg ) }
  TargetExecutionException ( Exception e ) { super ( e ) }
  TargetExecutionException ( String msg , Exception e ) { super ( msg , e ) }
}
