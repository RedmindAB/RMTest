package se.redmind.rmtest.cucumber.parameterized;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;

/**
 * @author Jeremy Comte
 */
public class Steps {

    private int count;

    @Then("^this number is (\\d+)$")
    public void this_number_is(int count) {
        Assert.assertEquals(count, this.count);
    }

    @Then("^that we write down the amount of letters in \"([^\"]*)\"$")
    public void we_write_down_the_amount_of_letters_in_value(String value) {
        count = value.length();
    }

    @Then("^that we write down the amount of letters in:$")
    public void we_write_down_the_amount_of_letters_in_block_value(String value) {
        count = value.length();
    }

    @Then("^that we multiply it by (\\d+)$")
    public void i_multiply_it_by(int factor) {
        count *= factor;
    }

    @When("^I run, I fail$")
    public void i_run_I_fail() {
        Assert.fail();
    }
}
