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
 * File name: VerbQueryRequest.go
 * Created on: Sep 30, 2018
 * Created by: achavan
 * SVN id: $id: $
 *
 */

type QueryRequestMessage struct {
	*AbstractProtocolMessage
	queryExpr       string
	edgeExpr        string
	traverseExpr    string
	endExpr         string
	queryHashId     int64
	command         int
	queryObject     types.TGQuery
	fetchSize       int
	batchSize       int
	traversalDepth  int
	edgeLimit       int
	sortAttrName    string
	sortOrderDsc    bool
	sortResultLimit int
}

func DefaultQueryRequestMessage() *QueryRequestMessage {
	// We must register the concrete type for the encoder and decoder (which would
	// normally be on a separate machine from the encoder). On each end, this tells the
	// engine which concrete type is being sent that implements the interface.
	gob.Register(QueryRequestMessage{})

	newMsg := QueryRequestMessage{
		AbstractProtocolMessage: DefaultAbstractProtocolMessage(),
	}
	newMsg.fetchSize = 1000
	newMsg.batchSize = 50
	newMsg.traversalDepth = 3
	newMsg.edgeLimit = 0
	newMsg.sortOrderDsc = false
	newMsg.sortResultLimit = 50
	newMsg.verbId = VerbQueryRequest
	newMsg.BufLength = int(reflect.TypeOf(newMsg).Size())
	return &newMsg
}

// Create New Message Instance
func NewQueryRequestMessage(authToken, sessionId int64) *QueryRequestMessage {
	newMsg := DefaultQueryRequestMessage()
	newMsg.authToken = authToken
	newMsg.sessionId = sessionId
	newMsg.BufLength = int(reflect.TypeOf(*newMsg).Size())
	return newMsg
}

/////////////////////////////////////////////////////////////////
// Helper functions for QueryRequestMessage
/////////////////////////////////////////////////////////////////

func (msg *QueryRequestMessage) GetBatchSize() int {
	return msg.batchSize
}

func (msg *QueryRequestMessage) GetEdgeLimit() int {
	return msg.edgeLimit
}

func (msg *QueryRequestMessage) GetFetchSize() int {
	return msg.fetchSize
}

func (msg *QueryRequestMessage) GetSortAttrName() string {
	return msg.sortAttrName
}

func (msg *QueryRequestMessage) GetSortOrderDsc() bool {
	return msg.sortOrderDsc
}

func (msg *QueryRequestMessage) GetSortResultLimit() int {
	return msg.sortResultLimit
}

func (msg *QueryRequestMessage) GetTraversalDepth() int {
	return msg.traversalDepth
}

func (msg *QueryRequestMessage) GetQuery() string {
	return msg.queryExpr
}

func (msg *QueryRequestMessage) GetEdgeFilter() string {
	return msg.edgeExpr
}

func (msg *QueryRequestMessage) GetTraversalCondition() string {
	return msg.traverseExpr
}

func (msg *QueryRequestMessage) GetEndCondition() string {
	return msg.endExpr
}

func (msg *QueryRequestMessage) GetQueryHashId() int64 {
	return msg.queryHashId
}

func (msg *QueryRequestMessage) GetCommand() int {
	return msg.command
}

func (msg *QueryRequestMessage) GetQueryObject() types.TGQuery {
	return msg.queryObject
}

func (msg *QueryRequestMessage) SetBatchSize(size int) {
	if size < 10 || size > 32767 {
		msg.batchSize = 50
	} else {
		msg.batchSize = size
	}
}

func (msg *QueryRequestMessage) SetEdgeLimit(size int) {
	if size < 0 || size > 32767 {
		msg.edgeLimit = 1000
	} else {
		msg.edgeLimit = size
	}
}

func (msg *QueryRequestMessage) SetFetchSize(size int) {
	if size < 0 {
		msg.fetchSize = 1000
	} else {
		msg.fetchSize = size
	}
}

func (msg *QueryRequestMessage) SetSortAttrName(name string) {
	if len(name) != 0 {
		msg.sortAttrName = name
	}
}

func (msg *QueryRequestMessage) SetSortOrderDsc(order bool) {
	msg.sortOrderDsc = order
}

func (msg *QueryRequestMessage) SetSortResultLimit(limit int) {
	if limit < 0 {
		msg.sortResultLimit = 50
	} else {
		msg.sortResultLimit = limit
	}
}

func (msg *QueryRequestMessage) SetTraversalDepth(depth int) {
	if depth < 1 || depth > 1000 {
		msg.traversalDepth = 3
	} else {
		msg.traversalDepth = depth
	}
}

func (msg *QueryRequestMessage) SetQuery(expr string) {
	msg.queryExpr = expr
}

func (msg *QueryRequestMessage) SetEdgeFilter(expr string) {
	msg.edgeExpr = expr
}

func (msg *QueryRequestMessage) SetTraversalCondition(expr string) {
	msg.traverseExpr = expr
}

func (msg *QueryRequestMessage) SetEndCondition(expr string) {
	msg.endExpr = expr
}

func (msg *QueryRequestMessage) SetQueryHashId(hash int64) {
	msg.queryHashId = hash
}

func (msg *QueryRequestMessage) SetCommand(cmd int) {
	msg.command = cmd
}

func (msg *QueryRequestMessage) SetQueryObject(query types.TGQuery) {
	msg.queryObject = query
}

/////////////////////////////////////////////////////////////////
// Implement functions from Interface ==> TGMessage
/////////////////////////////////////////////////////////////////

// FromBytes constructs a message object from the input buffer in the byte format
func (msg *QueryRequestMessage) FromBytes(buffer []byte) (types.TGMessage, types.TGError) {
	logger.Log(fmt.Sprint("Entering QueryRequestMessage:FromBytes"))
	if len(buffer) < 0 {
		logger.Error(fmt.Sprint("ERROR: Returning QueryRequestMessage:FromBytes w/ Error: Invalid Message Buffer"))
		return nil, exception.CreateExceptionByType(types.TGErrorInvalidMessageLength)
	}

	is := iostream.NewProtocolDataInputStream(buffer)

	// First member attribute / element of message header is BufLength
	bufLen, err := is.ReadInt()
	if err != nil {
		logger.Error(fmt.Sprint("ERROR: Returning QueryRequestMessage:FromBytes w/ Error in reading buffer length from message buffer"))
		return nil, err
	}
	logger.Log(fmt.Sprintf("Inside QueryRequestMessage:FromBytes read bufLen as '%+v'", bufLen))
	if bufLen != len(buffer) {
		errMsg := fmt.Sprint("Buffer length mismatch")
		return nil, exception.GetErrorByType(types.TGErrorInvalidMessageLength, types.INTERNAL_SERVER_ERROR, errMsg, "")
	}

	logger.Log(fmt.Sprint("Inside QueryRequestMessage:FromBytes - about to APMReadHeader"))
	err = APMReadHeader(msg, is)
	if err != nil {
		errMsg := fmt.Sprintf("Unable to recreate message from '%+v' in byte format", buffer)
		return nil, exception.GetErrorByType(types.TGErrorIOException, types.INTERNAL_SERVER_ERROR, errMsg, "")
	}

	logger.Log(fmt.Sprint("Inside QueryRequestMessage:FromBytes - about to ReadPayload"))
	err = msg.ReadPayload(is)
	if err != nil {
		errMsg := fmt.Sprintf("Unable to recreate message from '%+v' in byte format", buffer)
		return nil, exception.GetErrorByType(types.TGErrorIOException, types.INTERNAL_SERVER_ERROR, errMsg, "")
	}

	logger.Log(fmt.Sprintf("QueryRequestMessage::FromBytes resulted in '%+v'", msg))
	return msg, nil
}

// ToBytes converts a message object into byte format to be sent over the network to TGDB server
func (msg *QueryRequestMessage) ToBytes() ([]byte, int, types.TGError) {
	logger.Log(fmt.Sprint("Entering QueryRequestMessage:ToBytes"))
	os := iostream.DefaultProtocolDataOutputStream()

	logger.Log(fmt.Sprint("Inside QueryRequestMessage:ToBytes - about to APMWriteHeader"))
	err := APMWriteHeader(msg, os)
	if err != nil {
		errMsg := fmt.Sprintf("Unable to export message '%+v' in byte format", msg)
		return nil, -1, exception.GetErrorByType(types.TGErrorIOException, types.INTERNAL_SERVER_ERROR, errMsg, "")
	}

	logger.Log(fmt.Sprint("Inside QueryRequestMessage:ToBytes - about to WritePayload"))
	err = msg.WritePayload(os)
	if err != nil {
		errMsg := fmt.Sprintf("Unable to export message '%+v' in byte format", msg)
		return nil, -1, exception.GetErrorByType(types.TGErrorIOException, types.INTERNAL_SERVER_ERROR, errMsg, "")
	}

	_, err = os.WriteIntAt(0, os.GetLength())
	if err != nil {
		logger.Error(fmt.Sprint("ERROR: Returning QueryRequestMessage:ToBytes w/ Error in writing buffer length"))
		return nil, -1, err
	}
	logger.Log(fmt.Sprintf("QueryRequestMessage::ToBytes results bytes-on-the-wire in '%+v'", os.GetBuffer()))
	return os.GetBuffer(), os.GetLength(), nil
}

// GetAuthToken gets the authToken
func (msg *QueryRequestMessage) GetAuthToken() int64 {
	return msg.HeaderGetAuthToken()
}

// GetIsUpdatable checks whether this message updatable or not
func (msg *QueryRequestMessage) GetIsUpdatable() bool {
	return msg.GetUpdatableFlag()
}

// GetMessageByteBufLength gets the MessageByteBufLength. This method is called after the toBytes() is executed.
func (msg *QueryRequestMessage) GetMessageByteBufLength() int {
	return msg.HeaderGetMessageByteBufLength()
}

// GetRequestId gets the requestId for the message. This will be used as the CorrelationId
func (msg *QueryRequestMessage) GetRequestId() int64 {
	return msg.HeaderGetRequestId()
}

// GetSequenceNo gets the sequenceNo of the message
func (msg *QueryRequestMessage) GetSequenceNo() int64 {
	return msg.HeaderGetSequenceNo()
}

// GetSessionId gets the session id
func (msg *QueryRequestMessage) GetSessionId() int64 {
	return msg.HeaderGetSessionId()
}

// GetTimestamp gets the Timestamp
func (msg *QueryRequestMessage) GetTimestamp() int64 {
	return msg.HeaderGetTimestamp()
}

// GetVerbId gets verbId of the message
func (msg *QueryRequestMessage) GetVerbId() int {
	return msg.HeaderGetVerbId()
}

// SetAuthToken sets the authToken
func (msg *QueryRequestMessage) SetAuthToken(authToken int64) {
	msg.HeaderSetAuthToken(authToken)
}

// SetDataOffset sets the offset at which data starts in the payload
func (msg *QueryRequestMessage) SetDataOffset(dataOffset int16) {
	msg.HeaderSetDataOffset(dataOffset)
}

// SetIsUpdatable sets the updatable flag
func (msg *QueryRequestMessage) SetIsUpdatable(updateFlag bool) {
	msg.SetUpdatableFlag(updateFlag)
}

// SetMessageByteBufLength sets the message buffer length
func (msg *QueryRequestMessage) SetMessageByteBufLength(bufLength int) {
	msg.HeaderSetMessageByteBufLength(bufLength)
}

// SetRequestId sets the request id
func (msg *QueryRequestMessage) SetRequestId(requestId int64) {
	msg.HeaderSetRequestId(requestId)
}

// SetSequenceNo sets the sequenceNo
func (msg *QueryRequestMessage) SetSequenceNo(sequenceNo int64) {
	msg.HeaderSetSequenceNo(sequenceNo)
}

// SetSessionId sets the session id
func (msg *QueryRequestMessage) SetSessionId(sessionId int64) {
	msg.HeaderSetSessionId(sessionId)
}

// SetTimestamp sets the timestamp
func (msg *QueryRequestMessage) SetTimestamp(timestamp int64) types.TGError {
	if !(msg.isUpdatable || timestamp != -1) {
		logger.Error(fmt.Sprint("ERROR: Returning APMReadHeader:setTimestamp as !msg.IsUpdatable && timestamp != -1"))
		errMsg := fmt.Sprintf("Mutating a readonly message '%s'", GetVerb(msg.verbId).name)
		return exception.GetErrorByType(types.TGErrorGeneralException, types.INTERNAL_SERVER_ERROR, errMsg, "")
	}
	msg.HeaderSetTimestamp(timestamp)
	return nil
}

// SetVerbId sets verbId of the message
func (msg *QueryRequestMessage) SetVerbId(verbId int) {
	msg.HeaderSetVerbId(verbId)
}

func (msg *QueryRequestMessage) String() string {
	var buffer bytes.Buffer
	buffer.WriteString("QueryRequestMessage:{")
	buffer.WriteString(fmt.Sprintf("QueryExpr: %s", msg.queryExpr))
	buffer.WriteString(fmt.Sprintf(", EdgeExpr: %s", msg.edgeExpr))
	buffer.WriteString(fmt.Sprintf(", TraverseExpr: %s", msg.traverseExpr))
	buffer.WriteString(fmt.Sprintf(", EndExpr: %s", msg.endExpr))
	buffer.WriteString(fmt.Sprintf(", QueryHashId: %d", msg.queryHashId))
	buffer.WriteString(fmt.Sprintf(", Command: %d", msg.command))
	//buffer.WriteString(fmt.Sprintf(", QueryObject: %+v", msg.queryObject))
	buffer.WriteString(fmt.Sprintf(", FetchSize: %d", msg.fetchSize))
	buffer.WriteString(fmt.Sprintf(", BatchSize: %d", msg.batchSize))
	buffer.WriteString(fmt.Sprintf(", TraversalDepth: %d", msg.traversalDepth))
	buffer.WriteString(fmt.Sprintf(", EdgeLimit: %d", msg.edgeLimit))
	buffer.WriteString(fmt.Sprintf(", SortAttrName: %s", msg.sortAttrName))
	buffer.WriteString(fmt.Sprintf(", SortOrderDsc: %+v", msg.sortOrderDsc))
	buffer.WriteString(fmt.Sprintf(", SortResultLimit: %d", msg.sortResultLimit))
	buffer.WriteString(fmt.Sprintf(", BufLength: %d", msg.BufLength))
	strArray := []string{buffer.String(), msg.APMMessageToString()+"}"}
	msgStr := strings.Join(strArray, ", ")
	return msgStr
}

// UpdateSequenceAndTimeStamp updates the SequenceAndTimeStamp, if message is mutable
// @param timestamp
// @return TGMessage on success, error on failure
func (msg *QueryRequestMessage) UpdateSequenceAndTimeStamp(timestamp int64) types.TGError {
	return msg.SetSequenceAndTimeStamp(timestamp)
}

// ReadHeader reads the bytes from input stream and constructs a common header of network packet
func (msg *QueryRequestMessage) ReadHeader(is types.TGInputStream) types.TGError {
	return APMReadHeader(msg, is)
}

// WriteHeader exports the values of the common message header attributes to output stream
func (msg *QueryRequestMessage) WriteHeader(os types.TGOutputStream) types.TGError {
	return APMWriteHeader(msg, os)
}

// ReadPayload reads the bytes from input stream and constructs message specific payload attributes
func (msg *QueryRequestMessage) ReadPayload(is types.TGInputStream) types.TGError {
	// No-Op for Now
	return nil
}

// WritePayload exports the values of the message specific payload attributes to output stream
func (msg *QueryRequestMessage) WritePayload(os types.TGOutputStream) types.TGError {
	startPos := os.(*iostream.ProtocolDataOutputStream).GetPosition()
	logger.Log(fmt.Sprintf("Entering QueryRequestMessage::WritePayload at output buffer position at: %d", startPos))
	os.(*iostream.ProtocolDataOutputStream).WriteInt(1) // datalength
	os.(*iostream.ProtocolDataOutputStream).WriteInt(1) //checksum
	os.(*iostream.ProtocolDataOutputStream).WriteInt(msg.GetCommand())
	os.(*iostream.ProtocolDataOutputStream).WriteInt(msg.GetFetchSize())
	os.(*iostream.ProtocolDataOutputStream).WriteShort(msg.GetBatchSize())
	os.(*iostream.ProtocolDataOutputStream).WriteShort(msg.GetTraversalDepth())
	os.(*iostream.ProtocolDataOutputStream).WriteShort(msg.GetEdgeLimit())
	// Has sort attr
	if msg.GetSortAttrName() != "" {
		os.(*iostream.ProtocolDataOutputStream).WriteBoolean(true)
		err := os.(*iostream.ProtocolDataOutputStream).WriteUTF(msg.GetSortAttrName())
		if err != nil {
			logger.Error(fmt.Sprint("ERROR: Returning QueryRequestMessage:WritePayload w/ Error in writing sortAttrName to message buffer"))
			return err
		}
		os.(*iostream.ProtocolDataOutputStream).WriteBoolean(msg.GetSortOrderDsc())
		os.(*iostream.ProtocolDataOutputStream).WriteInt(msg.GetSortResultLimit())
	} else {
		os.(*iostream.ProtocolDataOutputStream).WriteBoolean(false)
	}

	// CREATE, EXECUTE.
	if msg.GetCommand() == 1 || msg.GetCommand() == 2 {
		if msg.GetQuery() == "" {
			// isNull is true
			os.(*iostream.ProtocolDataOutputStream).WriteBoolean(true)
		} else {
			os.(*iostream.ProtocolDataOutputStream).WriteBoolean(false)
			err := os.(*iostream.ProtocolDataOutputStream).WriteUTF(msg.GetQuery())
			if err != nil {
				logger.Error(fmt.Sprint("ERROR: Returning QueryRequestMessage:WritePayload w/ Error in writing queryStr to message buffer"))
				return err
			}
		}
		if msg.GetEdgeFilter() == "" {
			os.(*iostream.ProtocolDataOutputStream).WriteBoolean(true)
		} else {
			os.(*iostream.ProtocolDataOutputStream).WriteBoolean(false)
			err := os.(*iostream.ProtocolDataOutputStream).WriteUTF(msg.GetEdgeFilter())
			if err != nil {
				logger.Error(fmt.Sprint("ERROR: Returning QueryRequestMessage:WritePayload w/ Error in writing edgeFilter to message buffer"))
				return err
			}
		}
		if msg.GetTraversalCondition() == "" {
			os.(*iostream.ProtocolDataOutputStream).WriteBoolean(true)
		} else {
			os.(*iostream.ProtocolDataOutputStream).WriteBoolean(false)
			err := os.(*iostream.ProtocolDataOutputStream).WriteUTF(msg.GetTraversalCondition())
			if err != nil {
				logger.Error(fmt.Sprint("ERROR: Returning QueryRequestMessage:WritePayload w/ Error in writing traversalFilter to message buffer"))
				return err
			}
		}
		if msg.GetEndCondition() == "" {
			os.(*iostream.ProtocolDataOutputStream).WriteBoolean(true)
		} else {
			os.(*iostream.ProtocolDataOutputStream).WriteBoolean(false)
			err := os.(*iostream.ProtocolDataOutputStream).WriteUTF(msg.GetEndCondition())
			if err != nil {
				logger.Error(fmt.Sprint("ERROR: Returning QueryRequestMessage:WritePayload w/ Error in writing endFilter to message buffer"))
				return err
			}
		}
	} else if msg.GetCommand() == 3 || msg.GetCommand() == 4 {
		// EXECUTED, CLOSE
		os.(*iostream.ProtocolDataOutputStream).WriteLong(msg.GetQueryHashId())
	}
	currPos := os.GetPosition()
	length := currPos - startPos
	logger.Log(fmt.Sprintf("Returning QueryRequestMessage::WritePayload at output buffer position at: %d after writing %d payload bytes", currPos, length))
	return nil
}

/////////////////////////////////////////////////////////////////
// Implement functions from Interface ==> encoding/BinaryMarshaler
/////////////////////////////////////////////////////////////////

func (msg *QueryRequestMessage) MarshalBinary() ([]byte, error) {
	// A simple encoding: plain text.
	var b bytes.Buffer
	_, err := fmt.Fprintln(&b, msg.BufLength, msg.verbId, msg.sequenceNo, msg.timestamp,
		msg.requestId, msg.dataOffset, msg.authToken, msg.sessionId, msg.isUpdatable, msg.queryExpr, msg.edgeExpr,
		msg.traverseExpr, msg.endExpr, msg.queryHashId, msg.command, msg.queryObject, msg.fetchSize, msg.batchSize,
		msg.traversalDepth, msg.edgeLimit, msg.sortAttrName, msg.sortOrderDsc, msg.sortResultLimit)
	if err != nil {
		logger.Error(fmt.Sprintf("ERROR: Returning QueryRequestMessage:MarshalBinary w/ Error: '%+v'", err.Error()))
		return nil, err
	}
	return b.Bytes(), nil
}

/////////////////////////////////////////////////////////////////
// Implement functions from Interface ==> encoding/BinaryUnmarshaler
/////////////////////////////////////////////////////////////////

// UnmarshalBinary modifies the receiver so it must take a pointer receiver.
func (msg *QueryRequestMessage) UnmarshalBinary(data []byte) error {
	// A simple encoding: plain text.
	b := bytes.NewBuffer(data)
	_, err := fmt.Fscanln(b, &msg.BufLength, &msg.verbId, &msg.sequenceNo,
		&msg.timestamp, &msg.requestId, &msg.dataOffset, &msg.authToken, &msg.sessionId, &msg.isUpdatable,
		&msg.queryExpr, &msg.edgeExpr, &msg.traverseExpr, &msg.endExpr, &msg.queryHashId, &msg.command, &msg.queryObject,
		&msg.fetchSize, &msg.batchSize, &msg.traversalDepth, &msg.edgeLimit, &msg.sortAttrName, &msg.sortOrderDsc,
		&msg.sortResultLimit)
	if err != nil {
		logger.Error(fmt.Sprintf("ERROR: Returning QueryRequestMessage:UnmarshalBinary w/ Error: '%+v'", err.Error()))
		return err
	}
	return nil
}
