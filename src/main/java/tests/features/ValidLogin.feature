Feature: Login Functionality Test

  Scenario: User logs in with correct credentials
    Given User is on the login page
    When User enters correct credentials and clicks the login button
    Then User is redirected to the home page