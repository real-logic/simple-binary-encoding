module github.com/real-logic/simple-binary-encoding/otf/test

require (
	github.com/real-logic/simple-binary-encoding v0.0.0-00010101000000-000000000000
	github.com/stretchr/testify v1.8.4
)

replace github.com/real-logic/simple-binary-encoding => ../../../../sbe-tool/src/main/golang

go 1.12
