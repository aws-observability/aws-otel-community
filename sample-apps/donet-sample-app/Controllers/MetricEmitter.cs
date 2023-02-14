using System;
using System.Collections.Generic;
using OpenTelemetry;
using System.Diagnostics.Metrics;
using OpenTelemetry.Metrics;
using OpenTelemetry.Instrumentation;

namespace integration_test_app.Controllers
{
    public class MetricEmitter
    {
        const string DIMENSION_API_NAME = "apiName";
        const string DIMENSION_STATUS_CODE = "statusCode";
        

        static string API_COUNTER_METRIC = "totalApiRequests";
        static string API_LATENCY_METRIC = "latencyTime";
        static string API_SUM_METRIC = "totalBytesSent";
        static string API_TOTAL_TIME_METRIC = "timeAlive";
        static string API_TOTAL_HEAP_SIZE = "totalHeapSiz";
        static string API_TOTAL_THREAD_SIZE = "threadsActive";
        static string API_CPU_USAGE = "cpuUsage";
    

        Histogram<double> apiLatencyRecorder;
        Counter<long> totalTimeSentObserver;

        static long apiRequestSent = 0;
        static long totalBytesSent = 0;
        static long totalHeapSize  = 0;
        static long totalThreadSize = 0;
        static int cpuUsage = 0;

        // The below API name and status code dimensions are currently shared by all metrics observer in
        // this class.
        string apiNameValue = "";
        string statusCodeValue = "";

        
        public MetricEmitter()
        {
            Meter meter = new Meter("aws-otel", "1.0");
            using var meterProvider = Sdk.CreateMeterProviderBuilder()
                .AddMeter("aws-otel")
                .AddOtlpExporter()
                .Build();
            
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
            

            meter.CreateObservableCounter(totalApiRequestSent,() => apiRequestSent, 
                "1",
                "Increments by one every time a sampleapp endpoint is used");

            meter.CreateObservableCounter(totalApiBytesSentMetricName, () => totalBytesSent, 
                "By",
                "Keeps a sum of the total amount of bytes sent while the application is alive");

            apiLatencyRecorder = meter.CreateHistogram<double>(latencyMetricName,
                 "ms", 
                 "Measures latency time in buckets of 100 300 and 500");

            totalTimeSentObserver = meter.CreateCounter<long>(totaltimealiveMetricName,
                "ms",
                "Measures the total time the application has been alive");

            meter.CreateObservableUpDownCounter(totalHeapSizeMetricName, () => totalHeapSize, 
                "By",
                "The current total heap size”");  

            meter.CreateObservableCounter(totalThreadsMetricName, () => totalThreadSize,
                "1",
                "The current total number of threads”");

            meter.CreateObservableGauge(cpuUsageMetricName, () => cpuUsage,
                "1",
                "Cpu usage percent");

        }   
        
        public void emitReturnTimeMetric(long returnTime, String apiName, String statusCode) {
            apiLatencyRecorder.Record(
                returnTime,
                new KeyValuePair<string, object>(DIMENSION_API_NAME, apiName),
                new KeyValuePair<string, object>(DIMENSION_STATUS_CODE, statusCode));
        }

        public void apiRequestSentMetric(String apiName, String statusCode) {
                apiRequestSent += 1;
                Console.WriteLine("apiBs: "+ apiRequestSent);  
        } 
    
        public void updateTotalBytesSentMetric(int bytes, String apiName, String statusCode) {
            totalBytesSent += bytes;
            Console.WriteLine("Total amount of bytes sent while the application is alive:"+ totalBytesSent);
            apiNameValue = apiName;
            statusCodeValue = statusCode;
        }

        public void updateTotalHeapSizeMetric(int heapSize) {
            totalHeapSize += heapSize;
        }

        public void updateTotalThreadSizeMetric(int threadSize) {
            totalThreadSize += threadSize;
        }

        public void updateCpuUsageMetric(int cpuUsage) {
            cpuUsage = cpuUsage;
        }

        public void updateTotalTimeMetric(int totaltime) {
           totalTimeSentObserver.Add(totaltime);
        }

    }
}