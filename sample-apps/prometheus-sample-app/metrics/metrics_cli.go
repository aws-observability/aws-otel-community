package metrics

import (
	"context"
	"flag"
	"fmt"
	"io/ioutil"
	"log"
	"math/rand"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/prometheus/client_golang/prometheus/promhttp"
	"gopkg.in/yaml.v3"
)

// mean and standard deviation for histogram and summary normal distribution
var (
	normDomain = flag.Float64("normal.domain", 0.5, "The domain for the normal distribution.")
	normMean   = flag.Float64("normal.mean", 0.18, "The mean for the normal distribution.")
)

type Config struct {
	Address        string `yaml:"Address"`
	Type           string `yaml:"Type"`
	MetricsCount   int    `yaml:"MetricsCount"`
	LabelsCount    int    `yaml:"LabelsCount"`
	DataPointCount int    `yaml:"DataPointCount"`
	Frequency      int    `yaml:"Frequency"`
	Random         bool   `yaml:"Random"`
}

/*
	defaultType valid values include - "all" "counter" "gauge" "histogram" "summary"
	defaultMetricsCount valid values should be >= 0
	defaultFreq valid values should be >= 0
	defaultRand valid values should be boolean
	defaultLabelsCount valid values should be >= 0
	defaultDataPointCount valid values should be > 0
*/
var defaultType = "all"
var defaultMetricsCount = 1
var defaultLabelsCount = 1
var defaultDataPointCount = 1
var defaultFreq = 15
var defaultRand = false
var defaultAddress = "0.0.0.0:8080"

type CommandLine struct{}

func (conf *Config) Parse(data []byte) error {
	return yaml.Unmarshal(data, conf)
}

func healthCheckHandler(w http.ResponseWriter, r *http.Request) {
	fmt.Fprintf(w, "healthy")
}

// initConnection handles the metric creation and also updates the metrics via go routines
// The delegation logic is handled here
func (conf *Config) initConnection() {

	rand.Seed(time.Now().Unix())
	mc := newMetricCollector()
	mc.interval = time.Duration(conf.Frequency) * time.Second
	mc.labelValues, mc.labelKeys = generateLabels(conf.LabelsCount)
	mc.datapointCount = conf.DataPointCount
	switch conf.Type {
	case "counter":
		createCounter(conf.MetricsCount, mc)
	case "gauge":
		createGauge(conf.MetricsCount, mc)
	case "histogram":
		createHistogram(conf.MetricsCount, mc)
	case "summary":
		createSummary(conf.MetricsCount, mc)
	case "all":
		createAll(conf.MetricsCount, mc, conf.Random)
	default:
		log.Fatal("Invalid type")
	}
	log.Print("Server Started")
	log.Println("Serving on address: " + conf.Address)
	if conf.Random {
		log.Println("Producing randomized metrics per type")
	} else {
		log.Println("Producing " + fmt.Sprintf("%d", conf.MetricsCount) + " metric(s) per type")
	}

	// Server handling
	srv := &http.Server{
		Addr:    conf.Address,
		Handler: nil,
	}
	done := make(chan os.Signal, 1)
	signal.Notify(done, os.Interrupt, syscall.SIGINT, syscall.SIGTERM)

	go func() {
		if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			log.Fatalf("listen: %s\n", err)
		}
	}()
	log.Println("Updating at a frequency of "+fmt.Sprintf("%d", mc.interval/time.Second), "seconds")
	http.HandleFunc("/", healthCheckHandler)
	http.Handle("/metrics", promhttp.HandlerFor(promRegistry, promhttp.HandlerOpts{}))

	<-done
	log.Print("Server Stopped")
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer func() {
		cancel()
	}()

	if err := srv.Shutdown(ctx); err != nil {
		log.Fatalf("Server Shutdown Failed:%+v", err)
	}
	log.Print("Server Exited")

}

func createCounter(count int, mc metricCollector) {
	mc.registerCounter(count)
	updateLoop(mc.updateCounter, mc.interval)
}

func createGauge(count int, mc metricCollector) {
	mc.registerGauge(count)
	updateLoop(mc.updateGauge, mc.interval)
}

func createHistogram(count int, mc metricCollector) {
	mc.registerHistogram(count)
	updateLoop(mc.updateHistogram, mc.interval)

}

func createSummary(count int, mc metricCollector) {
	mc.registerSummary(count)
	updateLoop(mc.updateSummary, mc.interval)
}

// createAll generates all 4 metric types
// If isRandom is sent as true, createAll will generate randomized metrics. Other-wise createALl will steadily create the 4 types of metrics with a fixed count (provided by the user
func createAll(count int, mc metricCollector, isRandom bool) {

	if isRandom {
		idx := rand.Intn(4)
		lower := 1
		upper := 4
		amount := rand.Intn(upper-lower) + lower
		metrics := []string{"counter", "gauge", "histogram", "summary"}
		rands := []int{rand.Intn(200), rand.Intn(200), rand.Intn(200), rand.Intn(200)}
		for i := 0; i <= amount; i++ {
			if idx >= len(metrics) {
				idx = 0
			}
			str := metrics[idx]
			idx++
			switch str {
			case "counter":
				createCounter(rands[0], mc)
			case "gauge":
				createGauge(rands[1], mc)
			case "histogram":
				createHistogram(rands[2], mc)
			case "summary":
				createSummary(rands[3], mc)
			}
		}

	} else {
		mc.registerCounter(count)
		mc.registerGauge(count)
		mc.registerHistogram(count)
		mc.registerSummary(count)
		go mc.updateMetrics()

	}

}

// Run reads the config file and uses the data as default arguments.
// These arguments can be overridden by CLI input (see README)
func (cli *CommandLine) Run() {
	data, err := ioutil.ReadFile("config.yaml")
	if err != nil {
		log.Fatal(err)
	}
	var conf Config
	if err := conf.Parse(data); err != nil {
		log.Fatal(err)
	}
	generateCmd := flag.NewFlagSet("generate", flag.ExitOnError)

	// Handling it without viper / cobra for now - still follows flags >  configuration file > defaults
	// defaults are set first
	// config file is read - if there are valid values, config file overrides defaults
	// flags will use config values as default values and override them with CLI input
	usedType := defaultType
	usedMetricsCount := defaultMetricsCount
	usedLabelsCount := defaultLabelsCount
	usedDataPointCount := defaultDataPointCount
	usedFreq := defaultFreq
	usedRand := defaultRand
	usedAddress := defaultAddress
	if conf.Type != "" {
		usedType = conf.Type
	}
	if conf.MetricsCount > 0 {
		usedMetricsCount = conf.MetricsCount
	}
	if conf.LabelsCount > 0 {
		usedLabelsCount = conf.LabelsCount
	}
	if conf.DataPointCount > 0 {
		usedDataPointCount = conf.DataPointCount
	}
	if conf.Frequency > 0 {
		usedFreq = conf.Frequency
	}
	if conf.Random {
		usedRand = conf.Random
	}
	if conf.Address != "" {
		usedAddress = conf.Address
	}

	metricType := generateCmd.String("metric_type", usedType, "Type of metric (counter, gauge, histogram, summary)")
	metricCount := generateCmd.Int("metric_count", usedMetricsCount, "Amount of metrics to create")
	labelCount := generateCmd.Int("label_count", usedLabelsCount, "Amount of labels per metric to create")
	dataPointCount := generateCmd.Int("datapoint_count", usedDataPointCount, "Number of data-points per metric to create")
	metricFreq := generateCmd.Int("metric_frequency", usedFreq, "Refresh interval in seconds")
	addressPtr := generateCmd.String("listen_address", usedAddress, "server listening address")
	rand := generateCmd.Bool("is_random", usedRand, "Metrics specification")

	if len(os.Args) > 1 {
		err := generateCmd.Parse(os.Args[1:])
		if err != nil {
			log.Panic(err)
		}
	}

	conf.Type = *metricType
	conf.MetricsCount = *metricCount
	conf.LabelsCount = *labelCount
	conf.DataPointCount = *dataPointCount
	conf.Frequency = *metricFreq
	conf.Random = *rand
	conf.Address = *addressPtr

	conf.initConnection()

}
