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
methods. In general the Encode methods attempt to be strictly
enforcing to ensure only valid SBE is emitted. Where it remains within
the standard the Decode methods attempt to be forgiving in their
interpretation.

To maintain consistency of datastreams, `Encode()` will fail without
writing to the stream, and `Decode()` will fail but will have read (and
where possible decoded) an entire message from the stream.

Secondly, constant values are not set when an object is created. As
golang does not provide constructors, we instead have each support
type `Foo` provide a `FooInit()` function which will fill out constant
values. Decoding automatically fills out constant values.

Some design decisions are based around the structure of sbe-tool
itself. sbe-tool parses the XML into an internal representation (IR)
and then passes this to the language specific generator. As a result
some information on the xml structure has been lost.

An examples of this include the use of the `<ref>` tag which if used to
the same underlying type in a composite will create two differently
named definitions of the type in the IR.

A second example occurs where a field references an aliased primitive
type definition in which case we lose the aliased type name in the IR
as it is unreferenced.

Code Layout
-----------
To match golang's requirement on directory structure and file layout,
the build environment, example and test code are structured into a top
level gocode directory hierarchy.

Code is generated into this structure with pre-existing test code in place.

For the example, the code is generated into a library and the matching
example code lives in it's own directory at the same level. For
example, the example-schema generates the baseline library code into
`gocode/src/baseline` and example code lives in `gocode/src/example-schema`.

To use this layout you should `set GOPATH=/path/to/gocode` or use the
supplied Makefile which does this for you. For the tests you will need
to not have `GOPATH` set or use the supplied Makefile which does this
for you.

Alpha Code Warning
------------------
The golang generator is still under development and the provided APIs
are still subject to change. Some design decisions remain
open. Currently the Car example (and extension) works but this is not
an exhaustive set of features.


Design Notes
============

Primitive Types
---------------
Most Primitive types map well to golang basic types. Character arrays
are represented as either fixed or variable sized byte arrays.

Unicode
-------
The SBE spec allows character arrays and vardata elements to specify a
character encoding such as "ASCII" or "UTF-8". For vardata this is
additionally allowed on integral types such as uint8. The spec does
not mandate a set of supported encodings but golang currently supports:
 * ASCII
 * UTF-8

Enumerations and Choices
------------------------
golang does not have an enumeration type and so enums are represented
as a type and a variable with the known constant values is
provided. To preserve name uniqueness within a message the const
values contain the enum type name.

Range Checking of enumeration values is only performed when the
schemaVersion is equal to or greater than the actingVersion so that
new enumeration values are allowed to propogate to older decoders.

Messages and Composites
-----------------------
Messages and composites are both represented as golang structures with
`Encode()` and `Decode()` methods amongst others.

Groups and VarData
------------------
Groups and VarData are represented as arrays with their NumInGroup
being implicit in `len()`

Semantic Types
--------------
The golang generator provides no special support for semantic
types (as for Java and C++).

The `Boolean` semantic type is not represented as a golang `bool` as the
spec requires that discrepancies between SinceVersion and
ActingVersion can be resolved using the NullValue.

The `String` semantic type is simply represented as a byte array as
golang Strings are immutable and are not useful in structures.

The `UTCTimestamp`, `UTCTimeOnly`, `UTCDateOnly` and `MktTime` semantic types
are not represented as a golang `Time` type as none of these semantic
types contain a location. Mapping to a golang `Time` type will be
relatively easy for the application developer who will know the
location and can set it accordingly (null is UTC). They can also
decide what to with golang `Time`'s nanosecond timer when converting
(probably treating it as zero).

Roadmap
=======
 * Examples documented
 * Windows developer support (currently tested on Linux/MacOS)
 * Enhance Design/Rationale document (this one)
 * presence=optional
 * Unnecessary code removal (e.g., GroupSizeEncoding)
 * Further Unicode support
 * Testing/Bug fixes
 * Benchmarking & Performance Enhancements
