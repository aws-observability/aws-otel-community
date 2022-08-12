package com.amazon.sampleapp;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;

public class MetricEmitter {
  private static final Logger logger = LogManager.getLogger();
  private static Config getConfig() { return new Config(); }
  static final AttributeKey<String> DIMENSION_API_NAME = AttributeKey.stringKey("apiName");
  static final AttributeKey<String> DIMENSION_STATUS_CODE = AttributeKey.stringKey("statusCode");
  static int currentTimeAlive = 0;
  static int currentBytesSent = 0;
  static int currentActiveThreads = 0;

  static String API_COUNTER_METRIC = "totalBytesSent";

  static String API_ASYNC_COUNTER_METRIC = "totalApiRequests";

  static String API_HISTOGRAM_METRIC = "latencyTime";

  static String API_RANDOM_COUNTER_METRIC = "timeAlive";

  static String API_RANDOM_ASYNC_UPDOWN_METRIC = "totalHeapSize";

  static String API_RANDOM_UPDOWN_METRIC = "threadsActive";

  static String API_RANDOM_GAUGE = "cpuUsage";

  // The below API name and status code dimensions are currently shared by all metrics observer in
  // this class.
  String apiNameValue = "";
  String statusCodeValue = "";

  //Declaring variables to track values of metrics
  DoubleHistogram apiLatencyHistogram;
  LongCounter bytesCounter;
  LongCounter timeCounter;
  LongCounter threadsCounter;

  long totalApiRequests;
  long totalHeapSize;
  long cpuUsage;


  public MetricEmitter() {
    Meter meter =
        GlobalOpenTelemetry.meterBuilder("aws-otel").setInstrumentationVersion("1.0").build();

    // give a instanceId appending to the metricname so that we can check the metric for each round
    // of integ-test
    String totalBytesSentName = API_COUNTER_METRIC;
    String totalApiRequestsName = API_ASYNC_COUNTER_METRIC;
    String latencyTimeName = API_HISTOGRAM_METRIC;
    String timeAliveName = API_RANDOM_COUNTER_METRIC;
    String totalHeapSizeName = API_RANDOM_ASYNC_UPDOWN_METRIC;
    String threadsActiveName = API_RANDOM_UPDOWN_METRIC;
    String cpuUsageName = API_RANDOM_GAUGE;

    String instanceId = System.getenv("INSTANCE_ID");
    if (instanceId != null && !instanceId.trim().equals("")) {
        totalBytesSentName = API_COUNTER_METRIC + "_" + instanceId;
        totalApiRequestsName = API_ASYNC_COUNTER_METRIC + "_" + instanceId;
        latencyTimeName = API_HISTOGRAM_METRIC + "_" + instanceId;
        timeAliveName = API_RANDOM_COUNTER_METRIC + "_" + instanceId;
        totalHeapSizeName = API_RANDOM_ASYNC_UPDOWN_METRIC + "_" + instanceId;
        threadsActiveName = API_RANDOM_UPDOWN_METRIC + "_" + instanceId;
        cpuUsageName = API_RANDOM_GAUGE + "_" + instanceId;
    }

    //building synchronous request-based counter metric
    bytesCounter = meter
            .counterBuilder(totalBytesSentName)
            .setDescription("API request load sent in bytes")
            .setUnit("mb")
            .build();

    //building asynchronous request-based counter metric
    meter
            .counterBuilder(totalApiRequestsName)
            .setDescription("Total amount of API requests to endpoint")
            .setUnit("1")
            .buildWithCallback(measurement ->
                    measurement.record(
                            totalApiRequests,
                            Attributes.of(DIMENSION_API_NAME, apiNameValue, DIMENSION_STATUS_CODE, statusCodeValue)));

    //building histogram request-based metric
    apiLatencyHistogram = meter
            .histogramBuilder(latencyTimeName)
            .setDescription("API latency time")
            .setUnit("ms")
            .build();

    //building synchronous random-based counter metric
    timeCounter = meter
            .counterBuilder(timeAliveName)
            .setDescription("How Long Application is Alive")
            .setUnit("s")
            .build();

    //building asynchronous random-based up-down counter metric
    meter
            .upDownCounterBuilder(totalHeapSizeName)
            .setDescription("Heap size")
            .setUnit("1")
            .buildWithCallback(measurement ->
                    measurement.record(
                            totalHeapSize,
                            Attributes.of(DIMENSION_API_NAME, apiNameValue, DIMENSION_STATUS_CODE, statusCodeValue)));

    //building synchronous random-based up-down counter metric
    threadsCounter = meter
            .counterBuilder(threadsActiveName)
            .setDescription("Number of Threads Active")
            .setUnit("1")
            .build();

    //building random-based gauge metric
    meter
            .gaugeBuilder(cpuUsageName)
            .setDescription("Measures CPU Usage")
            .setUnit("1")
            .ofLongs()
            .buildWithCallback(measurement ->
                    measurement.record(
                            cpuUsage,
                            Attributes.of(DIMENSION_API_NAME, apiNameValue, DIMENSION_STATUS_CODE, statusCodeValue)));
  }

  /**
   * emit http request load size with counter metrics type
   *
   * @param bytes
   * @param apiName
   * @param statusCode
   */
  public void emitBytesSentMetric(int bytes, String apiName, String statusCode) {
    Attributes bytesAttributes = Attributes.of(DIMENSION_API_NAME, apiName, DIMENSION_STATUS_CODE, statusCode);
    bytesCounter.add(bytes, bytesAttributes);
    currentBytesSent += bytes;
    logger.info("Total Bytes Sent: " + currentBytesSent);
  }

  /**
   * emit total API requests metrics
   *
   * @param apiName
   * @param statusCode
   */
  public void emitApiRequestsMetric(String apiName, String statusCode) {
    totalApiRequests += 1;
    apiNameValue = apiName;
    statusCodeValue = statusCode;
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
    apiLatencyHistogram.record(
            returnTime, Attributes.of(DIMENSION_API_NAME, apiName, DIMENSION_STATUS_CODE, statusCode));
    logger.info("New Latency Time: " + returnTime);
  }

  /**
   * update total time sample app has run for
   *
   * @param apiName
   * @param statusCode
   */
  public void emitTimeAliveMetric(String apiName, String statusCode) {
    Config config = getConfig();
    Attributes timeAttributes = Attributes.of(DIMENSION_API_NAME, apiName, DIMENSION_STATUS_CODE, statusCode);
    timeCounter.add(config.getTimeAdd(), timeAttributes);
    currentTimeAlive += config.getTimeAdd();
    logger.info("Current Time Alive: " + currentTimeAlive);
  }

  /**
   * update total heap size
   *
   * @param heapChange
   * @param apiName
   * @param statusCode
   */
  public void emitHeapSizeMetric(int heapChange, String apiName, String statusCode) {
    totalHeapSize += heapChange;
    apiNameValue = apiName;
    statusCodeValue = statusCode;
    logger.info("Heap Size: " + totalHeapSize);
  }

  /**
   * update number of active threads
   *
   * @param threadChange
   * @param apiName
   * @param statusCode
   */
  public void emitActiveThreadsMetric(int threadChange, String apiName, String statusCode) {
    Attributes threadAttributes = Attributes.of(DIMENSION_API_NAME, apiName, DIMENSION_STATUS_CODE, statusCode);
    threadsCounter.add(threadChange, threadAttributes);
    currentActiveThreads += threadChange;
    logger.info("Threads Active: " + currentActiveThreads);
  }
  /**
   * update CPU usage
   *
   * @param newCpuUsage
   * @param apiName
   * @param statusCode
   */
  public void emitCpuUsageMetric(Long newCpuUsage, String apiName, String statusCode) {
    cpuUsage = newCpuUsage;
    apiNameValue = apiName;
    statusCodeValue = statusCode;
    logger.info("CPU Usage: " + cpuUsage);
  }
}
