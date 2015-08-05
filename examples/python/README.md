Simple test programs demonstrating encoding and decoding with python

based on work originally by Mark McIlroy (https://github.com/mmcilroy/sbe_tests)

First build SBE as usual \(see [README.md](https://github.com/real-logic/simple-binary-encoding)\)
```
# SBE Jar file will be in simple-binary-encoding/build/libs
#
SBE_JAR=some/path/to/sbe.jar
SCHEMA_XML=../resources/example-schema.xml

# generate python code
java -Dsbe.target.language=python -jar ${SBE_JAR} ${SCHEMA_XML}
echo "" > baseline/__init__.py

# encode car using python
python encode_example.py

# decode car using python
python decode_example.py

```
