module github.com/aws-otel-commnunity/sample-apps/go-sample-app

go 1.19

require (
	github.com/aws/aws-sdk-go v1.44.319
	github.com/gorilla/mux v1.8.0
	github.com/spf13/viper v1.16.0
	go.opentelemetry.io/contrib/instrumentation/github.com/gorilla/mux/otelmux v0.37.0
	go.opentelemetry.io/contrib/instrumentation/net/http/otelhttp v0.40.0
	go.opentelemetry.io/contrib/propagators/aws v1.15.0
	go.opentelemetry.io/otel v1.15.0-rc.1
	go.opentelemetry.io/otel/exporters/otlp/otlpmetric/otlpmetricgrpc v0.38.0-rc.1
	go.opentelemetry.io/otel/exporters/otlp/otlptrace/otlptracegrpc v1.15.0-rc.1
	go.opentelemetry.io/otel/metric v1.15.0-rc.1
	go.opentelemetry.io/otel/sdk v1.15.0-rc.1
	go.opentelemetry.io/otel/sdk/metric v0.38.0-rc.1
	go.opentelemetry.io/otel/trace v1.15.0-rc.1
)

require (
	github.com/cenkalti/backoff/v4 v4.2.0 // indirect
	github.com/felixge/httpsnoop v1.0.3 // indirect
	github.com/fsnotify/fsnotify v1.6.0 // indirect
	github.com/go-logr/logr v1.2.3 // indirect
	github.com/go-logr/stdr v1.2.2 // indirect
	github.com/golang/protobuf v1.5.3 // indirect
	github.com/grpc-ecosystem/grpc-gateway/v2 v2.15.2 // indirect
	github.com/hashicorp/hcl v1.0.0 // indirect
	github.com/jmespath/go-jmespath v0.4.0 // indirect
	github.com/magiconair/properties v1.8.7 // indirect
	github.com/mitchellh/mapstructure v1.5.0 // indirect
	github.com/niemeyer/pretty v0.0.0-20200227124842-a10e7caefd8e // indirect
	github.com/pelletier/go-toml/v2 v2.0.8 // indirect
	github.com/spf13/afero v1.9.5 // indirect
	github.com/spf13/cast v1.5.1 // indirect
	github.com/spf13/jwalterweatherman v1.1.0 // indirect
	github.com/spf13/pflag v1.0.5 // indirect
	github.com/subosito/gotenv v1.4.2 // indirect
	go.opentelemetry.io/otel/exporters/otlp/internal/retry v1.15.0-rc.1 // indirect
	go.opentelemetry.io/otel/exporters/otlp/otlpmetric v0.38.0-rc.1 // indirect
	go.opentelemetry.io/otel/exporters/otlp/otlptrace v1.15.0-rc.1 // indirect
	go.opentelemetry.io/proto/otlp v0.19.0 // indirect
	golang.org/x/net v0.10.0 // indirect
	golang.org/x/sys v0.8.0 // indirect
	golang.org/x/text v0.9.0 // indirect
	google.golang.org/genproto v0.0.0-20230410155749-daa745c078e1 // indirect
	google.golang.org/grpc v1.55.0 // indirect
	google.golang.org/protobuf v1.30.0 // indirect
	gopkg.in/check.v1 v1.0.0-20200227125254-8fa46927fb4f // indirect
	gopkg.in/ini.v1 v1.67.0 // indirect
	gopkg.in/yaml.v2 v2.4.0 // indirect
	gopkg.in/yaml.v3 v3.0.1 // indirect
)
