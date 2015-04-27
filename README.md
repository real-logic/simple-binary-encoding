Simple Binary Encoding (SBE)
============================

[SBE](http://www.fixtradingcommunity.org/pg/file/fplpo/read/1196757/simple-binary-encoding-release-candidate-2) is OSI layer 6 presentation for encoding and decoding application messages in binary format for low-latency applications.

Further details on the background and usage of SBE can be found on the [Wiki](https://github.com/real-logic/simple-binary-encoding/wiki).

Benchmark tools and information can be found [here](https://github.com/real-logic/simple-binary-encoding/tree/master/perf) and run from he root perf-build.xml file.

An XSD for SBE specs can be found [here](https://github.com/real-logic/simple-binary-encoding/blob/master/main/resources/fpl/SimpleBinary1-0.xsd)

License (See LICENSE file for full license)
-------------------------------------------
Copyright 2013 Real Logic Limited

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Binaries
--------

Binaries and dependency information for Maven, Ivy, Gradle, and others can be found at [http://search.maven.org](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22uk.co.real-logic%22%20AND%20a%3A%22sbe%22).

Example for Maven:

```xml
<dependency>
    <groupId>uk.co.real-logic</groupId>
    <artifactId>sbe</artifactId>
    <version>1.0-RC2</version>
</dependency>
```

For .NET, you will find the [binaries on NuGet](http://www.nuget.org/packages/Adaptive.SBE/)

Search for 'SBE' in NuGet or run the following command line in the NuGet console

    Install-Package Adaptive.SBE


Directory Layout
----------------

Main source code

    main

Unit tests

    test

Examples of usage

    examples


Build
-----

Full clean build:

    $ ./gradlew

Run the Java examples

    $ ./gradlew runJavaExamples

Distribution
------------

Jars for the executable, source, and javadoc can be found in

    build/libs

Android Build
-------------

In order to build the android binaries you need to run the android:dist target 

    $ ant android:dist

As a prerequisite, you need Android SDK to be installed and the path needs to be configured inside `build-local.properties` file. 
You need at least Android target 19 (it comes with Java 7 support) and Android build tools version at least 20.0.0. E.g. 

    android.sdk.dir=/opt/android-sdk-linux
    android.target=android-19
    android.build.tools.version=20.0.0

Android jars for the codec, source, and javadoc can be found in

    target/dist

C++ Build using CMake
---------------------

NOTE: Linux, Mac OS, and Windows only for the moment. See [FAQ](https://github.com/real-logic/simple-binary-encoding/wiki/Frequently-Asked-Questions).
Windows builds have been tested with Visual Studio Express 12.

First build using gradle to generate the SBE jar.

    $ ./gradlew

Then build and test with CMake in the build subdirectory created by the gradle build.

    $ cd build
    $ cmake ..
    $ cmake --build .
    $ ctest

C# Build
--------

See [readme.md](https://github.com/real-logic/simple-binary-encoding/tree/master/vs2013) in vs2013 directory


