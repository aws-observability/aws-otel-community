# Centralized Sampling Integration Tests

## Run
To run the integration tests there are three requirements for the tests
to successfully run.
1. There must be the ADOT collector running with
   AWS X-Ray on port 2000, i.e. Collector running at http://localhost:2000.
2. There must be one of the sample apps configured for centralized sampling
   running on port 8080 i.e. sample app running at http://localhost:8080.
3. There must be no pre-existing sampling rules on the aws-account being used to run
   the tests.

If any of these components are missing the tests will fail and throw an IOException
immediately. For full instructions on how to run all components of the test
see the parent folders README.

### Run as a command
Run this command in the  directory `centralized-sampling-tests`
```shell
./gradlew :integration-tests:run
```
