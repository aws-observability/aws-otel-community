package collection

import (
	"context"
	"fmt"
	"math/rand"
	"sync/atomic"

	"go.opentelemetry.io/otel/metric"
	"go.opentelemetry.io/otel/metric/instrument"
)

// requestBasedMetricCollector contains all the request based metric instruments.
type requestBasedMetricCollector struct {
	total_bytes_sent   instrument.Int64Counter
	total_api_requests instrument.Int64ObservableCounter
	latency_time       instrument.Int64Histogram
	config             Config
	meter              metric.Meter
	counter            int64
}

// AddApiRequest adds 1 to the rqmc counter
func (rqmc *requestBasedMetricCollector) AddApiRequest() {
	atomic.AddInt64(&rqmc.counter, 1)
}

// GetApiRequest gets the rqmc counter
func (rqmc *requestBasedMetricCollector) GetApiRequest() int {
	return int(atomic.LoadInt64(&rqmc.counter))
}

// NewRequestBasedMetricCollector returns a new type struct that holds and registers the 3 request based metric instruments used in the Go-Sample-App;
// TotalBytesSent, TotalRequests, LatencyTime
func NewRequestBasedMetricCollector(ctx context.Context, cfg Config, mp metric.MeterProvider) requestBasedMetricCollector {

	rqmc := requestBasedMetricCollector{config: cfg}
	rqmc.meter = mp.Meter("github.com/aws-otel-commnunity/sample-apps/go-sample-app/collection")
	rqmc.registerTotalBytesSent()
	rqmc.registerTotalRequests()
	rqmc.registerLatencyTime()
	return rqmc
}

// registerTotalBytesSent registers a Synchronous counter called TotalBytesSent.
func (rqmc *requestBasedMetricCollector) registerTotalBytesSent() {
	total_bytes_sent, err := rqmc.meter.Int64Counter(
		total_bytes_sent+testingId,
		instrument.WithDescription("Keeps a sum of the total amount of bytes sent while the application is alive"),
		instrument.WithUnit("By"),
	)
	if err != nil {
		fmt.Println(err)
	}
	rqmc.total_bytes_sent = total_bytes_sent
}

// registerTotalRequests registers an Asynchronous counter called TotalApiRequests.
func (rqmc *requestBasedMetricCollector) registerTotalRequests() {
	total_api_requests, err := rqmc.meter.Int64ObservableCounter(
		total_api_requests+testingId,
		instrument.WithDescription("Increments by one every time a sampleapp endpoint is used"),
		instrument.WithUnit("1"),
	)
	if err != nil {
		fmt.Println(err)
	}
	rqmc.total_api_requests = total_api_requests
}

// registerLatencyTime registers a Synchronous histogram called LatencyTime.
func (rqmc *requestBasedMetricCollector) registerLatencyTime() {
	latency_time, err := rqmc.meter.Int64Histogram(
		latency_time+testingId,
		instrument.WithDescription("Measures latency time in buckets of 100 300 and 500"),
		instrument.WithUnit("ms"),
	)
	if err != nil {
		fmt.Println(err)
	}
	rqmc.latency_time = latency_time
}

// StartTotalRequestCallBack starts the callback for the TotalApiRequests.
func (rqmc *requestBasedMetricCollector) StartTotalRequestCallback() {
	if _, err := rqmc.meter.RegisterCallback(
		// SDK periodically calls this function to collect data.
		func(ctx context.Context, o metric.Observer) error {
			o.ObserveInt64(rqmc.total_api_requests, int64(rqmc.GetApiRequest()), requestMetricCommonLabels...)

			return nil
		},
		rqmc.total_api_requests,
	); err != nil {
		panic(err)
	}
}

// UpdateTotalBytesSent updates TotalBytesSent with a value between 0 and 1024
func (rqmc *requestBasedMetricCollector) UpdateTotalBytesSent(ctx context.Context) {
	min := 0
	max := 1024
	rqmc.total_bytes_sent.Add(ctx, int64(rand.Intn(max-min)+min), requestMetricCommonLabels...)
}

// UpdateLatencyTime updates LatencyTime adds an aditional value between 0 and 512 to the histogram distribution.
func (rqmc *requestBasedMetricCollector) UpdateLatencyTime(ctx context.Context) {
	min := 0
	max := 512
	rqmc.latency_time.Record(ctx, int64(rand.Intn(max-min)+min), requestMetricCommonLabels...)
}
