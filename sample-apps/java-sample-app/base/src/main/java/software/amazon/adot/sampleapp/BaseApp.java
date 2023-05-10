/*
 * Copyright The OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package software.amazon.adot.sampleapp;

import static spark.Spark.after;
import static spark.Spark.before;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.ipAddress;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import okhttp3.Call;
import okhttp3.Request;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.services.s3.S3Client;
import spark.Response;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Base sample application class with the common code that is sed for auto instrumentation and for manual
 * instrumentation.
 */
public abstract class BaseApp {
    final Logger logger = LogManager.getLogger();

    private final String REQUEST_TIME = "request-time";
    private static Attributes COMMON_SPAN_ATTRIBUTES;

    private final RandomMetricsProducer randomMetricsProducer;
    private final RequestMetricsProducer requestMetricsProducer;

    protected final OpenTelemetry otel = GlobalOpenTelemetry.get();
    protected final Tracer tracer = otel.getTracer("java-sample-app");

    private final S3Client s3Client;
    private final Call.Factory httpClient;


    private final Config config;

    public BaseApp(Config config, String instrumentation) {
        this.config = config;
        COMMON_SPAN_ATTRIBUTES = Attributes.of(
            AttributeKey.stringKey("signal"), "trace",
            AttributeKey.stringKey("language"), instrumentation
        );
        randomMetricsProducer = new RandomMetricsProducer(config, instrumentation);
        requestMetricsProducer = new RequestMetricsProducer(instrumentation);
        s3Client = buildS3Client();
        httpClient = buildHttpClient();
    }

    /**
     * Creates a S3 client with any customizations.
     * @return
     */
    protected abstract S3Client buildS3Client();

    /**
     * Creates a OkHttpClient with any customizations.
     * @return
     */
    protected abstract Call.Factory buildHttpClient();

    /**
     * Start the sample application
     */
    public final void start() {
        logger.info("Starting application");
        randomMetricsProducer.start();

        String port;
        String host;
        String listenAddress = System.getenv("LISTEN_ADDRESS");

        // set host and port number of sample app
        if (listenAddress == null) {
            logger.info(config.getHost());
            host = config.getHost();
            port = config.getPort();
        } else {
            String[] splitAddress = listenAddress.split(":");
            host = splitAddress[0];
            port = splitAddress[1];
        }

        // set sampleapp app port number and ip address
        port(Integer.parseInt(port));
        ipAddress(host);

        // Define the handlers for each of the 4 endpoints supported by the sample app
        get("/", (req, res) -> "healthcheck");
        get("/outgoing-http-call", (req, res) -> {
            instrument("outgoing-http-call", () -> httpCall("https://aws.amazon.com"));

            return getXrayTraceId();
        });
        get("/aws-sdk-call", (req, res) -> {
            instrument("aws-sdk-call", () -> s3Client.listBuckets());

            return getXrayTraceId();
        });
        get("/outgoing-sampleapp", (req, res) -> {
            instrument("invoke-sample-apps", () -> {
                ArrayList<String> samplePorts = config.getSamplePorts();

                if (samplePorts.isEmpty()) {
                    instrument("leaf-request", () -> {
                        httpCall("https://aws.amazon.com");
                    });
                } else {
                    for (String appPort: samplePorts) {
                        instrument("invoke-sampleapp", () -> httpCall(String.format("localhost:%s", appPort)));
                    }
                }

            });

            return getXrayTraceId();
        });

        // Define handlers that will be called for every request
        before(this::beforeRequest);
        after(this::afterRequest);
        exception(Exception.class, (exception, request, response) -> {
            exception.printStackTrace();
        });
    }

    // The following methods are overridden in the manual implementation to add support to traces in all requests
    // in a similar fashion to how it is done by the spark auto instrumentation.
    protected void beforeRequest(spark.Request request, spark.Response response) {
        // Get the current time so that we know how much time it took to handle this request
        request.attribute(REQUEST_TIME, System.currentTimeMillis());
    }

    protected void afterRequest(spark.Request request, Response response) {
        String statusCode = String.valueOf(response.status());
        Long requestStartTime = request.attribute(REQUEST_TIME);

        logger.info(String.format("Handled request:%s %s - %s", request.requestMethod(), request.url(), statusCode));

        // Update request based metrics.
        requestMetricsProducer.emitApiLatencyMetric(
                System.currentTimeMillis() - requestStartTime,
                request.pathInfo(),
                statusCode);
        requestMetricsProducer.emitBytesSentMetric(response.toString().length(), request.pathInfo(), statusCode);
        requestMetricsProducer.updateApiRequestsMetric();
    }

    /**
     * Instrument a piece of code by creating a span wrapping it.
     * @param spanName Name of the span for the instrumented code
     * @param code The code that should be instrumented.
     */
    private void instrument(String spanName, Runnable code) {
        Span span = tracer.spanBuilder(spanName).startSpan();
        span.setAllAttributes(COMMON_SPAN_ATTRIBUTES);

        try (Scope scope = span.makeCurrent()) {
            code.run();
        }

        span.end();
    }

    /**
     * Utility method to make HTTP calls. We are basically wrapping OkHttp calls
     * @param url
     */
    private void httpCall(String url) {
        try {
            httpClient.newCall(new Request.Builder().url(url).build())
                    .execute()
                    .close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Utility method to get the AWS Xray trace id in the Xray format.
     * @return the trace id
     */
    private static String getXrayTraceId() {
        String traceId = Span.current().getSpanContext().getTraceId();
        String xrayTraceId = "1-" + traceId.substring(0, 8) + "-" + traceId.substring(8);

        return String.format("{\"traceId\": \"%s\"}", xrayTraceId);
    }

}
