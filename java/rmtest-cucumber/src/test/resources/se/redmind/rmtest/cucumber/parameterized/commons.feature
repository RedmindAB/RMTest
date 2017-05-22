Feature: this file contains the parameterized scenarios

  @parameterized
  Scenario: we count the letters in "<value>" and multiply it by <factor>
    this is a simple scenario made of 2 steps

    Given that we count the letters in "<value>"
    And that we multiply it by <factor>

  @parameterized
  Scenario: we count the letters in "<value>" and multiply it by <factor> twice
    this is a composite scenario made of another scenario and an extra step

    Given that we count the letters in "<value>" and multiply it by <factor>
    And that we multiply it by <factor>

  @parameterized
  Scenario: that we count the letters in "<word>"
    Given that we write down the amount of letters in "<word>"

  @parameterized
  Scenario: that we count the letters in a block of text containing "<word>"
    Given that we write down the amount of letters in:
    """
    <word>
    """
