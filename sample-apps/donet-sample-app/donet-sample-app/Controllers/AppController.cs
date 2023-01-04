using Amazon.S3;
using Microsoft.AspNetCore.Mvc;
using System.Diagnostics;
using System.Net.Http;

namespace donet_sample_app.Controllers
{

    var meter = new Meter("MyApplication");

    var counter = meter.CreateCounter<int>("Requests");
    var histogram = meter.CreateHistogram<float>("RequestDuration", unit: "ms");

    [ApiController]
    [Route("[controller]")]
    public class AppController : ControllerBase
    {
        private readonly AmazonS3Client s3Client = new AmazonS3Client();
        private readonly HttpClient httpClient = new HttpClient();

        [HttpGet]
        [Route("/outgoing-http-call")]
        public string OutgoingHttp()
        {
            counter.Add(1, KeyValuePair.Create<string, object?>("name", name));
            //var stopwatch = Stopwatch.StartNew();
            _ = httpClient.GetAsync("https://aws.amazon.com").Result;

            //histogram.Record(stopwatch.ElapsedMilliseconds,tag: KeyValuePair.Create<string, object?>("Host", "https://aws.amazon.com"));


            return GetTraceId();
        }

        [HttpGet]
        [Route("/aws-sdk-call")]
        public string AWSSDKCall()
        {
            _ = s3Client.ListBucketsAsync().Result;

            return GetTraceId();
        }

        [HttpGet]
        [Route("/outgoing-sampleapp")]
        public string AWSSDKCall()
        {
            _ = s3Client.ListBucketsAsync().Result;

            return GetTraceId();
        }

        [HttpGet]
        [Route("/")]
        public string Default()
        {
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
    }

}
