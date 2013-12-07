Simple Binary Encoding (SBE)
============================

SBE is OSI layer 6 representation for encoding and decoding application messages in binary format for low-latency applications.

Further details on the background and usage of SBE can be found on the [Wiki](https://github.com/real-logic/simple-binary-encoding/wiki).

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

Directory Layout
----------------

Main source code

    src/main

Unit tests

    src/test

Examples of usage

    examples


Build
-----

Full clean build:

    $ ant

Run the Java examples

    $ ant examples:java

Distribution
------------

Jars for the executable, source, and javadoc can be found in

    target/dist

C++ Build
---------

NOTE: Linux and Mac OS only for the moment. See [FAQ](https://github.com/real-logic/simple-binary-encoding/wiki/Frequently-Asked-Questions).

Dependent build:

    $ ant cpp:test

If you have doxygen installed:

    $ ant cpp

Run the C++99 examples

    $ ant cpp:examples
