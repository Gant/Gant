There are trivial and yet breaking changes to the way in which things are output in Groovy 1.6.x compared to
Groovy 1.5.x.  In particular, strings in lists are no longer embedded in double quotes on output -- this
makes things more harmonious with the way Java does things -- and the groovyc task no longer issues the
message "No sources to compile" when there are no sources to compile.  Sadly, it means some of the Gant
tests have to distinguish which version of Groovy is being used.  If support for Groovy 1.5.x is removed
then this code must be reviewed and the special code removed.
