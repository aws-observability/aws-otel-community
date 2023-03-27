package com.amazon.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * File that contains all test cases to be used to make calls to the sample app while sample rules
 * are in place
 */
public class testCases {
  public testCase[] allCases;

  /** Test case used to make calls to specific endpoints with specific headers */
  public testCases() {
    this.allCases =
        new testCase[] {
          getDefaultUser(),
          getAdminGetSampled(),
          getAdminPostSampled(),
          getAdminImportantEndpoint(),
          getTestImportantEndpoint(),
          getServiceImportantEndpoint(),
          getServiceGetSampled(),
          getServicePostSampled(),
          getMultAttributesGetSampled(),
          getMultAttributesPostSampled(),
          getMultAttributesImportantEndpoint(),
          getPostOnly(),
          getServiceNameTest(),
          getAdminServiceNameTest()
        };
  }

  /**
   * Get rules that will be applied to any test case
   *
   * @return list of ruleNames
   */
  public static List<GenericConstants.SampleRuleName> getDefaultMatches() {
    return Arrays.asList(
        GenericConstants.SampleRuleName.Default,
        GenericConstants.SampleRuleName.AcceptAll,
        GenericConstants.SampleRuleName.SampleNone,
        GenericConstants.SampleRuleName.HighReservoir,
        GenericConstants.SampleRuleName.MixedReservoir,
        GenericConstants.SampleRuleName.LowReservoir);
  }

  /**
   * adds default matches to another list of specific rule matches
   *
   * @param matches - list of specific rule matches to have defaults added onto
   * @return list of rule names that match with a testCase
   */
  public static List<GenericConstants.SampleRuleName> getMatches(List<GenericConstants.SampleRuleName> matches) {
    List<GenericConstants.SampleRuleName> defaultMatches = getDefaultMatches();
    matches.addAll(defaultMatches);
    return matches;
  }

  /**
   * Tests default user with nothing extra tested
   *
   * @return default user
   */
  public testCase getDefaultUser() {
    List<GenericConstants.SampleRuleName> matches = getDefaultMatches();
    return new testCase(
        GenericConstants.Users.Test.getUser(), "default", "false", matches, "/getSampled", "GET");
  }

  /**
   * Tests the importantEndpoint endpoint, specifically test ImportantEndpoint SampleRule
   *
   * @return testCases importantTest user
   */
  private testCase getTestImportantEndpoint() {
    List<GenericConstants.SampleRuleName> matches =
        getMatches(
            new ArrayList<>(
                Arrays.asList(
                    GenericConstants.SampleRuleName.ImportantEndpoint,
                    GenericConstants.SampleRuleName.SampleNoneAtEndpoint)));
    return new testCase(
        GenericConstants.Users.Test.getUser(),
        "importantTest",
        "false",
        matches,
        "/importantEndpoint",
        "GET");
  }

  /**
   * Tests the user attribute with admin, specifically tests ImportantAttribute SampleRule
   *
   * @return testCases admin User
   */
  private testCase getAdminGetSampled() {
    List<GenericConstants.SampleRuleName> matches =
        getMatches(
            new ArrayList<>(
                Arrays.asList(GenericConstants.SampleRuleName.ImportantAttribute)));
    return new testCase(
        GenericConstants.Users.Admin.getUser(),
        "adminGetSampled",
        "false",
        matches,
        "/getSampled",
        "GET");
  }

  /**
   * Tests a post with an admin user, Specifically tests priority with PostRule SampleRule and
   * ImportantAttribute SampleRule
   *
   * @return testCases adminPost user
   */
  private testCase getAdminPostSampled() {
    List<GenericConstants.SampleRuleName> matches =
        getMatches(
            new ArrayList<>(
                Arrays.asList(
                    GenericConstants.SampleRuleName.ImportantAttribute,
                    GenericConstants.SampleRuleName.PostRule)));
    return new testCase(
        GenericConstants.Users.Admin.getUser(),
        "adminPostSampled",
        "false",
        matches,
        "/getSampled",
        "POST");
  }

  /**
   * Tests an admin user hitting importantEndpoint, Specifically tests priority with
   * ImportantEndpoint SampleRule and ImportantAttribute SampleRule
   *
   * @return testCases adminPost user
   */
  private testCase getAdminImportantEndpoint() {
    List<GenericConstants.SampleRuleName> matches =
        getMatches(
            new ArrayList<>(
                Arrays.asList(
                    GenericConstants.SampleRuleName.ImportantAttribute,
                    GenericConstants.SampleRuleName.SampleNoneAtEndpoint,
                    GenericConstants.SampleRuleName.ImportantEndpoint)));
    return new testCase(
        GenericConstants.Users.Admin.getUser(),
        "importantAdmin",
        "false",
        matches,
        "/importantEndpoint",
        "GET");
  }

  /**
   * Tests a service user at getSampled, Specifically tests AttributeAtEndpoint SampleRule
   *
   * @return testCases serviceGetSamped user
   */
  private testCase getServiceGetSampled() {
    List<GenericConstants.SampleRuleName> matches =
        getMatches(
            new ArrayList<>(
                Arrays.asList(
                    GenericConstants.SampleRuleName.AttributeAtEndpoint)));
    return new testCase(
        GenericConstants.Users.Service.getUser(),
        "serviceGetSampled",
        "false",
        matches,
        "/getSampled",
        "GET");
  }

  /**
   * Tests a service user at getSampled using Post Method, Specifically tests priority with
   * AttributeAtEndpoint SampleRule and PostRule SampleRule
   *
   * @return testCases servicePostSampled user
   */
  private testCase getServicePostSampled() {
    List<GenericConstants.SampleRuleName> matches =
        getMatches(
            new ArrayList<>(
                Arrays.asList(
                    GenericConstants.SampleRuleName.AttributeAtEndpoint,
                    GenericConstants.SampleRuleName.PostRule)));
    return new testCase(
        GenericConstants.Users.Service.getUser(),
        "servicePostSampled",
        "false",
        matches,
        "/getSampled",
        "POST");
  }

  /**
   * Tests a service user at importantEndpoint, Specifically tests ImportantEndpoint Sample Rule and
   * makes sure AttributeatEndpoint is not giving false positives
   *
   * @return testCases serviceImportantEndpoint user
   */
  private testCase getServiceImportantEndpoint() {
    List<GenericConstants.SampleRuleName> matches =
        getMatches(
            new ArrayList<>(
                Arrays.asList(
                    GenericConstants.SampleRuleName.ImportantEndpoint,
                    GenericConstants.SampleRuleName.SampleNoneAtEndpoint)));
    return new testCase(
        GenericConstants.Users.Service.getUser(),
        "serviceImportant",
        "false",
        matches,
        "/importantEndpoint",
        "GET");
  }

  /**
   * Tests a user with admin and required=true attributes, Specifically used to test
   * MultipleAttributes Sample Rule
   *
   * @return testCases multipleAttributes user
   */
  private testCase getMultAttributesGetSampled() {
    List<GenericConstants.SampleRuleName> matches =
        getMatches(
            new ArrayList<>(
                Arrays.asList(
                    GenericConstants.SampleRuleName.MultipleAttributes,
                    GenericConstants.SampleRuleName.ImportantAttribute)));
    return new testCase(
        GenericConstants.Users.Admin.getUser(),
        "multAttributeGetSampled",
        "true",
        matches,
        "/getSampled",
        "GET");
  }

  /**
   * Tests a user with admin and required=true attributes, Specifically used to test priority with
   * MultipleAttributes Sample Rule and PostRule user
   *
   * @return testCases multipleAttributesPost User
   */
  private testCase getMultAttributesPostSampled() {
    List<GenericConstants.SampleRuleName> matches =
        getMatches(
            new ArrayList<>(
                Arrays.asList(
                    GenericConstants.SampleRuleName.MultipleAttributes,
                    GenericConstants.SampleRuleName.ImportantAttribute,
                    GenericConstants.SampleRuleName.PostRule)));
    return new testCase(
        GenericConstants.Users.Admin.getUser(),
        "multAttributePostSampled",
        "true",
        matches,
        "/getSampled",
        "POST");
  }

  /**
   * Tests a user making a post getSampled call, Specifically used to test PostRule Sample Rule
   *
   * @return testCases postSampled user
   */
  private testCase getPostOnly() {
    List<GenericConstants.SampleRuleName> matches =
        getMatches(
            new ArrayList<>(
                Arrays.asList(GenericConstants.SampleRuleName.PostRule)));
    return new testCase(
        GenericConstants.Users.Test.getUser(), "PostOnly", "false", matches, "/getSampled", "POST");
  }

  /**
   * Tests a user with admin and required=true attributes, Specifically used to test priority with
   * MultipleAttributes Sample Rule and ImportantEndpoint user
   *
   * @return testCases multipleAttributesImportantEndpoint User
   */
  private testCase getMultAttributesImportantEndpoint() {
    List<GenericConstants.SampleRuleName> matches =
        getMatches(
            new ArrayList<>(
                Arrays.asList(
                    GenericConstants.SampleRuleName.MultipleAttributes,
                    GenericConstants.SampleRuleName.ImportantAttribute,
                    GenericConstants.SampleRuleName.ImportantEndpoint,
                    GenericConstants.SampleRuleName.SampleNoneAtEndpoint)));
    return new testCase(
        GenericConstants.Users.Admin.getUser(),
        "multAttributeImportant",
        "true",
        matches,
        "/importantEndpoint",
        "GET");
  }

  /**
   * Tests a user that sets the ServiceName of its spans, Specifically used to test
   * ImportantServiceName Sample Rule
   *
   * @return testCases serviceName user
   */
  private testCase getServiceNameTest() {
    List<GenericConstants.SampleRuleName> matches =
        getMatches(
            new ArrayList<>(Arrays.asList(GenericConstants.SampleRuleName.ImportantServiceName)));
    return new testCase(
        GenericConstants.Users.Test.getUser(),
        "ImportantServiceName",
        "false",
        matches,
        "/getSampled",
        "GET");
  }

  /**
   * Tests an admin user that sets the ServiceName of its spans, Specifically used to test priority
   * with ImportantServiceName Sample Rule and ImportantAttribute Sample Rule
   *
   * @return testCases adminServiceName user
   */
  private testCase getAdminServiceNameTest() {
    List<GenericConstants.SampleRuleName> matches =
        getMatches(
            new ArrayList<>(
                Arrays.asList(
                    GenericConstants.SampleRuleName.ImportantServiceName,
                    GenericConstants.SampleRuleName.ImportantAttribute)));
    return new testCase(
        GenericConstants.Users.Admin.getUser(),
        "ImportantServiceName",
        "false",
        matches,
        "/getSampled",
        "GET");
  }

  /**
   * Get all testCases
   *
   * @return list of all testCases
   */
  public testCase[] getAllTestCases() {
    return this.allCases;
  }
}
