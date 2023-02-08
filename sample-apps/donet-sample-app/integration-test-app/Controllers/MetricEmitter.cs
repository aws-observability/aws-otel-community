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
        
        // Request based metrics
        static string API_SUM_METRIC = "totalApiRequests";
        static string API_COUNTER_METRIC = "totalBytesSent";
        static string API_LATENCY_METRIC = "latencyTime";

        // Random metrics
        static string API_TIME_ALIVE = "timeAlive";
        static string API_TOTAL_HEAP_SIZE = "totalHeapSize";
        static string API_THREAD_ACTIVE = "threadsActive";
        static string API_CPU_USAGE = "cpuUsage";

        Histogram<double> apiLatencyRecorder;
        Counter<long> totalBytesSentObserver;

        long apiBytesSent;
        long queueSizeChange;

        long totalBytesSent;
        long apiLastLatency;
        long actualQueueSize;

    
        string apiNameValue = "";
        string statusCodeValue = "";
        
        public MetricEmitter()
        {
            Meter meter = new Meter("aws-otel", "1.0");
            using var meterProvider = Sdk.CreateMeterProviderBuilder()
                .AddMeter("aws-otel")
                .AddOtlpExporter()
                .Build();
            
            string totalApiRequests = API_SUM_METRIC;
            string totalBytesSent = API_COUNTER_METRIC;
            string latencyTime = API_LATENCY_METRIC;
            string timeAlive = API_TIME_ALIVE;
            string totalHeapSize = API_TOTAL_HEAP_SIZE;
            string threadsActive = API_THREAD_ACTIVE;
            string cpuUsage = API_CPU_USAGE;

            string instanceId = Environment.GetEnvironmentVariable("INSTANCE_ID");
            if (instanceId != null && !instanceId.Trim().Equals(""))
            {
                latencyTime = API_LATENCY_METRIC + "_" + instanceId;
                totalBytesSent = API_COUNTER_METRIC + "_" + instanceId;
                totalApiRequests = API_SUM_METRIC + "_" + instanceId;
                timeAlive = API_TIME_ALIVE + "_" + instanceId;
                totalHeapSize = API_TOTAL_HEAP_SIZE + "_" + instanceId;
                threadsActive = API_THREAD_ACTIVE + "_" + instanceId;
                cpuUsage = API_CPU_USAGE + "_" + instanceId;
       
            }
            apiLatencyRecorder = meter.CreateHistogram<double>(latencyTime,
                 "ms", 
                 "Measures latency time in buckets of 100 300 and 500");


            KeyValuePair<string,object> dimApiName = new KeyValuePair<string, object>(DIMENSION_API_NAME, apiNameValue);
            KeyValuePair<string, object> dimStatusCode =
                new KeyValuePair<string, object>(DIMENSION_STATUS_CODE, statusCodeValue);
            
            meter.CreateObservableGauge(totalApiRequests, () => totalApiRequests, 
                "1",
                "Increments by one every time a sampleapp endpoint is used");
            
            meter.CreateObservableCounter(totalBytesSent,() => apiBytesSent, 
                "By",
                "Keeps a sum of the total amount of bytes sent while the application is alive");

            meter.CreateObservableCounter(timeAlive,() => timeAlive, 
                "ms",
                "Total amount of time that the application has been alive");

            meter.CreateObservableGauge(totalHeapSize, () => totalHeapSize, 
                "one", "The actual queue size observed at collection interval");

            meter.CreateObservableCounter(threadsActive,() => threadsActive, 
                "1",
                "The total number of threads active");

            meter.CreateObservableGauge(cpuUsage,() => cpuUsage, 
                "ms",
                "Total amount of time that the application has been alive");

            
          
            
        }
        
        public void emitReturnTimeMetric(long returnTime, String apiName, String statusCode) {
            apiLatencyRecorder.Record(
                returnTime,
                new KeyValuePair<string, object>(DIMENSION_API_NAME, apiName),
                new KeyValuePair<string, object>(DIMENSION_STATUS_CODE, statusCode));
        }
        public void emitBytesSentMetric(int bytes, String apiName, String statusCode) {
            Console.WriteLine("ebs: " + bytes);
            apiBytesSent += bytes;
            Console.WriteLine("apiBs: "+ apiBytesSent);
        } 
       
        public void emitQueueSizeChangeMetric(int queueSizeChange, String apiName, String statusCode) {
            queueSizeChange += queueSizeChange;
        }
        
        public void updateTotalBytesSentMetric(int bytes, String apiName, String statusCode) {
            totalBytesSent += bytes;
            apiNameValue = apiName;
            statusCodeValue = statusCode;
        }
        
        public void updateLastLatencyMetric(long returnTime, String apiName, String statusCode) {
            apiLastLatency = returnTime;
            apiNameValue = apiName;
            statusCodeValue = statusCode;
        }
        
        public void updateActualQueueSizeMetric(int queueSizeChange, String apiName, String statusCode) {
            actualQueueSize += queueSizeChange;
            apiNameValue = apiName;
            statusCodeValue = statusCode;
        }
        

    }
}