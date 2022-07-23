package collection

import (
	"context"
	"time"

	"go.opentelemetry.io/contrib/propagators/aws/xray"
	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/exporters/otlp/otlpmetric"
	"go.opentelemetry.io/otel/exporters/otlp/otlpmetric/otlpmetricgrpc"
	"go.opentelemetry.io/otel/exporters/otlp/otlptrace/otlptracegrpc"
	"go.opentelemetry.io/otel/metric/global"
	semconv "go.opentelemetry.io/otel/semconv/v1.8.0"
	"google.golang.org/grpc"

	"go.opentelemetry.io/otel/sdk/metric/aggregator/histogram"
	controller "go.opentelemetry.io/otel/sdk/metric/controller/basic"
	processor "go.opentelemetry.io/otel/sdk/metric/processor/basic"
	selector "go.opentelemetry.io/otel/sdk/metric/selector/simple"
	"go.opentelemetry.io/otel/sdk/resource"
	sdktrace "go.opentelemetry.io/otel/sdk/trace"
)

var grpcEndpoint = "0.0.0.0:4317"

// StartClient starts the OTEL controller which periodically collects signals and exports them.
// Trace exporter and Metric exporter are both configured.
func StartClient(ctx context.Context) (func(context.Context) error, error) {

	// Setup trace related
	tp, err := setupTraceProvider(ctx)
	if err != nil {
		return nil, err
	}
	otel.SetTracerProvider(tp)
	otel.SetTextMapPropagator(xray.Propagator{})

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

func setupTraceProvider(ctx context.Context) (*sdktrace.TracerProvider, error) {
	traceExporter, err := otlptracegrpc.New(ctx, otlptracegrpc.WithInsecure(), // INSECURE !! NOT TO BE USED FOR ANYTHING OTHER THAN DEMO
		otlptracegrpc.WithEndpoint(grpcEndpoint),
		otlptracegrpc.WithDialOption(grpc.WithBlock()))
	if err != nil {
		return nil, err
	}

	idg := xray.NewIDGenerator()
	res := resource.NewWithAttributes(
		semconv.SchemaURL,
		// the service name used to display traces in backends
		semconv.ServiceNameKey.String("go-sampleapp-service"), // Should have a unique name
	)
	tp := sdktrace.NewTracerProvider(
		sdktrace.WithSampler(sdktrace.AlwaysSample()),
		sdktrace.WithResource(res),
		sdktrace.WithBatcher(traceExporter),
		sdktrace.WithIDGenerator(idg),
	)
	return tp, nil
}

func setupMetricsController(ctx context.Context) (*controller.Controller, error) {
	metricClient := otlpmetricgrpc.NewClient(
		otlpmetricgrpc.WithInsecure(), // INSECURE !! NOT TO BE USED FOR ANYTHING OTHER THAN DEMO
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
