rmtest-cucumber-selenium
======

The example code can be seen in the **rmtest-example** module

### Basic Gherkin test
#### Java
To setup the basics for the tests to run you need a java-class in
**src/test/java/foo/bar** looking like this

    @RunWith(WebDriverRunner.class)
	@Parameterized.UseParametersRunnerFactory(ParameterizedCucumberRunnerFactory.class)
	@WebDriverRunnerOptions(parallelize=@Parallelize)
	@CucumberOptions(glue={"se.redmind.rmtest.cucumber"}, plugin="pretty")
	public class TestingTheCucumber {
		public static class Steps{
		}
	}

Explanation:

**`@RunWith(WebDriverRunner.class)`** is a jUnit annotation that takes in a runner-class so that the tests can run in a "jUnit manner".

**`@Parameterized.UseParametersRunnerFactory(ParameterizedCucumberRunnerFactory.class)`** create instances of the testclass equal to the amount of browsers/devices that you want to test on.

**`@WebDriverRunnerOptions(parallelize=@Parallelize)`** Makes the tests run in parallell.

**`@CucumberOptions(glue={"se.redmind.rmtest.cucumber"}, plugin="pretty")` glue** tells cucumber where the Java-code to handle the Gherkin tests are. With RMTest there are predefined **Given**'s, **When**'s and **Then**'s in the module **rmtest-cucumber-selenium** in the package displayed above.

#### Gherkin
Now when we have that setup the java side, it's time to write a test.

**! IMPORTANT !**
Since we have our "testclass" in the folder **src/test/java/foo/bar** cucumber will look for the .feature-file in the **src/test/resources/foo/bar** folder so thats where we put our file **test.feature**

	Feature: describe the feature we test

	  Background: setup the before the test
	    Given that we navigate to "http://OUR-WEB-APPLICATION.com"
	    And that we know the element with id "input" as "name-input"

	  Scenario: test something
		When we input "something" in the "name-input"
		Then the input "name-input" reads "something"

Instead of having all aliases like **`And that we know the element with id "input" as "name-input"`** in the feature file, its possible to have it in another file.

example of **src/test/resources/foo/bar/aliases**:

	| type  | id                      | value       |
	| id	| id-of-the-element       | name-input 	|

when we have this file it's possible to import the aliases into our feature file like this.

	Feature: describe the feature we test

	  Background: setup the before the test
	    Given that we navigate to "http://OUR-WEB-APPLICATION.com"
	    Given the aliases defined in the file "src/test/resources/foo/bar/aliases"

	  Scenario: test something
		When we input "something" in the "name-input"
		Then the input "name-input" reads "something"
