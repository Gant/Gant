gant.targets.tests.Clean_Test, gant.tools.tests.Execute_Test, gant.tools.tests.LaTeX_Test are coded for
versions of Groovy after 1.5.1.  There was a trivial and yet breaking change to the way in which things are
output in Groovy 1.5.2 and later.  In particular, strings are no longer embedded in double quotes on output.
This makes things more harmonious with the way Java does things.  Sadly though it means the three mentioned
tests have test method failures when using Groovy 1.0, 1.5.0 or 1.5.1.

Gant no longer builds against Groovy 1.0.  Groovy 1.0 does not have the joint Groovy/Java compiler to cope
with arbitrarily mixed Java and Groovy code -- this is needed to support the introduction of the Gant Ant
Task which is coded in Java but refers to the Gant main class which is coded in Groovy.  Also, Groovy 1.0
does not have the groovy.lang.GroovySystem class which is used to access various things related to
metaclasses.
