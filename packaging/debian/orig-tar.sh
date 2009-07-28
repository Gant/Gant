#!/bin/sh -e

# called by uscan with '--upstream-version' <version> <file>
DIR=gant-$2.orig

# clean up the upstream tarball
tar zxf $3
mv gant-$2 $DIR
GZIP=--best tar -c -z -f $3 --exclude '*.jar' $DIR
rm -rf $DIR

# move to directory 'tarballs'
if [ -r .svn/deb-layout ]; then
  . .svn/deb-layout
  mv $3 $origDir
  echo "moved $3 to $origDir"
fi
