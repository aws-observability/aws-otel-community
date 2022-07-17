package collection

import (
	"context"
	"fmt"
	"io/ioutil"
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
func AwsSdkCall(w http.ResponseWriter, r *http.Request) {
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
		"Example Trace",
	)
	defer span.End()

	xrayTraceID := getXrayTraceID(span)
	json := simplejson.New()
	json.Set("traceId", xrayTraceID)
	payload, _ := json.MarshalJSON()

	w.Write(payload)
}

// OutgoingSampleApp makes a request to another Sampleapp and generates an Xray Trace ID.
func OutgoingSampleApp(w http.ResponseWriter, r *http.Request) {
	// TODO
}

// OutgoingHttpCall makes an HTTP GET request to https://aws.amazon.com and generates an Xray Trace ID.
func OutgoingHttpCall(w http.ResponseWriter, r *http.Request) {
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
		"CollectorExporter-Example",
	)
	defer span.End()

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
