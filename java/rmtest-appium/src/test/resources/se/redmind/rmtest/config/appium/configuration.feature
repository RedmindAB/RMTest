@configuration
Feature: RMTest Configuration file

  Scenario: read and validate a valid appium grid config file
    When we read the following configuration file:
      """
      drivers:
        - type: appium-grid
          hubIp: the host name or the ip of the selenium grid server
      """
    And that we validate it
    Then we get no error
