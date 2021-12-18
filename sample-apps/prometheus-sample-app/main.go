package main

import (
	"github.com/open-o11y/prometheus-sample-app/metrics"
)

func main() {

	cmd := metrics.CommandLine{}
	cmd.Run()
}
