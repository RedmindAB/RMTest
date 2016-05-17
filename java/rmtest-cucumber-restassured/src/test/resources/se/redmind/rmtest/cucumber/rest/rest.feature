Feature: Test REST applications

  Background: init the URL and port
    Given that url is "http://localhost"
    And custom port is the same as webserver;

  Scenario: basic call works
    When we get "/"
    Then content is "hello"

  Scenario: send back json object
    Given we send:
      """
      {
        "foo":"bar"
      }
      """
    When we get "/json"
    Then json key "foo" is "bar"
    Then parameter "foo" is "bar"

  Scenario: send back json array
    Given we send:
      """
      [
        { "key0" : "value0" },
        { "key1" : "value1" },
        { "key2" : "value2" }
      ]
      """
    When we get "/json"
    Then index 1 has the key "key1" and value "value1"
    Then index 0 has the key "key0" and value "value0"
    Then parameter "[1].key1" is "value1"

  Scenario: send back json object with array
    Given we send:
      """
      { "array" :
        [
          { "key" : "value0", "key2" : { "number" : 1 } },
          { "key" : "value1" },
          { "key" : 2.1 }
        ]
      }
      """
    When we get "/json"
    Then parameter "array[1].key" is "value1"
    Then size of "array" is 3
    Then parameter "array[0].key2.number" is 1
    Then parameter "array[2].key" is 2.1

  Scenario: header is application/json
    When we get "/json"
    Then header "Content-Type" is "application/json"

  Scenario: set header
    Given we set header "customHeader" to "custom"
    When we get "/header/customHeader"
    Then header "customHeader" is "custom"

  Scenario: get custom status
    When we get "/status/500"
    Then status is 500

  Scenario: meassure time
    When we get "/"
    Then time is below 1000 milliseconds
    Then time is below 1 seconds
    Then time is below 1000

  Scenario: send params
    Given we send param "user" with value "me"
    And we send param "user2" with "me2"
    When we get "/param"
    Then parameter "user" is "me"
    Then parameter "user2" is "me2"

  Scenario: CRUD
    Given we send:
      """
      { "type":"postrequest" }
      """
    When we post "/db/crud"
    Then status is 200
    When we get "/db/crud"
    Then status is 200
    Then parameter "type" is "postrequest"
    Given we send:
      """
      { "type":"patched" }
      """
    When we patch "/db/crud"
    Then status is 200
    When we get "/db/crud"
    Then status is 200
    Then parameter "type" is "patched"
    Given we send:
      """
      { "newtype":"put" }
      """
    When we put "/db/crud"
    Then status is 200
    When we get "/db/crud"
    Then parameter "newtype" is "put"
    When we delete "/db/crud"
    Then status is 200

  Scenario: set cookies
    Given we set cookie "cookie1" to "cookie1value"
    When we get "/cookie/cookie1"
    Then parameter "cookie1" is "cookie1value"

  Scenario: use custom steps
    Given i set my custom json to:
    """
    {"custom":"step"}
    """
    When we get "/json"
    Then parameter "custom" is "step"

  Scenario: use custom response
    Given i set my custom json to:
    """
    {"custom":"step"}
    """
    When we get "/json"
    Then custom response "custom" is "step"

  Scenario: use custom validatable response
    Given i set my custom json to:
    """
    {"custom":"step"}
    """
    When we get "/json"
    Then custom validatable response "custom" is "step"

  Scenario: send null value
    Given we send:
    """
    { "value" : null }
    """
    When we get "/json"
    Then parameter "value" has no value

  Scenario: send empty value
    Given we send:
    """
    { "value" : "" }
    """
    When we get "/json"
    Then parameter "value" has no value
