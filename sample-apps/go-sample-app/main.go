package main

import (
	"context"
	"fmt"
	"log"
	"math/rand"
	"net"
	"net/http"
	"time"

	"github.com/aws-otel-commnunity/sample-apps/go-sample-app/collection"
	"github.com/gorilla/mux"
	"go.opentelemetry.io/contrib/instrumentation/github.com/gorilla/mux/otelmux"
	"go.opentelemetry.io/otel/metric/global"
)

// This sample application is in conformance with the ADOT SampleApp requirements spec.
func main() {
	ctx := context.Background()

	// The seed for 'random' values used in this applicaiton
	rand.Seed(time.Now().UnixNano())

	// Client starts
	shutdown, err := collection.StartClient(ctx)
	if err != nil {
		log.Fatal(err)
	}
	defer shutdown(ctx)

	// (Metric related) Creates and configures random based metrics based on a configuration file (config.yaml).
	mp := global.MeterProvider()
	cfg := collection.GetConfiguration()

	// (Metric related) Starts request based metric and registers necessary callbacks
	rmc := collection.NewRandomMetricCollector(mp)
	rmc.RegisterMetricsClient(ctx, *cfg)
	rqmc := collection.NewRequestBasedMetricCollector(ctx, *cfg, mp)
	rqmc.StartTotalRequestCallback()

	s3Client, err := collection.NewS3Client()
	if err != nil {
		fmt.Println(err)
	}
	// Creates a router, client and web server with several endpoints
	r := mux.NewRouter()
	client := http.Client{
		// Transport: otelhttp.NewTransport(http.DefaultTransport),
	}

	r.Use(otelmux.Middleware("Go-Sampleapp-Server"))

	// Three endpoints
	r.HandleFunc("/aws-sdk-call", func(w http.ResponseWriter, r *http.Request) {
		collection.AwsSdkCall(w, r, &rqmc, s3Client)
	})

	r.HandleFunc("/outgoing-http-call", func(w http.ResponseWriter, r *http.Request) {
		collection.OutgoingHttpCall(w, r, client, &rqmc)
	})

	r.HandleFunc("/outgoing-sampleapp", func(w http.ResponseWriter, r *http.Request) {
		collection.OutgoingSampleApp(w, r, client, &rqmc)
	})

	// Root endpoint
	http.Handle("/", r)

	srv := &http.Server{
		Addr: net.JoinHostPort(cfg.Host, cfg.Port),
	}
	fmt.Println("Listening on port:", srv.Addr)
	log.Fatal(srv.ListenAndServe())

}
