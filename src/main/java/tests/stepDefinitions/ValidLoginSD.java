package tests.stepDefinitions;

import graphql.Assert;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class ValidLoginSD {

    WebDriver driver;
    LoginPage loginPage;
    HomePage homePage;

    @Given("User is on the login page")
    public void user_is_on_the_login_page() {
        driver = new ChromeDriver();
        loginPage.navigateToLoginPage();
    }

    @When("User enters correct credentials and clicks the login button")
    public void user_enters_correct_credentials_and_clicks_the_login_button() {
        loginPage.enterUsername("username");
        loginPage.enterPassword("password");
        homePage = loginPage.clickLoginButton();
    }

    @Then("User is redirected to the home page")
    public void user_is_redirected_to_the_home_page() {
        Assert.assertTrue(homePage.isAt());
        driver.quit();
    }
}
