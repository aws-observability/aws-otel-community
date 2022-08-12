package com.amazon.sampleapp;

import static spark.Spark.*;

import io.opentelemetry.api.trace.Span;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.ThreadLocalRandom;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.services.s3.S3Client;
import java.util.ArrayList;

public class App extends Thread {
  private static final Logger logger = LogManager.getLogger();
  private static final boolean shouldSampleAppLog =
      System.getenv().getOrDefault("SAMPLE_APP_LOG_LEVEL", "INFO").equals("INFO");

  static final String REQUEST_START_TIME = "requestStartTime";

  private static MetricEmitter buildMetricEmitter() {
    return new MetricEmitter();
  }

  private static Config getConfig() { return new Config(); }
  static boolean threadActive = false;
  static String randApiName;
  static String randStatusCode;

  public static void main(String[] args) {
    MetricEmitter metricEmitter = buildMetricEmitter();
    Config config = getConfig();
    final Call.Factory httpClient = new OkHttpClient();
    final S3Client s3 = S3Client.builder().build();
    String port;
    String host;
    String listenAddress = System.getenv("LISTEN_ADDRESS");

    //set host and port number of sample app
    if (listenAddress == null) {
        logger.info(config.getHost());
        host = config.getHost();
        port = config.getPort();
    } else {
      String[] splitAddress = listenAddress.split(":");
      host = splitAddress[0];
      port = splitAddress[1];
    }
    logger.info("dead");
    // set sampleapp app port number and ip address
    port(Integer.parseInt(port));
    ipAddress(host);

    get(
        "/",
        (req, res) -> {
            //create a thread to start emitting request based metrics in a separate thread
          if(!threadActive) {
              randApiName = req.pathInfo();
              randStatusCode = String.valueOf(res.status());
              App thread = new App();
              threadActive = true;
              thread.start();
          }
          return "healthcheck";
        });

    /** trace http request */
    get(
        "/outgoing-http-call",
        (req, res) -> {
            if(!threadActive) {
                randApiName = req.pathInfo();
                randStatusCode = String.valueOf(res.status());
                App thread = new App();
                threadActive = true;
                thread.start();
            }
          if (shouldSampleAppLog) {
            logger.info("Executing outgoing-http-call");
          }

          try (Response response =
              httpClient
                  .newCall(new Request.Builder().url("https://aws.amazon.com").build())
                  .execute()) {
          } catch (IOException e) {
            throw new UncheckedIOException("Could not fetch endpoint", e);
          }

          return getXrayTraceId();
        });

    /** trace aws sdk request */
    get(
        "/aws-sdk-call",
        (req, res) -> {
            if(!threadActive) {
                randApiName = req.pathInfo();
                randStatusCode = String.valueOf(res.status());
                App thread = new App();
                threadActive = true;
                thread.start();
            }
          if (shouldSampleAppLog) {
            logger.info("Executing aws-sdk-all");
          }

          s3.listBuckets();

          return getXrayTraceId();
        });

    /** sample app request */
    get(
            "/outgoing-sampleapp",
            (req, res) -> {
                if (shouldSampleAppLog) {
                    logger.info("Executing outgoing-sampleapp call");
                }
                ArrayList<String> samplePorts = config.getSamplePorts();
                if (config.getSamplePorts().isEmpty()) {
                    try (Response response =
                                 httpClient
                                         .newCall(new Request.Builder().url("https://aws.amazon.com").build())
                                         .execute()) {
                    } catch (IOException e) {
                        throw new UncheckedIOException("Could not fetch endpoint", e);
                    }
                }
                else {
                    for (String i : samplePorts) {
                        try (Response response =
                                     httpClient
                                             .newCall(new Request.Builder().url("localhost:" + i).build())
                                             .execute()) {
                        } catch (IOException e) {
                            throw new UncheckedIOException("Could not fetch endpoint", e);
                        }
                    }
                }
                return getXrayTraceId();
            });

    /** record a start time for each request */
    before(
        (req, res) -> {
          req.attribute(REQUEST_START_TIME, System.currentTimeMillis());
        });

    after(
        (req, res) -> {
          // emitting request-based metrics
          if (req.pathInfo().equals("/outgoing-http-call")) {
            String statusCode = String.valueOf(res.status());
            // calculate return time
            Long requestStartTime = req.attribute(REQUEST_START_TIME);
            logger.info("Start time: " + requestStartTime);
            // emit api latency
            metricEmitter.emitApiLatencyMetric(
                System.currentTimeMillis() - requestStartTime, req.pathInfo(), statusCode);
            //generate a random value of bytes to emit
            int mimicBytes = mimicBytesSent();
            metricEmitter.emitBytesSentMetric(mimicBytes, req.pathInfo(), statusCode);
            //increment the number of requests to emit
            metricEmitter.emitApiRequestsMetric(req.pathInfo(), statusCode);
          }
        });

    exception(
        Exception.class,
        (exception, request, response) -> {
          // Handle the exception here
          exception.printStackTrace();
        });
  }

  public void run() {
      MetricEmitter metricEmitter = buildMetricEmitter();
      Config config = getConfig();
      while(true) {
          metricEmitter.emitTimeAliveMetric(randApiName, randStatusCode);
          metricEmitter.emitHeapSizeMetric(mimicHeapSize(), randApiName, randStatusCode);
          metricEmitter.emitActiveThreadsMetric(mimicActiveThreads(), randApiName, randStatusCode);
          metricEmitter.emitCpuUsageMetric(mimicCpuUsage(), randApiName, randStatusCode);
          try {
              Thread.sleep(1000 * config.getInterval());
          } catch (InterruptedException e) {
              throw new RuntimeException(e);
          }
      }
  }
  // get x-ray trace id
  private static String getXrayTraceId() {
    String traceId = Span.current().getSpanContext().getTraceId();
    String xrayTraceId = "1-" + traceId.substring(0, 8) + "-" + traceId.substring(8);

    return String.format("{\"traceId\": \"%s\"}", xrayTraceId);
  }

  private static int mimicBytesSent() {
      int generatedBytes = ThreadLocalRandom.current().nextInt(100);
      return generatedBytes;
  }

  private static int mimicHeapSize() {
      Config config = getConfig();
      int generatedHeapSize = ThreadLocalRandom.current().nextInt(config.getHeap());
      return generatedHeapSize;
  }

  private static int mimicActiveThreads() {
      Config config = getConfig();
      int generatedThreads = ThreadLocalRandom.current().nextInt(config.getThreads());
      return generatedThreads;
  }

  private static long mimicCpuUsage() {
      Config config = getConfig();
      long generatedCpu = ThreadLocalRandom.current().nextLong(config.getCpu());
      return generatedCpu;
  }
}
