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
var tracer = otel.Tracer("go-sample-app-tracer")

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
		"get-aws-s3-buckets",
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
	ctx, span := tracer.Start(
		ctx,
		"invoke-sample-apps",
	)
	defer span.End()
	count := len(rqmc.config.SampleAppPorts)

	// If there are no sample app port list is empty then make a request to amazon.com (leaf request)
	if count == 0 {
		ctx, span := tracer.Start(
			ctx,
			"leaf-request",
		)

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

		span.End()

	} else { // If there are sample app ports to make a request to (chain request)
		rqmc.invokeSampleApps(ctx, client)
	}
	writeResponse(span, w)

}

func (rqmc *requestBasedMetricCollector) invokeSampleApps(ctx context.Context, client http.Client) {

	for _, port := range rqmc.config.SampleAppPorts {
		if port != "" {
			invoke(ctx, port, client)
		}
	}
}

func invoke(ctx context.Context, port string, client http.Client) {

	ctx, span := tracer.Start(
		ctx,
		"invoke-sampleapp",
	)
	addr := "http://" + net.JoinHostPort("0.0.0.0", port) + "/outgoing-sampleapp"
	fmt.Println(addr)
	req, _ := http.NewRequestWithContext(ctx, "GET", addr, nil)
	res, err := client.Do(req)

	if err != nil {
		fmt.Println(err)
	}

	defer res.Body.Close()
	defer span.End()

}

// OutgoingHttpCall makes an HTTP GET request to https://aws.amazon.com and generates an Xray Trace ID.
func (rqmc *requestBasedMetricCollector) OutgoingHttpCall(w http.ResponseWriter, r *http.Request, client http.Client) {

	w.Header().Set("Content-Type", "application/json")
	ctx := r.Context()

	newCtx, span := tracer.Start(
		ctx,
		"outgoing-http-call",
	)
	defer span.End()

	req, _ := http.NewRequestWithContext(newCtx, "GET", "https://aws.amazon.com", nil)
	res, err := client.Do(req)
	if err != nil {
		fmt.Println(err)
	}
	defer res.Body.Close()

	// Request based metrics provided by rqmc
	rqmc.AddApiRequest()
	rqmc.UpdateTotalBytesSent(newCtx)
	rqmc.UpdateLatencyTime(newCtx)
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
	w.Header().Set("Content-Type", "application/json")
	w.Write(payload)
}
