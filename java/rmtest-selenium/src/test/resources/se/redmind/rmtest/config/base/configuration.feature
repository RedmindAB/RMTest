@configuration
Feature: RMTest Configuration file

  Scenario: read and validate an invalid config file
    When we read the following configuration file:
      """
      autoCloseDrivers: true
      jsonReportSavePath: /some/path/target/RMTReports
      rmReportIP: 127.0.0.1
      rmReportLivePort: 12345
      """
    And that we validate it
    Then we get a ValidationException

  Scenario: read and validate a valid local config file
    When we read the following configuration file:
      """
      drivers:
        - type: phantomjs
          description: headless
        - type: firefox
        - type: chrome
          chromedriver: /some/path/chromedriver
      android:
        home: /some/path
        toolsVersion: 4.4
      defaultTimeOut: 10
      autoCloseDrivers: true
      jsonReportSavePath: /some/path/target/RMTReports
      rmReportIP: 127.0.0.1
      rmReportLivePort: 12345
      """
    And that we validate it
    Then we get no error

  Scenario: read and validate a valid grid config file
    When we read the following configuration file:
      """
      drivers:
        - type: grid
          hubIp: 127.0.0.1
          enableLiveStream: false
      autoCloseDrivers: true
      jsonReportSavePath: /some/path/target/RMTReports
      rmReportIP: 127.0.0.1
      rmReportLivePort: 12345
      """
    And that we validate it
    Then we get no error

  Scenario: read and validate a valid legacy local config file
    When we read the following configuration file:
      """
      {
        "configuration":
        {
          "androidHome": "/some/path",
          "localIp": "127.0.0.1",
          "hubIp": "127.0.0.1",
          "AndroidBuildtoolsVersion": "4.4",
          "runOnGrid": "false",
          "usePhantomJS": "true",
          "useChrome": "true",
          "useFirefox": "false",
          "autoCloseDrivers": "true",
          "RmReportIP": "127.0.0.1",
          "RmReportLivePort" : "12345",
          "enableLiveStream": "false"
        }
      }
      """
    And that we validate it
    Then we get no error

  Scenario: read and validate a valid legacy grid config file
    When we read the following configuration file:
      """
      {
       "configuration":
       {
         "androidHome": "/Users/oskeke/Library/Android/sdk",
         "localIp": "10.12.14.82",
         "hubIp": "10.12.14.82",
         "AndroidBuildtoolsVersion": "4.4",
         "runOnGrid": "true",
         "usePhantomJS": "false",
         "useChrome": "false",
         "useFirefox": "false",
         "autoCloseDrivers": "true",
         "RmReportIP":" 127.0.0.1",
         "RmReportLivePort": "12345",
         "enableLiveStream": "false",
         "jsonReportSavePath": "~/tmp/testReport"
       }
      }
      """
    And that we validate it
    Then we get no error

  Scenario: override a configuration propery by using a system.property
    Given that the system property "drivers" is set to "- type: firefox\n- type: chrome"
    When we read the following configuration file:
      """
      drivers:
       - type: phantomjs
      """
    Given that we apply the system properties
    Then the configuration is:
      """
      drivers:
        - type: firefox
        - type: chrome
      """
