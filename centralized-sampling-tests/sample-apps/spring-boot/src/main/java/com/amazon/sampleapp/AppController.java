package com.amazon.sampleapp;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.contrib.awsxray.AwsXrayRemoteSampler;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import java.time.Duration;

import static io.opentelemetry.semconv.resource.attributes.ResourceAttributes.SERVICE_NAME;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AppController {
  private final Tracer tracer;
  private final String serviceName = "aws-otel-integ-test";

  /**
   * Injects the tracer into application controller, so it can be used by a later function Creates a
   * resource and open-telemetry agent then uses those to create/set a tracer.
   */
  AppController() {
    String host = System.getenv("XRAY_ENDPOINT");
    if (host == null) {
      host = "http://localhost:2000";
    }

    Resource resource = Resource.builder()
            .put(SERVICE_NAME, serviceName)
            .build();

    OpenTelemetry openTelemetry =
        OpenTelemetrySdk.builder()
            .setTracerProvider(
                SdkTracerProvider.builder()
                    .setResource(resource)
                    .setSampler(
                        AwsXrayRemoteSampler.newBuilder(resource)
                            .setEndpoint(host)
                            .setPollingInterval(Duration.ofSeconds(10))
                            .build())
                    .build())
            .buildAndRegisterGlobal();
    this.tracer = openTelemetry.getTracer("centralized-sampling-tests");
  }

  /**
   * Get endpoint for /getSampled that requires three header values for user, service_name, and
   * required Returns the number of times a span was sampled out of the creation of 1000 spans
   */
  @GetMapping(value = "/getSampled")
  @ResponseBody
  public int getSampled(
      @RequestHeader("user") String userAttribute,
      @RequestHeader("service_name") String name,
      @RequestHeader("required") String required,
      @RequestHeader("totalSpans") String totalSpans) {
    Attributes attributes =
        Attributes.of(
            AttributeKey.stringKey("http.method"), "GET",
            AttributeKey.stringKey("http.url"), "http://localhost:8080/getSampled",
            AttributeKey.stringKey("user"), userAttribute,
            AttributeKey.stringKey("http.route"), "/getSampled",
            AttributeKey.stringKey("required"), required,
            AttributeKey.stringKey("http.target"), "/getSampled");
    return getSampledSpanCount(name, totalSpans, attributes);
  }

  /**
   * Post endpoint for /getSampled that requires three header values for user, service_name, and
   * required Returns the number of times a span was sampled out of the creation of 1000 spans
   */
  @PostMapping("/getSampled")
  @ResponseBody
  public int postSampled(
      @RequestHeader("user") String userAttribute,
      @RequestHeader("service_name") String name,
      @RequestHeader("required") String required,
      @RequestHeader("totalSpans") String totalSpans) {
    Attributes attributes =
        Attributes.of(
            AttributeKey.stringKey("http.method"), "POST",
            AttributeKey.stringKey("http.url"), "http://localhost:8080/getSampled",
            AttributeKey.stringKey("user"), userAttribute,
            AttributeKey.stringKey("http.route"), "/getSampled",
            AttributeKey.stringKey("required"), required,
            AttributeKey.stringKey("http.target"), "/getSampled");
    return getSampledSpanCount(name, totalSpans, attributes);
  }

  /**
   * Get endpoint for /importantEndpoint that requires three header values for user, service_name,
   * and required Returns the number of times a span was sampled out of the creation of 1000 spans
   */
  @GetMapping("/importantEndpoint")
  @ResponseBody
  public int importantEndpoint(
      @RequestHeader("user") String userAttribute,
      @RequestHeader("service_name") String name,
      @RequestHeader("required") String required,
      @RequestHeader("totalSpans") String totalSpans) {
    Attributes attributes =
        Attributes.of(
            AttributeKey.stringKey("http.method"), "GET",
            AttributeKey.stringKey("http.url"), "http://localhost:8080/importantEndpoint",
            AttributeKey.stringKey("user"), userAttribute,
            AttributeKey.stringKey("http.route"), "/importantEndpoint",
            AttributeKey.stringKey("required"), required,
            AttributeKey.stringKey("http.target"), "/importantEndpoint");
    return getSampledSpanCount(name, totalSpans, attributes);
  }

  /**
   * Creates x amount of spans with x being supplied by totalSpans and returns how many of those
   * spans were sampled
   *
   * @param name name of the span that will end up being the service-name
   * @param totalSpans number of spans to make
   * @param attributes attributes to set for the span
   * @return the number of times a span was sampled
   */
  private int getSampledSpanCount(String name, String totalSpans, Attributes attributes) {
    int numSampled = 0;
    int spans = Integer.parseInt(totalSpans);

    for (int i = 0; i < spans; i++) {

      Span span =
          this.tracer
              .spanBuilder(serviceName)
              .setSpanKind(SpanKind.SERVER)
              .setAllAttributes(attributes)
              .startSpan();

      if (span.getSpanContext().isSampled()) {
        numSampled++;
      }
      span.end();
    }
    return numSampled;
  }
}
