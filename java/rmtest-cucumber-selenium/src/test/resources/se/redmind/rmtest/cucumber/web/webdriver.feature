Feature: WebDriver functionalities
  This file contains a lot of examples and try to cover most of the functions and syntaxes supported by the WebDriverSteps

  Background:
    # this is the only local step and will start the local web server
    Given that we know our local spark instance
    And that we navigate to "${spark}/bootstrap-tldr/"
    And that we maximize the window

  Scenario: navigation
    When we navigate to "${spark}/bootstrap-tldr/#css"
    Then the current url ends with "#css"
    And the current url !ends with "#admin"
    When we go back
    Then the current url ends with "/"
    When we go forward
    Then the current url ends with "#css"
    Then we refresh
    Then the current url ends with "#css"

  Scenario: string predicates
    Then the title reads "Bootstrap TLDR"
    And the title contains "oots"
    And the title starts with "Boot"
    And the title ends with "TLDR"
    And the current url matches "http://.+/"

  Scenario: elements by id, xpath, class, css selector, link text, partial link text, tag
    Given that the element with id "typography" reads "Typography"
    And that the element with xpath "//*[@id="typography"]" reads "Typography"
    Then the element with class "text-left" reads ".text-left"
    And the element with css "body > div.container > div > div.col-md-9 > blockquote > p" reads "Bootstrap TLDR"
    Then the attribute "href" of the element with link text "Typography" equals "${spark}/bootstrap-tldr/#typography"
    And it reads "Typography"
    Then the element with partial link text "Typo" links to "${spark}/bootstrap-tldr/#typography"
    And we select the element with tag "body"

  Scenario: passing a variable to a parameterized scenario works
    Given that we know "typography" as "someVariable"
    When we select the well known element containing "${someVariable}"
    Then it reads "Typography"

  @parameterized
  Scenario: we select the well known element containing <something>
    * we select the element with xpath "//*[@id="<something>"]"

  Scenario: alias and javascript
    # alias
    Given that we know the element with xpath "//*[@id="sidebar"]/a" as "backToTop"
    # click
    When we click on "backToTop"
    # current url
    Then the current url ends with "#top"
    # javascript
    And executing "return window.scrollY;" returns 0
    When we execute "return document.evaluate('count(//p)', document, null, XPathResult.ANY_TYPE, null).numberValue;" as "paragraphs"
    Then "${paragraphs}" equals "138"
    And evaluating "${paragraphs} + 1" returns 139
    When we evaluate "${paragraphs} + 1" as "paragraphs"
    Then "${paragraphs}" equals "139"
    Given that we know the element with id "typography" as "typographyElement"
    And "${typographyElement}_${paragraphs}" equals "Typography_139"

  Scenario: alias composition
    Given that we know "typo" as "first"
    And that we know "graphy" as "second"
    Then the element with id "${first}${second}" reads "Typography"
    Given that we know "${first}${second}" as "firstAndSecond"
    Then the element with id "${firstAndSecond}" reads "Typography"
    Given that we know the value of the element with id "${firstAndSecond}" as "typographyContent"
    Then "${typographyContent}" equals "Typography"

  Scenario: element visibility or existence
    Given that the element with id "css" is present
    And the element with id "typography" exists
    And the element with id "css" is displayed
    And the element with id "superman" !is present

  Scenario: element selection and attribute/properties assertion
    # select an element
    Given this alias:
      | type  | id                                                               | value      |
      | xpath | /html/body/div[2]/div/div[1]/div[13]/div/table/tbody/tr[3]/td[2] | successBox |
    Then the element "successBox" reads "Indicates a successful or positive action"
    When we select the "successBox"
    # assert the current element
    Then it reads "Indicates a successful or positive action"
    # css check
    And its property "background-color" equals "rgba(223, 240, 216, 1)"
    # one liners on properties and attributes
    Then the property "background-color" of the "successBox" equals "rgba(223, 240, 216, 1)"
    Then the attribute "colspan" of the "successBox" is "4"
    # selection
    When we click on the checkbox with xpath "/html/body/div[2]/div/div[1]/div[16]/form/div[4]/div/div/label/input"
    Then this element is selected
    When we hover on the element with xpath "/html/body/div[2]/div/div[1]/div[69]/button[1]"
    Then we know its attribute "aria-describedby" as "tooltip"
    And the element with id "${tooltip}" is displayed

  Scenario: count of elements
    Then the amount of elements with xpath "//p" equals 138
    Given that we count the elements with xpath "//p" as "paragraphs"
    Then "${paragraphs}" equals "138"
    And the amount of elements with class "row" equals 15
    And the amount of elements with tag "body" equals 1

  Scenario: loading aliases from an external file and inputing value in a text control
    Given the aliases defined in the file "src/test/resources/se/redmind/rmtest/cucumber/web/aliases"
    And that we know "te" as "first"
    When we input "${first}" in the element "input.form-control"
    Then its attribute "value" reads "te"
    When we click the element "input.form-control"
    And that we input "st"
    Then this element reads "test"
    When we input "_${UUID()}"
    Then its attribute "value" matches "test_[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}"
    When we clear this element
    Then this element reads ""
    When we execute "arguments[0].value='some test'" on this element
    Then this element reads "some test"

  @ignore
  Scenario: selecting a file to upload
    Given that we know the current path as "path"
    When we select the element with id "example-file-input"
    And we input "${path}/pom.xml"
    Then it ends with "pom.xml"

  Scenario: cookies
    Given that we add those cookies:
      | name          | value                 |
      | Authorization | base64(user:password) |
      | base          | something cool        |
      | sessionid     | 1lknsdf912lk12eas90   |
    When we navigate to "${spark}/cookie/valueOf/Authorization"
    Then the page content is "base64(user:password)"
    Given that we delete the cookie "Authorization"
    When we navigate to "${spark}/cookie/valueOf/Authorization"
    Then the page content is "null"
    Given that we delete all the cookies
    When we navigate to "${spark}/cookie/valueOf/sessionid"
    Then the page content is "null"

  Scenario: helper functions
    Given that we know "UUID()" as "myRandomId"
    Then "${myRandomId}" matches "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}"
    And "ID()" equals "1"
    And "${ID()}" equals "2"

  Scenario: the static counter is not reset between scenarios
    And "ID()" equals "3"

  Scenario: frame support
    Given that we switch to the frame with name "bottom"
    Then the element with id "content" reads "something"
    Given that we switch to the default content
    And that we switch to the frame with id "bottom"
    Then the element with id "content" reads "something"
    Given that we switch to the default content
    And that we know the frame with id "bottom" as "bottomFrame"
    When we switch to the frame "bottomFrame"
    Then the element with id "content" reads "something"
    Given that we switch to the default content
    And that we switch to the frame with index 0
    Then the element with id "content" reads "something"
