package collection

import (
	"context"
	"encoding/json"
	"fmt"
	"net"
	"net/http"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/s3"
	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/trace"
)

// Contains all of the endpoint logic.
var tracer = otel.Tracer("sample-app")

type response struct {
	TraceID string `json:"traceID"`
}

// AwsSdkCall mocks a request to s3. ListBuckets are nil so no credentials are needed.
// Generates an Xray Trace ID.
func (rqmc *requestBasedMetricCollector) AwsSdkCall(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	ctx := r.Context()
	s, err := session.NewSession(&aws.Config{
		Region: aws.String("us-west-2")},
	)

	// TODO: Convert this into an interface
	svc := s3.New(s)
	svc.ListBuckets(nil) // nil or else would need real aws credentials
	if err != nil {
		fmt.Println(err)
	}

	_, span := tracer.Start(
		ctx,
		"AWS-SDK-CALL-TRACE",
	)
	defer span.End()

	// Request based metrics provided by rqmc
	rqmc.AddApiRequest()
	rqmc.UpdateTotalBytesSent(ctx)
	rqmc.UpdateLatencyTime(ctx)

	writeResponse(span, w)
}

// OutgoingSampleApp makes a request to another Sampleapp and generates an Xray Trace ID. It will also make a request to amazon.com.
func (rqmc *requestBasedMetricCollector) OutgoingSampleApp(w http.ResponseWriter, r *http.Request, client http.Client) {

	ctx := r.Context()
	w.Header().Set("Content-Type", "application/json")
	_, span := tracer.Start(
		ctx,
		"OUTGOING-LEAF-CALL",
	)

	count := rqmc.invokeSampleApps(ctx, client)

	// Second call
	if count == 0 {

		defer span.End()

		req, _ := http.NewRequestWithContext(ctx, "GET", "https://www.amazon.com", nil)
		res, err := client.Do(req)
		if err != nil {
			fmt.Println(err)
		}
		defer res.Body.Close()
		// Request based metrics provided by rqmc
		rqmc.AddApiRequest()
		rqmc.UpdateTotalBytesSent(ctx)
		rqmc.UpdateLatencyTime(ctx)
	}
	writeResponse(span, w)
}

func (rqmc *requestBasedMetricCollector) invokeSampleApps(ctx context.Context, client http.Client) int {
	for _, port := range rqmc.config.SampleAppPorts {
		if port != "" {
			invoke(ctx, port, client)
		}
	}
	return len(rqmc.config.SampleAppPorts)
}

func invoke(ctx context.Context, port string, client http.Client) {

	_, span := tracer.Start(
		ctx,
		"OUTGOING-SAMPLEAPP-CALL",
	)
	defer span.End()

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
func (rqmc *requestBasedMetricCollector) OutgoingHttpCall(w http.ResponseWriter, r *http.Request, client http.Client) {

	w.Header().Set("Content-Type", "application/json")
	ctx := r.Context()

	_, span := tracer.Start(
		ctx,
		"OUTGOING-HTTP-CALL-TRACE",
	)
	defer span.End()

	req, _ := http.NewRequestWithContext(ctx, "GET", "https://aws.amazon.com", nil)
	res, err := client.Do(req)
	if err != nil {
		fmt.Println(err)
	}
	defer res.Body.Close()

	// Request based metrics provided by rqmc
	rqmc.AddApiRequest()
	rqmc.UpdateTotalBytesSent(ctx)
	rqmc.UpdateLatencyTime(ctx)
	writeResponse(span, w)

}

// getXrayTraceID generates a trace ID in Xray format from the span context.
func getXrayTraceID(span trace.Span) string {
	xrayTraceID := span.SpanContext().TraceID().String()
	return fmt.Sprintf("1-%s-%s", xrayTraceID[0:8], xrayTraceID[8:])
}

func writeResponse(span trace.Span, w http.ResponseWriter) {
	xrayTraceID := getXrayTraceID(span)
	payload, _ := json.Marshal(response{TraceID: xrayTraceID})

	w.Write(payload)
}
