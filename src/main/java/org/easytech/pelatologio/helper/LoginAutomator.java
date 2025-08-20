package org.easytech.pelatologio.helper;

import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.Logins;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.time.Duration;

public class LoginAutomator {
    private WebDriver driver = null;

    // Constructor to set up the driver based on the browser type in the properties file
    public LoginAutomator(boolean incognito) throws IOException {
        //System.setProperty("webdriver.chrome.driver", "C:\\web_driver\\chromedriver.exe");
        String browserType = AppSettings.loadSetting("browser");

        switch (browserType.toLowerCase()) {
            case "chrome":
                ChromeOptions chromeOptions = new ChromeOptions();
                if (incognito) {
                    chromeOptions.addArguments("--incognito");
                }
                driver = new ChromeDriver(chromeOptions);
                break;
            case "firefox":
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                if (incognito) {
                    firefoxOptions.addArguments("-private");
                }
                driver = new FirefoxDriver(firefoxOptions);
                break;
            case "edge":
                EdgeOptions edgeOptions = new EdgeOptions();
                if (incognito) {
                    edgeOptions.addArguments("inprivate");
                }
                driver = new EdgeDriver(edgeOptions);
                break;
            default:
                throw new IllegalArgumentException("Unsupported browser: " + browserType);
        }
    }

    // Method to open a URL and fill the login form
    public void openAndFillLoginForm(String url, String username, String password, By usernameLocator, By passwordLocator, By submitButtonLocator) {
        driver.get(url);

        // Εντοπισμός πεδίων username και password και εισαγωγή τιμών
            WebElement usernameField = driver.findElement(usernameLocator);
            WebElement passwordField = driver.findElement(passwordLocator);

            usernameField.sendKeys(username);
            passwordField.sendKeys(password);

        // Υποβολή φόρμας ή πάτημα κουμπιού αν χρειάζεται
        driver.findElement(submitButtonLocator).click();
    }

    public void openAndFillRegistermyPOSForm(String url, String username, String password, String phone, By usernameLocator, By passwordLocator, By phoneLocator) {
        driver.get(url);

        // Εντοπισμός πεδίων username και password και εισαγωγή τιμών
        WebElement usernameField = driver.findElement(usernameLocator);
        WebElement passwordField = driver.findElement(passwordLocator);
        WebElement phoneField = driver.findElement(phoneLocator);

        usernameField.click();
        usernameField.sendKeys(username);
        passwordField.click();
        passwordField.sendKeys(password);
        phoneField.click();
        phoneField.sendKeys(phone);
    }

    public void openAndFillLoginFormErgani(String url, String username, String password, By usernameLocator, By passwordLocator, By submitButtonLocator) throws InterruptedException {
        driver.get(url);
        // Περιμένουμε να φορτώσει πλήρως η σελίδα πριν την ανανεώσουμε
        Thread.sleep(2000); // Δώσε λίγο χρόνο για το αρχικό load
        driver.navigate().refresh(); // Ανανέωση της σελίδας
        Thread.sleep(5000); // Δώσε λίγο χρόνο μετά την ανανέωση
        // Εντοπισμός πεδίων username και password και εισαγωγή τιμών
//        WebElement usernameField = driver.findElement(usernameLocator);
//        WebElement passwordField = driver.findElement(passwordLocator);
//
//        usernameField.sendKeys(username);
//        passwordField.sendKeys(password);
//        Thread.sleep(3000);
//        // Υποβολή φόρμας ή πάτημα κουμπιού αν χρειάζεται
//        driver.findElement(submitButtonLocator).click();
    }

    public void openAndFillLoginFormDas(String url, String username, String password, By usernameLocator,By nextButtonLocator, By passwordLocator) {
        driver.get(url);

        // Εντοπισμός πεδίων username και password και εισαγωγή τιμών
        WebElement usernameField = driver.findElement(usernameLocator);
        usernameField.sendKeys(username);
        driver.findElement(nextButtonLocator).click();
        WebElement passwordField = driver.findElement(passwordLocator);
        passwordField.sendKeys(password);

    }

    public void openPage(String url) {
        driver.get(url);
    }

    // Close the driver
    public void close() {
        if (driver != null) {
            driver.quit();
        }
    }

    public void openGemi(String url, String afm) {
        driver.get(url);
        // Εντοπισμός πεδίων username και password και εισαγωγή τιμών
        WebElement searchBar = driver.findElement(By.id("AutocompleteSearchItem"));
        searchBar.sendKeys(afm);

    }

    public void openAndFillLoginFormAuthorizations(String url, String username, String password, By usernameLocator, By passwordLocator, By btnLogin) {
        driver.get(url);

        // Εντοπισμός πεδίων username και password και εισαγωγή τιμών
        WebElement usernameField = driver.findElement(usernameLocator);
        WebElement passwordField = driver.findElement(passwordLocator);

        usernameField.sendKeys(username);
        passwordField.sendKeys(password);

        // Υποβολή φόρμας ή πάτημα κουμπιού αν χρειάζεται
        driver.findElement(btnLogin).click();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        driver.get("https://www1.gsis.gr/taxisnet/mytaxisnet/protected/grantEInvoiceProviderAuthorization.htm");
        WebElement vatField = driver.findElement(By.name("authorizationRequest.granteeVatNumber"));
        vatField.sendKeys("801400290");
    }

    public void openAndFillLoginRegisterCloud(String url, String username, String password, By usernameLocator, By passwordLocator, By btnLogin, Customer customer, Logins login) {
        driver.get(url);

        // Εντοπισμός πεδίων username και password και εισαγωγή τιμών
        WebElement usernameField = driver.findElement(usernameLocator);
        WebElement passwordField = driver.findElement(passwordLocator);

        usernameField.sendKeys(username);
        passwordField.sendKeys(password);

        // Υποβολή φόρμας ή πάτημα κουμπιού αν χρειάζεται
        driver.findElement(btnLogin).click();
        driver.get("https://app.simplycloud.gr/Partners/NewAccount");
        WebElement email = driver.findElement(By.id("email"));
        email.sendKeys(login.getUsername());
        WebElement passwordField2 = driver.findElement(By.id("password"));
        passwordField2.sendKeys(login.getPassword());
        WebElement name = driver.findElement(By.id("name"));
        name.sendKeys(customer.getName());
        WebElement mobile = driver.findElement(By.id("mobile"));
        mobile.sendKeys(login.getPhone());
        WebElement VATNumber = driver.findElement(By.id("VATNumber"));
        VATNumber.sendKeys(customer.getAfm());
    }

    public void openAndFillLoginRegisterEmblem(String url, String username, String password, By usernameLocator, By passwordLocator, By btnLogin,Customer customer, Logins login) {
        driver.get(url);

        // Εντοπισμός πεδίων username και password και εισαγωγή τιμών
        WebElement usernameField = driver.findElement(usernameLocator);
        WebElement passwordField = driver.findElement(passwordLocator);

        usernameField.sendKeys(username);
        passwordField.sendKeys(password);

        // Υποβολή φόρμας ή πάτημα κουμπιού αν χρειάζεται
        driver.findElement(btnLogin).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement addCustomerButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Προσθήκη Πελάτη')]")));
        addCustomerButton.click();

        WebElement email2 = driver.findElement(By.name("userEmail"));
        email2.sendKeys(login.getUsername());
        WebElement userPassword2 = driver.findElement(By.id("userPassword"));
        userPassword2.sendKeys(login.getPassword());
        WebElement userFname = driver.findElement(By.id("userFname"));
        userFname.sendKeys(customer.getName());
        WebElement email = driver.findElement(By.id("afm"));
        email.sendKeys(customer.getAfm());
        WebElement afmSearch = driver.findElement(By.id("taxisAfmButton"));
        afmSearch.click();
        WebElement mobile = driver.findElement(By.id("phone1"));
        mobile.sendKeys(login.getPhone());
        WebElement email1 = driver.findElement(By.id("email1"));
        email1.sendKeys(login.getUsername());

    }
}
