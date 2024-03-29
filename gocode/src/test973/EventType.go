// Generated SBE (Simple Binary Encoding) message codec

package test973

import (
	"io"
)

type EventType [8]bool
type EventTypeChoiceValue uint8
type EventTypeChoiceValues struct {
	A     EventTypeChoiceValue
	Bb    EventTypeChoiceValue
	Ccc   EventTypeChoiceValue
	D     EventTypeChoiceValue
	EeEee EventTypeChoiceValue
}

var EventTypeChoice = EventTypeChoiceValues{0, 1, 2, 3, 4}

func (e *EventType) Encode(_m *SbeGoMarshaller, _w io.Writer) error {
	var wireval uint8 = 0
	for k, v := range e {
		if v {
			wireval |= (1 << uint(k))
		}
	}
	return _m.WriteUint8(_w, wireval)
}

func (e *EventType) Decode(_m *SbeGoMarshaller, _r io.Reader, actingVersion uint16) error {
	var wireval uint8

	if err := _m.ReadUint8(_r, &wireval); err != nil {
		return err
	}

	var idx uint
	for idx = 0; idx < 8; idx++ {
		e[idx] = (wireval & (1 << idx)) > 0
	}
	return nil
}

func (EventType) EncodedLength() int64 {
	return 1
}

func (*EventType) ASinceVersion() uint16 {
	return 0
}

func (e *EventType) AInActingVersion(actingVersion uint16) bool {
	return actingVersion >= e.ASinceVersion()
}

func (*EventType) ADeprecated() uint16 {
	return 0
}

func (*EventType) BbSinceVersion() uint16 {
	return 0
}

func (e *EventType) BbInActingVersion(actingVersion uint16) bool {
	return actingVersion >= e.BbSinceVersion()
}

func (*EventType) BbDeprecated() uint16 {
	return 0
}

func (*EventType) CccSinceVersion() uint16 {
	return 0
}

func (e *EventType) CccInActingVersion(actingVersion uint16) bool {
	return actingVersion >= e.CccSinceVersion()
}

func (*EventType) CccDeprecated() uint16 {
	return 0
}

func (*EventType) DSinceVersion() uint16 {
	return 0
}

func (e *EventType) DInActingVersion(actingVersion uint16) bool {
	return actingVersion >= e.DSinceVersion()
}

func (*EventType) DDeprecated() uint16 {
	return 0
}

func (*EventType) EeEeeSinceVersion() uint16 {
	return 0
}

func (e *EventType) EeEeeInActingVersion(actingVersion uint16) bool {
	return actingVersion >= e.EeEeeSinceVersion()
}

func (*EventType) EeEeeDeprecated() uint16 {
	return 0
}
