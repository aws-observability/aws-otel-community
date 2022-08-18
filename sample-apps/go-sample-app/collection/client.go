package collection

import (
	"context"
	"time"

	"go.opentelemetry.io/contrib/propagators/aws/xray"
	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/attribute"
	"go.opentelemetry.io/otel/exporters/otlp/otlpmetric"
	"go.opentelemetry.io/otel/exporters/otlp/otlpmetric/otlpmetricgrpc"
	"go.opentelemetry.io/otel/exporters/otlp/otlptrace/otlptracegrpc"
	"go.opentelemetry.io/otel/metric/global"
	semconv "go.opentelemetry.io/otel/semconv/v1.12.0"
	"google.golang.org/grpc"

	"go.opentelemetry.io/otel/sdk/metric/aggregator/histogram"
	controller "go.opentelemetry.io/otel/sdk/metric/controller/basic"
	processor "go.opentelemetry.io/otel/sdk/metric/processor/basic"
	selector "go.opentelemetry.io/otel/sdk/metric/selector/simple"
	"go.opentelemetry.io/otel/sdk/resource"
	sdktrace "go.opentelemetry.io/otel/sdk/trace"
)

const grpcEndpoint = "0.0.0.0:4317"

const serviceName = "go"

var tracer = otel.Tracer("github.com/aws-otel-commnunity/sample-apps/go-sample-app/collection")

// Names for metric instruments
const apiTimeAlive = "timeAlive"
const apiCpuUsage = "cpuUsage"
const apiTotalHeapSize = "totalHeapSize"
const apiThreadsActive = "threadsActive"
const apiTotalBytesSent = "totalBytesSent"
const apiTotalApiRequests = "totalApiRequests"
const apiLatencyTime = "latencyTime"

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
}

// StartClient starts the OTEL controller which periodically collects signals and exports them.
// Trace exporter and Metric exporter are both configured.
func StartClient(ctx context.Context) (func(context.Context) error, error) {

	// Setup trace related
	tp, err := setupTraceProvider(ctx)
	if err != nil {
		return nil, err
	}

	otel.SetTracerProvider(tp)
	otel.SetTextMapPropagator(xray.Propagator{}) // Set AWS X-Ray propagator

	// Setup metric related
	ctrl, err := setupMetricsController(ctx)
	if err != nil {
		return nil, err
	}
	global.SetMeterProvider(ctrl)

	return func(context.Context) error {
		cxt, cancel := context.WithTimeout(ctx, time.Second)
		defer cancel()

		defer func() {
			err = tp.Shutdown(ctx)
		}()
		if err != nil {
			return err
		}
		// pushes any last exports to the receiver
		if err := ctrl.Stop(cxt); err != nil {
			return err
		}
		return nil
	}, nil
}

// setupTraceProvider configures a trace exporter and an AWS X-Ray ID Generator.
func setupTraceProvider(ctx context.Context) (*sdktrace.TracerProvider, error) {
	// INSECURE !! NOT TO BE USED FOR ANYTHING IN PRODUCTION
	traceExporter, err := otlptracegrpc.New(ctx, otlptracegrpc.WithInsecure(),
		otlptracegrpc.WithEndpoint(grpcEndpoint),
		otlptracegrpc.WithDialOption(grpc.WithBlock()))
	if err != nil {
		return nil, err
	}

	idg := xray.NewIDGenerator()

	res := resource.NewWithAttributes(
		semconv.SchemaURL,
		semconv.ServiceNameKey.String("go-sample-app"), // Should have a unique name. Service name displayed in backends
	)

	tp := sdktrace.NewTracerProvider(
		sdktrace.WithSampler(sdktrace.AlwaysSample()),
		sdktrace.WithBatcher(traceExporter),
		sdktrace.WithResource(res),
		sdktrace.WithIDGenerator(idg),
	)
	return tp, nil
}

// setupMetricsController configures a metric exporter and a controller with a histogram tracking latency.
func setupMetricsController(ctx context.Context) (*controller.Controller, error) {
	metricClient := otlpmetricgrpc.NewClient(
		// INSECURE !! NOT TO BE USED FOR ANYTHING IN PRODUCTION
		otlpmetricgrpc.WithInsecure(),
		otlpmetricgrpc.WithEndpoint(grpcEndpoint))
	metricExp, _ := otlpmetric.New(ctx, metricClient)

	controller := controller.New(
		processor.NewFactory(
			selector.NewWithHistogramDistribution(
				histogram.WithExplicitBoundaries([]float64{100, 300, 500}), // Tracking latency
			),
			metricExp,
		),
		controller.WithExporter(metricExp),
		controller.WithCollectPeriod(3*time.Second),
	)

	if err := controller.Start(ctx); err != nil {
		return nil, err
	}

	return controller, nil
}
