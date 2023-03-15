package com.amazon.tests;

import java.util.List;

public class testCase {
  private final List<GenericConstants.SampleRuleName> matches;
  private final String name;
  private final String user;
  private final String required;
  private final String method;
  private final String endpoint;

  /**
   * Test case used to make calls to specific endpoints with specific headers
   *
   * @param user - type of user - ex - service, admin, test
   * @param name - name that should be assigned to the spans service name
   * @param required - header that is either "true" or "false" used to check attributes
   * @param matches - Sample rules that should trigger on this test case. Names are derived from
   *     SampleRules.java
   * @param endpoint - endpoint to hit, either /getSampled or /importantEndpoint
   * @param method - Method used to make the call, either "POST" or "GET"
   */
  public testCase(
      String user,
      String name,
      String required,
      List<GenericConstants.SampleRuleName> matches,
      String endpoint,
      String method) {
    this.matches = matches;
    this.name = name;
    this.user = user;
    this.required = required;
    this.endpoint = endpoint;
    this.method = method;
  }

  /**
   * getter for name
   *
   * @return String name
   */
  public String getName() {
    return this.name;
  }

  /**
   * getter for user
   *
   * @return String user
   */
  public String getUser() {
    return this.user;
  }

  /**
   * getter for required
   *
   * @return String required
   */
  public String getRequired() {
    return this.required;
  }

  /**
   * getter for method
   *
   * @return String method
   */
  public String getMethod() {
    return this.method;
  }

  /**
   * getter for endpoint
   *
   * @return String endpoint
   */
  public String getEndpoint() {
    return this.endpoint;
  }

  /**
   * getter for matches
   *
   * @return List<String> matches
   */
  public List<GenericConstants.SampleRuleName> getMatches() {
    return this.matches;
  }
}
