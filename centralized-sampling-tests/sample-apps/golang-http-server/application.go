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
	sampler "go.opentelemetry.io/contrib/samplers/aws/xray"
	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/attribute"
	"go.opentelemetry.io/otel/exporters/otlp/otlptrace/otlptracegrpc"
	"go.opentelemetry.io/otel/sdk/trace"
	oteltrace "go.opentelemetry.io/otel/trace"

	"google.golang.org/grpc"
)

func getSampledSpanCount(name string, totalSpans string, attributes []attribute.KeyValue) int {
	tracer := otel.Tracer(name)

	var sampleCount = 0
	totalSamples, err := strconv.Atoi(totalSpans)
	if err != nil {
		log.Println(err)
		return -1
	}

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
		_, err := w.Write([]byte("healthcheck"))
		if err != nil {
			log.Println(err)
		}
	}))

	http.Handle("/getSampled", http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		serviceName := r.Header.Get("Service_name")
		totalSpans := r.Header.Get("Totalspans")

		var attributes = []attribute.KeyValue{
			attribute.KeyValue{"http.method", attribute.StringValue(r.Method)},
			attribute.KeyValue{"http.url", attribute.StringValue("http://localhost:8080/getSampled")},
			attribute.KeyValue{"http.route", attribute.StringValue("/getSampled")},
			attribute.KeyValue{"http.target", attribute.StringValue("/getSampled")},
		}

		var totalSampled = getSampledSpanCount(serviceName, totalSpans, attributes)
		_, err := w.Write([]byte(strconv.Itoa(totalSampled)))
		if err != nil {
			log.Println(err)
		}
	}))

	http.Handle("/importantEndpoint", http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		serviceName := r.Header.Get("Service_name")
		totalSpans := r.Header.Get("Totalspans")

		var attributes = []attribute.KeyValue{
			attribute.KeyValue{"http.method", attribute.StringValue("GET")},
			attribute.KeyValue{"http.url", attribute.StringValue("http://localhost:8080/importantEndpoint")},
			attribute.KeyValue{"http.route", attribute.StringValue("/importantEndpoint")},
			attribute.KeyValue{"http.target", attribute.StringValue("/importantEndpoint")},
		}

		var totalSampled = getSampledSpanCount(serviceName, totalSpans, attributes)
		_, err := w.Write([]byte(strconv.Itoa(totalSampled)))
		if err != nil {
			log.Println(err)
		}
	}))

	listenAddress := os.Getenv("LISTEN_ADDRESS")
	if listenAddress == "" {
		listenAddress = "localhost:8080"
	}
	log.Println("App is listening on %s !", listenAddress)
	_ = http.ListenAndServe(listenAddress, nil)
}

func start_xray() (bool, error) {
	ctx := context.Background()

	exporterEndpoint := os.Getenv("OTEL_EXPORTER_OTLP_ENDPOINT")
	if exporterEndpoint == "" {
		exporterEndpoint = "localhost:4317"
	}

	log.Println("Creating new OTLP trace exporter...")
	traceExporter, err := otlptracegrpc.New(ctx, otlptracegrpc.WithInsecure(), otlptracegrpc.WithEndpoint(exporterEndpoint), otlptracegrpc.WithDialOption(grpc.WithBlock()))
	if err != nil {
		log.Fatalf("Failed to create new OTLP trace exporter: %v", err)
		return false, err
	}

	idg := xray.NewIDGenerator()

	samplerEndpoint := os.Getenv("XRAY_ENDPOINT")
	if samplerEndpoint == "" {
		samplerEndpoint = "http://localhost:2000"
	}
	endpointUrl, err := url.Parse(samplerEndpoint)

	res, err := sampler.NewRemoteSampler(ctx, "aws-otel-integ-test", "", sampler.WithEndpoint(*endpointUrl), sampler.WithSamplingRulesPollingInterval(10*time.Second))
	if err != nil {
		log.Fatalf("Failed to create new XRay Remote Sampler: %v", err)
		return false, err
	}

	// attach remote sampler to tracer provider
	tp := trace.NewTracerProvider(
		trace.WithSampler(res),
		trace.WithBatcher(traceExporter),
		trace.WithIDGenerator(idg),
	)

	otel.SetTracerProvider(tp)
	otel.SetTextMapPropagator(xray.Propagator{})

	return true, nil
}

func main() {
	log.Println("Starting Golang OTel Sample App...")

	_, err := start_xray()
	if err != nil {
		log.Fatalf("Failed to start XRay: %v", err)
		return
	}

	webServer()
}
