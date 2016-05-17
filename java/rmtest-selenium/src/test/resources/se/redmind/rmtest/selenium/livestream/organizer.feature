Feature: A JsonReportOrganizer

  Scenario: A JsonReportOrganizer can read a json report
    Given that we parse a json report file "/jsonreports/mixedJsonReport.json"
    Then the report shows that there are 10 tests
    And 26 gherkin scenarios
    And 4 regular tests
    And 6 gherkin maps
