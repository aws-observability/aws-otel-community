package software.amazon.adot.sampleapp;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import java.util.Collections;
import java.util.Map;
import org.apache.logging.log4j.core.util.ContextDataProvider;

/**
 * a format for consumption by AWS X-Ray and related services.
 */
public class AwsXrayContextDataProvider implements ContextDataProvider {
    private static final String TRACE_ID_KEY = "AWS-XRAY-TRACE-ID";

    @Override
    public Map<String, String> supplyContextData() {
        Span currentSpan = Span.current();
        SpanContext spanContext = currentSpan.getSpanContext();
        if (!spanContext.isValid()) {
            return Collections.emptyMap();
        }

        String value =
                "1-"
                        + spanContext.getTraceId().substring(0, 8)
                        + "-"
                        + spanContext.getTraceId().substring(8)
                        + "@"
                        + spanContext.getSpanId();
        return Collections.singletonMap(TRACE_ID_KEY, value);
    }
}
