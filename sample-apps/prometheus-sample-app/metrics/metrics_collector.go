package metrics

import (
	"fmt"
	"log"
	"math/rand"
	"time"

	"github.com/prometheus/client_golang/prometheus"
)

type metricCollector struct {
	counters       []*prometheus.CounterVec
	gauges         []*prometheus.GaugeVec
	histograms     []*prometheus.HistogramVec
	summarys       []*prometheus.SummaryVec
	datapointCount int
	labelValues    []string
	labelKeys      []string
	interval       time.Duration
}

var (
	promRegistry = prometheus.NewRegistry() // local Registry so we don't get Go metrics, etc.
)

func newMetricCollector() metricCollector {
	return metricCollector{}
}

// Periodically record metric values and labels for counter metric.
func (mc *metricCollector) updateCounter() {
	for _, c := range mc.counters {
		for i := 0; i < mc.datapointCount; i++ {
			labels := datapointLabels(i, mc.labelKeys, mc.labelValues)
			c.With(labels).Add(rand.Float64())
		}
	}
}

// Periodically record metric values and labels for gauge metric.
func (mc *metricCollector) updateGauge() {
	for _, c := range mc.gauges {
		for i := 0; i < mc.datapointCount; i++ {
			labels := datapointLabels(i, mc.labelKeys, mc.labelValues)
			c.With(labels).Set(rand.Float64())
		}
	}
}

// Periodically record metric values and labels for histogram metric.
func (mc *metricCollector) updateHistogram() {
	for idx := 0; idx < len(mc.histograms); idx++ {
		for i := 0; i < mc.datapointCount; i++ {
			labels := datapointLabels(i, mc.labelKeys, mc.labelValues)
			// generate fictional values for histogram with random normal distribution
			v := (rand.NormFloat64() * *normDomain) + *normMean
			mc.histograms[idx].With(labels).Observe(v)
		}
	}
}

// Periodically record metric values and labels for summary metric.
func (mc *metricCollector) updateSummary() {
	for idx := 0; idx < len(mc.summarys); idx++ {
		for i := 0; i < mc.datapointCount; i++ {
			labels := datapointLabels(i, mc.labelKeys, mc.labelValues)
			// generate fictional values for summary with random normal distribution
			v := (rand.NormFloat64() * *normDomain) + *normMean
			mc.summarys[idx].With(labels).Observe(v)
		}
	}
}

func updateLoop(update func(), delay time.Duration) {
	go func() {
		for {
			time.Sleep(delay)
			log.Println("Updating metrics ...")
			update()
		}
	}()
}

func (mc *metricCollector) updateMetrics() {

	if mc.counters != nil {
		updateLoop(mc.updateCounter, mc.interval)
	}
	if mc.gauges != nil {
		updateLoop(mc.updateGauge, mc.interval)
	}

	if mc.histograms != nil {
		updateLoop(mc.updateHistogram, mc.interval)
	}
	if mc.summarys != nil {
		updateLoop(mc.updateSummary, mc.interval)
	}

}

// Register the counter and label keys with Prometheus's default registry.
func (mc *metricCollector) registerCounter(count int) {
	for idx := 0; idx < count; idx++ {
		namespace := "test"
		counter := prometheus.NewCounterVec(
			prometheus.CounterOpts{
				Namespace: namespace,
				Name:      fmt.Sprintf("counter%v", idx),
				Help:      "This is my counter",
			},
			append([]string{"datapoint_id"}, mc.labelKeys...))
		promRegistry.MustRegister(counter)
		mc.counters = append(mc.counters, counter)
	}
}

// Register the gauge and label keys with Prometheus's default registry.
func (mc *metricCollector) registerGauge(count int) {
	for idx := 0; idx < count; idx++ {
		namespace := "test"
		gauge := prometheus.NewGaugeVec(
			prometheus.GaugeOpts{
				Namespace: namespace,
				Name:      fmt.Sprintf("gauge%v", idx),
				Help:      "This is my gauge",
			},
			append([]string{"datapoint_id"}, mc.labelKeys...))
		promRegistry.MustRegister(gauge)
		mc.gauges = append(mc.gauges, gauge)
	}
}

// Register the histogram and label keys with Prometheus's default registry.
func (mc *metricCollector) registerHistogram(count int) {
	for idx := 0; idx < count; idx++ {
		namespace := "test"
		histogram := prometheus.NewHistogramVec(
			prometheus.HistogramOpts{
				Namespace: namespace,
				Name:      fmt.Sprintf("histogram%v", idx),
				Help:      "This is my histogram",
				Buckets:   []float64{0.1, 0.5, 1},
			},
			append([]string{"datapoint_id"}, mc.labelKeys...))
		promRegistry.MustRegister(histogram)
		mc.histograms = append(mc.histograms, histogram)
	}
}

// Register the summary and label keys with Prometheus's default registry.
func (mc *metricCollector) registerSummary(count int) {
	for idx := 0; idx < count; idx++ {
		namespace := "test"
		summary := prometheus.NewSummaryVec(
			prometheus.SummaryOpts{
				Namespace: namespace,
				Name:      fmt.Sprintf("summary%v", idx),
				Help:      "This is my summary",
				Objectives: map[float64]float64{
					0.1:  0.5,
					0.5:  0.5,
					0.99: 0.5,
				},
			},
			append([]string{"datapoint_id"}, mc.labelKeys...))
		promRegistry.MustRegister(summary)
		mc.summarys = append(mc.summarys, summary)
	}
}

// Method to generate constant labels for each metric as per given label count.
// This method uses foo and bar strings as key value pair
func generateLabels(labelCount int) ([]string, []string) {
	labelKeys := make([]string, labelCount, labelCount)
	for idx := 0; idx < labelCount; idx++ {
		labelKeys[idx] = fmt.Sprintf("foo_%v", idx)
	}
	labelValues := make([]string, labelCount, labelCount)
	for idx := 0; idx < labelCount; idx++ {
		labelValues[idx] = fmt.Sprintf("bar_%v", idx)
	}
	return labelValues, labelKeys
}

// Method to generate unique data-point label for each metric
func datapointLabels(datapointID int, labelKeys []string, labelValues []string) prometheus.Labels {
	labelsDatapoint := prometheus.Labels{
		"datapoint_id": fmt.Sprintf("%v", datapointID),
	}
	for idx, key := range labelKeys {
		labelsDatapoint[key] = labelValues[idx]
	}
	return labelsDatapoint
}
