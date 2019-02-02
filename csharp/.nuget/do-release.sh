#! /usr/bin/env bash


# Ideally we'd get this from top level version.txt but this release is
# done outside the release process so we end up doing this by hand.
ROOTDIR=`dirname $0`
VERSIONTXT=`cat $ROOTDIR/../../version.txt`
VERSION=${VERSIONTXT%-SNAPSHOT} # Seems to be what's used

echo "Version check"
echo "version.txt=$VERSION"
echo "SBE.nuspec=`grep '<version>' SBE.nuspec`"
echo

# Copy in the jar
cp $ROOTDIR/../../sbe-tool/build/libs/sbe-tool-$VERSIONTXT.jar $ROOTDIR/sbe-tool-all.jar

# Build the nuget package
NUGET=$ROOTDIR/nuget.exe
if [ ! -f $NUGET ]; then echo nuget.exe not found. Obtain it from https://dist.nuget.org/index.html; exit 1; fi

$NUGET pack $ROOTDIR/sbe.nuspec


