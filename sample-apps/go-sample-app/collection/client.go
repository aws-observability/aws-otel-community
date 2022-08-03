package collection

import (
	"context"
	"fmt"
	"time"

	"go.opentelemetry.io/contrib/propagators/aws/xray"
	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/attribute"
	"go.opentelemetry.io/otel/exporters/otlp/otlpmetric"
	"go.opentelemetry.io/otel/exporters/otlp/otlpmetric/otlpmetricgrpc"
	"go.opentelemetry.io/otel/exporters/otlp/otlptrace/otlptracegrpc"
	"go.opentelemetry.io/otel/metric/global"
	semconv "go.opentelemetry.io/otel/semconv/v1.8.0"
	"google.golang.org/grpc"

	"go.opentelemetry.io/contrib/detectors/aws/ec2"
	"go.opentelemetry.io/contrib/detectors/aws/ecs"
	"go.opentelemetry.io/contrib/detectors/aws/eks"
	"go.opentelemetry.io/otel/sdk/metric/aggregator/histogram"
	controller "go.opentelemetry.io/otel/sdk/metric/controller/basic"
	processor "go.opentelemetry.io/otel/sdk/metric/processor/basic"
	selector "go.opentelemetry.io/otel/sdk/metric/selector/simple"
	"go.opentelemetry.io/otel/sdk/resource"
	sdktrace "go.opentelemetry.io/otel/sdk/trace"
)

const GRPC_ENDPOINT = "0.0.0.0:4317"

const SERVICE_NAME = "go"

var tracer = otel.Tracer("ADOT-Tracer-Sample")

// Names for metric instruments
const API_TIME_ALIVE = "timeAlive"
const API_CPU_USAGE = "cpuUsage"
const API_TOTAL_HEAP_SIZE = "totalHeapSize"
const API_THREADS_ACTIVE = "threadsActive"
const API_TOTAL_BYTES_SENT = "totalBytesSent"
const API_TOTAL_API_REQUESTS = "totalApiRequests"
const API_LATENCY_TIME = "latencyTime"

// Common attributes for traces and metrics (random, request)
var requestMetricCommonLabels = []attribute.KeyValue{
	attribute.String("signal", "metric"),
	attribute.String("language", SERVICE_NAME),
	attribute.String("metricType", "request"),
}

var randomMetricCommonLabels = []attribute.KeyValue{
	attribute.String("signal", "metric"),
	attribute.String("language", SERVICE_NAME),
	attribute.String("metricType", "random"),
}

var traceCommonLabels = []attribute.KeyValue{
	attribute.String("signal", "trace"),
	attribute.String("language", SERVICE_NAME),
	attribute.Int("statusCode", 0),
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

		defer tp.Shutdown(ctx)

		// pushes any last exports to the receiver
		if err := ctrl.Stop(cxt); err != nil {
			return err
		}
		return nil
	}, nil
}

// setupTraceProvider configures a trace exporter and an AWS X-Ray ID Generator.
func setupTraceProvider(ctx context.Context) (*sdktrace.TracerProvider, error) {
	traceExporter, err := otlptracegrpc.New(ctx, otlptracegrpc.WithInsecure(), // INSECURE !! NOT TO BE USED FOR ANYTHING IN PRODUCTION
		otlptracegrpc.WithEndpoint(GRPC_ENDPOINT),
		otlptracegrpc.WithDialOption(grpc.WithBlock()))
	if err != nil {
		return nil, err
	}

	idg := xray.NewIDGenerator()
	ec2Res, ecsRes, eksRes := getResourceDetectors()
	fmt.Println(ec2Res, ecsRes, eksRes)
	res := resource.NewWithAttributes(
		semconv.SchemaURL,
		semconv.ServiceNameKey.String("go-sampleapp-service"), // Should have a unique name. Service name displayed in backends
	)

	tp := sdktrace.NewTracerProvider(
		sdktrace.WithSampler(sdktrace.AlwaysSample()),
		sdktrace.WithBatcher(traceExporter),
		sdktrace.WithIDGenerator(idg),
		sdktrace.WithResource(res),
		// sdktrace.WithResource(ec2Res),
		// sdktrace.WithResource(ecsRes),
		// sdktrace.WithResource(eksRes),
	)
	return tp, nil
}

// getResourceDetectors returns resource detectors for ec2, ecs and eks.
func getResourceDetectors() (*resource.Resource, *resource.Resource, *resource.Resource) {
	ec2ResourceDetector := ec2.NewResourceDetector()
	ec2Res, _ := ec2ResourceDetector.Detect(context.Background())

	ecsResourceDetector := ecs.NewResourceDetector()
	ecsRes, _ := ecsResourceDetector.Detect(context.Background())

	eksResourceDetector := eks.NewResourceDetector()
	eksRes, _ := eksResourceDetector.Detect(context.Background())

	return ec2Res, ecsRes, eksRes
}

// setupMetricsController configures a metric exporter and a controller with a histogram tracking latency.
func setupMetricsController(ctx context.Context) (*controller.Controller, error) {
	metricClient := otlpmetricgrpc.NewClient(
		otlpmetricgrpc.WithInsecure(), // INSECURE !! NOT TO BE USED FOR ANYTHING IN PRODUCTION
		otlpmetricgrpc.WithEndpoint(GRPC_ENDPOINT))
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
