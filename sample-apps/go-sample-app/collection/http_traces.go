package collection

import (
	"context"
	"fmt"
	"io/ioutil"
	"net"
	"net/http"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/s3"
	"github.com/bitly/go-simplejson"
	"go.opentelemetry.io/contrib/instrumentation/net/http/otelhttp"
	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/trace"
)

// Contains all of the endpoint logic.
var tracer = otel.Tracer("sample-app")

// AwsSdkCall mocks a request to s3. ListBuckets are nil so no credentials are needed.
// Generates an Xray Trace ID.
func (rqmc *requestBasedMetricCollector) AwsSdkCall(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")

	s, err := session.NewSession(&aws.Config{
		Region: aws.String("us-west-2")},
	)

	svc := s3.New(s)
	svc.ListBuckets(nil) // nil or else would need real aws credentials
	if err != nil {
		fmt.Println(err)
	}

	_, span := tracer.Start(
		context.Background(),
		"AWS-SDK-CALL-TRACE",
	)
	defer span.End()

	// Request based metrics provided by rqmc
	// rqmc.AddApiRequest()
	// rqmc.UpdateTotalBytesSent()
	// rqmc.UpdateLatencyTime()

	xrayTraceID := getXrayTraceID(span)
	json := simplejson.New()
	json.Set("traceId", xrayTraceID)
	payload, _ := json.MarshalJSON()

	w.Write(payload)
}

// OutgoingSampleApp makes a request to another Sampleapp and generates an Xray Trace ID. It will also make a request to amazon.com.
func (rqmc *requestBasedMetricCollector) OutgoingSampleApp(w http.ResponseWriter, r *http.Request) {

	ctx := r.Context()
	traceId := getXrayTraceID(trace.SpanFromContext(ctx))
	w.Header().Set("Content-Type", "application/json")

	json := simplejson.New()
	json.Set("traceId", traceId)
	rqmc.invokeSampleApps(ctx)
	payload, _ := json.MarshalJSON()

	_, _ = w.Write(payload)

	// Second call
	client := http.Client{
		Transport: otelhttp.NewTransport(http.DefaultTransport),
	}
	req, _ := http.NewRequestWithContext(ctx, "GET", "https://www.amazon.com", nil)
	res, err := client.Do(req)
	if err != nil {
		fmt.Println(err)
	}
	defer res.Body.Close()
	// Request based metrics provided by rqmc
	//rqmc.AddApiRequest()
	//rqmc.UpdateTotalBytesSent()
	//rqmc.UpdateLatencyTime()

}

func (rqmc *requestBasedMetricCollector) invokeSampleApps(ctx context.Context) {
	for _, port := range rqmc.config.SampleAppPorts {
		if port != "" {
			invoke(ctx, port)
		}
	}
}

func invoke(ctx context.Context, port string) {
	client := http.Client{
		Transport: otelhttp.NewTransport(http.DefaultTransport),
	}
	addr := "http://" + net.JoinHostPort("0.0.0.0", port) + "/outgoing-sampleapp"
	fmt.Println(addr)
	req, _ := http.NewRequestWithContext(ctx, "GET", addr, nil)
	res, err := client.Do(req)
	if err != nil {
		fmt.Println(err)
	}
	defer res.Body.Close()

}

// OutgoingHttpCall makes an HTTP GET request to https://aws.amazon.com and generates an Xray Trace ID.
func (rqmc *requestBasedMetricCollector) OutgoingHttpCall(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	client := http.Client{Transport: otelhttp.NewTransport(http.DefaultTransport)}
	ctx := r.Context()
	xrayTraceID, _ := func(ctx context.Context) (string, error) {
		req, _ := http.NewRequestWithContext(ctx, "GET", "https://aws.amazon.com", nil)
		res, err := client.Do(req)
		if err != nil {
			fmt.Println(err)
		}
		_, _ = ioutil.ReadAll(res.Body)
		_ = res.Body.Close()
		return getXrayTraceID(trace.SpanFromContext(ctx)), err
	}(ctx)

	_, span := tracer.Start(
		ctx,
		"OUTGOING-HTTP-CALL-TRACE",
	)
	defer span.End()

	// Request based metrics provided by rqmc
	// rqmc.AddApiRequest()
	// rqmc.UpdateTotalBytesSent()
	// rqmc.UpdateLatencyTime()

	json := simplejson.New()
	json.Set("traceId", xrayTraceID)
	payload, _ := json.MarshalJSON()
	_, _ = w.Write(payload)
}

// getXrayTraceID generates a trace ID in Xray format from the span context.
func getXrayTraceID(span trace.Span) string {
	xrayTraceID := span.SpanContext().TraceID().String()
	result := fmt.Sprintf("1-%s-%s", xrayTraceID[0:8], xrayTraceID[8:])
	return result
}
