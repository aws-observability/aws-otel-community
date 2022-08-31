package collection

import (
	"github.com/spf13/viper"
)

// Config contains random based metrics; values inputed by configuration file or defaulted values
type Config struct {
	Host                    string   `mapstructure:"Host"`
	Port                    string   `mapstructure:"Port"`
	TimeInterval            int64    `mapstructure:"TimeInterval"`
	TimeAliveIncrementer    int64    `mapstructure:"RandomTimeAliveIncrementer"`
	TotalHeapSizeUpperBound int64    `mapstructure:"RandomTotalHeapSizeUpperBound"`
	ThreadsActiveUpperBound int64    `mapstructure:"RandomThreadsActiveUpperBound"`
	CpuUsageUpperBound      int64    `mapstructure:"RandomCpuUsageUpperBound"`
	SampleAppPorts          []string `mapstructure:"SampleAppPorts"`
}

// GetConfiguration returns a configured Config struct with the precedence; Default Values < Configuration File.
func GetConfiguration() *Config {
	var arr []string
	viper.SetDefault("Host", "0.0.0.0")
	viper.SetDefault("Port", "8080")
	viper.SetDefault("TimeInterval", 1)
	viper.SetDefault("RandomTimeAliveIncrementer", 1)
	viper.SetDefault("RandomTotalHeapSizeUpperBound", 100)
	viper.SetDefault("RandomThreadsActiveUpperBound", 10)
	viper.SetDefault("RandomCpuUsageUpperBound", 100)
	viper.SetDefault("SampleAppPorts", arr)

	viper.SetConfigFile("config.yaml")
	viper.ReadInConfig()

	cfg := &Config{}
	viper.Unmarshal(cfg)

	return cfg
}
