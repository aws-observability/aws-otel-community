package main

import (
	"context"
	"net"
	"net/http"

	"github.com/aws-otel-commnunity/sample-apps/go-sample-app/collection"
	"github.com/gorilla/mux"
	"go.opentelemetry.io/contrib/instrumentation/github.com/gorilla/mux/otelmux"
)

// This sample application is in conformance with the ADOT SampleApp requirements spec.
func main() {
	ctx := context.Background()

	// Creates and configures random based metrics based on a configuration file (config.yaml).
	cfg := collection.GetConfiguration()
	rmc := collection.NewRandomMetricCollector()
	rmc.RegisterMetricsClient(ctx, *cfg)
	collection.StartClient(ctx)

	// Creates a router and web server with several endpoints
	r := mux.NewRouter()
	r.Use(otelmux.Middleware("Go-Sampleapp-Server"))

	r.HandleFunc("/outgoing-http-call", collection.OutgoingHttpCall)
	r.HandleFunc("/aws-sdk-call", collection.AwsSdkCall)
	r.HandleFunc("/outgoing-sampleapp", collection.OutgoingSampleApp)

	http.Handle("/", r)

	srv := &http.Server{
		Addr: net.JoinHostPort(cfg.Host, cfg.Port),
	}
	srv.ListenAndServe()

}
