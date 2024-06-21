// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

using System.Diagnostics;
using Microsoft.AspNetCore.Mvc;

namespace dotnet_sample_app.Controllers;

[ApiController]
[Route("[controller]")]
public class AppController : ControllerBase
{
    private static readonly ActivitySource Tracer = new ("centralized-sampling-tests");

    [HttpGet]
    [Route("/")]
    public string Default()
    {
        return "Application started!";
    }

    // Get endpoint for /getSampled that requires three header values for user, service_name, and
    // required Returns the number of times a span was sampled out of the creation of 1000 spans
    [HttpGet]
    [Route("/getSampled")]
    public int GetSampled()
    {
        this.Request.Headers.TryGetValue("user", out var userAttribute);
        this.Request.Headers.TryGetValue("service_name", out var name);
        this.Request.Headers.TryGetValue("required", out var required);
        this.Request.Headers.TryGetValue("totalSpans", out var totalSpans);

        ActivityTagsCollection attributes = new ActivityTagsCollection
        {
            { "http.request.method", "GET" },
            { "url.full", "http://localhost:8080/getSampled" },
            { "user", userAttribute },
            { "http.route", "/getSampled" },
            { "required", required },
            { "url.path", "/getSampled" },
        };

        return this.GetSampledSpanCount(name, totalSpans, attributes);
    }

    // Post endpoint for /getSampled that requires three header values for user, service_name, and
    // required Returns the number of times a span was sampled out of the creation of 1000 spans
    [HttpPost]
    [Route("/getSampled")]
    public int PostSampled()
    {
        this.Request.Headers.TryGetValue("user", out var userAttribute);
        this.Request.Headers.TryGetValue("service_name", out var name);
        this.Request.Headers.TryGetValue("required", out var required);
        this.Request.Headers.TryGetValue("totalSpans", out var totalSpans);

        ActivityTagsCollection attributes = new ActivityTagsCollection
        {
            { "http.request.method", "POST" },
            { "url.full", "http://localhost:8080/getSampled" },
            { "user", userAttribute },
            { "http.route", "/getSampled" },
            { "required", required },
            { "url.path", "/getSampled" },
        };

        return this.GetSampledSpanCount(name, totalSpans, attributes);
    }

    // Post endpoint for /getSampled that requires three header values for user, service_name, and
    // required Returns the number of times a span was sampled out of the creation of 1000 spans
    [HttpGet]
    [Route("/importantEndpoint")]
    public int ImportantEndpoint()
    {
        this.Request.Headers.TryGetValue("user", out var userAttribute);
        this.Request.Headers.TryGetValue("service_name", out var name);
        this.Request.Headers.TryGetValue("required", out var required);
        this.Request.Headers.TryGetValue("totalSpans", out var totalSpans);

        ActivityTagsCollection attributes = new ActivityTagsCollection
        {
            { "http.request.method", "GET" },
            { "url.full", "http://localhost:8080/importantEndpoint" },
            { "user", userAttribute },
            { "http.route", "/importantEndpoint" },
            { "required", required },
            { "url.path", "/importantEndpoint" },
        };

        return this.GetSampledSpanCount(name, totalSpans, attributes);
    }

    /**
   * Creates x amount of spans with x being supplied by totalSpans and returns how many of those
   * spans were sampled
   *
   * @param name name of the span that will end up being the service-name
   * @param totalSpans number of spans to make
   * @param attributes attributes to set for the span
   * @return the number of times a span was sampled
   */
    private int GetSampledSpanCount(string name, string totalSpans, ActivityTagsCollection attributes)
    {
        int numSampled = 0;
        int spans = int.Parse(totalSpans);

        for (int i = 0; i < spans; i++)
        {
            Activity.Current = null;
            using (Activity activity = Tracer.StartActivity(ActivityKind.Server, tags: attributes, name: name))
            {
                if (activity?.Recorded == true && activity?.IsAllDataRequested == true)
                {
                    numSampled++;
                }
            }
        }

        return numSampled;
    }
}
