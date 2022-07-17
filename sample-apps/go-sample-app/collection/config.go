package collection

import (
	"github.com/spf13/viper"
)

// Config contains random based metrics; values inputed by configuration file or defaulted values
type Config struct {
	Host                    string
	Port                    string
	TimeInterval            int64
	TimeAliveIncrementer    int64
	TotalheapSizeUpperBound int64
	ThreadsActiveUpperBound int64
	CpuUsageUpperBound      int64
}

// GetConfiguration returns a configured Config struct with the precedence; Default Values < Configuration File.
func GetConfiguration() *Config {
	viper.SetConfigFile("config.yaml")
	viper.ReadInConfig()
	// Default values
	viper.SetDefault("Host", "0.0.0.0")
	viper.SetDefault("Port", "4567")
	viper.SetDefault("TimeInterval", 1)
	viper.SetDefault("RandomTimeAliveIncrementer", 1)
	viper.SetDefault("RandomTotalHeapSizeUpperBound", 100)
	viper.SetDefault("RandomThreadsActiveUpperBound", 10)
	viper.SetDefault("RandomCpuUsageUpperBound", 100)

	host, _ := viper.Get("Host").(string)
	port, _ := viper.Get("Port").(string)
	timeInterval := viper.Get("TimeInterval").(int)
	timeAliveIncrementer, _ := viper.Get("RandomTimeAliveIncrementer").(int)
	totalHeapSizeUpperBound, _ := viper.Get("RandomTotalHeapSizeUpperBound").(int)
	threadsActiveUpperBound, _ := viper.Get("RandomThreadsActiveUpperBound").(int)
	cpuUsageUpperBound, _ := viper.Get("RandomCpuUsageUpperBound").(int)
	cfg := Config{
		Host:                    host,
		Port:                    port,
		TimeInterval:            int64(timeInterval),
		TimeAliveIncrementer:    int64(timeAliveIncrementer),
		TotalheapSizeUpperBound: int64(totalHeapSizeUpperBound),
		ThreadsActiveUpperBound: int64(threadsActiveUpperBound),
		CpuUsageUpperBound:      int64(cpuUsageUpperBound),
	}

	return &cfg
}
