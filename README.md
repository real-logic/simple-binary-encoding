Simple Binary Encoding (SBE)
============================

[SBE](https://github.com/FIXTradingCommunity/fix-simple-binary-encoding) is an OSI layer 6 presentation for 
encoding and decoding binary application messages for low-latency financial applications. This repository contains 
the reference implementations in Java, C++, Golang, and C#.

Further details on the background and usage of SBE can be found on the
[Wiki](https://github.com/real-logic/simple-binary-encoding/wiki).

An XSD for SBE specs can be found
[here](https://github.com/real-logic/simple-binary-encoding/blob/master/sbe-tool/src/main/resources/fpl/sbe.xsd)

For the latest version information and changes see the [Change Log](https://github.com/real-logic/simple-binary-encoding/wiki/Change-Log) with **downloads** at [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Csbe). 

The Java and C++ SBE implementations are designed with work very efficiently with the
[Aeron](https://github.com/real-logic/Aeron) messaging system for low-latency and
high-throughput communications. The Java SBE implementation has a dependency on
[Agrona](https://github.com/real-logic/Agrona) for its buffer implementations.

License (See LICENSE file for full license)
-------------------------------------------
Copyright 2014 - 2017 Real Logic Limited  
Copyright 2017 MarketFactory Inc

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
Binaries and dependency information for Maven, Ivy, Gradle, and others can be found at 
[http://search.maven.org](http://search.maven.org/#search%7Cga%7C1%7Csbe).

Example for Maven:

```xml
<dependency>
    <groupId>uk.co.real-logic</groupId>
    <artifactId>sbe-all</artifactId>
    <version>1.7.0</version>
</dependency>
```


Directory Layout
----------------
Main source code

    sbe-tool/src/main

Unit tests

    sbe-tool/src/test

Samples of usage

    sbe-samples/src/main


Build
-----

The project is built with [Gradle](http://gradle.org/) using this [build.gradle](https://github.com/real-logic/simple-binary-encoding/blob/master/build.gradle) file.

Full clean build:

    $ ./gradlew

Run the Java examples

    $ ./gradlew runJavaExamples


Distribution
------------
Jars for the executable, source, and javadoc for the various modules can be found in

    <module>/build/libs

C++ Build using CMake
---------------------
NOTE: Linux, Mac OS, and Windows only for the moment. See
[FAQ](https://github.com/real-logic/simple-binary-encoding/wiki/Frequently-Asked-Questions).
Windows builds have been tested with Visual Studio Express 12.

First build using Gradle to generate the SBE jar.

    $ ./gradlew

For convenience, a script is provided that does a full clean, build, and test of all targets as a Release build.

    $ ./cppbuild/cppbuild

If you are comfortable with using CMake, then a full clean, build, and test looks like:

    $ mkdir -p cppbuild/Debug
    $ cd cppbuild/Debug
    $ cmake ../..
    $ cmake --build . --clean-first
    $ ctest

Golang Build
------------

First build using Gradle to generate the SBE jar and then use it to
generate the golang code for testing

    $ ./gradlew
    $ ./gradlew generateGolangCodecs

For convenience on Linux, a gnu Makefile is provided that runs some
tests and containes some examples

    $ cd gocode
    # make # test, examples, bench

Users of golang generated code should see the [user
documentation](https://github.com/real-logic/simple-binary-encoding/wiki/Golang-User-Guide).

Developers wishing to enhance the golang generator should see the [developer
documentation](https://github.com/real-logic/simple-binary-encoding/blob/master/gocode/README.md)

C# Build
--------
As of May 2017, the csharp build is considered a preview release. API stability is not yet guaranteed. User and Developer guides are not yet released or are incomplete.

First build using Gradle to generate the SBE jar and then use it to
generate the C# code used for testing and the examples.

    $ ./gradlew
    $ ./gradlew generateCSharpCodecs

You can then use the [Visual Studio 2017 Community solution](https://github.com/real-logic/simple-binary-encoding/blob/master/csharp/csharp.sln) to build and explore the
example. This solution also builds some tests which can be run via the provided
[runtests.sh](https://github.com/real-logic/simple-binary-encoding/blob/master/csharp/runtests.sh) script.

Users of csharp generated code should see the [user documentation (coming)](https://github.com/real-logic/simple-binary-encoding/wiki/Csharp-User-Guide).

Developers wishing to enhance the csharp generator should see the [developer documentation (coming)](https://github.com/real-logic/simple-binary-encoding/blob/master/csharp/README.md)
