@tag1
Feature: those scenarios shouldn't run
  # Only tests that have the tag @unit should run

  @unit @ignore
  Scenario: Failure because of ignore
    When I run, I fail

  @integration
  Scenario: Failure because of wrong tag
    When I run, I fail

  @ignore
  Scenario: should not run because of ignore should allways be ignored
    When I run, I fail

  Scenario: should not run because of no tag
    When I run, I fail

  @unit @integration @ignore
  Scenario: Failure because of all tags
    When I run, I fail
