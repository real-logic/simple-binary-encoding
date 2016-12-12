ROOT=..
SBE_TOOL_VERSION=`cat $(ROOT)/version.txt`
SBE_JAR=$(ROOT)/sbe-all/build/libs/sbe-all-${SBE_TOOL_VERSION}.jar

SCHEMA_DIR=$(ROOT)/sbe-samples/src/main/resources
EXAMPLES_SCHEMA=$(SCHEMA_DIR)/example-schema.xml
EXAMPLES_OUTPUT=EXAMPLES_OUTPUT

#JAVA=/usr/lib/jvm/java-8-openjdk-amd64/bin/java
JAVA=java

TESTDIR=$(ROOT)/sbe-tool/src/test/golang

JAVA_FILES="-jar ${SBE_JAR}"

# During development we can speed up a build by selective building and
# using this but it needs periodic refresh for agrona version
#JAVA_FILES="-cp ${$CP}"
#CP=$(ROOT)/sbe-all/build/classes/main:$(ROOT)/sbe-all/build/resources/main:$(ROOT)/sbe-tool/build/libs/sbe-tool-$(SBE_TOOL_VERSION).jar:/home/bill/.gradle/caches/modules-2/files-2.1/org.agrona/Agrona/0.5.5/5a576d17c57c5c85b9a1ce19f030746e214e281b/Agrona-0.5.5.jar

# FIXME: Go doesn't like relative paths so us a gnumake extension for now
GOPATH=$(realpath $(ROOT)/gocode)
OUTPUTDIR=$(GOPATH)/src

# Local examples to get started
SIMPLE=$(GOPATH)/resources/simple.xml
COMPOSITE=$(GOPATH)/resources/example-composite.xml

# Existing test cases
XMLTESTDIR=$(ROOT)/sbe-tool/src/test/resources
BASIC_TYPES=$(XMLTESTDIR)/basic-types-schema.xml
BASIC_GROUP=$(XMLTESTDIR)/basic-group-schema.xml
BASIC_VARDATA=$(XMLTESTDIR)/basic-variable-length-schema.xml
COMPOSITE4=$(XMLTESTDIR)/composite-elements-schema-rc4.xml
NESTED_GROUP=$(XMLTESTDIR)/group-with-data-schema.xml
FIX_BINARY=$(XMLTESTDIR)/FixBinary.xml

# Convenience during development
#SAVE_FORMAT=mkdir -p fmt && cp *.go fmt &&
SAVE_FORMAT=


GeneratedStubExample: $(STUB_EXAMPLE)
	$(JAVA) \
	-Dsbe.output.dir=$(OUTPUTDIR) \
	-Dsbe.generate.ir="false" \
	-Dsbe.target.language="golang" \
	-jar ${SBE_JAR} \
	$(EXAMPLES_SCHEMA)
	(export GOPATH=$(GOPATH) && \
        cd $(OUTPUTDIR)/baseline && \
	go build && \
	go test && \
	go install && \
	cd $(OUTPUTDIR)/example-schema && \
	go build && \
	./example-schema)

test: fix-binary nested-group basic-vardata basic-group basic-types simple composite composite-elements

fix-binary:
	$(JAVA) \
	-Dsbe.output.dir=$(OUTPUTDIR) \
	-Dsbe.target.language=golang \
	-Dfile.encoding=US-ASCII \
	-Duser.country=US \
	-Duser.langimuage=en \
	-Duser.variant \
	-jar ${SBE_JAR} \
	$(FIX_BINARY)
	(cd $(OUTPUTDIR)/mktdata && \
	go build && \
	$(SAVE_FORMAT) \
	go fmt && echo && \
	go test)

nested-group:
	$(JAVA) \
	-Dsbe.output.dir=$(OUTPUTDIR) \
	-Dsbe.target.language=golang \
	-Dfile.encoding=US-ASCII \
	-Duser.country=US \
	-Duser.langimuage=en \
	-Duser.variant \
	-jar ${SBE_JAR} \
	$(NESTED_GROUP)
	(cd $(OUTPUTDIR)/group_with_data && \
	go build && \
	$(SAVE_FORMAT) \
	go fmt && echo && \
	go test)

composite-elements:
	$(JAVA) \
	-Dsbe.output.dir=$(OUTPUTDIR) \
	-Dsbe.target.language=golang \
	-Dfile.encoding=US-ASCII \
	-Duser.country=US \
	-Duser.langimuage=en \
	-Duser.variant \
	-jar ${SBE_JAR} \
	$(COMPOSITE4)
	(cd $(OUTPUTDIR)/composite_elements && \
	go build && \
	$(SAVE_FORMAT) \
	go fmt && echo && \
	go test)

basic-vardata:
	$(JAVA) \
	-Dsbe.output.dir=$(OUTPUTDIR)/vardata \
	-Dsbe.target.language=golang \
	-Dfile.encoding=US-ASCII \
	-Duser.country=US \
	-Duser.langimuage=en \
	-Duser.variant \
	-jar ${SBE_JAR} \
	$(BASIC_VARDATA)
	(cd $(OUTPUTDIR)/vardata/'SBE tests' && \
	go build && \
	$(SAVE_FORMAT) \
	go fmt && echo && \
	go test)

basic-group:
	$(JAVA) \
	-Dsbe.output.dir=$(OUTPUTDIR)/group \
	-Dsbe.target.language=golang \
	-Dfile.encoding=US-ASCII \
	-Duser.country=US \
	-Duser.language=en \
	-Duser.variant \
	-jar ${SBE_JAR} \
	$(BASIC_GROUP)
	(cd $(OUTPUTDIR)/group/'SBE tests' && \
	go build && \
	$(SAVE_FORMAT) \
	go fmt && echo && \
	go test)

basic-types:
	$(JAVA) \
	-Dsbe.output.dir=$(OUTPUTDIR)/basic \
	-Dsbe.target.language=golang \
	-Dfile.encoding=US-ASCII \
	-Duser.country=US \
	-Duser.language=en \
	-Duser.variant \
	-jar ${SBE_JAR} \
	$(BASIC_TYPES)
	(cd $(OUTPUTDIR)/basic/'SBE tests' && \
	go build && \
	$(SAVE_FORMAT) \
	go fmt && echo && \
	go test)


composite:
	$(JAVA) \
	-Dsbe.output.dir=$(OUTPUTDIR) \
	-Dsbe.target.language=golang \
	-Dfile.encoding=US-ASCII \
	-Duser.country=US \
	-Duser.language=en \
	-Duser.variant \
	-jar ${SBE_JAR} \
	$(COMPOSITE)
	(cd $(OUTPUTDIR)/composite && \
	go build && \
	$(SAVE_FORMAT) \
	go fmt && echo && \
	go test)

simple:
	$(JAVA) \
	-Dsbe.output.dir=$(OUTPUTDIR) \
	-Dsbe.target.language=golang \
	-Dfile.encoding=US-ASCII \
	-Duser.country=US \
	-Duser.language=en \
	-Duser.variant \
	-jar ${SBE_JAR} \
	$(SIMPLE)
	(cd $(OUTPUTDIR)/simple && \
	go build && \
	$(SAVE_FORMAT) \
	go fmt && echo && \
	go test)
