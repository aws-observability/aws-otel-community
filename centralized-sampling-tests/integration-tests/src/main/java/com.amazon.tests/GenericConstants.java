package com.amazon.tests;

import java.util.Arrays;

/** Constant global variables used in tests */
public class GenericConstants {
  public static final int MAX_RETRIES = 4;
  public static final int WAIT_FOR_RESERVOIR = 20;
  public static final int TOTAL_CALLS = 1000;
  public static final int DEFAULT_RATE = (int) (.05 * TOTAL_CALLS) + 1;
  public static final int DEFAULT_RANGE = 10;
  public static final int RETRY_WAIT = 1;
  public static final String USER = "user";
  public static final String SERVICE_NAME = "service_name";
  public static final String REQUIRED = "required";
  public static final String TOTAL_SPANS = "totalSpans";

  /** Possible values for User attribute used in tests */
  public enum Users {
    Admin("admin"),
    Test("test"),
    Service("service");
    private final String user;

    /**
     * Set the user
     *
     * @param user String user
     */
    Users(String user) {
      this.user = user;
    }

    /**
     * Return the user as a string
     *
     * @return user
     */
    String getUser() {
      return user;
    }
  }

  /** Enum to represent all possible SampleRule Names */
  public enum SampleRuleName {
    Default("Default"),
    AcceptAll("AcceptAll"),
    SampleNone("SampleNone"),
    HighReservoir("HighReservoir"),
    MixedReservoir("MixedReservoir"),
    LowReservoir("LowReservoir"),
    ImportantEndpoint("ImportantEndpoint"),
    SampleNoneAtEndpoint("SampleNoneAtEndpoint"),
    ImportantAttribute("ImportantAttribute"),
    PostRule("PostRule"),
    AttributeAtEndpoint("AttributeAtEndpoint"),
    ImportantServiceName("ImportantServiceName"),
    MultipleAttributes("MultipleAttributes");

    private final String name;

    /**
     * Set the rule name
     *
     * @param name String name
     */
    SampleRuleName(String name) {
      this.name = name;
    }

    /**
     * Return the name as a string
     *
     * @return name
     */
    String getSampleName() {
      return name;
    }
  }
}
