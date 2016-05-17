@unit @tag2
Feature: we want to be able to use a parameterized scenario in another feature's background

  Background:
    When that we count the letters in "saturday" and multiply it by 2
    Then this number is 16
    When that we count the letters in "saturday" and multiply it by 2 twice

  Scenario: this is the end of the background, as a background alone would not run
    Then this number is 32
