package main

import (
	"flag"
	"fmt"
	"math/rand"
	"net/http"
	"time"

	"log"

	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promhttp"
)

var (
	promRegistry = prometheus.NewRegistry() // local Registry so we don't get Go metrics, etc.
)

type metricBatch struct {
	counter   prometheus.Counter
	gauge     prometheus.Gauge
	histogram prometheus.Histogram
	summary   prometheus.Summary
}

func main() {
	mc := newMetricCollector()

	addressPtr := flag.String("listen_address", "0.0.0.0:8080", "server listening address")
	metricCountPtr := flag.Int("metric_count", 1, "number of samples to produce per metric type")

	flag.Parse()

	address := *addressPtr
	mc.metricCount = *metricCountPtr

	rand.Seed(time.Now().Unix())

	mc.registerMetrics()
	go mc.updateMetrics()

	log.Println("Serving on address: " + address)
	log.Println("Producing " + fmt.Sprintf("%d", mc.metricCount) + " metrics per type")

	http.HandleFunc("/", healthCheckHandler)
	http.Handle("/metrics", promhttp.HandlerFor(promRegistry, promhttp.HandlerOpts{}))

	log.Fatal(http.ListenAndServe(address, nil))
}

func healthCheckHandler(w http.ResponseWriter, r *http.Request) {
	fmt.Fprintf(w, "healthy")
}
