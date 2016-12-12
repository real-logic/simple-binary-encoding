Overview
========

The generated golang code attempts to match golang idioms where such
choices are both possible and reasonable.

The golang SBE backend represents FIX messages as golang types and
provides Encode and Decode methods for these types and their embedded
elements. This differs from the Java and C++ implementations that provide a
flyweight idiom and has some important ramifications.

Firstly, any semantic checking of values is not performed by a
Getter/Setter method but is instead performed in the Encode and Decode
<<<<<<< HEAD
methods. In general the Encode methods attempt to be strictly and
=======
methods. In general the Encode methods attempt to be stricty and
>>>>>>> e5b9a0dda1fbbf83172d5653e32637d48ec6fdad
enforcing to ensure only valid SBE is emitted. Where it remains within
the standard the Decode methods attempt to be forgiving in their
interpretation.

Secondly, constant values are not set when an object is created. As
golang does not provide constructors, we instead have each support
type Foo provide a FooInit() function which will fill out constant
values.

Some design decisions are based around the structure of sbe-tool
itself. sbe-tool parses the XML into an internal representation (IR)
and then passes this to the language specific generator. As a result
some information on the xml structure has been lost.

An examples of this include the use of the <ref> tag which if used to
the same underlying type in a composite will create two differently
named definitions of the type in the IR.

<<<<<<< HEAD
A second example is if a field references a type definition that is
=======
A second exmaple is if a field references a type definition that is
>>>>>>> e5b9a0dda1fbbf83172d5653e32637d48ec6fdad
primitive then we lose the type name in the IR so it is unreferenced.


Primitive Types
---------------
Most Primitive types map well to golang basic types. Character arrays
<<<<<<< HEAD
are represented as either fixed or variable sized byte arrays.

[FIXME: Unicode]
=======
are represented as either fixed or vriable sized byte arrays.

[FIXME: unicode]
>>>>>>> e5b9a0dda1fbbf83172d5653e32637d48ec6fdad

Enumerations and Choices
------------------------
golang does not have an enumeration type and so enums are represented
as a type and a variable with the known constant values is
provided. To preserve name uniqueness within a message the const
values contain the enum type name.

Messages and Composites
-----------------------
Messages and composites are both represented as golang structures with
Encode() and Decode() methods amongst others.

Groups and VarData
------------------
Groups and VarData are represented as arrays with their NumInGroup
being implicit in len()

Semantic Types
--------------

The Boolean semantic type is not represented as a golang bool as the
spec requires that discrepancies between SinceVersion and
ActingVersion can be resolved using the Null Value.

The String semantic type is simply represented as a byte array as
golang Strings are immutable and hence they are not useful in
structures.

[FIXME: insert more here]


Code Layout
-----------
To match golang's requirement on directory structure and file layout,
<<<<<<< HEAD
the build environment, example and test code are structured into a top
level gocode directory hierarchy.
=======
the build enevinment, example and test code are structured into a top
level gocode directory heirarchy.
>>>>>>> e5b9a0dda1fbbf83172d5653e32637d48ec6fdad

Code is generated into this structure with pre-existing test code in place.

For the example, the code is generated into a library and the matching
example code lives in it's own directory at the same level. For
<<<<<<< HEAD
example the example-schema generates the baseline library code into
=======
example teh example-schema generates the baseline library code into
>>>>>>> e5b9a0dda1fbbf83172d5653e32637d48ec6fdad
gocode/src/baseline and example code lives in gocode/src/example-schema.

To use this layout you will need to set GOPATH=/path/to/gocode or use
the supplied Makefile which does this for you.

Roadmap
-------
 * Deprecated support
 * Support full Range checking on both Encode and Decode
 * Car example-extension-schema works
 * Examples documented
 * Restructure recursive group generation as it's currently somewhat broken
 * Chase up Ennn error message meanings with Real Logic and support?
 * go format fixes
<<<<<<< HEAD
 * Windows developer support (currently tested on Linux/MacOS)
 * Enhance Design/Rationale document (this one)
=======
 * Enhaance Design/Rationale document (this one)
>>>>>>> e5b9a0dda1fbbf83172d5653e32637d48ec6fdad
 * presence=optional
 * Unnecessary code removal (e.g., GroupSizeEncoding)
 * Unicode
 * Testing/Bug fixes
 * Semantic types (perhaps just date/time, possibly more)
 * Benchmarking & Performance Enhancements
