package json

import (
	"fmt"
	"strings"

	"github.com/real-logic/simple-binary-encoding/otf"
)

// JsonPrinter pretty prints JSON based upon the given Ir.
type JsonPrinter struct {
	headerDecoder *otf.OftHeaderDecoder
	ir            *otf.IrDecoder
}

// NewJsonPrinter creates a new JsonPrinter for a given message Ir.
func NewJsonPrinter(ir *otf.IrDecoder) (*JsonPrinter, error) {
	d, err := otf.NewOtfHeaderDecoder(ir.Header())
	return &JsonPrinter{
		headerDecoder: d,
		ir:            ir,
	}, err
}

// Print the encoded message to the output.
func (printer *JsonPrinter) Print(buffer []byte, output *strings.Builder) error {
	blockLength, err := printer.headerDecoder.BlockLength(buffer)
	if err != nil {
		return fmt.Errorf("Error getting blockLength: %s", err)
	}
	templateId, err := printer.headerDecoder.TemplateId(buffer)
	if err != nil {
		return fmt.Errorf("Error getting templateId: %s", err)
	}
	schemaId, err := printer.headerDecoder.SchemaId(buffer)
	if err != nil {
		return fmt.Errorf("Error getting schemaId: %s", err)
	}
	actingVersion, err := printer.headerDecoder.SchemaVersion(buffer)
	if err != nil {
		return fmt.Errorf("Error getting schemaVersion: %s", err)
	}
	err = printer.validateId(schemaId)
	if err != nil {
		return err
	}

	messageOffset := printer.headerDecoder.EncodedLength()
	msgTokens := printer.ir.MessageByID(int32(templateId))

	otf.Decode(
		buffer[messageOffset:],
		actingVersion,
		blockLength,
		msgTokens,
		newJsonTokenListener(output),
	)

	return nil
}

func (printer *JsonPrinter) validateId(schemaId uint64) error {
	if schemaId != uint64(printer.ir.Id()) {
		return fmt.Errorf("Required schema id %d but was %d", printer.ir.Id(), schemaId)
	}
	return nil
}

// PrintJson the encoded message to a String.
func (printer *JsonPrinter) PrintJson(buffer []byte) (string, error) {
	sb := strings.Builder{}
	if err := printer.Print(buffer, &sb); err != nil {
		return "", err
	}
	return sb.String(), nil
}
