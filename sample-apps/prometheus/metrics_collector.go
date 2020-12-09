package main

import (
	"fmt"
	"math"
	"math/rand"
	"time"

	"github.com/prometheus/client_golang/prometheus"
)

var (
	promRegistry = prometheus.NewRegistry() // local Registry so we don't get Go metrics, etc.
)

type metricCollector struct {
	counters   []prometheus.Counter
	gauges     []prometheus.Gauge
	histograms []prometheus.Histogram
	summarys   []prometheus.Summary

	metricCount int
}

func newMetricCollector(metricCount int) metricCollector {
	return metricCollector{metricCount: metricCount}
}

func (mc *metricCollector) updateMetrics() {
	for {
		for idx := 0; idx < mc.metricCount; idx++ {
			mc.counters[idx].Add(rand.Float64())
			mc.gauges[idx].Add(rand.Float64())
			lowerBound := math.Mod(rand.Float64(), 1)
			increment := math.Mod(rand.Float64(), 0.05)
			for i := lowerBound; i < 1; i += increment {
				mc.histograms[idx].Observe(i)
				mc.summarys[idx].Observe(i)
			}
		}
		// generate new metrics in 30 second intervals
		time.Sleep(time.Second * 30)
	}
}

func (mc *metricCollector) registerMetrics() {
	for idx := 0; idx < mc.metricCount; idx++ {
		namespace := "test"
		counter := prometheus.NewCounter(
			prometheus.CounterOpts{
				Namespace: namespace,
				Name:      fmt.Sprintf("counter%v", idx),
				Help:      "This is my counter",
				// labels can be added like this
				// ConstLabels: prometheus.Labels{
				// 	"label1": "val1",
				// },
			})
		gauge := prometheus.NewGauge(
			prometheus.GaugeOpts{
				Namespace: namespace,
				Name:      fmt.Sprintf("gauge%v", idx),
				Help:      "This is my gauge",
			})
		histogram := prometheus.NewHistogram(
			prometheus.HistogramOpts{
				Namespace: namespace,
				Name:      fmt.Sprintf("histogram%v", idx),
				Help:      "This is my histogram",
				Buckets:   []float64{0.005, 0.1, 1},
			})
		summary := prometheus.NewSummary(
			prometheus.SummaryOpts{
				Namespace: namespace,
				Name:      fmt.Sprintf("summary%v", idx),
				Help:      "This is my summary",
				Objectives: map[float64]float64{
					0.1:  0.5,
					0.5:  0.5,
					0.99: 0.5,
				},
			})

		promRegistry.MustRegister(counter)
		promRegistry.MustRegister(gauge)
		promRegistry.MustRegister(histogram)
		promRegistry.MustRegister(summary)

		mc.counters = append(mc.counters, counter)
		mc.gauges = append(mc.gauges, gauge)
		mc.histograms = append(mc.histograms, histogram)
		mc.summarys = append(mc.summarys, summary)
	}
}
