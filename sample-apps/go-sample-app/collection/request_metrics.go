package collection

import (
	"context"
	"fmt"
	"math/rand"
	"sync/atomic"

	"go.opentelemetry.io/otel/metric/instrument"
	"go.opentelemetry.io/otel/metric/instrument/asyncint64"
	"go.opentelemetry.io/otel/metric/instrument/syncint64"
)

// requestBasedMetricCollector contains all the request based metric instruments.
type requestBasedMetricCollector struct {
	totalBytesSent syncint64.Counter
	totalRequests  asyncint64.Counter
	latencyTime    syncint64.Histogram
	context        context.Context
	n              int64
	config         Config
}

// AddApiRequest adds 1 to the rqmc counter
func (rqmc *requestBasedMetricCollector) AddApiRequest() {
	atomic.AddInt64(&rqmc.n, 1)
}

// GetApiRequest gets the rqmc counter
func (rqmc *requestBasedMetricCollector) GetApiRequest() int {
	return int(atomic.LoadInt64(&rqmc.n))
}

// NewRequestBasedMetricCollector returns a new type struct that holds and registers the 3 request based metric instruments used in the Go-Sample-App;
// TotalBytesSent, TotalRequests, LatencyTime
func NewRequestBasedMetricCollector(ctx context.Context, cfg Config) requestBasedMetricCollector {
	rqmc := requestBasedMetricCollector{context: ctx, config: cfg}
	rqmc.registerTotalBytesSent()
	rqmc.registerTotalRequests()
	rqmc.registerLatencyTime()
	return rqmc
}

// registerTotalBytesSent registers a Synchronous counter called TotalBytesSent.
func (rqmc *requestBasedMetricCollector) registerTotalBytesSent() {
	totalBytesSent, err := meter.SyncInt64().Counter(
		"Total Bytes Sent",
		instrument.WithDescription("Keeps a sum of the total amount of bytes sent while the application is alive"),
		instrument.WithUnit("mb"),
	)
	if err != nil {
		fmt.Println(err)
	}
	rqmc.totalBytesSent = totalBytesSent
}

// registerTotalRequests registers an Asynchronous counter called TotalApiRequests.
func (rqmc *requestBasedMetricCollector) registerTotalRequests() {
	totalRequests, err := meter.AsyncInt64().Counter(
		"Total API Requests",
		instrument.WithDescription("Increments by one every time a sampleapp endpoint is used"),
		instrument.WithUnit("1"),
	)
	if err != nil {
		fmt.Println(err)
	}
	rqmc.totalRequests = totalRequests
}

// registerLatencyTime registers a Synchronous histogram called LatencyTime.
func (rqmc *requestBasedMetricCollector) registerLatencyTime() {
	latencyTime, err := meter.SyncInt64().Histogram(
		"Latency Time",
		instrument.WithDescription("Measures latency time"),
		instrument.WithUnit("ms"),
	)
	if err != nil {
		fmt.Println(err)
	}
	rqmc.latencyTime = latencyTime
}

// StartTotalRequestCallBack starts the callback for the TotalApiRequests.
func (rqmc *requestBasedMetricCollector) StartTotalRequestCallback() {
	if err := meter.RegisterCallback(
		[]instrument.Asynchronous{
			rqmc.totalRequests,
		},
		// SDK periodically calls this function to collect data.
		func(ctx context.Context) {
			rqmc.totalRequests.Observe(ctx, int64(rqmc.GetApiRequest()))
			fmt.Println("Total requests observed")
		},
	); err != nil {
		panic(err)
	}
}

// UpdateTotalBytesSent updates TotalBytesSent with a value between 0 and 1024
func (rqmc *requestBasedMetricCollector) UpdateTotalBytesSent() {
	min := 0
	max := 1024
	rqmc.totalBytesSent.Add(rqmc.context, int64(rand.Intn(max-min)+min))
}

// UpdateLatencyTime updates LatencyTime adds an aditional value between 0 and 512 to the histogram distribution.
func (rqmc *requestBasedMetricCollector) UpdateLatencyTime() {
	min := 0
	max := 512
	rqmc.latencyTime.Record(rqmc.context, int64(rand.Intn(max-min)+min))
}
