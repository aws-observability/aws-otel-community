package com.amazon.tests;

import java.io.IOException;
import java.lang.Math;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.jayway.jsonpath.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;

/**
 * File that runs all the integrations tests and creates/deletes sample rules
 */
public class CentralizedSamplingIntegrationTests {

    /**
     * Set up new httpClient
     *
     * @return OkhttpClient
     */
    @Bean
    public static Call.Factory httpClient() {
        return new OkHttpClient();
    }

    private static final Logger logger =
            LoggerFactory.getLogger(CentralizedSamplingIntegrationTests.class);

    private static SampleRules sampleRulesObj = new SampleRules();

    private static testCases testCasesObj = new testCases();

    /**
     * Main function that runs the three tests for Centralized Sampling SampleRulesTests - tests rules
     * filter and sample the correct number of targets ReservoirTests - tests that reservoir works
     * correctly with Sampling Rules PriorityTests - test Priority of sampling rules that higher
     * priority rules get sampled first
     *
     * @param args
     * @throws IOException          - throws if calls to xray backend fail
     * @throws InterruptedException - throws if tests fail
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        deleteAllRules();
        sampleRulesTests();
        reservoirTests();
        priorityTests();
        System.exit(0);
    }

    /**
     * Function to
     *
     * @param testCase   - testCase to make call with, determines headers, method, and endpoint, etc.
     * @param sampleRule - sampleRule that is currently active
     * @return true if call is successful and has expected sampling rate, false else
     */
    public static boolean makeCalls(testCase testCase, SampleRule sampleRule) throws IOException {
        RequestBody reqbody = null;
        String stringResp = "";
        if (testCase.getMethod().equals("POST")) {
            reqbody = RequestBody.create(null, new byte[0]);
        }
        String host = System.getenv("TARGET_ADDRESS");
        if (host == null) {
            host = "http://localhost:8080";
        }
        try (Response response =
                     httpClient()
                             .newCall(
                                     new Request.Builder()
                                             .addHeader(GenericConstants.USER, testCase.getUser())
                                             .addHeader(GenericConstants.SERVICE_NAME, testCase.getName())
                                             .addHeader(GenericConstants.REQUIRED, testCase.getRequired())
                                             .addHeader(
                                                     GenericConstants.TOTAL_SPANS, String.valueOf(GenericConstants.TOTAL_CALLS))
                                             .url(host + testCase.getEndpoint())
                                             .method(testCase.getMethod(), reqbody)
                                             .build())
                             .execute()) {
            stringResp = response.body().string();

        } catch (IOException e) {
            throw new IOException("Could not fetch endpoint", e);
        }
        int expectedRate = GenericConstants.DEFAULT_RATE;
        if (testCase.getMatches().contains(sampleRule.getName())) {
            expectedRate =
                    (int) Math.round(sampleRule.getExpectedSampled() * GenericConstants.TOTAL_CALLS);
        }
        double range = expectedRate * .1 + GenericConstants.DEFAULT_RANGE;
        int roundedRange = (int) Math.round(range);
        if (expectedRate == 0) {
            roundedRange = 0;
        }
        logger.info(
                "Sampled rate: "
                        + stringResp
                        + ". Expected rate: "
                        + expectedRate
                        + " for Sample Rule "
                        + sampleRule.getName().getSampleName()
                        + " and test case "
                        + testCase.getName());
        if (Integer.parseInt(stringResp) > expectedRate + roundedRange
                || Integer.parseInt(stringResp) < expectedRate - roundedRange) {
            logger.warn("Sampled rate does not match expected rate");
            return false;
        }
        return true;
    }

    /**
     * Function to call xray backend to create a sampleRule
     *
     * @param jsonBody - JSONBody of the sampleRule
     * @param ruleName - name of the Rule
     * @throws IOException - throws if unable to connect to xray backend
     */
    public static void makeRule(String jsonBody, String ruleName) throws IOException {
        // Default rule exists in x-ray by default hence the name
        if (ruleName.equals("Default")) {
            return;
        }
        String host = System.getenv("XRAY_ENDPOINT");
        if (host == null) {
            host = "http://localhost:2000";
        }

        logger.info("Creating " + ruleName + " sample rule");

        MediaType json = MediaType.get("application/json; charset=utf-8");
        RequestBody reqbody = RequestBody.create(json, jsonBody);
        try (Response response =
                     httpClient()
                             .newCall(
                                     new Request.Builder()
                                             .url(host + "/CreateSamplingRule")
                                             .method("POST", reqbody)
                                             .build())
                             .execute()) {
        } catch (IOException e) {
            throw new IOException("Could not fetch endpoint", e);
        }
    }

    /**
     * Function to call xray backend to delete a sampleRule
     *
     * @param ruleName - name of the Rule
     * @throws IOException - throws if unable to connect to xray backend
     */
    public static void deleteRule(String ruleName) throws IOException {
        String host = System.getenv("XRAY_ENDPOINT");
        if (host == null) {
            host = "http://localhost:2000";
        }
        logger.info("Deleting " + ruleName + " sample rule");

        MediaType json = MediaType.get("application/json; charset=utf-8");
        String jsonBody = "{\n" + "   \"RuleName\": \"" + ruleName + "\"\n" + "}\n";
        RequestBody reqbody = RequestBody.create(json, jsonBody);
        try (Response response =
                     httpClient()
                             .newCall(
                                     new Request.Builder()
                                             .url(host + "/DeleteSamplingRule")
                                             .method("POST", reqbody)
                                             .build())
                             .execute()) {
        } catch (IOException e) {
            throw new IOException("Could not fetch endpoint", e);
        }
    }

    /**
     * Function that deletes all sample rules before the tests start
     * @throws IOException
     */
    public static void deleteAllRules() throws IOException {
        String host = System.getenv("XRAY_ENDPOINT");
        if (host == null) {
            host = "http://localhost:2000";
        }
        logger.info("Deleting all sample rules");

        RequestBody reqbody = RequestBody.create(null, new byte[0]);
        try (Response response =
                     httpClient()
                             .newCall(
                                     new Request.Builder()
                                             .url(host + "/GetSamplingRules")
                                             .method("POST", reqbody)
                                             .build())
                             .execute()) {
            String rules = response.body().string();

            List<String> ruleNames = JsonPath.read(rules, "$..RuleName");
            for (String ruleName : ruleNames) {
                deleteRule(ruleName);
            }
        } catch (IOException e) {
            throw new IOException("Could not fetch endpoint", e);
        }
    }

    /**
     * Function to run tests on Reservoir SampleRules. Creates one rule at a time, waits 20s for
     * reservoir to be set runs all test cases, then deletes the rule. Does this for all reservoir
     * sample rules
     *
     * @throws IOException          throws if unable to connect to xray backend
     * @throws InterruptedException if a test fails after retries
     */
    public static void reservoirTests() throws IOException, InterruptedException {
        SampleRule[] sampleRules = sampleRulesObj.getReservoirRules();
        for (SampleRule sampleRule : sampleRules) {
            try {
                makeRule(sampleRule.getJson(), sampleRule.getName().getSampleName());
            } catch (IOException exception) {
                logger.error("Could not fetch endpoint, XRay backend might not be running");
                throw new IOException();
            }
            boolean passed = false;
            for (int j = 0; j < GenericConstants.MAX_RETRIES; j++) {
                try {
                    passed = makeCalls(testCasesObj.getDefaultUser(), sampleRule);
                } catch (Exception e) {
                    logger.error("Could not fetch endpoint, sample app might not be started");
                } finally {
                    if (passed) {
                        break;
                    } else if (j < GenericConstants.MAX_RETRIES - 1) {
                        logger.warn("Test failed, attempting retry");
                        TimeUnit.SECONDS.sleep(GenericConstants.WAIT_FOR_RESERVOIR);
                    } else {
                        logger.error(
                                "Test failed for Sample rule: "
                                        + sampleRule.getName()
                                        + " and test case "
                                        + testCasesObj.getDefaultUser().getName());
                        deleteRule(sampleRule.getName().getSampleName());
                        throw new InterruptedException();
                    }
                }
            }
            deleteRule(sampleRule.getName().getSampleName());
        }
    }

    /**
     * Function to run tests on Priority SampleRules, creates all sample rules then makes calls and
     * verifies the priority rule for the testCase is the expected Sample result, then deletes all
     * rules
     *
     * @throws IOException          if unable to connect to xray backend
     * @throws InterruptedException if tests fail after retries
     */
    public static void priorityTests() throws IOException, InterruptedException {
        testCase[] allTestCases = testCasesObj.getAllTestCases();
        SampleRule[] sampleRules = sampleRulesObj.getPriorityRules();
        for (SampleRule sampleRule : sampleRules) {
            try {
                makeRule(sampleRule.getJson(), sampleRule.getName().getSampleName());
            } catch (IOException exception) {
                logger.error("Could not fetch endpoint, XRay backend might not be running");
                throw new IOException();
            }
        }
        TimeUnit.SECONDS.sleep(GenericConstants.RETRY_WAIT);
        for (testCase allTestCase : allTestCases) {
            int priority = sampleRules.length - 1;
            for (int j = 0; j < sampleRules.length; j++) {
                if (allTestCase.getMatches().contains(sampleRules[j].getName())
                        && priority == sampleRules.length - 1) {
                    priority = j;
                }
            }
            boolean passed = false;
            for (int k = 0; k < GenericConstants.MAX_RETRIES; k++) {
                try {
                    passed = makeCalls(allTestCase, sampleRules[priority]);
                } catch (Exception e) {
                    logger.error("Could not fetch endpoint, sample app might not be started");
                } finally {
                    if (passed) {
                        break;
                    } else if (k < GenericConstants.MAX_RETRIES - 1) {
                        logger.warn("Test failed, attempting retry");
                        TimeUnit.SECONDS.sleep(GenericConstants.RETRY_WAIT);
                    }
                }
            }

            if (!passed) {
                logger.error(
                        "Test failed for Sample rule: "
                                + sampleRules[priority].getName().getSampleName()
                                + " and test case "
                                + allTestCase.getName());
                for (SampleRule sampleRule : sampleRules) {
                    deleteRule(sampleRule.getName().getSampleName());
                }
                throw new InterruptedException();
            } else {
                logger.info(
                        "Test passed for Sample rule: "
                                + sampleRules[priority].getName().getSampleName()
                                + " and test case "
                                + allTestCase.getName());
            }
        }
        for (SampleRule sampleRule : sampleRules) {
            deleteRule(sampleRule.getName().getSampleName());
        }
    }

    /**
     * Runs tests for each sample Rule individually. Creates a sample rule, waits 1s for rule to be
     * applied, verifies the expected sampling rate matches the expected rate for the testCase, then
     * deletes the rule. Repeats this for all sample rules
     *
     * @throws IOException          if unable to connect to xray backend
     * @throws InterruptedException if tests fail
     */
    public static void sampleRulesTests() throws IOException, InterruptedException {
        SampleRule[] sampleRules = sampleRulesObj.getSampleRules();
        testCase[] allTestCases = testCasesObj.getAllTestCases();

        for (SampleRule sampleRule : sampleRules) {
            try {
                makeRule(sampleRule.getJson(), sampleRule.getName().getSampleName());
            } catch (IOException exception) {
                logger.error("Could not fetch endpoint, XRay backend might not be running");
                throw new IOException();
            }
            TimeUnit.SECONDS.sleep(GenericConstants.RETRY_WAIT);
            for (testCase allTestCase : allTestCases) {
                boolean passed = false;
                for (int k = 0; k < GenericConstants.MAX_RETRIES; k++) {
                    try {
                        passed = makeCalls(allTestCase, sampleRule);
                    } catch (Exception e) {
                        logger.error("Could not fetch endpoint, sample app might not be started");
                    } finally {
                        if (passed) {
                            break;
                        } else if (k < GenericConstants.MAX_RETRIES - 1) {
                            logger.warn("Test failed here, attempting retry");
                            TimeUnit.SECONDS.sleep(GenericConstants.RETRY_WAIT);
                        }
                    }
                }
                if (!passed) {
                    logger.error(
                            "Test failed for Sample rule: "
                                    + sampleRule.getName().getSampleName()
                                    + " and test case "
                                    + allTestCase.getName());
                    deleteRule(sampleRule.getName().getSampleName());
                    throw new InterruptedException();
                } else {
                    logger.info(
                            "Test passed for Sample rule: "
                                    + sampleRule.getName().getSampleName()
                                    + " and test case "
                                    + allTestCase.getName());
                }
            }
            deleteRule(sampleRule.getName().getSampleName());
        }
    }
}
