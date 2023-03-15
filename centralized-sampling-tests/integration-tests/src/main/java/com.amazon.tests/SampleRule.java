package com.amazon.tests;

import org.json.simple.JSONObject;

/** Class used to create Sample Rules, used by SampleRules file to create sampleRules */
public class SampleRule {
  private GenericConstants.SampleRuleName name;
  private String json;
  private double expectedSampled;

  /**
   * Default constructor for a sample rule
   *
   * @param sampleRuleBuilder - sampleRule builder that has all of these components
   */
  public SampleRule(SampleRuleBuilder sampleRuleBuilder) {
    this.json = sampleRuleBuilder.json;
    this.name = sampleRuleBuilder.name;
    this.expectedSampled = sampleRuleBuilder.expectedSampled;
  }

  /**
   * @return enum name of the rule
   */
  public GenericConstants.SampleRuleName getName() {
    return this.name;
  }

  /**
   * @return String JSON string to be sent to xray to create rule
   */
  public String getJson() {
    return this.json;
  }

  /**
   * @return double expected rate of sampling for this rule
   */
  public double getExpectedSampled() {
    return this.expectedSampled;
  }

  /** Builder class for sampleRule */
  public static class SampleRuleBuilder {
    private final GenericConstants.SampleRuleName name;
    private final int priority;
    private int reservoir;
    private final double rate;
    private String serviceName;
    private String method;
    private String path;
    private JSONObject attributes;
    private final double expectedSampled;
    private String json;

    /**
     * Default constructor for SampleRuleBuilder
     *
     * @param name - name of the rule
     * @param priority priority of the rule
     * @param rate rate at which the rule should sample
     * @param expectedSampled - number expected to be sampled by this rule
     */
    public SampleRuleBuilder(
        GenericConstants.SampleRuleName name, int priority, double rate, double expectedSampled) {
      this.name = name;
      this.priority = priority;
      this.rate = rate;
      this.reservoir = 1;
      this.serviceName = "*";
      this.method = "*";
      this.path = "*";
      this.attributes = null;
      this.expectedSampled = expectedSampled;
    }

    /**
     * Set the path as something other than default
     *
     * @param path - path to filter the rule by
     * @return SampleRuleBuilder
     */
    public SampleRuleBuilder setPath(String path) {
      this.path = path;
      return this;
    }

    /**
     * Set the attributes as something other than default
     *
     * @param attributes - attributes to filter the rule by
     * @return SampleRuleBuilder
     */
    public SampleRuleBuilder setAttributes(JSONObject attributes) {
      this.attributes = attributes;
      return this;
    }

    /**
     * Set the method as something other than default
     *
     * @param method - method to filter the rule by
     * @return SampleRuleBuilder
     */
    public SampleRuleBuilder setMethod(String method) {
      this.method = method;
      return this;
    }

    /**
     * Set the reservoir as something other than default
     *
     * @param reservoir - size of the reservoir
     * @return SampleRuleBuilder
     */
    public SampleRuleBuilder setReservoir(int reservoir) {
      this.reservoir = reservoir;
      return this;
    }

    /**
     * Set the serviceName as something other than default
     *
     * @param serviceName - serviceName to filter the rule by
     * @return SampleRuleBuilder
     */
    public SampleRuleBuilder setServiceName(String serviceName) {
      this.serviceName = serviceName;
      return this;
    }

    /**
     * Build function for SampleRuleBuilder Creates the jsonBody of the sample rule and returns a
     * sampleRule
     *
     * @return SampleRule object
     */
    public SampleRule build() {
      JSONObject jsonObject = new JSONObject();
      JSONObject jsonBody = new JSONObject();
      jsonBody.put("FixedRate", rate);
      jsonBody.put("Host", "*");
      jsonBody.put("HTTPMethod", method);
      jsonBody.put("Priority", priority);
      jsonBody.put("ReservoirSize", reservoir);
      jsonBody.put("ResourceARN", "*");
      jsonBody.put("RuleName", name.getSampleName());
      jsonBody.put("ServiceName", serviceName);
      jsonBody.put("ServiceType", "*");
      jsonBody.put("URLPath", path);
      jsonBody.put("Version", 1);
      if (attributes != null) {
        jsonBody.put("Attributes", attributes);
      }
      jsonObject.put("SamplingRule", jsonBody);
      this.json = jsonObject.toString();
      return new SampleRule(this);
    }
  }
}
