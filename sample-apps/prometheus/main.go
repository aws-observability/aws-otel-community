package main

import (
	"flag"
	"fmt"
	"math/rand"
	"net/http"
	"time"

	"log"

	"github.com/prometheus/client_golang/prometheus/promhttp"
)

func main() {
	var listen string
	var metricCount int

	flag.StringVar(&listen, "listen_address", "0.0.0.0:8080", "server listening address")
	flag.IntVar(&metricCount, "metric_count", 1, "number of samples to produce per metric type")

	flag.Parse()

	mc := newMetricCollector(metricCount)

	rand.Seed(time.Now().Unix())

	mc.registerMetrics()
	go mc.updateMetrics()

	log.Println("Serving on address: " + listen)
	log.Println("Producing " + fmt.Sprintf("%d", mc.metricCount) + " metrics per type")

	http.HandleFunc("/", healthCheckHandler)
	http.Handle("/metrics", promhttp.HandlerFor(promRegistry, promhttp.HandlerOpts{}))

	log.Fatal(http.ListenAndServe(listen, nil))
}

func healthCheckHandler(w http.ResponseWriter, r *http.Request) {
	fmt.Fprintf(w, "healthy")
}
