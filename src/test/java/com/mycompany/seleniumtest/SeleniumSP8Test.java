/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.seleniumtest;

import java.util.List;
import static org.hamcrest.CoreMatchers.is;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SeleniumSP8Test {

    private static final int WAIT_MAX = 4;
    static WebDriver driver;

    @BeforeClass
    public static void setup() {
        //System.setProperty("webdriver.gecko.driver", "seleniumDrivers/geckodriver");
        System.setProperty("webdriver.chrome.driver", "seleniumDrivers/chromedriver");

        //Reset Database
        com.jayway.restassured.RestAssured.given().get("http://localhost:3000/reset");
        driver = new ChromeDriver();
        driver.get("http://localhost:3000");
    }

    @AfterClass
    public static void tearDown() {
        driver.quit();
        //Reset Database 
        com.jayway.restassured.RestAssured.given().get("http://localhost:3000/reset");
    }

    @Test
    //Verify that page is loaded and all expected data are visible
    public void test1() throws Exception {
        (new WebDriverWait(driver, WAIT_MAX)).until((ExpectedCondition<Boolean>) (WebDriver d) -> {
            WebElement e = d.findElement(By.tagName("tbody"));
            List<WebElement> rows = e.findElements(By.tagName("tr"));
            Assert.assertThat(rows.size(), is(5));
            return true;
        });
    }

    @Test
    //Write 2002 in the filter text and verify that we only see two rows
    public void test2() throws Exception {
        //No need to WAIT, since we are running test in a fixed order, we know the DOM is ready (because of the wait in test1)
        WebElement element = driver.findElement(By.id("filter"));

        element.sendKeys("2002");
        element = driver.findElement(By.tagName("tbody"));
        List<WebElement> rows = element.findElements(By.tagName("tr"));
        Assert.assertThat(rows.size(), is(2));
    }

    @Test
    //Clear the text in the filter text and verify that we have the original five rows
    public void test3() throws Exception {
        WebElement element = driver.findElement(By.id("filter"));

        // element.clear() - somehow doesn't trigger the javascript
        // so I send 4 backspaces instead
        element.sendKeys("\b\b\b\b");
        element = driver.findElement(By.tagName("tbody"));
        List<WebElement> rows = element.findElements(By.tagName("tr"));
        Assert.assertThat(rows.size(), is(5));
    }

    @Test
    //Click the sort “button” for Year, and verify that the top row contains the car with id 938 and the last row the car with id = 940.
    public void test4() throws Exception {
        WebElement element = driver.findElement(By.id("h_year"));

        element.click();
        Assert.assertThat(driver.findElements(By.xpath("//tbody/tr[1 and td ='938']")).size(), is(1));
        Assert.assertThat(driver.findElements(By.xpath("//tbody/tr[3 and td ='940']")).size(), is(1));
    }

    @Test
    //Press the edit button for the car with the id 938. Change the Description to "Cool car", and save changes.
    //Verify that the row for car with id 938 now contains "Cool car" in the Description column
    public void test5() throws Exception {
        WebElement row = driver.findElement(By.xpath("//tbody/tr[td='938']"));
        WebElement a = row.findElements(By.tagName("a")).get(0);
        a.click();  //Click the tag

        WebElement element = driver.findElement(By.id("description"));
        element.clear();
        element.sendKeys("Cool car");
        element = driver.findElement(By.id("save"));
        element.click();
        row = driver.findElement(By.xpath("//tbody/tr[td ='938']"));
        // description column is the 6th column
        Assert.assertThat(row.findElements(By.xpath("//td[6 and text()= 'Cool car']")).size(), is(1));

    }

    @Test
    //Click the new “Car Button”, and click the “Save Car” button. Verify that we have an error message with the text
    //“All fields are required” and we still only have five rows in the all cars table.
    public void test6() throws Exception {
        WebElement element = driver.findElement(By.id("new"));
        element.click();
        element = driver.findElement(By.id("save"));

        element.click();

        element = driver.findElement(By.id("submiterr"));
        Assert.assertThat(element.getText(), is("All fields are required"));

        element = driver.findElement(By.tagName("tbody"));
        List<WebElement> rows = element.findElements(By.tagName("tr"));
        Assert.assertThat(rows.size(), is(5));

    }

    @Test
    //Click the new Car Button, and add the following values for a new car
    //Year:         2008
    //Registered:     2002-5-5
    //Make:         Kia
    //Model:         Rio
    //Description:     As new
    //Price:         31000

    //Click “Save car”, and verify that the new car was added to the table with all cars .
    public void test7() throws Exception {
        WebElement element = driver.findElement(By.id("new"));
        element.click();
        element = driver.findElement(By.id("year"));
        element.sendKeys("2008");

        element = driver.findElement(By.id("registered"));
        element.sendKeys("2002-5-5");

        element = driver.findElement(By.id("make"));
        element.sendKeys("Kia");

        element = driver.findElement(By.id("model"));
        element.sendKeys("Rio");

        element = driver.findElement(By.id("description"));
        element.sendKeys("As new");

        element = driver.findElement(By.id("price"));
        element.sendKeys("31000");

        element = driver.findElement(By.id("save"));
        element.click();

        // now the car should be added, so the size of the rows should be one more, i.e. 6
        element = driver.findElement(By.tagName("tbody"));
        List<WebElement> rows = element.findElements(By.tagName("tr"));
        Assert.assertThat(rows.size(), is(6));

        // this would throw an NoSuchElementException if it could not find the element, so need to test further
        WebElement row = driver.findElement(By.xpath("//tbody/tr[td ='2008' and td = 'Kia' and td = 'Rio' and td = 'As new' ]"));
        // description column is the 6th column
        //Assert.assertThat(row.findElements(By.xpath("//td[6 and text()= 'As new']")).size(), is(1));
    }

}
