package collection

import (
	"context"
	"os"
	"time"

	"go.opentelemetry.io/contrib/propagators/aws/xray"
	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/attribute"
	"go.opentelemetry.io/otel/exporters/otlp/otlpmetric/otlpmetricgrpc"
	"go.opentelemetry.io/otel/exporters/otlp/otlptrace/otlptracegrpc"
	semconv "go.opentelemetry.io/otel/semconv/v1.17.0"

	"go.opentelemetry.io/otel/sdk/metric"
	"go.opentelemetry.io/otel/sdk/resource"
	sdktrace "go.opentelemetry.io/otel/sdk/trace"
)

var cfg = GetConfiguration()

const serviceName = "go"

var testingId = ""

var tracer = otel.Tracer("github.com/aws-otel-commnunity/sample-apps/go-sample-app/collection")

// Names for metric instruments
const time_alive = "time_alive"
const cpu_usage = "cpu_usage"
const total_heap_size = "total_heap_size"
const threads_active = "threads_active"
const total_bytes_sent = "total_bytes_sent"
const total_api_requests = "total_api_requests"
const latency_time = "latency_time"

// Common attributes for traces and metrics (random, request)
var requestMetricCommonLabels = []attribute.KeyValue{
	attribute.String("signal", "metric"),
	attribute.String("language", serviceName),
	attribute.String("metricType", "request"),
}

var randomMetricCommonLabels = []attribute.KeyValue{
	attribute.String("signal", "metric"),
	attribute.String("language", serviceName),
	attribute.String("metricType", "random"),
}

var traceCommonLabels = []attribute.KeyValue{
	attribute.String("signal", "trace"),
	attribute.String("language", serviceName),
	attribute.String("host", cfg.Host),
	attribute.String("port", cfg.Port),
}

// StartClient starts the traces and metrics providers which periodically collects signals and exports them.
// Trace exporter and Metric exporter are both configured.
func StartClient(ctx context.Context) (func(context.Context) error, error) {

	if id, present := os.LookupEnv("INSTANCE_ID"); present {
		testingId = "_" + id
	}
	res := resource.NewWithAttributes(
		semconv.SchemaURL,
		semconv.ServiceName("go-sample-app"),
	)
	if _, present := os.LookupEnv("OTEL_RESOURCE_ATTRIBUTES"); present {
		res, _ = resource.New(ctx, resource.WithFromEnv())
	}

	// Setup trace related
	tp, err := setupTraceProvider(ctx, res)
	if err != nil {
		return nil, err
	}

	otel.SetTracerProvider(tp)
	otel.SetTextMapPropagator(xray.Propagator{}) // Set AWS X-Ray propagator

	exp, err := otlpmetricgrpc.New(ctx, otlpmetricgrpc.WithInsecure())
	if err != nil {
		return nil, err
	}
	meterProvider := metric.NewMeterProvider(metric.WithResource(res), metric.WithReader(metric.NewPeriodicReader(exp)))

	otel.SetMeterProvider(meterProvider)

	return func(context.Context) (err error) {
		ctx, cancel := context.WithTimeout(ctx, time.Second)
		defer cancel()

		defer func() {
			tpErr := tp.Shutdown(ctx)
			if tpErr != nil {
				err = tpErr
			}
		}()
		// pushes any last exports to the receiver
		mpErr := meterProvider.Shutdown(ctx)
		if mpErr != nil {
			err = mpErr
		}
		return
	}, nil
}

// setupTraceProvider configures a trace exporter and an AWS X-Ray ID Generator.
func setupTraceProvider(ctx context.Context, res *resource.Resource) (*sdktrace.TracerProvider, error) {
	// INSECURE !! NOT TO BE USED FOR ANYTHING IN PRODUCTION
	traceExporter, err := otlptracegrpc.New(ctx, otlptracegrpc.WithInsecure())

	if err != nil {
		return nil, err
	}

	idg := xray.NewIDGenerator()

	tp := sdktrace.NewTracerProvider(
		sdktrace.WithSampler(sdktrace.AlwaysSample()),
		sdktrace.WithBatcher(traceExporter),
		sdktrace.WithResource(res),
		sdktrace.WithIDGenerator(idg),
	)
	return tp, nil
}
