package main

import (
	"io/ioutil"

	"gopkg.in/yaml.v3"
)

var (

	// Request based metrics; values generated upon endpoint requests
	totalRequests        string
	totalPageFaults      string
	latencyTime          string
	totalAllocatedMemory string
	totalActiveReqests   string

	// Default values for random based metrics
	defaultHost                    = "0.0.0.0"
	defaultPort                    = "4567"
	defaultTimeAliveInrementer     = 5
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

}

// Reads the config file and writes to the struct with the appropriate values
func (c *conf) getConf() *conf {
	yamlFile, err := ioutil.ReadFile("config.yaml")
	if err != nil {
		//logs here
		return c.validateConf()
	}
	err = yaml.Unmarshal(yamlFile, c)
	if err != nil {
		//more logs here
		return c.validateConf()
	}
	return c
}

// Default to default values incase config file is missing
func (c *conf) validateConf() *conf {
	c.Host = defaultHost
	c.Port = defaultPort
	c.TimeAliveIncrementer = int64(defaultTimeAliveInrementer)
	c.TotalheapSizeUpperBound = int64(defaultTotalHeapSizeUpperBound)
	c.ThreadsActiveUpperBound = int64(defaultThreadsActiveUpperBound)
	c.CpuUsageUpperBound = int64(defaultCpuUsageUpperBound)
	return c
}
