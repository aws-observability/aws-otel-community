package com.amazon.tests;

import org.json.simple.JSONObject;

/** File that contains all sample Rules that will be created to be used for testing */
public class SampleRules {
  private final SampleRule[] sampleRules;
  private final SampleRule[] priorityRules;
  private final SampleRule[] reservoirRules;

  public SampleRules() {
    this.sampleRules =
        new SampleRule[] {
          getSampleNone(),
          getAcceptAll(),
          getImportantRule(),
          getImportantAttribute(),
          getAttributeatEndpoint(),
          getlowReservoirHighRate(),
          getMethodRule(),
          getMultipleAttribute(),
          getDefaultRule(),
          getServiceNameRule(),
          getSampleNoneAtEndpoint()
        };
    this.priorityRules =
        new SampleRule[] {
          getImportantRule(),
          getImportantAttribute(),
          getAttributeatEndpoint(),
          getMethodRule()
        };

    this.reservoirRules = new SampleRule[] {getHighReservoirLowRate(), getMixedReservoir()};
  }
  /**
   * Sample rule that samples all targets
   *
   * @return AcceptAll SampleRule
   */
  private SampleRule getAcceptAll() {
    return new SampleRule.SampleRuleBuilder(GenericConstants.SampleRuleName.AcceptAll, 1000, 1, 1)
        .build();
  }

  /**
   * Sample rule that samples no targets
   *
   * @return SampleNone SampleRule
   */
  private SampleRule getSampleNone() {
    return new SampleRule.SampleRuleBuilder(
            GenericConstants.SampleRuleName.SampleNone, 1000, 0.0, 0.0)
        .setReservoir(0)
        .build();
  }

  /**
   * Sample rule that samples no targets at a specific endpoint
   *
   * @return SampleNoneAtEndpoint SampleRule
   */
  private SampleRule getSampleNoneAtEndpoint() {
    return new SampleRule.SampleRuleBuilder(
            GenericConstants.SampleRuleName.SampleNoneAtEndpoint, 1000, 0.0, 0.0)
        .setReservoir(0)
        .setPath("/importantEndpoint")
        .build();
  }

  /**
   * Sample rule that samples Post method targets at a rate of .1
   *
   * @return PostRule SampleRule
   */
  private SampleRule getMethodRule() {
    return new SampleRule.SampleRuleBuilder(GenericConstants.SampleRuleName.PostRule, 10, .1, .11)
        .setMethod("POST")
        .build();
  }

  /**
   * Sample rule that samples all targets at a specific endpoint
   *
   * @return ImportantEndpoint SampleRule
   */
  private SampleRule getImportantRule() {
    return new SampleRule.SampleRuleBuilder(
            GenericConstants.SampleRuleName.ImportantEndpoint, 1, 1.0, 1)
        .setPath("/importantEndpoint")
        .build();
  }

  /**
   * Sample rule that samples targets with certain attributes at a specific endpoint at a rate of .5
   *
   * @return AttributeAtEndpoint SampleRule
   */
  private SampleRule getAttributeatEndpoint() {
    JSONObject attributes = new JSONObject();
    attributes.put(GenericConstants.USER, GenericConstants.Users.Service.getUser());
    return new SampleRule.SampleRuleBuilder(
            GenericConstants.SampleRuleName.AttributeAtEndpoint, 8, 0.5, .51)
        .setPath("/getSampled")
        .setAttributes(attributes)
        .build();
  }

  /**
   * Sample rule that samples all targets with no reservoir at a rate of .8
   *
   * @return LowReservoir SampleRule
   */
  private SampleRule getlowReservoirHighRate() {
    return new SampleRule.SampleRuleBuilder(
            GenericConstants.SampleRuleName.LowReservoir, 10, .8, .80)
        .setReservoir(0)
        .build();
  }

  /**
   * Sample rule that samples 500 targets and the rest at a rate of 0
   *
   * @return HighReservoir SampleRule
   */
  private SampleRule getHighReservoirLowRate() {
    return new SampleRule.SampleRuleBuilder(
            GenericConstants.SampleRuleName.HighReservoir, 2000, 0.0, .50)
        .setReservoir(500)
        .build();
  }

  /**
   * Sample rule that samples 500 targets and the rest at a rate of .5
   *
   * @return HighReservoir SampleRule
   */
  private SampleRule getMixedReservoir() {
    return new SampleRule.SampleRuleBuilder(
            GenericConstants.SampleRuleName.MixedReservoir, 2000, .5, .75)
        .setReservoir(500)
        .build();
  }

  /**
   * Sample rule that samples targets that have important attribute at a rate of .5
   *
   * @return ImportantAttribute SampleRule
   */
  private SampleRule getImportantAttribute() {
    JSONObject attributes = new JSONObject();
    attributes.put(GenericConstants.USER, GenericConstants.Users.Admin.getUser());
    return new SampleRule.SampleRuleBuilder(
            GenericConstants.SampleRuleName.ImportantAttribute, 2, .5, .5)
        .setAttributes(attributes)
        .build();
  }

  /**
   * Sample rule that samples targets that have multiple attributes of importance at a rate of .4
   *
   * @return MultipleAttributes SampleRule
   */
  private SampleRule getMultipleAttribute() {
    JSONObject attributes = new JSONObject();
    attributes.put(GenericConstants.USER, GenericConstants.Users.Admin.getUser());
    attributes.put(GenericConstants.REQUIRED, "true");
    return new SampleRule.SampleRuleBuilder(
            GenericConstants.SampleRuleName.MultipleAttributes, 9, .4, .41)
        .setAttributes(attributes)
        .build();
  }

  /**
   * Default sample Rule that exists in Xray backend by default
   *
   * @return Default SampleRule
   */
  private SampleRule getDefaultRule() {
    return new SampleRule.SampleRuleBuilder(
            GenericConstants.SampleRuleName.Default, 10000, .06, .06)
        .build();
  }

  /**
   * Sample rule that samples targets that have an important service name at a rate of .4
   *
   * @return ImportantServiceName SampleRule
   */
  private SampleRule getServiceNameRule() {
    return new SampleRule.SampleRuleBuilder(
            GenericConstants.SampleRuleName.ImportantServiceName, 3, 1, 1)
        .setServiceName("adot-integ-test")
        .build();
  }

  /**
   * get all sample rules to test individually except for reservoir rules
   *
   * @return list of SampleRules
   */
  public SampleRule[] getSampleRules() {
    return this.sampleRules;
  }

  /**
   * get a list of rules that tests priority
   *
   * @return list of SampleRules
   */
  public SampleRule[] getPriorityRules() {
    return this.priorityRules;
  }

  /**
   * get a list of rules that tests reservoirs
   *
   * @return list of SampleRules
   */
  public SampleRule[] getReservoirRules() {
    return this.reservoirRules;
  }
}
