package sbe

//go:generate java -Dsbe.output.dir=. -Dsbe.target.language=golang -Dsbe.go.generate.generate.flyweights=true -jar ${SBE_JAR} ../resources/sbe-ir.xml
