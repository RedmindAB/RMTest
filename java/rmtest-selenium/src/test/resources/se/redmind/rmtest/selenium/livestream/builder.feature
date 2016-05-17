Feature: Test result builder functionality

  Scenario: System properties are set and received properly
    Given that we apply the system property "test.property.name" to "test.property.value"
    And build a json object containing the system properties
    And converts it to a list
    Then the list should be the same size as system properties
    And the custom property should be the same as "test.property.value"

  Scenario: Add tests with OK names and different outcomes
    Given that we have a list of tests with display names:
      | "testGoogle1[OSX_UNKNOWN_AnApple_chrome_UNKNOWN](se.redmind.rmtest.selenium.example.GoogleExample)" |
      | "testGoogle2[OSX_UNKNOWN_AnApple_chrome_UNKNOWN](se.redmind.rmtest.selenium.example.GoogleExample)" |
      | "testGoogle3[OSX_UNKNOWN_AnApple_chrome_UNKNOWN](se.redmind.rmtest.selenium.example.GoogleExample)" |
    Given we add tests to result builder
    And add test 1 as "finished"
    And add test 2 as "failure"
    And add test 3 as "ignored"
    And build the JsonObject
    Then the total number of tests should be 3
    And the 1st test should be "passed"
    And the 2nd test should be "failure"
    And the 3rd test should be "skipped"

  Scenario: Assumption fail test
    Given that we have a list of tests with display names:
      | "testGoogle1[OSX_UNKNOWN_AnApple_chrome_UNKNOWN](se.redmind.rmtest.selenium.example.GoogleExample)" |
    Given we add tests to result builder
    And add test 1 as "assumptionfailure"
    And build the JsonObject
    And the 1st test should be "skipped"

  Scenario: Verify that the test is gherkin
    Given we create a Description that is a Scenario
    Then the test is a gherkin
    Given we create a Description that is not a Scenario
    Then the test is not a gherkin

