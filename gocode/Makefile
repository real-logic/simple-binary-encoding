ROOT=..
SBE_TOOL_VERSION=$(shell cat $(ROOT)/version.txt)
SBE_JAR=$(ROOT)/sbe-all/build/libs/sbe-all-${SBE_TOOL_VERSION}.jar

#JAVA=/usr/lib/jvm/java-8-openjdk-amd64/bin/java
JAVA=java

TESTDIR=$(ROOT)/sbe-tool/src/test/golang

JAVA_FILES="-jar ${SBE_JAR}"

# FIXME: Go doesn't like relative paths so us a gnumake extension for now
GOPATH=$(realpath $(ROOT)/gocode)
OUTPUTDIR=$(GOPATH)/src
XMLLOCAL=$(GOPATH)/resources

# Local examples to get started
SIMPLE=$(XMLLOCAL)/simple.xml
COMPOSITE=$(XMLLOCAL)/example-composite.xml

# Existing test cases
XMLTESTDIR=$(ROOT)/sbe-tool/src/test/resources
BASIC_TYPES=$(XMLTESTDIR)/basic-types-schema.xml
BASIC_GROUP=$(XMLTESTDIR)/basic-group-schema.xml
BASIC_VARDATA=$(XMLTESTDIR)/basic-variable-length-schema.xml
COMPOSITE4=$(XMLTESTDIR)/composite-elements-schema-rc4.xml
NESTED_GROUP=$(XMLTESTDIR)/group-with-data-schema.xml
FIX_BINARY=$(XMLTESTDIR)/FixBinary.xml

SCHEMA_DIR=$(ROOT)/sbe-samples/src/main/resources
EXAMPLES_SCHEMA=$(SCHEMA_DIR)/example-schema.xml
EXTENSION_SCHEMA=$(SCHEMA_DIR)/example-extension-schema.xml
EXTENSION2_SCHEMA=$(XMLLOCAL)/example-extension-2-schema.xml
BIGENDIAN_SCHEMA=$(XMLLOCAL)/example-bigendian.xml
GROUP_EXTENSION=$(XMLLOCAL)/group-with-data-extension-schema.xml

# Convenience during development
#SAVE_FORMAT=mkdir -p fmt && cp *.go fmt &&
SAVE_FORMAT=

$(OUTPUTDIR)/example-schema/example-schema: $(SBE_JAR) $(OUTPUTDIR)/baseline/SbeMarshalling.go $(OUTPUTDIR)/baseline-bigendian/SbeMarshalling.go $(OUTPUTDIR)/extension/SbeMarshalling.go $(OUTPUTDIR)/extension2/SbeMarshalling.go $(OUTPUTDIR)/example-schema/CarExample.go $(OUTPUTDIR)/example-schema/CarExample_test.go
	(export GOPATH=$(GOPATH) && \
	cd $(OUTPUTDIR)/example-schema && \
	go build && \
	go fmt && \
	./example-schema)

clean:
	rm -f $(OUTPUTDIR)/*/SbeMarshalling.go $(OUTPUTDIR)/*/*/SbeMarshalling.go

$(OUTPUTDIR)/baseline/SbeMarshalling.go: $(SBE_JAR)
	$(JAVA) \
	-Dsbe.output.dir=$(OUTPUTDIR) \
	-Dsbe.generate.ir="false" \
	-Dsbe.target.language="golang" \
	-jar ${SBE_JAR} \
	$(EXAMPLES_SCHEMA)
	(export GOPATH=$(GOPATH) && \
        cd $(OUTPUTDIR)/baseline && \
	go build && \
	go fmt && \
	go test && \
	go install)

$(OUTPUTDIR)/baseline-bigendian/SbeMarshalling.go: $(SBE_JAR)
	$(JAVA) \
	-Dsbe.output.dir=$(OUTPUTDIR) \
	-Dsbe.generate.ir="false" \
	-Dsbe.target.language="golang" \
	-jar ${SBE_JAR} \
	$(BIGENDIAN_SCHEMA)
	(export GOPATH=$(GOPATH) && \
        cd $(OUTPUTDIR)/baseline-bigendian && \
	go build && \
	go fmt && \
	go test && \
	go install)

$(OUTPUTDIR)/extension/SbeMarshalling.go: $(SBE_JAR)
	$(JAVA) \
	-Dsbe.output.dir=$(OUTPUTDIR) \
	-Dsbe.generate.ir="false" \
	-Dsbe.target.language="golang" \
	-jar ${SBE_JAR} \
	$(EXTENSION_SCHEMA) && \
	(export GOPATH=$(GOPATH) && \
        cd $(OUTPUTDIR)/extension && \
	go build && \
	go install)

$(OUTPUTDIR)/extension2/SbeMarshalling.go: $(SBE_JAR)
	$(JAVA) \
	-Dsbe.output.dir=$(OUTPUTDIR) \
	-Dsbe.generate.ir="false" \
	-Dsbe.target.language="golang" \
	-jar ${SBE_JAR} \
	$(EXTENSION2_SCHEMA) && \
	(export GOPATH=$(GOPATH) && \
        cd $(OUTPUTDIR)/extension2 && \
	go build && \
	go install)

bench: $(SBE_JAR) $(OUTPUTDIR)/example-schema/example-schema
	(export GOPATH=$(GOPATH) && \
        cd $(OUTPUTDIR)/example-schema && \
	go test --bench . -cpuprofile=cpu.out && \
	go tool pprof -text example-schema.test cpu.out)

test: fix-binary nested-group nested-group-extension basic-vardata basic-group basic-types simple composite composite-elements

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
	(export GOPATH=$(GOPATH) && \
	cd $(OUTPUTDIR)/mktdata && \
	go build && \
	$(SAVE_FORMAT) \
	go fmt && echo && \
	go test)

# Note this one installs so it can be used in the extension below
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
	(export GOPATH=$(GOPATH) && \
	cd $(OUTPUTDIR)/group_with_data && \
	go build && \
	$(SAVE_FORMAT) \
	go fmt && echo && \
	go install && \
	go test)

nested-group-extension:
	$(JAVA) \
	-Dsbe.output.dir=$(OUTPUTDIR) \
	-Dsbe.target.language=golang \
	-Dfile.encoding=US-ASCII \
	-Duser.country=US \
	-Duser.langimuage=en \
	-Duser.variant \
	-jar ${SBE_JAR} \
	$(GROUP_EXTENSION)
	(export GOPATH=$(GOPATH) && \
	cd $(OUTPUTDIR)/group_with_data_extension && \
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
	(export GOPATH=$(GOPATH) && \
	cd $(OUTPUTDIR)/composite_elements && \
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
	(export GOPATH=$(GOPATH)/vardata && \
	cd $(OUTPUTDIR)/vardata/'SBE tests' && \
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
	(export GOPATH=$(GOPATH)/group && \
	cd $(OUTPUTDIR)/group/'SBE tests' && \
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
	(export GOPATH=$(GOPATH)/basic && \
	cd $(OUTPUTDIR)/basic/'SBE tests' && \
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
	(export GOPATH=$(GOPATH) && \
	cd $(OUTPUTDIR)/composite && \
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
	(export GOPATH=$(GOPATH) && \
	cd $(OUTPUTDIR)/simple && \
	go build && \
	$(SAVE_FORMAT) \
	go fmt && echo && \
	go test)
