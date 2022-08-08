package collection

import (
	"context"
	"fmt"
	"math/rand"
	"time"

	"go.opentelemetry.io/otel/metric"
	"go.opentelemetry.io/otel/metric/instrument"
	"go.opentelemetry.io/otel/metric/instrument/asyncint64"
	"go.opentelemetry.io/otel/metric/instrument/syncint64"
)

var (
	threadsActive int64 = 0
	threadsBool         = true
)

// randomMetricCollector contains all the random based metric instruments.
type randomMetricCollector struct {
	timeAlive     syncint64.Counter
	cpuUsage      asyncint64.Gauge
	heapSize      asyncint64.UpDownCounter
	threadsActive syncint64.UpDownCounter
	meter         metric.Meter
}

// NewRandomMetricCollector returns a new type struct that holds and registers the 4 random based metric instruments used in the Go-Sample-App;
// HeapSize, ThreadsActive, TimeAlive, CpuUsage
func NewRandomMetricCollector(mp metric.MeterProvider) randomMetricCollector {
	rmc := randomMetricCollector{}
	rmc.meter = mp.Meter("github.com/aws-otel-commnunity/sample-apps/go-sample-app/collection")
	rmc.registerHeapSize()
	rmc.registerThreadsActive()
	rmc.registerTimeAlive()
	rmc.registerCpuUsage()
	return rmc
}

// registerTimeAlive registers a Synchronous Counter called TimeAlive.
func (rmc *randomMetricCollector) registerTimeAlive() {
	timeAlive, err := rmc.meter.SyncInt64().Counter(
		serviceName+"."+apiTimeAlive,
		instrument.WithDescription("Total amount of time that the application has been alive"),
		instrument.WithUnit("ms"),
	)
	if err != nil {
		fmt.Println(err)
	}
	rmc.timeAlive = timeAlive
}

// registerCpuUsage registers an Asynchronous Gauge called CpuUsage.
func (rmc *randomMetricCollector) registerCpuUsage() {
	cpuUsage, err := rmc.meter.AsyncInt64().Gauge(
		serviceName+"."+apiCpuUsage,
		instrument.WithDescription("Cpu usage percent"),
		instrument.WithUnit("1"),
	)
	if err != nil {
		fmt.Println(err)
	}
	rmc.cpuUsage = cpuUsage

}

// registerHeapSize registers an Asynchronous UpDownCounter called HeapSize.
func (rmc *randomMetricCollector) registerHeapSize() {
	totalHeapSize, err := rmc.meter.AsyncInt64().UpDownCounter(
		serviceName+"."+apiTotalHeapSize,
		instrument.WithDescription("The current total heap size"),
		instrument.WithUnit("By"),
	)
	if err != nil {
		fmt.Println(err)
	}
	rmc.heapSize = totalHeapSize

}

// registerThreadsActive registers a Synchronous UpDownCounter called ThreadsActive.
func (rmc *randomMetricCollector) registerThreadsActive() {
	threadsActive, err := rmc.meter.SyncInt64().UpDownCounter(
		serviceName+"."+apiThreadsActive,
		instrument.WithUnit("1"),
		instrument.WithDescription("The total amount of threads active"),
	)
	if err != nil {
		fmt.Println(err)
	}
	rmc.threadsActive = threadsActive
}

// UpdateMetricsClient generates new metric values for Synchronous instruments every TimeInterval and
// Asynchronous instruments every CollectPeriod configured by the controller.
func (rmc *randomMetricCollector) RegisterMetricsClient(ctx context.Context, cfg Config) {
	go func() {
		for {
			rmc.updateTimeAlive(ctx, cfg)
			rmc.updateThreadsActive(ctx, cfg)
			time.Sleep(time.Second * time.Duration(cfg.TimeInterval))
		}
	}()
	rmc.updateCpuUsage(ctx, cfg)
	rmc.updateTotalHeapSize(ctx, cfg)
}

// updateTimeAlive updates TimeAlive by TimeAliveIncrementer increments.
func (rmc *randomMetricCollector) updateTimeAlive(ctx context.Context, cfg Config) {
	rmc.timeAlive.Add(ctx, cfg.TimeAliveIncrementer*1000, randomMetricCommonLabels...) // in millisconds
}

// updateCpuUsage updates CpuUsage by a value between 0 and CpuUsageUpperBound every SDK call.
func (rmc *randomMetricCollector) updateCpuUsage(ctx context.Context, cfg Config) {
	min := 0
	max := int(cfg.CpuUsageUpperBound)
	if err := rmc.meter.RegisterCallback(
		[]instrument.Asynchronous{
			rmc.cpuUsage,
		},
		// SDK periodically calls this function to collect data.
		func(ctx context.Context) {
			cpuUsage := int64(rand.Intn(max-min) + min)
			rmc.cpuUsage.Observe(ctx, cpuUsage, randomMetricCommonLabels...)
		},
	); err != nil {
		panic(err)
	}
}

// updateTotalHeapSize updates HeapSize by a value between 0 and TotalHeapSizeUpperBound every SDK call.
func (rmc *randomMetricCollector) updateTotalHeapSize(ctx context.Context, cfg Config) {
	min := 0
	max := int(cfg.TotalHeapSizeUpperBound)
	if err := rmc.meter.RegisterCallback(
		[]instrument.Asynchronous{
			rmc.heapSize,
		},
		// SDK periodically calls this function to collect data.
		func(ctx context.Context) {
			totalHeapSize := int64(rand.Intn(max-min) + min)
			rmc.heapSize.Observe(ctx, totalHeapSize, randomMetricCommonLabels...)
		},
	); err != nil {
		panic(err)
	}
}

// updateThreadsActive updates ThreadsActive by a value between 0 and 10 in increments or decrements of 1 based on previous value.
func (rmc *randomMetricCollector) updateThreadsActive(ctx context.Context, cfg Config) {
	if threadsBool {
		if threadsActive < int64(cfg.ThreadsActiveUpperBound) {
			rmc.threadsActive.Add(ctx, 1, randomMetricCommonLabels...)
			threadsActive++
		} else {
			threadsBool = false
			threadsActive--
		}

	} else {
		if threadsActive > 0 {
			rmc.threadsActive.Add(ctx, -1, randomMetricCommonLabels...)
			threadsActive--
		} else {
			threadsBool = true
			threadsActive++
		}
	}
}
