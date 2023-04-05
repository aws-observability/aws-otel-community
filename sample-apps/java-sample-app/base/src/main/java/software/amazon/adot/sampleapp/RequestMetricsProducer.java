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

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class RequestMetricsProducer {
    private static final Logger logger = LogManager.getLogger();

    static final AttributeKey<String> DIMENSION_API_NAME = AttributeKey.stringKey("apiName");
    static final AttributeKey<String> DIMENSION_STATUS_CODE = AttributeKey.stringKey("statusCode");

    static String API_COUNTER_METRIC = "total_bytes_sent";
    static String API_ASYNC_COUNTER_METRIC = "total_api_requests";
    static String API_HISTOGRAM_METRIC = "latency_time";

    private static Attributes COMMON_REQUEST_METRICS_ATTRIBUTES;


    // Declaring variables to track values of metrics
    private DoubleHistogram apiLatencyHistogram;
    private LongCounter bytesCounter;

    long totalApiRequests;

    private long currentBytesSent = 0;

    public RequestMetricsProducer(String instrumentation) {
        COMMON_REQUEST_METRICS_ATTRIBUTES = Attributes.of(
                AttributeKey.stringKey("signal"), "metric",
                AttributeKey.stringKey("language"), instrumentation,
                AttributeKey.stringKey("metricType"), "request");
        Meter meter =
                GlobalOpenTelemetry.meterBuilder("adot-java-sample-app")
                        .setInstrumentationVersion("1.0")
                        .build();

        // give a instanceId appending to the metricname so that we can check the metric for each
        // round
        // of integ-test
        String totalBytesSentName = API_COUNTER_METRIC;
        String totalApiRequestsName = API_ASYNC_COUNTER_METRIC;
        String latencyTimeName = API_HISTOGRAM_METRIC;

        String instanceId = System.getenv("INSTANCE_ID");
        if (instanceId != null && !instanceId.trim().equals("")) {
            totalBytesSentName = API_COUNTER_METRIC + "_" + instanceId;
            totalApiRequestsName = API_ASYNC_COUNTER_METRIC + "_" + instanceId;
            latencyTimeName = API_HISTOGRAM_METRIC + "_" + instanceId;
        }

        // building synchronous request-based counter metric
        bytesCounter =
                meter.counterBuilder(totalBytesSentName)
                        .setDescription("API request load sent in bytes")
                        .setUnit("mb")
                        .build();

        // building asynchronous request-based counter metric
        meter.counterBuilder(totalApiRequestsName)
                .setDescription("Total amount of API requests to endpoint")
                .setUnit("1")
                .buildWithCallback(
                        measurement ->
                                measurement.record(
                                        totalApiRequests, COMMON_REQUEST_METRICS_ATTRIBUTES));

        // building histogram request-based metric
        apiLatencyHistogram =
                meter.histogramBuilder(latencyTimeName)
                        .setDescription("API latency time")
                        .setUnit("ms")
                        .build();

    }

    /**
     * emit http request load size with counter metrics type
     *
     * @param bytes
     * @param apiName
     * @param statusCode
     */
    public void emitBytesSentMetric(int bytes, String apiName, String statusCode) {
        Attributes bytesAttributes = Attributes.builder()
                .putAll(COMMON_REQUEST_METRICS_ATTRIBUTES)
                .put(DIMENSION_API_NAME, apiName)
                .build();
        bytesCounter.add(bytes, bytesAttributes);
        currentBytesSent += bytes;
        logger.info("Total Bytes Sent: " + currentBytesSent);
    }

    /**
     * emit total API requests metrics
     *
     */
    public void updateApiRequestsMetric() {
        totalApiRequests += 1;
        logger.info("API Requests:" + totalApiRequests);
    }

    /**
     * emit http request latency metrics with summary metric type
     *
     * @param returnTime
     * @param apiName
     * @param statusCode
     */
    public void emitApiLatencyMetric(Long returnTime, String apiName, String statusCode) {
        Attributes latencyAttributes = Attributes.builder()
                .putAll(COMMON_REQUEST_METRICS_ATTRIBUTES)
                .put(DIMENSION_API_NAME, apiName)
                .build();

        apiLatencyHistogram.record(returnTime, latencyAttributes);
        logger.info("New Latency Time: " + returnTime);
    }
}
