### Application interface

This Ruby sample app will generate Traces based on defined events. Metrics do not yet have a finished implementation upstream so they will be left as  coming soon. To generate traces, one of the following endpoints need to be hit. 

1. /
    - Ensures the application is running
2. /outgoing-http-call
    - Makes a HTTP request to aws.amazon.com (http://aws.amazon.com/)
3. /aws-sdk-call
    - Makes a call to AWS S3 to list buckets for the account corresponding to the provided AWS credentials
4. /outgoing-sampleapp
    - Makes a call to all other sample app ports configured at <host>:<port>/outgoing-sampleapp. If none available, makes a HTTP request to www.amazon.com (http://www.amazon.com/) 

### Requirements

* Ruby 2.7+
* Rails 7.0+

Note: This example requires Ruby 2.7+. The OpenTelemetry Ruby documentation (https://opentelemetry.io/docs/instrumentation/ruby/getting_started/#requirements) also requires Ruby 2.7+.

### Running the application

For more information on running a ruby application using manual instrumentation, please refer to the ADOT Ruby Manual Instrumentation Documentation (https://aws-otel.github.io/docs/getting-started/ruby-sdk/trace-manual-instr). In this context, the ADOT Collector is being run locally as a sidecar.

By default, in the provided configuration file, the host and port are set to 0.0.0.0:8080.

In order to run the application

- Clone the repository
`git clone https://github.com/aws-observability/aws-otel-community.git`
- Switch into the directory
`cd sample-apps/ruby-rails-sample-app`
- Install dependecies
`bundle`
- Run the rails server
`rails server`
Now the application is ran and the endpoints can be called at 0.0.0.0:8080/<one-of-4-endpoints>.

### Non conformance

Upstream Ruby SDK Metrics have a status of Not Yet Implemented. 
This ruby sample app will not implement Metrics until the status of the Ruby Metrics SDK is Stable or Alpha.
Ruby currently does not have resource detectors implemented upstream. 
Tracking issue below:
https://github.com/open-telemetry/opentelemetry-ruby-contrib/issues/34
