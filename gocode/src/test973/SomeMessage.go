// Generated SBE (Simple Binary Encoding) message codec

package test973

import (
	"io"
	"io/ioutil"
)

type SomeMessage struct {
	MyEvent EventType
}

func (s *SomeMessage) Encode(_m *SbeGoMarshaller, _w io.Writer, doRangeCheck bool) error {
	if doRangeCheck {
		if err := s.RangeCheck(s.SbeSchemaVersion(), s.SbeSchemaVersion()); err != nil {
			return err
		}
	}
	if err := s.MyEvent.Encode(_m, _w); err != nil {
		return err
	}
	return nil
}

func (s *SomeMessage) Decode(_m *SbeGoMarshaller, _r io.Reader, actingVersion uint16, blockLength uint16, doRangeCheck bool) error {
	if s.MyEventInActingVersion(actingVersion) {
		if err := s.MyEvent.Decode(_m, _r, actingVersion); err != nil {
			return err
		}
	}
	if actingVersion > s.SbeSchemaVersion() && blockLength > s.SbeBlockLength() {
		io.CopyN(ioutil.Discard, _r, int64(blockLength-s.SbeBlockLength()))
	}
	if doRangeCheck {
		if err := s.RangeCheck(actingVersion, s.SbeSchemaVersion()); err != nil {
			return err
		}
	}
	return nil
}

func (s *SomeMessage) RangeCheck(actingVersion uint16, schemaVersion uint16) error {
	return nil
}

func SomeMessageInit(s *SomeMessage) {
	return
}

func (*SomeMessage) SbeBlockLength() (blockLength uint16) {
	return 1
}

func (*SomeMessage) SbeTemplateId() (templateId uint16) {
	return 1
}

func (*SomeMessage) SbeSchemaId() (schemaId uint16) {
	return 973
}

func (*SomeMessage) SbeSchemaVersion() (schemaVersion uint16) {
	return 0
}

func (*SomeMessage) SbeSemanticType() (semanticType []byte) {
	return []byte("")
}

func (*SomeMessage) SbeSemanticVersion() (semanticVersion string) {
	return "1.0"
}

func (*SomeMessage) MyEventId() uint16 {
	return 1
}

func (*SomeMessage) MyEventSinceVersion() uint16 {
	return 0
}

func (s *SomeMessage) MyEventInActingVersion(actingVersion uint16) bool {
	return actingVersion >= s.MyEventSinceVersion()
}

func (*SomeMessage) MyEventDeprecated() uint16 {
	return 0
}

func (*SomeMessage) MyEventMetaAttribute(meta int) string {
	switch meta {
	case 1:
		return ""
	case 2:
		return ""
	case 3:
		return ""
	case 4:
		return "required"
	}
	return ""
}
