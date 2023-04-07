package main

import (
	"context"
	"log"
	"net/http"
	"net/url"
	"os"
	"strconv"
	"time"

	"go.opentelemetry.io/contrib/propagators/aws/xray"
	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/exporters/otlp/otlptrace/otlptracegrpc"
	sample "go.opentelemetry.io/contrib/samplers/aws/xray"
	"go.opentelemetry.io/otel/attribute"
	"go.opentelemetry.io/otel/sdk/trace"
	oteltrace "go.opentelemetry.io/otel/trace"

	"google.golang.org/grpc"
)

func getSampledSpanCount(name string, totalSpans string, attributes []attribute.KeyValue) int {
	tracer := otel.Tracer(name)

	var sampleCount = 0
	totalSamples, _ := strconv.Atoi(totalSpans)
	ctx := context.Background()

	for i := 0; i < totalSamples; i++ {
		_, span := tracer.Start(ctx, name, oteltrace.WithSpanKind(oteltrace.SpanKindServer), oteltrace.WithAttributes(attributes...))

		if span.SpanContext().IsSampled() {
			sampleCount++
		}

		span.End()
	}

	return sampleCount
}

func webServer() {
	http.Handle("/", http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		_, _ = w.Write([]byte("healthcheck"))
	}))

	http.Handle("/getSampled", http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		userAttribute := r.Header["User"][0]
		required := r.Header["Required"][0]

		var attributes = []attribute.KeyValue{
			attribute.KeyValue{"http.method", attribute.StringValue(r.Method)},
			attribute.KeyValue{"http.url", attribute.StringValue("http://localhost:8080/getSampled")},
			attribute.KeyValue{"user", attribute.StringValue(userAttribute)},
			attribute.KeyValue{"http.route", attribute.StringValue("/getSampled")},
			attribute.KeyValue{"required", attribute.StringValue(required)},
			attribute.KeyValue{"http.target", attribute.StringValue("/getSampled")},
		}

		var totalSampled = getSampledSpanCount(r.Header["Service_name"][0], r.Header["Totalspans"][0], attributes)
		_, _ = w.Write([]byte(strconv.Itoa(totalSampled)))
	}))

	http.Handle("/importantEndpoint", http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		userAttribute := r.Header["User"][0]
		required := r.Header["Required"][0]

		var attributes = []attribute.KeyValue{
			attribute.KeyValue{"http.method", attribute.StringValue("GET")},
			attribute.KeyValue{"http.url", attribute.StringValue("http://localhost:8080/importantEndpoint")},
			attribute.KeyValue{"user", attribute.StringValue(userAttribute)},
			attribute.KeyValue{"http.route", attribute.StringValue("/importantEndpoint")},
			attribute.KeyValue{"required", attribute.StringValue(required)},
			attribute.KeyValue{"http.target", attribute.StringValue("/importantEndpoint")},
		}

		var totalSampled = getSampledSpanCount(r.Header["Service_name"][0], r.Header["Totalspans"][0], attributes)
		_, _ = w.Write([]byte(strconv.Itoa(totalSampled)))
	}))

	listenAddress := os.Getenv("LISTEN_ADDRESS")
	if listenAddress == "" {
		listenAddress = "0.0.0.0:8080"
	}
	log.Println("App is listening on %s !", listenAddress)
	_ = http.ListenAndServe(listenAddress, nil)
}

func start_xray() {
	ctx := context.Background()

	exporterEndpoint := os.Getenv("OTEL_EXPORTER_OTLP_ENDPOINT")
	if exporterEndpoint == "" {
		exporterEndpoint = "0.0.0.0:4317"
	}

	log.Println("Creating new OTLP trace exporter...")
	traceExporter, err := otlptracegrpc.New(ctx, otlptracegrpc.WithInsecure(), otlptracegrpc.WithEndpoint(exporterEndpoint), otlptracegrpc.WithDialOption(grpc.WithBlock()))
	if err != nil {
		log.Fatalf("failed to create new OTLP trace exporter: %v", err)
	}

	idg := xray.NewIDGenerator()

	samplerEndpoint := os.Getenv("XRAY_ENDPOINT")
	if samplerEndpoint == "" {
		samplerEndpoint = "http://0.0.0.0:2000"
	}
	endpointUrl, err := url.Parse(samplerEndpoint)

	res, err := sample.NewRemoteSampler(ctx, "aws-otel-integ-test", "", sample.WithEndpoint(*endpointUrl), sample.WithSamplingRulesPollingInterval(10*time.Second))
	if err != nil {
		return
	}

	// attach remote sampler to tracer provider
	tp := trace.NewTracerProvider(
		trace.WithSampler(res),
		trace.WithBatcher(traceExporter),
		trace.WithIDGenerator(idg),
	)

	otel.SetTracerProvider(tp)
	otel.SetTextMapPropagator(xray.Propagator{})
}

func main() {
	log.Println("Starting Golang OTel Sample App...")

	start_xray()
	webServer()
}
