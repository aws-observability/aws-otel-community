using System;
using Amazon.S3;
using Microsoft.AspNetCore.Mvc;
using System.Diagnostics;
using System.Net.Http;
using Microsoft.AspNetCore.Http.Extensions;
using OpenTelemetry;
using OpenTelemetry.Metrics;
using OpenTelemetry.Instrumentation;
using System.Diagnostics.Metrics;
using OpenTelemetry.Resources;
using OpenTelemetry.Trace;

namespace donet_sample_app.Controllers
{
    [ApiController]
    [Route("[controller]")]
    public class AppController : ControllerBase
    {
        private readonly AmazonS3Client s3Client = new AmazonS3Client();
        private readonly HttpClient httpClient = new HttpClient();
        private readonly MetricEmitter _metricEmitter = new MetricEmitter();
        private static Random rand = new Random(DateTime.Now.Millisecond);
        

        public AppController()
        {
            //Random Metrics
            _metricEmitter.updateTotalTimeMetric(rand.Next(10,30));
            _metricEmitter.updateTotalHeapSizeMetric(rand.Next(100,500));
            _metricEmitter.updateTotalThreadSizeMetric(rand.Next(1,20));
            _metricEmitter.updateCpuUsageMetric(rand.Next(1,100));
            // _metricEmitter.updateTotalHeapMetric(rand.Next(1,100));
        }

        
        [HttpGet]
        [Route("/outgoing-http-call")]
        public string OutgoingHttp()
        {
            var res = httpClient.GetAsync("https://aws.amazon.com").Result;
            string statusCode = res.StatusCode.ToString();
            
            // Request Based Metrics
            _metricEmitter.emitReturnTimeMetric(MimicLatency());
            int loadSize = MimicPayLoadSize();
            _metricEmitter.apiRequestSentMetric(Request.GetDisplayUrl(),statusCode);
            _metricEmitter.updateTotalBytesSentMetric(loadSize, Request.GetDisplayUrl(),statusCode);

            
            return GetTraceId();
        }

        [HttpGet]
        [Route("/get-aws-s3-bucket")]
        public string AWSSDKCall()
        {
            var res = s3Client.ListBucketsAsync().Result;
            string statusCode = res.HttpStatusCode.ToString();
            
            // Request Based Metrics
            _metricEmitter.emitReturnTimeMetric(MimicLatency());
            int loadSize = MimicPayLoadSize();
            _metricEmitter.apiRequestSentMetric(Request.GetDisplayUrl(),statusCode);
            _metricEmitter.updateTotalBytesSentMetric(loadSize, Request.GetDisplayUrl(),statusCode);
            _metricEmitter.totalTimeSentObserver.Add(3);


            return GetTraceId();
        }

        [HttpGet]
        [Route("/")]
        public string Default()
        {
            return "Application started!";
        }

        [HttpGet]
        [Route("/outgoing-sampleapp")]
        public string Default2()
        {
            var port = 4567;
            
            return "Application started!";
        }

        private string GetTraceId()
        {
            var traceId = Activity.Current.TraceId.ToHexString();
            var version = "1";
            var epoch = traceId.Substring(0, 8);
            var random = traceId.Substring(8);
            return "{" + "\"traceId\"" + ": " + "\"" + version + "-" + epoch + "-" + random + "\"" + "}";
        }

        private static int MimicPayLoadSize()
        {
            return rand.Next(101);
        }

        private static int MimicLatency()
        {
            return rand.Next(100,500);
        }
    }

}
