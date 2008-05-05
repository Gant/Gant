There are trivial and yet breaking changes to the way in which things are output in Groovy 1.6.x compared
to Groovy 1.5.x.  In particular, strings are no longer embedded in double quotes on output -- this makes
things more harmonious with the way Java does things -- and the groovyc task no longer issues the message
"No sources to compile" when there are no sources to compile.  Sadly, it means some of the tests have to
distinguish which version of Groovy is being used.  If support for Groovy 1.5.x is removed then this code
must be reviewed and the special code removed.

Gant no longer builds against Groovy 1.0.  Groovy 1.0 does not have the joint Groovy/Java compiler to cope
with arbitrarily mixed Java and Groovy code -- this is needed to support the introduction of the Gant Ant
Task which is coded in Java but refers to the Gant main class which is coded in Groovy.  Also, Groovy 1.0
does not have the groovy.lang.GroovySystem class which is used to access various things related to
metaclasses.
