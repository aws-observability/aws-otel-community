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

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;

import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.contrib.awsxray.AwsXrayIdGenerator;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.extension.aws.AwsXrayPropagator;
import io.opentelemetry.instrumentation.awssdk.v2_2.AwsSdkTelemetry;
import io.opentelemetry.instrumentation.okhttp.v3_0.OkHttpTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.services.s3.S3Client;
import spark.Response;

public class ManualApp extends BaseApp {

    private static final String REQUEST_OTEL_SCOPE = "requestOtelContext";
    private static final String REQUEST_OTEL_SPAN = "requestOtelSpan";

    // Configures Opentelemetry Manually setting each parameter used in this application
    private static OpenTelemetry buildOpentelemetry() {
        Resource resource = Resource.getDefault().merge(
                Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, "java-sample-app")));

        return OpenTelemetrySdk.builder()
                .setPropagators(
                        ContextPropagators.create(
                                TextMapPropagator.composite(
                                        W3CTraceContextPropagator.getInstance(),
                                        AwsXrayPropagator.getInstance())))
                .setTracerProvider(
                        SdkTracerProvider.builder()
                                .addSpanProcessor(
                                        BatchSpanProcessor.builder(OtlpGrpcSpanExporter.getDefault()).build())
                                .setIdGenerator(AwsXrayIdGenerator.getInstance())
                                .setResource(resource)
                                .build())
                .setMeterProvider(
                        SdkMeterProvider.builder()
                                .registerMetricReader(
                                        PeriodicMetricReader.builder(OtlpGrpcMetricExporter.getDefault()).build())
                                .setResource(resource)

                                .build())
                .buildAndRegisterGlobal();
    }

    private static final OpenTelemetry otel = buildOpentelemetry();

    public ManualApp(Config config) {
        super(config);
    }

    // Customizations for sample application using Manual Instrumentation. We are instrumenting third party libraries
    // explicitly.
    @Override
    protected Call.Factory buildHttpClient() {
        return OkHttpTelemetry.builder(otel).build().newCallFactory(new OkHttpClient.Builder().build());
    }

    @Override
    protected S3Client buildS3Client() {
       return S3Client.builder()
               .overrideConfiguration(
                       ClientOverrideConfiguration.builder()
                               .addExecutionInterceptor(AwsSdkTelemetry.create(otel).newExecutionInterceptor())
                               .build())
               .build();
    }

    // Getter used to extract context propagation information.
    private static final TextMapGetter<spark.Request> getter =
            new TextMapGetter<>() {
                @Override
                public Iterable<String> keys(spark.Request carrier) {
                    return carrier.headers();
                }

                @Override
                public String get(spark.Request carrier, String key) {
                    if (carrier.headers().contains(key)) {
                        return carrier.headers(key);
                    }
                    return "";
                }
            };

    // Override the Spark Java methods used for handling request. Creates spans for every request that is received.
    @Override
    protected void beforeRequest(spark.Request request, spark.Response response) {
        super.beforeRequest(request, response);

        Context context = otel.getPropagators().getTextMapPropagator().extract(Context.current(), request, getter);

        Span span = tracer.spanBuilder(String.format("%s %s", request.requestMethod(), request.pathInfo()))
                .setParent(context)
                .setSpanKind(SpanKind.SERVER)
                .startSpan();

        Scope scope = span.makeCurrent();

        request.attribute(REQUEST_OTEL_SCOPE, scope);
        request.attribute(REQUEST_OTEL_SPAN, span);
    }

    protected void afterRequest(spark.Request request, Response response) {
        super.afterRequest(request, response);
        Span span = request.attribute(REQUEST_OTEL_SPAN);
        Scope scope = request.attribute(REQUEST_OTEL_SCOPE);

        span.setAttribute(SemanticAttributes.HTTP_METHOD, request.requestMethod());
        span.setAttribute(SemanticAttributes.HTTP_SCHEME, request.scheme());
        span.setAttribute(SemanticAttributes.HTTP_HOST, request.host());
        span.setAttribute(SemanticAttributes.HTTP_TARGET, request.pathInfo());
        span.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, response.status());

        scope.close();
        span.end();
    }
}
