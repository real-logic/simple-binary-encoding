Simple Binary Encoding (SBE)
============================

[SBE](https://github.com/FIXTradingCommunity/fix-simple-binary-encoding) is an OSI layer 6 presentation for 
encoding and decoding binary application messages for low-latency financial applications. This repository contains 
the reference implementations in Java and C++.

Further details on the background and usage of SBE can be found on the
[Wiki](https://github.com/real-logic/simple-binary-encoding/wiki).

An XSD for SBE specs can be found
[here](https://github.com/real-logic/simple-binary-encoding/blob/master/sbe-tool/src/main/resources/fpl/SimpleBinary1-0.xsd)

For the latest version information and changes see the [Change Log](https://github.com/real-logic/simple-binary-encoding/wiki/Change-Log). 

This SBE implementation is designed with work very efficiently with the [Aeron](https://github.com/real-logic/Aeron) 
messaging system for low-latency and high-throughput communications. The generated codec stubs can wrap the Aeron buffers
directly for zero copy semantics. 

License (See LICENSE file for full license)
-------------------------------------------
Copyright 2014 - 2016 Real Logic Limited

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
[http://search.maven.org](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22uk.co.real-logic%22%20AND%20a%3A%22sbe%22).

Example for Maven:

```xml
<dependency>
    <groupId>uk.co.real-logic</groupId>
    <artifactId>sbe-all</artifactId>
    <version>1.3.5-RC3</version>
</dependency>
```


Directory Layout
----------------
Main source code

    sbe-tool/src/main

Unit tests

    sbe-tool/src/test

Examples of usage

    sbe-samples/src/main


Build
-----
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

First build using gradle to generate the SBE jar.

    $ ./gradlew

For convenience, a script is provided that does a full clean, build, and test of all targets as a Release build.

    $ ./cppbuild/cppbuild

If you are comfortable with using CMake, then a full clean, build, and test looks like:

    $ mkdir -p cppbuild/Debug
    $ cd cppbuild/Debug
    $ cmake ../..
    $ cmake --build . --clean-first
    $ ctest



