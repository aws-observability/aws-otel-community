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
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class RandomMetricsProducer {
    private static final Logger logger = LogManager.getLogger();

    static int currentTimeAlive = 0;
    static int currentActiveThreads = 0;

    static String API_RANDOM_COUNTER_METRIC = "timeAlive";
    static String API_RANDOM_ASYNC_UPDOWN_METRIC = "totalHeapSize";
    static String API_RANDOM_UPDOWN_METRIC = "threadsActive";
    static String API_RANDOM_GAUGE = "cpuUsage";


    static private final Attributes COMMON_RANDOM_METRIC_ATTRIBUTES = Attributes.of(
            AttributeKey.stringKey("signal"), "metric",
            AttributeKey.stringKey("language"), "java",
            AttributeKey.stringKey("metricType"), "random");

    LongCounter timeCounter;
    LongCounter threadsCounter;

    long totalHeapSize;
    long cpuUsage;

    private final Config config;

    static ScheduledThreadPoolExecutor threadPool = new ScheduledThreadPoolExecutor(1);

    public RandomMetricsProducer(Config cfg) {
        this.config = cfg;
        Meter meter =
                GlobalOpenTelemetry.meterBuilder("adot-java-sample-app")
                        .setInstrumentationVersion("1.0")
                        .build();

        // give a instanceId appending to the metricname so that we can check the metric for each
        // round
        // of integ-test
        String timeAliveName = API_RANDOM_COUNTER_METRIC;
        String totalHeapSizeName = API_RANDOM_ASYNC_UPDOWN_METRIC;
        String threadsActiveName = API_RANDOM_UPDOWN_METRIC;
        String cpuUsageName = API_RANDOM_GAUGE;

        String instanceId = System.getenv("INSTANCE_ID");
        if (instanceId != null && !instanceId.trim().equals("")) {
            timeAliveName = API_RANDOM_COUNTER_METRIC + "_" + instanceId;
            totalHeapSizeName = API_RANDOM_ASYNC_UPDOWN_METRIC + "_" + instanceId;
            threadsActiveName = API_RANDOM_UPDOWN_METRIC + "_" + instanceId;
            cpuUsageName = API_RANDOM_GAUGE + "_" + instanceId;
        }

        // building synchronous random-based counter metric
        timeCounter =
                meter.counterBuilder(timeAliveName)
                        .setDescription("How Long Application is Alive")
                        .setUnit("s")
                        .build();

        // building asynchronous random-based up-down counter metric
        meter.upDownCounterBuilder(totalHeapSizeName)
                .setDescription("Heap size")
                .setUnit("1")
                .buildWithCallback(measurement -> measurement.record(totalHeapSize, COMMON_RANDOM_METRIC_ATTRIBUTES));

        // building synchronous random-based up-down counter metric
        threadsCounter =
                meter.counterBuilder(threadsActiveName)
                        .setDescription("Number of Threads Active")
                        .setUnit("1")
                        .build();

        // building random-based gauge metric
        meter.gaugeBuilder(cpuUsageName)
                .setDescription("Measures CPU Usage")
                .setUnit("1")
                .ofLongs()
                .buildWithCallback(
                        measurement ->
                                measurement.record(
                                        cpuUsage, COMMON_RANDOM_METRIC_ATTRIBUTES));
    }

    /** update total time sample app has run for */
    public void emitTimeAliveMetric() {
        timeCounter.add(config.getTimeAdd(), COMMON_RANDOM_METRIC_ATTRIBUTES);
        currentTimeAlive += config.getTimeAdd();
        logger.info("Current Time Alive: " + currentTimeAlive);
    }

    /**
     * update total heap size
     *
     * @param heapChange
     */
    public void updateSizeMetric(int heapChange) {
        totalHeapSize += heapChange;
    }

    /**
     * update number of active threads
     *
     * @param threadChange
     */
    public void emitActiveThreadsMetric(int threadChange) {
        threadsCounter.add(threadChange);
        currentActiveThreads += threadChange;
        logger.info("Threads Active: " + currentActiveThreads);
    }
    /**
     * update CPU usage
     *
     * @param newCpuUsage
     */
    public void updateCpuUsageMetric(Long newCpuUsage) {
        cpuUsage = newCpuUsage;
        logger.info("CPU Usage: " + cpuUsage);
    }

    public void start() {
        threadPool.scheduleAtFixedRate(() -> {
                    emitTimeAliveMetric();
                    updateSizeMetric(mimicHeapSize());
                    emitActiveThreadsMetric(mimicActiveThreads());
                    updateCpuUsageMetric(mimicCpuUsage());
                }, 0,
                config.getInterval(),  TimeUnit.SECONDS);
    }

    private int mimicHeapSize() {
        return ThreadLocalRandom.current().nextInt(config.getHeap());
    }

    private int mimicActiveThreads() {
        return ThreadLocalRandom.current().nextInt(config.getThreads());
    }

    private long mimicCpuUsage() {
        return ThreadLocalRandom.current().nextLong(config.getCpu());
    }
}
