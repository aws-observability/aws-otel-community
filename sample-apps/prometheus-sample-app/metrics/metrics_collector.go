package metrics

import (
	"flag"
	"fmt"
	"log"
	"math"
	"math/rand"
	"time"

	"github.com/prometheus/client_golang/prometheus"
)

var (
	normDomain = flag.Float64("normal.domain", 0.0002, "The domain for the normal distribution.")
	normMean   = flag.Float64("normal.mean", 0.00001, "The mean for the normal distribution.")
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

func (mc *metricCollector) updateCounter() {
	for _, c := range mc.counters {
		for i := 0; i < mc.datapointCount; i++ {
			labels := datapointLabels(i, mc.labelKeys, mc.labelValues)
			c.With(labels).Add(rand.Float64())
		}
	}
}

func (mc *metricCollector) updateGauge() {
	for _, c := range mc.gauges {
		for i := 0; i < mc.datapointCount; i++ {
			labels := datapointLabels(i, mc.labelKeys, mc.labelValues)
			c.With(labels).Set(rand.Float64())
		}
	}
}

func (mc *metricCollector) updateHistogram() {
	for idx := 0; idx < len(mc.histograms); idx++ {
		for i := 0; i < mc.datapointCount; i++ {
			lowerBound := math.Mod(rand.Float64(), 1)
			increment := math.Mod(rand.Float64(), 0.05)
			labels := datapointLabels(i, mc.labelKeys, mc.labelValues)
			for j := lowerBound; j < 1; j += increment {
				mc.histograms[idx].With(labels).Observe(j)
			}
		}

	}
}

func (mc *metricCollector) updateSummary() {
	for idx := 0; idx < len(mc.summarys); idx++ {
		for i := 0; i < mc.datapointCount; i++ {
			labels := datapointLabels(i, mc.labelKeys, mc.labelValues)
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

func (mc *metricCollector) registerSummary(count int) {
	for idx := 0; idx < count; idx++ {
		namespace := "test"
		summary := prometheus.NewSummaryVec(
			prometheus.SummaryOpts{
				Namespace:  namespace,
				Name:       fmt.Sprintf("summary%v", idx),
				Help:       "This is my summary",
				Objectives: map[float64]float64{0.5: 0.05, 0.9: 0.01, 0.99: 0.001},
			},
			append([]string{"datapoint_id"}, mc.labelKeys...))
		promRegistry.MustRegister(summary)
		mc.summarys = append(mc.summarys, summary)
	}
}

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

func datapointLabels(datapointID int, labelKeys []string, labelValues []string) prometheus.Labels {
	labelsDatapoint := prometheus.Labels{
		"datapoint_id": fmt.Sprintf("%v", datapointID),
	}
	for idx, key := range labelKeys {
		labelsDatapoint[key] = labelValues[idx]
	}
	return labelsDatapoint
}
