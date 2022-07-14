package main

import (
	"context"
	"fmt"
	"io/ioutil"
	"log"
	"os"
	"os/signal"
	"syscall"
	"time"

	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/exporters/otlp/otlpmetric"
	"go.opentelemetry.io/otel/exporters/otlp/otlpmetric/otlpmetrichttp"
	"go.opentelemetry.io/otel/metric/global"
	"go.opentelemetry.io/otel/metric/instrument"
	controller "go.opentelemetry.io/otel/sdk/metric/controller/basic"
	"go.opentelemetry.io/otel/sdk/metric/export/aggregation"
	processor "go.opentelemetry.io/otel/sdk/metric/processor/basic"
	selector "go.opentelemetry.io/otel/sdk/metric/selector/simple"
	"gopkg.in/yaml.v3"
)

var (
	meter = global.MeterProvider().Meter("OTLP_METRIC_SAMPLE_APP")

	// Request based metrics; values generated upon endpoint requests
	totalRequests        string
	totalPageFaults      string
	latencyTime          string
	totalAllocatedMemory string
	totalActiveReqests   string

	// Default values for random based metrics
	defaultHost                    = "0.0.0.0"
	defaultPort                    = "4567"
	defaultTimeAliveInrementer     = 1
	defaultTotalHeapSizeUpperBound = 100
	defaultThreadsActiveUpperBound = 10
	defaultCpuUsageUpperBound      = 100
)

// Random based metrics; values inputed by configuration file
type conf struct {
	Host                    string `yaml:"Host"`
	Port                    string `yaml:"Port"`
	TimeAliveIncrementer    int64  `yaml:"RandomTimeAliveIncrementer"`
	TotalheapSizeUpperBound int64  `yaml:"RandomTotalHeapSizeUpperBound"`
	ThreadsActiveUpperBound int64  `yaml:"RandomThreadsActiveUpperBound"`
	CpuUsageUpperBound      int64  `yaml:"RandomCpuUsageUpperBound"`
}

func main() {
	var c conf
	c.getConf()
	ctx := context.Background()
	shutdown := startClient(ctx)
	defer shutdown()

	go updateLoop(ctx)
	fmt.Println("Reporting measurements to locahost:3418...")
	ch := make(chan os.Signal, 3)
	signal.Notify(ch, syscall.SIGINT, syscall.SIGQUIT, syscall.SIGTERM)
	<-ch

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
	}
	ctrl := controller.New(
		processor.NewFactory(
			selector.NewWithHistogramDistribution(),
			metricExp,
		),
		controller.WithExporter(metricExp),
		controller.WithCollectPeriod(3*time.Second),
	)
	if err := ctrl.Start(ctx); err != nil {
		// Logs here
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

// Reads the config file and writes to the struct with the appropriate values
func (c *conf) getConf() *conf {
	yamlFile, err := ioutil.ReadFile("config.yaml")
	if err != nil {
		//logs here
		return c.getDefaultConfig()
	}
	err = yaml.Unmarshal(yamlFile, c)
	if err != nil {
		//more logs here
		return c.getDefaultConfig()
	}
	return c
}

// Default to default values incase config file is missing
func (c *conf) getDefaultConfig() *conf {
	c.Host = defaultHost
	c.Port = defaultPort
	c.TimeAliveIncrementer = int64(defaultTimeAliveInrementer)
	c.TotalheapSizeUpperBound = int64(defaultTotalHeapSizeUpperBound)
	c.ThreadsActiveUpperBound = int64(defaultThreadsActiveUpperBound)
	c.CpuUsageUpperBound = int64(defaultCpuUsageUpperBound)
	return c
}

func counterObserver(ctx context.Context) {
	counter, _ := meter.SyncInt64().Counter(
		"Time Alive",
		instrument.WithUnit("s"),
		instrument.WithDescription("Total time that the application has been alive for"),
	)
	counter.Add(ctx, 1)
}

func upDownCounterObserver(ctx context.Context) {
	upDownCounter, _ := meter.SyncInt64().UpDownCounter(
		"Threads Active",
		instrument.WithUnit("1"),
		instrument.WithDescription("Number of threads currently active"),
	)
	upDownCounter.Add(ctx, 1)
}

func updateLoop(ctx context.Context) {
	go func() {
		for {
			upDownCounterObserver(ctx)
			counterObserver(ctx)
			time.Sleep(time.Second * 1)
			log.Print("Updating TimeAlive...")
		}
	}()
}
