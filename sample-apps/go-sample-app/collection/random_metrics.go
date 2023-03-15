package collection

import (
	"context"
	"fmt"
	"math/rand"
	"time"

	"go.opentelemetry.io/otel/metric"
	"go.opentelemetry.io/otel/metric/instrument"
)

var (
	threadsActive int64 = 0
	threadsBool         = true
)

// randomMetricCollector contains all the random based metric instruments.
type randomMetricCollector struct {
	time_alive      instrument.Int64Counter
	cpu_usage       instrument.Int64ObservableGauge
	total_heap_size instrument.Int64ObservableUpDownCounter
	threads_active  instrument.Int64UpDownCounter
	meter           metric.Meter
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
	time_alive, err := rmc.meter.Int64Counter(
		time_alive+testingId,
		instrument.WithDescription("Total amount of time that the application has been alive"),
		instrument.WithUnit("ms"),
	)
	if err != nil {
		fmt.Println(err)
	}
	rmc.time_alive = time_alive
}

// registerCpuUsage registers an Asynchronous Gauge called CpuUsage.
func (rmc *randomMetricCollector) registerCpuUsage() {
	cpu_usage, err := rmc.meter.Int64ObservableGauge(
		cpu_usage+testingId,
		instrument.WithDescription("Cpu usage percent"),
		instrument.WithUnit("1"),
	)
	if err != nil {
		fmt.Println(err)
	}
	rmc.cpu_usage = cpu_usage

}

// registerHeapSize registers an Asynchronous UpDownCounter called HeapSize.
func (rmc *randomMetricCollector) registerHeapSize() {
	total_heap_size, err := rmc.meter.Int64ObservableUpDownCounter(
		total_heap_size+testingId,
		instrument.WithDescription("The current total heap size"),
		instrument.WithUnit("By"),
	)
	if err != nil {
		fmt.Println(err)
	}
	rmc.total_heap_size = total_heap_size

}

// registerThreadsActive registers a Synchronous UpDownCounter called ThreadsActive.
func (rmc *randomMetricCollector) registerThreadsActive() {
	threads_active, err := rmc.meter.Int64UpDownCounter(
		threads_active+testingId,
		instrument.WithUnit("1"),
		instrument.WithDescription("The total amount of threads active"),
	)
	if err != nil {
		fmt.Println(err)
	}
	rmc.threads_active = threads_active
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
	rmc.time_alive.Add(ctx, cfg.TimeAliveIncrementer*1000, randomMetricCommonLabels...) // in millisconds
}

// updateCpuUsage updates CpuUsage by a value between 0 and CpuUsageUpperBound every SDK call.
func (rmc *randomMetricCollector) updateCpuUsage(ctx context.Context, cfg Config) {
	min := 0
	max := int(cfg.CpuUsageUpperBound)
	if _, err := rmc.meter.RegisterCallback(
		// SDK periodically calls this function to collect data.
		func(ctx context.Context, o metric.Observer) error {
			cpuUsage := int64(rand.Intn(max-min) + min)
			o.ObserveInt64(rmc.cpu_usage, cpuUsage, randomMetricCommonLabels...)

			return nil
		},
		rmc.cpu_usage,
	); err != nil {
		panic(err)
	}
}

// updateTotalHeapSize updates HeapSize by a value between 0 and TotalHeapSizeUpperBound every SDK call.
func (rmc *randomMetricCollector) updateTotalHeapSize(ctx context.Context, cfg Config) {
	min := 0
	max := int(cfg.TotalHeapSizeUpperBound)
	if _, err := rmc.meter.RegisterCallback(
		// SDK periodically calls this function to collect data.
		func(ctx context.Context, o metric.Observer) error {
			totalHeapSize := int64(rand.Intn(max-min) + min)
			o.ObserveInt64(rmc.total_heap_size, totalHeapSize, randomMetricCommonLabels...)

			return nil
		},
		rmc.total_heap_size,
	); err != nil {
		panic(err)
	}
}

// updateThreadsActive updates ThreadsActive by a value between 0 and 10 in increments or decrements of 1 based on previous value.
func (rmc *randomMetricCollector) updateThreadsActive(ctx context.Context, cfg Config) {
	if threadsBool {
		if threadsActive < int64(cfg.ThreadsActiveUpperBound) {
			rmc.threads_active.Add(ctx, 1, randomMetricCommonLabels...)
			threadsActive++
		} else {
			threadsBool = false
			threadsActive--
		}

	} else {
		if threadsActive > 0 {
			rmc.threads_active.Add(ctx, -1, randomMetricCommonLabels...)
			threadsActive--
		} else {
			threadsBool = true
			threadsActive++
		}
	}
}
