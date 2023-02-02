using System;
using Amazon.S3;
using Microsoft.AspNetCore.Mvc;
using System.Diagnostics;
using System.Net.Http;
using Microsoft.AspNetCore.Http.Extensions;

namespace integration_test_app.Controllers
{
    [ApiController]
    [Route("[controller]")]
    public class AppController : ControllerBase
    {
        private readonly AmazonS3Client s3Client = new AmazonS3Client();
        private readonly HttpClient httpClient = new HttpClient();
        private readonly MetricEmitter _metricEmitter = new MetricEmitter();
        private static Random rand = new Random(DateTime.Now.Millisecond);

        [HttpGet]
        [Route("/outgoing-http-call")]
        public string OutgoingHttp()
        {
            var res = httpClient.GetAsync("https://aws.amazon.com").Result;
            string statusCode = res.StatusCode.ToString();
            // mimic latency for now
            _metricEmitter.emitReturnTimeMetric(MimicLatency(),Request.GetDisplayUrl(),statusCode);
            
            // emit load size
            int loadSize = MimicPayLoadSize();
            Console.WriteLine(loadSize);
            _metricEmitter.emitBytesSentMetric(loadSize,Request.GetDisplayUrl(),statusCode);
            _metricEmitter.updateTotalBytesSentMetric(loadSize, Request.GetDisplayUrl(),statusCode);
            
            return GetTraceId();
        }

        [HttpGet]
        [Route("/get-aws-s3-buckets")]
        public string AWSSDKCall()
        {
            _ = s3Client.ListBucketsAsync().Result;

            return GetTraceId();
        }

        [HttpGet]
        [Route("/")]
        public string Default()
        {
            return "Application has started!";
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
            return rand.Next(107);
        }

        private static int MimicLatency()
        {
            return rand.Next(90,5000);
        }
    }

}
