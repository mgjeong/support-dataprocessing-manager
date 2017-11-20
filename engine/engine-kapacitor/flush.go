package main

import (
	"github.com/influxdata/kapacitor/udf/agent"
	"log"
	"errors"
	"os"
	"encoding/json"

	"github.com/mgjeong/messaging-zmq/go/emf"
	"strings"
	"strconv"
	"time"
)

type flushHandler struct {
	sink    string
	address string

	targetFile *(os.File)
	targetEMF *(emf.EMFPublisher)

	agent *agent.Agent
}

func newFlushHandler(agent *agent.Agent) *flushHandler {
	return &flushHandler{agent: agent}
}

// Return the InfoResponse. Describing the properties of this Handler
func (f *flushHandler) Info() (*agent.InfoResponse, error) {
	info := &agent.InfoResponse{
		Wants:    agent.EdgeType_STREAM,
		Provides: agent.EdgeType_STREAM,
		Options: map[string]*agent.OptionInfo{
			"sink":    {ValueTypes: []agent.ValueType{agent.ValueType_STRING}},
			"address": {ValueTypes: []agent.ValueType{agent.ValueType_STRING}},
		},
	}
	return info, nil
}

// Initialize the Handler with the provided options.
func (f *flushHandler) Init(r *agent.InitRequest) (*agent.InitResponse, error) {
	init := &agent.InitResponse{
		Success: true,
		Error:   "",
	}

	for _, opt := range r.Options {
		switch opt.Name {
		case "sink":
			f.sink = opt.Values[0].Value.(*agent.OptionValue_StringValue).StringValue
		case "address":
			f.address = opt.Values[0].Value.(*agent.OptionValue_StringValue).StringValue
		}
	}

	if f.sink == "" {
		init.Success = false
		init.Error += " must supply sink type"
	}

	if f.address == "" {
		init.Success = false
		init.Error += " must supply target destination"
	}

	// Open file, or socket
	if f.addSink() != nil {
		init.Success = false
		init.Error = " fail to add new sink"
	}

	return init, nil
}

func (f *flushHandler) addSink() error {
	log.Println("KRKIM Start to make type: ", f.sink)
	switch f.sink {
	case "f":
		log.Println("KRKIM file")
		destination, err := os.Create(f.address)
		if err != nil {
			log.Println("KRKIM Error: unable to open target file")
			return err
		}
		f.targetFile = destination
	case "emf":
		log.Println("KRKIM emf sink")
		target := strings.Split(f.address, ":")
		emf.GetInstance().Initialize()
		startCB := func(code emf.EMFErrorCode) { log.Println("EMF starting by callback") }
		stopCB := func(code emf.EMFErrorCode) { log.Println("EMF stopping by callback") }
		errorCB := func(code emf.EMFErrorCode) { log.Println("EMF error by callback") }
		port, err := strconv.Atoi(target[1])
		log.Println("port: ", port)
		if err != nil {
			return errors.New("error: wrong port number for emf")
		}
		f.targetEMF = emf.GetEMFPublisher(port, startCB, stopCB, errorCB)
		result := f.targetEMF.Start()

		if result != 0 {
			log.Println("KRKIM failed to start emf publisher")
			return errors.New("error: failed to start emf publisher")
		}
	default:
		log.Println("KRKIM wrong sink type")
		return errors.New("error: unsupported sink type")
	}
	return nil
}

// Create a snapshot of the running state of the handler.
func (f *flushHandler) Snapshot() (*agent.SnapshotResponse, error) {
	return &agent.SnapshotResponse{}, nil
}

// Restore a previous snapshot.
func (f *flushHandler) Restore(req *agent.RestoreRequest) (*agent.RestoreResponse, error) {
	if f.addSink() != nil {
		return &agent.RestoreResponse{
			Success: false,
		}, errors.New("error: fail to restore sink")
	}
	return &agent.RestoreResponse{
		Success: true,
	}, nil
}

// A batch has begun.
func (f *flushHandler) BeginBatch(*agent.BeginBatch) error {
	return errors.New("batching not supported")
}

// A point has arrived.
func (f *flushHandler) Point(p *agent.Point) error {
	output := make(map[string]interface{}, 0)
	for key, value := range p.FieldsBool {
		output[key] = value
	}

	for key, value := range p.FieldsDouble {
		output[key] = value
	}

	for key, value := range p.FieldsInt {
		output[key] = value
	}

	for key, value := range p.FieldsString {
		output[key] = value
	}

	for key, value := range p.Tags {
		output[key] = value
	}

	jsonBytes, err := json.Marshal(output)

	if err != nil {
		return errors.New("error: fail to make json output")
	}

	if f.sink == "f" {
		log.Println("KRKIM Writing: ", string(jsonBytes))
		f.targetFile.Write(jsonBytes)
		f.targetFile.WriteString("\n")
	} else if f.sink == "emf" {
		var event = getEvent(jsonBytes)
		result := f.targetEMF.Publish(event)
		if result != 0 {
			log.Println("KRKIM error: failed to publish emf event")
		}
	}

	f.agent.Responses <- &agent.Response{
		Message: &agent.Response_Point{
			Point: p,
		},
	}
	return nil
}

func getEvent(data []byte) emf.Event {
	var event emf.Event

	var id string = "DPR-kapacitor"
	var now int64 = time.Now().UnixNano()
	var created int64 = 0
	var modified int64 = 0
	var origin int64 = now
	var pushed int64 = now
	device, _ := os.Hostname()

	event.Id = &id
	event.Created = &created
	event.Modified = &modified
	event.Origin = &origin
	event.Pushed = &pushed
	event.Device = &device

	var reading = &emf.Reading{}
	var rId string = "DPR-kapacitor"
	var rCreated int64 = 0
	var rModified int64 = 0
	var rOrigin int64 = now
	var rPushed int64 = now
	rDevice, _ := os.Hostname()
	var rName string = "DPR"
	var rValue = string(data)
	reading.Id = &rId
	reading.Created = &rCreated
	reading.Modified = &rModified
	reading.Origin = &rOrigin
	reading.Pushed = &rPushed
	reading.Device = &rDevice
	reading.Name = &rName
	reading.Value = &rValue

	event.Reading = make([]*emf.Reading, 1)
	event.Reading[0] = reading
	return event
}

// The batch is complete.
func (f *flushHandler) EndBatch(*agent.EndBatch) error {
	return errors.New("batching not supported")
}

// Gracefully stop the Handler.
// No other methods will be called.
func (f *flushHandler) Stop() {
	log.Println("Closing sink")
	if f.sink == "f" {
		f.targetFile.Close()
	} else if f.sink == "emf" {
		f.targetEMF.Stop()
	}
	close(f.agent.Responses)
}

func main() {
	thisAgent := agent.New(os.Stdin, os.Stdout)
	thisHandler := newFlushHandler(thisAgent)
	thisAgent.Handler = thisHandler

	log.Println("Starting agent")
	thisAgent.Start()
	err := thisAgent.Wait()
	if err != nil {
		log.Fatal(err)
	}
}
