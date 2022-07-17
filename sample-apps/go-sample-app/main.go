package main

import (
	"context"
	"fmt"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/aws-otel-commnunity/sample-apps/go-sample-app/collection"
	"github.com/gorilla/mux"
	"go.opentelemetry.io/contrib/instrumentation/github.com/gorilla/mux/otelmux"
	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/exporters/otlp/otlpmetric"
	"go.opentelemetry.io/otel/exporters/otlp/otlpmetric/otlpmetrichttp"
	"go.opentelemetry.io/otel/metric/global"
	controller "go.opentelemetry.io/otel/sdk/metric/controller/basic"
	"go.opentelemetry.io/otel/sdk/metric/export/aggregation"
	processor "go.opentelemetry.io/otel/sdk/metric/processor/basic"
	selector "go.opentelemetry.io/otel/sdk/metric/selector/simple"
)

func main() {

	cfg := collection.GetConfiguration()
	ctx := context.Background()
	shutdown := startClient(ctx)
	defer shutdown()

	rmc := collection.NewRandomMetricCollector()
	rmc.UpdateMetricsClient(ctx, *cfg)

	fmt.Println("Reporting measurements to locahost:3418...")
	ch := make(chan os.Signal, 3)
	signal.Notify(ch, syscall.SIGINT, syscall.SIGQUIT, syscall.SIGTERM)
	<-ch

	r := mux.NewRouter()
	r.Use(otelmux.Middleware("my-server"))

	// Three endpoints we are using; WIP not complete
	r.HandleFunc("/aws-sdk-call", http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {

	}))

	r.HandleFunc("/outgoing-http-call", http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {

	}))

	http.Handle("/outgoing-sampleapp", http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {

	}))

}

// Function that creates and returns a New client with certain options
// In this case we are sending insecure options (http instead of https)
func otlpmetricClient(endpoint string) otlpmetric.Client {
	options := []otlpmetrichttp.Option{
		otlpmetrichttp.WithInsecure(),
		otlpmetrichttp.WithEndpoint(endpoint),
	}

	return otlpmetrichttp.NewClient(options...)
}

func startClient(ctx context.Context) func() {
	endpoint := os.Getenv("OTLP_EXPORTER_OTLP_ENDPOINT")
	if endpoint == "" {
		endpoint = "0.0.0.0:4318"
	}
	cumulativeSelector := aggregation.CumulativeTemporalitySelector()
	metricExp, err := otlpmetric.New(ctx, otlpmetricClient(endpoint), otlpmetric.WithMetricAggregationTemporalitySelector(cumulativeSelector))
	if err != nil {
		//Logs here
		fmt.Println(err)
	}
	ctrl := controller.New(
		processor.NewFactory(
			selector.NewWithHistogramDistribution(),
			metricExp,
		),
		controller.WithExporter(metricExp),
		controller.WithCollectPeriod(3*time.Second), // Same as default
	)
	if err := ctrl.Start(ctx); err != nil {
		// Logs here
		fmt.Println(err)
	}
	global.SetMeterProvider(ctrl)
	// Pass function to shutdown the controller in a defer statement
	return func() {
		cxt, cancel := context.WithTimeout(ctx, time.Second)
		defer cancel()

		// pushes any last exports to the receiver
		if err := ctrl.Stop(cxt); err != nil {
			otel.Handle(err)
		}
	}
}
