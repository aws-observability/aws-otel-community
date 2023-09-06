using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using OpenTelemetry;
using System.Diagnostics.Metrics;
using OpenTelemetry.Metrics;
using OpenTelemetry.Instrumentation;

namespace dotnet_sample_app.Controllers
{
    public class MetricEmitter
    {
        const string DIMENSION_API_NAME = "apiName";
        const string DIMENSION_STATUS_CODE = "statusCode";
        
        static string API_COUNTER_METRIC = "total_api_requests";
        static string API_LATENCY_METRIC = "latency_time";
        static string API_SUM_METRIC = "total_bytes_sent"; 
        static string API_TOTAL_TIME_METRIC = "time_alive";
        static string API_TOTAL_HEAP_SIZE = "total_heap_size";
        static string API_TOTAL_THREAD_SIZE = "threads_active";
        static string API_CPU_USAGE = "cpu_usage"; 

        public Histogram<double> apiLatencyRecorder;
        public Counter<int> totalTimeSentObserver;
        public ObservableUpDownCounter<long> totalHeapSizeObserver;
        public UpDownCounter<int> totalThreadsObserver;

        private long apiRequestSent = 0;
        private long totalBytesSent = 0;
        private long totalHeapSize  = 0;
        private int cpuUsage = 0;
        private int totalTime = 1;
        private int totalThreads = 0;
        private bool threadsBool = true;
        private int returnTime = 100;

        private static Random rand = new Random(DateTime.Now.Millisecond);
        
        public MetricEmitter()
        {
            Meter meter = new Meter("adot", "1.0");            

            string latencyMetricName = API_LATENCY_METRIC;
            string totalApiRequestSent = API_COUNTER_METRIC;
            string totalApiBytesSentMetricName = API_SUM_METRIC;
            string totaltimealiveMetricName = API_TOTAL_TIME_METRIC;
            string totalHeapSizeMetricName = API_TOTAL_HEAP_SIZE;
            string totalThreadsMetricName = API_TOTAL_THREAD_SIZE;
            string cpuUsageMetricName = API_CPU_USAGE;

            string instanceId = Environment.GetEnvironmentVariable("INSTANCE_ID");
            if (instanceId != null && !instanceId.Trim().Equals(""))
            {
                latencyMetricName = API_LATENCY_METRIC + "_" + instanceId;
                totalApiRequestSent = API_COUNTER_METRIC + "_" + instanceId;
                totalApiBytesSentMetricName = API_SUM_METRIC + "_" + instanceId;
                totaltimealiveMetricName = API_TOTAL_TIME_METRIC + "_" + instanceId;
                totalHeapSizeMetricName = API_TOTAL_HEAP_SIZE + "_" + instanceId;
                totalThreadsMetricName = API_TOTAL_THREAD_SIZE + "_" + instanceId;
                cpuUsageMetricName = API_CPU_USAGE + "_" + instanceId;
    
            }
            

            meter.CreateObservableCounter(totalApiRequestSent,() => { 
                    return new Measurement<long>(apiRequestSent, new KeyValuePair<string, object>[] {
                        new KeyValuePair<string, object>("signal", "metric"), 
                        new KeyValuePair<string, object>("language", "dotnet"), 
                        new KeyValuePair<string, object>("metricType", "request")});
                }, 
                "1",
                "Increments by one every time a sampleapp endpoint is used");

            meter.CreateObservableCounter(totalApiBytesSentMetricName, () => { 
                    return new Measurement<long>(totalBytesSent, new KeyValuePair<string, object>[] {
                        new KeyValuePair<string, object>("signal", "metric"), 
                        new KeyValuePair<string, object>("language", "dotnet"), 
                        new KeyValuePair<string, object>("metricType", "request")});
                }, 
                "By",
                "Keeps a sum of the total amount of bytes sent while the application is alive");

            meter.CreateObservableGauge(cpuUsageMetricName, () => { 
                    return new Measurement<long>(cpuUsage, new KeyValuePair<string, object>[] {
                        new KeyValuePair<string, object>("signal", "metric"), 
                        new KeyValuePair<string, object>("language", "dotnet"), 
                        new KeyValuePair<string, object>("metricType", "random")});
                }, 
                "1",
                "Cpu usage percent");

            meter.CreateObservableUpDownCounter(totalHeapSizeMetricName, () => { 
                    return new Measurement<long>(totalHeapSize, new KeyValuePair<string, object>[] {
                        new KeyValuePair<string, object>("signal", "metric"), 
                        new KeyValuePair<string, object>("language", "dotnet"), 
                        new KeyValuePair<string, object>("metricType", "random")});
                }, 
                "1",
                "The current total heap size”");  

            apiLatencyRecorder = meter.CreateHistogram<double>(latencyMetricName, 
                 "ms", 
                 "Measures latency time in buckets of 100 300 and 500");

            totalThreadsObserver = meter.CreateUpDownCounter<int>(totalThreadsMetricName, 
                "1",
                "The total number of threads active”");

            totalTimeSentObserver = meter.CreateCounter<int>(totaltimealiveMetricName,
                "ms",
                "Measures the total time the application has been alive");
            

            totalTimeSentObserver.Add(totalTime,
                new KeyValuePair<string, object>("signal", "metric"),
                new KeyValuePair<string, object>("language", "dotnet"),
                new KeyValuePair<string, object>("metricType", "random"));
            totalThreadsObserver.Add(totalThreads++,
                new KeyValuePair<string, object>("signal", "metric"),
                new KeyValuePair<string, object>("language", "dotnet"),
                new KeyValuePair<string, object>("metricType", "random"));
            apiLatencyRecorder.Record(returnTime,
                new KeyValuePair<string, object>("signal", "metric"),
                new KeyValuePair<string, object>("language", "dotnet"),
                new KeyValuePair<string, object>("metricType", "request"));
        }   
        
        public void emitReturnTimeMetric(int returnTime) {
            apiLatencyRecorder.Record(
                returnTime,
                new KeyValuePair<string, object>("signal", "metric"),
                new KeyValuePair<string, object>("language", "dotnet"),
                new KeyValuePair<string, object>("metricType", "request"));
        }

        public void apiRequestSentMetric() {
            this.apiRequestSent += 1;
            Console.WriteLine("apiBs: "+ this.apiRequestSent);  
        } 
    
        public void updateTotalBytesSentMetric(int bytes) {
            totalBytesSent += bytes;
            Console.WriteLine("Total amount of bytes sent while the application is alive:"+ totalBytesSent);
        }

        public void updateTotalHeapSizeMetric() {
            this.totalHeapSize += rand.Next(0,1) * Program.cfg.RandomTotalHeapSizeUpperBound;
        }

        public void updateTotalThreadSizeMetric() {
            if (threadsBool) {
                if (totalThreads < Program.cfg.RandomThreadsActiveUpperBound) {
                    totalThreadsObserver.Add(1,
                        new KeyValuePair<string, object>("signal", "metric"),
                        new KeyValuePair<string, object>("language", "dotnet"),
                        new KeyValuePair<string, object>("metricType", "random"));
                    totalThreads += 1;
                }
                else {
                    threadsBool = false;
                    totalThreads -= 1;
                }
            }
            else {
                if (totalThreads > 0) {
                    totalThreadsObserver.Add(-1,
                        new KeyValuePair<string, object>("signal", "metric"),
                        new KeyValuePair<string, object>("language", "dotnet"),
                        new KeyValuePair<string, object>("metricType", "random"));
                    totalThreads -= 1;
                }
                else {
                    threadsBool = true;
                    totalThreads += 1;
                }
            }
        }   

        public void updateCpuUsageMetric() {
            this.cpuUsage = rand.Next(0,1) * Program.cfg.RandomCpuUsageUpperBound;
        }

        public void updateTotalTimeMetric() {
           totalTimeSentObserver.Add(Program.cfg.RandomTimeAliveIncrementer,
                new KeyValuePair<string, object>("signal", "metric"),
                new KeyValuePair<string, object>("language", "dotnet"),
                new KeyValuePair<string, object>("metricType", "random"));
        }

        public async Task UpdateRandomMetrics(CancellationToken cancellationToken = default) {
            void update() {
                updateTotalTimeMetric();
                updateTotalHeapSizeMetric();
                updateTotalThreadSizeMetric();
                updateCpuUsageMetric();
            }

            while (true) {
                var delayTask = Task.Delay(Program.cfg.TimeInterval * 1000, cancellationToken);
                await Task.Run(() => update());
                await delayTask;
            }
        }

    }
}
