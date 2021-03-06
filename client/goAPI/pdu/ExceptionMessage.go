package pdu

import (
	"bytes"
	"encoding/gob"
	"fmt"
	"github.com/TIBCOSoftware/tgdb-client/client/goAPI/exception"
	"github.com/TIBCOSoftware/tgdb-client/client/goAPI/iostream"
	"github.com/TIBCOSoftware/tgdb-client/client/goAPI/types"
	"reflect"
	"strings"
)

/**
 * Copyright 2018-19 TIBCO Software Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); You may not use this file except
 * in compliance with the License.
 * A copy of the License is included in the distribution package with this file.
 * You also may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF DirectionAny KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * File name: VerbExceptionMessage.go
 * Created on: Sep 30, 2018
 * Created by: achavan
 * SVN id: $id: $
 *
 */

type ExceptionMessage struct {
	*AbstractProtocolMessage
	exceptionMsg  string
	exceptionType int
}

func DefaultExceptionMessage() *ExceptionMessage {
	// We must register the concrete type for the encoder and decoder (which would
	// normally be on a separate machine from the encoder). On each end, this tells the
	// engine which concrete type is being sent that implements the interface.
	gob.Register(ExceptionMessage{})

	newMsg := ExceptionMessage{
		AbstractProtocolMessage: DefaultAbstractProtocolMessage(),
	}
	newMsg.verbId = VerbExceptionMessage
	newMsg.BufLength = int(reflect.TypeOf(newMsg).Size())
	return &newMsg
}

// Create New Message Instance
func NewExceptionMessage(authToken, sessionId int64) *ExceptionMessage {
	newMsg := DefaultExceptionMessage()
	newMsg.authToken = authToken
	newMsg.sessionId = sessionId
	newMsg.BufLength = int(reflect.TypeOf(newMsg).Size())
	return newMsg
}

func NewExceptionMessageWithType(exType int, msg string) *ExceptionMessage {
	newMsg := DefaultExceptionMessage()
	newMsg.exceptionMsg = msg
	newMsg.exceptionType = exType
	newMsg.BufLength = int(reflect.TypeOf(*newMsg).Size())
	return newMsg
}

/////////////////////////////////////////////////////////////////
// Helper Public functions for VerbExceptionMessage
/////////////////////////////////////////////////////////////////

func BuildFromException(ex types.TGError) *ExceptionMessage {
	exceptionMsg := NewExceptionMessageWithType(ex.GetErrorType(), ex.Error())
	return exceptionMsg
}

func (msg *ExceptionMessage) GetExceptionMsg() string {
	return msg.exceptionMsg
}

func (msg *ExceptionMessage) GetExceptionType() int {
	return msg.exceptionType
}

func (msg *ExceptionMessage) SetExceptionMsg(exMsg string) {
	msg.exceptionMsg = exMsg
}

func (msg *ExceptionMessage) SetExceptionType(exType int) {
	msg.exceptionType = exType
}

/////////////////////////////////////////////////////////////////
// Implement functions from Interface ==> TGMessage
/////////////////////////////////////////////////////////////////

// FromBytes constructs a message object from the input buffer in the byte format
func (msg *ExceptionMessage) FromBytes(buffer []byte) (types.TGMessage, types.TGError) {
	logger.Log(fmt.Sprint("Entering ExceptionMessage:FromBytes"))
	if len(buffer) < 0 {
		logger.Error(fmt.Sprint("ERROR: Returning ExceptionMessage:FromBytes w/ Error: Invalid Message Buffer"))
		return nil, exception.CreateExceptionByType(types.TGErrorInvalidMessageLength)
	}

	is := iostream.NewProtocolDataInputStream(buffer)

	// First member attribute / element of message header is BufLength
	bufLen, err := is.ReadInt()
	if err != nil {
		logger.Error(fmt.Sprint("ERROR: Returning ExceptionMessage:FromBytes w/ Error in reading buffer length from message buffer"))
		return nil, err
	}
	logger.Log(fmt.Sprintf("Inside ExceptionMessage:FromBytes read bufLen as '%+v'", bufLen))
	if bufLen != len(buffer) {
		errMsg := fmt.Sprint("Buffer length mismatch")
		return nil, exception.GetErrorByType(types.TGErrorInvalidMessageLength, types.INTERNAL_SERVER_ERROR, errMsg, "")
	}

	logger.Log(fmt.Sprint("Inside ExceptionMessage:FromBytes - about to APMReadHeader"))
	err = APMReadHeader(msg, is)
	if err != nil {
		errMsg := fmt.Sprintf("Unable to recreate message from '%+v' in byte format", buffer)
		return nil, exception.GetErrorByType(types.TGErrorIOException, types.INTERNAL_SERVER_ERROR, errMsg, "")
	}

	logger.Log(fmt.Sprint("Inside ExceptionMessage:FromBytes - about to ReadPayload"))
	err = msg.ReadPayload(is)
	if err != nil {
		errMsg := fmt.Sprintf("Unable to recreate message from '%+v' in byte format", buffer)
		return nil, exception.GetErrorByType(types.TGErrorIOException, types.INTERNAL_SERVER_ERROR, errMsg, "")
	}

	logger.Log(fmt.Sprintf("ExceptionMessage::FromBytes resulted in '%+v'", msg))
	return msg, nil
}

// ToBytes converts a message object into byte format to be sent over the network to TGDB server
func (msg *ExceptionMessage) ToBytes() ([]byte, int, types.TGError) {
	logger.Log(fmt.Sprint("Entering ExceptionMessage:ToBytes"))
	os := iostream.DefaultProtocolDataOutputStream()

	logger.Log(fmt.Sprint("Inside ExceptionMessage:ToBytes - about to APMWriteHeader"))
	err := APMWriteHeader(msg, os)
	if err != nil {
		errMsg := fmt.Sprintf("Unable to export message '%+v' in byte format", msg)
		return nil, -1, exception.GetErrorByType(types.TGErrorIOException, types.INTERNAL_SERVER_ERROR, errMsg, "")
	}

	logger.Log(fmt.Sprint("Inside ExceptionMessage:ToBytes - about to WritePayload"))
	err = msg.WritePayload(os)
	if err != nil {
		errMsg := fmt.Sprintf("Unable to export message '%+v' in byte format", msg)
		return nil, -1, exception.GetErrorByType(types.TGErrorIOException, types.INTERNAL_SERVER_ERROR, errMsg, "")
	}

	_, err = os.WriteIntAt(0, os.GetLength())
	if err != nil {
		logger.Error(fmt.Sprint("ERROR: Returning ExceptionMessage:ToBytes w/ Error in writing buffer length"))
		return nil, -1, err
	}
	logger.Log(fmt.Sprintf("ExceptionMessage::ToBytes results bytes-on-the-wire in '%+v'", os.GetBuffer()))
	return os.GetBuffer(), os.GetLength(), nil
}

// GetAuthToken gets the authToken
func (msg *ExceptionMessage) GetAuthToken() int64 {
	return msg.HeaderGetAuthToken()
}

// GetIsUpdatable checks whether this message updatable or not
func (msg *ExceptionMessage) GetIsUpdatable() bool {
	return msg.GetUpdatableFlag()
}

// GetMessageByteBufLength gets the MessageByteBufLength. This method is called after the toBytes() is executed.
func (msg *ExceptionMessage) GetMessageByteBufLength() int {
	return msg.HeaderGetMessageByteBufLength()
}

// GetRequestId gets the requestId for the message. This will be used as the CorrelationId
func (msg *ExceptionMessage) GetRequestId() int64 {
	return msg.HeaderGetRequestId()
}

// GetSequenceNo gets the sequenceNo of the message
func (msg *ExceptionMessage) GetSequenceNo() int64 {
	return msg.HeaderGetSequenceNo()
}

// GetSessionId gets the session id
func (msg *ExceptionMessage) GetSessionId() int64 {
	return msg.HeaderGetSessionId()
}

// GetTimestamp gets the Timestamp
func (msg *ExceptionMessage) GetTimestamp() int64 {
	return msg.HeaderGetTimestamp()
}

// GetVerbId gets verbId of the message
func (msg *ExceptionMessage) GetVerbId() int {
	return msg.HeaderGetVerbId()
}

// SetAuthToken sets the authToken
func (msg *ExceptionMessage) SetAuthToken(authToken int64) {
	msg.HeaderSetAuthToken(authToken)
}

// SetDataOffset sets the offset at which data starts in the payload
func (msg *ExceptionMessage) SetDataOffset(dataOffset int16) {
	msg.HeaderSetDataOffset(dataOffset)
}

// SetIsUpdatable sets the updatable flag
func (msg *ExceptionMessage) SetIsUpdatable(updateFlag bool) {
	msg.SetUpdatableFlag(updateFlag)
}

// SetMessageByteBufLength sets the message buffer length
func (msg *ExceptionMessage) SetMessageByteBufLength(bufLength int) {
	msg.HeaderSetMessageByteBufLength(bufLength)
}

// SetRequestId sets the request id
func (msg *ExceptionMessage) SetRequestId(requestId int64) {
	msg.HeaderSetRequestId(requestId)
}

// SetSequenceNo sets the sequenceNo
func (msg *ExceptionMessage) SetSequenceNo(sequenceNo int64) {
	msg.HeaderSetSequenceNo(sequenceNo)
}

// SetSessionId sets the session id
func (msg *ExceptionMessage) SetSessionId(sessionId int64) {
	msg.HeaderSetSessionId(sessionId)
}

// SetTimestamp sets the timestamp
func (msg *ExceptionMessage) SetTimestamp(timestamp int64) types.TGError {
	if !(msg.isUpdatable || timestamp != -1) {
		logger.Error(fmt.Sprint("ERROR: Returning APMReadHeader:setTimestamp as !msg.IsUpdatable && timestamp != -1"))
		errMsg := fmt.Sprintf("Mutating a readonly message '%s'", GetVerb(msg.verbId).name)
		return exception.GetErrorByType(types.TGErrorGeneralException, types.INTERNAL_SERVER_ERROR, errMsg, "")
	}
	msg.HeaderSetTimestamp(timestamp)
	return nil
}

// SetVerbId sets verbId of the message
func (msg *ExceptionMessage) SetVerbId(verbId int) {
	msg.HeaderSetVerbId(verbId)
}

func (msg *ExceptionMessage) String() string {
	var buffer bytes.Buffer
	buffer.WriteString("ExceptionMessage:{")
	buffer.WriteString(fmt.Sprintf("ExceptionMsg: %s", msg.exceptionMsg))
	buffer.WriteString(fmt.Sprintf(", ExceptionType: %d", msg.exceptionType))
	buffer.WriteString(fmt.Sprintf(", BufLength: %d", msg.BufLength))
	strArray := []string{buffer.String(), msg.APMMessageToString()+"}"}
	msgStr := strings.Join(strArray, ", ")
	return  msgStr
}

// UpdateSequenceAndTimeStamp updates the SequenceAndTimeStamp, if message is mutable
// @param timestamp
// @return TGMessage on success, error on failure
func (msg *ExceptionMessage) UpdateSequenceAndTimeStamp(timestamp int64) types.TGError {
	return msg.SetSequenceAndTimeStamp(timestamp)
}

// ReadHeader reads the bytes from input stream and constructs a common header of network packet
func (msg *ExceptionMessage) ReadHeader(is types.TGInputStream) types.TGError {
	return APMReadHeader(msg, is)
}

// WriteHeader exports the values of the common message header attributes to output stream
func (msg *ExceptionMessage) WriteHeader(os types.TGOutputStream) types.TGError {
	return APMWriteHeader(msg, os)
}

// ReadPayload reads the bytes from input stream and constructs message specific payload attributes
func (msg *ExceptionMessage) ReadPayload(is types.TGInputStream) types.TGError {
	logger.Log(fmt.Sprint("Entering ExceptionMessage:ReadPayload"))
	bType, err := is.(*iostream.ProtocolDataInputStream).ReadByte()
	if err != nil {
		logger.Error(fmt.Sprint("ERROR: Returning ExceptionMessage:ReadPayload w/ Error in reading bType from message buffer"))
		return err
	}
	logger.Log(fmt.Sprintf("ExceptionMessage:ReadPayload read bType as '%+v'", bType))

	exMsg, err := is.(*iostream.ProtocolDataInputStream).ReadUTF()
	if err != nil {
		logger.Error(fmt.Sprint("ERROR: Returning ExceptionMessage:ReadPayload w/ Error in reading exMsg from message buffer"))
		return err
	}
	logger.Log(fmt.Sprintf("ExceptionMessage:ReadPayload read exMsg as '%+v'", exMsg))

	msg.SetExceptionMsg(exMsg)
	msg.SetExceptionType(int(bType))
	logger.Log(fmt.Sprint("Returning ExceptionMessage:ReadPayload"))
	return nil
}

// WritePayload exports the values of the message specific payload attributes to output stream
func (msg *ExceptionMessage) WritePayload(os types.TGOutputStream) types.TGError {
	os.(*iostream.ProtocolDataOutputStream).WriteByte(msg.GetExceptionType())
	return os.(*iostream.ProtocolDataOutputStream).WriteUTF(msg.GetExceptionMsg())
}

/////////////////////////////////////////////////////////////////
// Implement functions from Interface ==> encoding/BinaryMarshaller
/////////////////////////////////////////////////////////////////

func (msg *ExceptionMessage) MarshalBinary() ([]byte, error) {
	// A simple encoding: plain text.
	var b bytes.Buffer
	_, err := fmt.Fprintln(&b, msg.BufLength, msg.verbId, msg.sequenceNo, msg.timestamp,
		msg.requestId, msg.dataOffset, msg.authToken, msg.sessionId, msg.isUpdatable, msg.exceptionMsg, msg.exceptionType)
	if err != nil {
		logger.Error(fmt.Sprintf("ERROR: Returning ExceptionMessage:MarshalBinary w/ Error: '%+v'", err.Error()))
		return nil, err
	}
	return b.Bytes(), nil
}

/////////////////////////////////////////////////////////////////
// Implement functions from Interface ==> encoding/BinaryUnmarshaller
/////////////////////////////////////////////////////////////////

// UnmarshalBinary modifies the receiver so it must take a pointer receiver.
func (msg *ExceptionMessage) UnmarshalBinary(data []byte) error {
	// A simple encoding: plain text.
	b := bytes.NewBuffer(data)
	_, err := fmt.Fscanln(b, &msg.BufLength, &msg.verbId, &msg.sequenceNo,
		&msg.timestamp, &msg.requestId, &msg.dataOffset, &msg.authToken, &msg.sessionId, &msg.isUpdatable,
		&msg.exceptionMsg, &msg.exceptionType)
	if err != nil {
		logger.Error(fmt.Sprintf("ERROR: Returning ExceptionMessage:UnmarshalBinary w/ Error: '%+v'", err.Error()))
		return err
	}
	return nil
}
