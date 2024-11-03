package org.easytech.pelatologio;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class WebPageOpener {

    private WebDriver driver;

    public WebPageOpener() {
        // Ρύθμιση του ChromeDriver. Πρέπει να ορίσεις τη διαδρομή προς το chromedriver
        System.setProperty("webdriver.chrome.driver", "path/to/chromedriver");
        this.driver = new ChromeDriver();
    }

    /**
     * Άνοιγμα της ιστοσελίδας και αυτόματη συμπλήρωση στοιχείων.
     * @param url Το URL της ιστοσελίδας.
     * @param username Το username για το login.
     * @param password Το password για το login.
     */
    public void openAndFill(String url, String username, String password) {
        try {
            // Άνοιγμα της σελίδας
            driver.get(url);

            // Εντοπισμός και συμπλήρωση των πεδίων username και password
            WebElement usernameField = driver.findElement(By.id("username"));  // Χρησιμοποίησε το id ή άλλο locator
            WebElement passwordField = driver.findElement(By.id("password"));

            usernameField.sendKeys(username);
            passwordField.sendKeys(password);

            // Εντοπισμός και πάτημα στο κουμπί "login"
            WebElement loginButton = driver.findElement(By.id("loginButton"));
            loginButton.click();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Κλείσιμο του προγράμματος περιήγησης.
     */
    public void close() {
        if (driver != null) {
            driver.quit();
        }
    }
}
