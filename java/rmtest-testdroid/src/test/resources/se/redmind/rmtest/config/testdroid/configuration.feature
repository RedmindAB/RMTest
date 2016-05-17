@configuration
Feature: RMTest Configuration file

  Scenario: read and validate a valid testDroid config file
    When we read the following configuration file:
      """
      drivers:
        - type: testdroid
          username: test-user
          password: test-password
          capabilities:
            platformName: Android
            testdroid_target: Android
            deviceName: Android Device
            testdroid_project: LocalAppium
            testdroid_testrun: Android Run 1
      """
    And that we validate it
    Then we get no error
