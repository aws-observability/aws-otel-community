package collection

import (
	"context"
	"fmt"
	"time"

	"go.opentelemetry.io/contrib/propagators/aws/xray"
	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/exporters/otlp/otlpmetric"
	"go.opentelemetry.io/otel/exporters/otlp/otlpmetric/otlpmetrichttp"
	"go.opentelemetry.io/otel/exporters/otlp/otlptrace/otlptracegrpc"
	"go.opentelemetry.io/otel/metric/global"
	"go.opentelemetry.io/otel/sdk/metric/export/aggregation"
	"go.opentelemetry.io/otel/sdk/resource"
	semconv "go.opentelemetry.io/otel/semconv/v1.4.0"
	"google.golang.org/grpc"

	controller "go.opentelemetry.io/otel/sdk/metric/controller/basic"
	processor "go.opentelemetry.io/otel/sdk/metric/processor/basic"
	selector "go.opentelemetry.io/otel/sdk/metric/selector/simple"
	sdktrace "go.opentelemetry.io/otel/sdk/trace"
)

// StartClient starts the OTEL controller which periodically collects signals and exports them.
// Trace exporter and Metric exporter are both configured.
func StartClient(ctx context.Context) {
	endpoint := "0.0.0.0:4318"

	// Setup trace related
	traceExporter, err := otlptracegrpc.New(ctx, otlptracegrpc.WithInsecure(), otlptracegrpc.WithEndpoint("0.0.0.0:4317"), otlptracegrpc.WithDialOption(grpc.WithBlock()))
	if err != nil {
		//Logs here
		fmt.Println(err)
	}
	idg := xray.NewIDGenerator()
	res := resource.NewWithAttributes(
		semconv.SchemaURL,
		// the service name used to display traces in backends
		semconv.ServiceNameKey.String("sampleapp-service1"), // Should have a unique name
	)
	tp := sdktrace.NewTracerProvider(
		sdktrace.WithSampler(sdktrace.AlwaysSample()),
		sdktrace.WithResource(res),
		sdktrace.WithBatcher(traceExporter),
		sdktrace.WithIDGenerator(idg),
	)

	otel.SetTracerProvider(tp)
	otel.SetTextMapPropagator(xray.Propagator{})

	// Setup metric related
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
