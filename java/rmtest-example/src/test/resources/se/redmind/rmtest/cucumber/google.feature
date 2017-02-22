Feature: This is an example of an HTML test in gherkin

  Background:
    Given that we navigate to "http://www.google.com"
    And that we know the element named "q" as "searchBox"
    And that we know the element named "btnG" as "searchButton"
    And that we know the element with id "ires" as "searchResult"

  Scenario: verify the title of the page
    Given that we send a rocket named "love" to the moon
    Then the title contains "Goo"

  Scenario: search for something using the keyboard, names and ids
    When we input "something" in the field named "q"
    And that we press enter
    Then the element with id "ires" is present

  Scenario: search for something using the mouse and aliases
    When we input "something" in the "searchBox"
    And that we click on the "searchButton"
    Then the "searchResult" is present
