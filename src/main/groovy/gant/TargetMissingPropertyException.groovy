package gant

/**
 *  Thrown when an undefined property is referenced during target execution.
 */
class TargetMissingPropertyException extends GantException {
  TargetMissingPropertyException ( ) { super ( ) }
  TargetMissingPropertyException ( String msg ) { super ( msg ) }
  TargetMissingPropertyException ( MissingPropertyException e ) { super ( e ) }
  TargetMissingPropertyException ( String msg , MissingPropertyException e ) { super ( msg , e ) }
}
