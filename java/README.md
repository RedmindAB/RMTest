RMTest
======

RMTest java is a test automation tool box created by RedmindAB.

It bundles a set of tools in a maven multi modules project so that each module can be used on its own or together with others.

#### How to use it

You can either clone the sources:

    git clone https://github.com/RedmindAB/RMTest.git

or directly add one of the modules as a dependency to your test project (all the jars and sources are available on maven central):

    <dependency>
        <groupId>se.redmind</groupId>
        <artifactId>rmtest-selenium</artifactId>
        <version>2.0.1</version>
        <scope>compile</scope>
    </dependency>

(Each module contains its own README file)

#### Current modules

| module name                   |  description                                                              |
| ----------------------------- | ------------------------------------------------------------------------- |
| rmtest-logback                | dependency bundle for logback                                             |
| rmtest-common                 | common utils and dependencies                                             |
| rmtest-cucumber               | improved cucumber runner supporting parameterized tests and scenarios     |
| rmtest-selenium               | parameterized test framework supporting local and remote selenium tests   |
| rmtest-cucumber-selenium      | cucumber webdriver steps based on rmtest-selenium                         |
| rmtest-cucumber-restassured   | cucumber rest steps based on restassured                                  |
| rmtest-cucumber-standalone    | a CLI oriented shaded jar including the webdriver and rest steps          |
| rmtest-appium                 | Appium support for rmtest-selenium                                        |
| rmtest-testdroid              | TestDroid support for rmtest-appium                                       |
| rmtest-example                | some basic examples                                                       |
