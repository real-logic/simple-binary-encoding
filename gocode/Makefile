ROOT=..
SBE_TOOL_VERSION=$(shell cat $(ROOT)/version.txt)
SBE_JAR=$(ROOT)/sbe-all/build/libs/sbe-all-${SBE_TOOL_VERSION}.jar

# FIXME: Go doesn't like relative paths so us a gnumake extension for now
GOPATH=$(realpath $(ROOT)/gocode)

# GO111MODULE=off is required for building with Go 1.16
# GOPATH-style support will be removed in Go 1.17
export GO111MODULE=off

# Convenience during development
#SAVE_FORMAT=mkdir -p fmt && cp *.go fmt &&
SAVE_FORMAT=

all: example test # bench


# This target is used to build golang files using parent gradle
# script's invocation of sbe-tool in case it needs to be run again. We
# use this one output file to check that dependency as it's simple
DEP=struct/src/simple/SbeMarshalling.go
$(DEP): $(SBE_JAR)
	(cd ..; ./gradlew generateGolangCodecs)
	(export GOPATH=$(GOPATH)/struct && \
	cd struct/src/simple && \
	go build && \
	$(SAVE_FORMAT) \
	go fmt && \
	go test)

# Will regenerate codecs by removing dependencies
clean:
	rm -f struct/src/*/SbeMarshalling.go struct/src/*/*/SbeMarshalling.go

# Example code and benchmarking
example: struct/src/example-schema/example-schema
struct/src/example-schema/example-schema: $(DEP)
	(export GOPATH=$(GOPATH)/struct && \
	cd struct/src/example-schema && \
	go build && \
	go fmt && \
	./example-schema)

bench: $(DEP)
	(export GOPATH=$(GOPATH)/struct && \
        cd struct/src/example-schema && \
	go test --bench . -cpuprofile=cpu.out && \
	go tool pprof -text example-schema.test cpu.out)

struct/src/baseline/SbeMarshalling.go: $(DEP)
	$(GOPATH)/sbe-tool -d src -s $(EXAMPLES_SCHEMA)
	(export GOPATH=$(GOPATH)/struct && \
        cd struct/src/baseline && \
	go build && \
	go fmt && \
	go test && \
	go install)

# The first set does a make install as there is a test that uses
# multiple packages and needs them in GOPATH The second set work in
# struct/src/foo, and the third need a GOPATH addition as for Java and C++
# they generate into the same directory but golang doesn't allow that
test: $(DEP)
	(export GOPATH=$(GOPATH)/struct && \
		(for t in baseline extension; do \
		export GOPATH=$(GOPATH)/struct && \
	        cd $(GOPATH)/struct/src/$$t && \
		go build && \
		go fmt && \
		go test && \
		go install \
		;done))
	(export GOPATH=$(GOPATH)/struct && \
		(for t in baseline-bigendian mktdata group_with_data group_with_data_extension composite_elements composite since-deprecated simple issue435 issue472 issue483 issue488 issue560 issue847 issue848 issue849 issue505 test973; do \
		cd $(GOPATH)/struct/src/$$t && \
		go build && \
		go fmt && \
		go test \
		;done))
	(for t in vardata group basic; do \
		export GOPATH=$(GOPATH)/struct/$$t && \
		cd $(GOPATH)/struct/src/$$t/'SBE tests' && \
		go build && \
		go fmt && \
		go test \
		;done)
	(export GOPATH=$(GOPATH)/flyweight && \
		(for t in baseline extension; do \
		export GOPATH=$(GOPATH)/flyweight && \
	        cd $(GOPATH)/flyweight/src/$$t && \
		go build && \
		go fmt && \
		go test && \
		go install \
		;done))
	(export GOPATH=$(GOPATH)/flyweight && \
		(for t in baseline-bigendian mktdata group_with_data group_with_data_extension composite_elements composite since-deprecated simple issue435 issue472 issue483 issue488 issue560 issue847 issue848 issue849; do \
		cd $(GOPATH)/flyweight/src/$$t && \
		go build && \
		go fmt && \
		go test \
		;done))
	(for t in vardata group basic; do \
		export GOPATH=$(GOPATH)/flyweight/$$t && \
		cd $(GOPATH)/flyweight/src/$$t/'SBE tests' && \
		go build && \
		go fmt && \
		go test \
		;done)
	(export SBE_JAR=$(SBE_JAR) && \
	 export GO111MODULE=on && \
		(cd $(GOPATH)/flyweight/src/otf && \
			go generate && \
			go build && \
			go fmt && \
			go test))
