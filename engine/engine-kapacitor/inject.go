package main

import (
	"github.com/influxdata/kapacitor/udf/agent"
	"log"
	"os"
	"context"
	"errors"
	"github.com/mgjeong/messaging-zmq/go/emf"
	"strconv"
	"strings"
	"net"
)

type injectHandler struct {
	source string
	address string
	topic   string

	childContext context.Context
	cancel       context.CancelFunc

	agent *agent.Agent
}

var conn *net.UDPConn
var hostname string

func newInjectHandler(agent *agent.Agent) *injectHandler {
	return &injectHandler{agent: agent}
}

func (p *injectHandler) Info() (*agent.InfoResponse, error) {
	info := &agent.InfoResponse{
		Wants:    agent.EdgeType_STREAM,
		Provides: agent.EdgeType_STREAM,
		Options: map[string]*agent.OptionInfo{
			"source": {ValueTypes: []agent.ValueType{agent.ValueType_STRING}},
			"address": {ValueTypes: []agent.ValueType{agent.ValueType_STRING}},
			"topic":   {ValueTypes: []agent.ValueType{agent.ValueType_STRING}},
		},
	}
	return info, nil
}

func (p *injectHandler) Init(r *agent.InitRequest) (*agent.InitResponse, error) {
	init := &agent.InitResponse{
		Success: true,
		Error:   "",
	}

	for _, opt := range r.Options {
		switch opt.Name {
		case "source":
			p.source = opt.Values[0].Value.(*agent.OptionValue_StringValue).StringValue
		case "address":
			p.address = opt.Values[0].Value.(*agent.OptionValue_StringValue).StringValue
		case "topic":
			p.topic = opt.Values[0].Value.(*agent.OptionValue_StringValue).StringValue
		}
	}

	if p.address == "" {
		init.Success = false
		init.Error += " must supply source address"
	}

	if p.source == "" {
		init.Success = false
		init.Error += " must specify table name"
	}

	log.Println("DPRuntime Wait to make source in PID ", os.Getpid())
	p.childContext, p.cancel = context.WithCancel(context.Background())

	go func(ctx context.Context) {
		// TODO: Read UDP port dynamically
		const udpPort = "9100"
		var emfInstance *emf.EZMQAPI = nil
		var emfSub *emf.EZMQSubscriber
		init := true
		for {
			select {
			case <-ctx.Done():
				if conn != nil {
					log.Println("Closing UDP connection")
					conn.Close()
				}
				if emfInstance != nil {
					log.Println("Terminating EZMQ")
					emfSub.Stop()
				}
				return
			default:
				if init {
					udpAddr, err := net.ResolveUDPAddr("udp", "localhost:"+udpPort)
					if err != nil {
						// TODO: error handling
						log.Println("DPRuntime: fail to resolve UDP address")
					}
					conn, err = net.DialUDP("udp", nil, udpAddr)
					if err != nil {
						// TODO: error handling
						log.Println("DPRuntime: fail to make udp connection")
					}
					emfInstance = initializeEZMQ()
					hostname = p.address
					emfSub = addSource()
					init = false
				}
			}
		}
	}(p.childContext)

	// TODO: Handle errors during setting EZMQ subscriber
	return init, nil
}

func initializeEZMQ() *emf.EZMQAPI {
	instance := emf.GetInstance()
	result := instance.Initialize()
	log.Println("Initializing EZMQ, error code: ", result)
	return instance
}

func addSource() *emf.EZMQSubscriber {
	log.Println("DPRuntime Start to make source [", hostname, "] in PID ", os.Getpid())
	target := strings.Split(hostname, ":")
	port, err := strconv.Atoi(target[1])
	subCB := func(event emf.Event) { eventHandler(event) }
	subTopicCB := func(topic string, event emf.Event) { eventHandlerWithTopic(topic, event) }
	if err != nil {
		// TODO: error handling
		log.Println("DPRuntime wrong port number")
	}

	subscriber := emf.GetEZMQSubscriber(target[0], port, subCB, subTopicCB)
	result := subscriber.Start()
	// TODO: error handling
	log.Println("DPRuntime subscriber started with error ", result)

	// TODO: subscribing topics
	result = subscriber.Subscribe()
	// TODO: error handling
	log.Println("DPRuntime subscriber is working with error ", result)
	return subscriber
}

func eventHandler(event emf.Event) {
	eventHandlerWithTopic("", event)
}

func eventHandlerWithTopic(topic string, event emf.Event) {
	msg := hostname
	if topic != "" {
		msg += ":" + topic
	}
	msg += " "
	readings := event.GetReading()
	for i := 0; i < len(readings); i++ {
		// TODO: Assemble keys and values into influx line, and send via UDP
		//log.Println("DPRuntime message: ", readings[i].GetValue())
		msg += readings[i].GetName() + "=" + readings[i].GetValue() + " "
	}
	log.Println("DPRuntime message: ", msg)

	forwardEventToEngine(msg)
}

func forwardEventToEngine(msg string) {
	// TODO: error handling
	_, err := conn.Write([]byte(msg))
	if err != nil {
		log.Println("DPRuntime failed to forward msg via UDP")
	}
}

func (p *injectHandler) Snapshot() (*agent.SnapshotResponse, error) {
	return &agent.SnapshotResponse{}, nil
}

func (p *injectHandler) Restore(req *agent.RestoreRequest) (*agent.RestoreResponse, error) {
	// TODO: implement
	return &agent.RestoreResponse{
		Success: true,
	}, nil
}

func (p *injectHandler) BeginBatch(batch *agent.BeginBatch) error {
	return errors.New("batching is not supported")
}

var count int64 = 0

func (p *injectHandler) Point(point *agent.Point) error {
	p.agent.Responses <- &agent.Response{
		Message: &agent.Response_Point{
			Point: point,
		},
	}
	log.Println("Pointing ", count)
	count = count + 1
	return nil
}

func (p *injectHandler) EndBatch(batch *agent.EndBatch) error {
	return nil
}

func (p *injectHandler) Stop() {
	log.Println("DPRuntime Stopping UDF")
	if p.childContext != nil {
		p.cancel()
	}
	close(p.agent.Responses)
}

func main() {
	thisAgent := agent.New(os.Stdin, os.Stdout)
	thisHandler := newInjectHandler(thisAgent)
	thisAgent.Handler = thisHandler

	log.Println("Starting agent")
	thisAgent.Start()
	err := thisAgent.Wait()
	if err != nil {
		log.Fatal(err)
	}
}
