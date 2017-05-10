#! /usr/bin/env bash

# Ideally we'd get this from top level version.txt but this release is done outside
# the release process so we end up doing this by hand. During dev this works.
ROOTDIR=`dirname $0`
VERSIONTXT=`cat $ROOTDIR/../../version.txt`
VERSION=${VERSIONTXT%-SNAPSHOT} # Seems to be what's used

# Write the version info into the assembly
# Note we're using 0.VersionNumber during alpha/beta
ASSEMBLYINFO=$ROOTDIR/../GlobalAssemblyInfo.cs
(cat << @EOF
[assembly: System.Reflection.AssemblyCopyright("Copyright © 2017 MarketFactory Inc. Copyright © Adaptive 2014. All rights reserved.")]
#if DEBUG
[assembly: System.Reflection.AssemblyDescription("Debug")]
#else
[assembly: System.Reflection.AssemblyDescription("Release")]
#endif
[assembly: System.Reflection.AssemblyVersion("VERSION")]
[assembly: System.Reflection.AssemblyFileVersion("VERSION")]
[assembly: System.Reflection.AssemblyInformationalVersion("VERSION")]
@EOF
)| sed -e "s+VERSION+0.$VERSION+g" > $ASSEMBLYINFO.new

# Copy in if new
if cmp $ASSEMBLYINFO $ASSEMBLYINFO.new
then
    rm $ASSEMBLYINFO.new
else
    cp $ASSEMBLYINFO.new $ASSEMBLYINFO
fi

# Copy in the jar
cp $ROOTDIR/../../sbe-tool/build/libs/sbe-tool-$VERSIONTXT-all.jar $ROOTDIR/sbe-tool-all.jar

# Build the nuget package
NUGET=$ROOTDIR/../packages/NuGet.CommandLine.3.5.0/tools/NuGet.exe
$NUGET pack $ROOTDIR/sbe.nuspec


