package com.ichbingrumpig.fungrams;

import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.Console;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * This project tries to combine the fun of Selenium's browser automation with
 * some useful automation of the Stud.IP website.
 *
 * Initially, the program asks for a username and password and logs into the Stud.IP
 * system of the University of Oldenburg (http://www.uni-oldenburg.de).
 * Once logged in, the driver navigates to the "Schwartes Brett" section.
 *
 * @author Enno Thoma, March 2018
 * @version 1.0-SNAPSHOT
 */
public class Studiptroll {
    /** The key for the command line argument to run headlessly */
    private static final String ARGUMENT_KEY_HEADLESS = "--headless";

    /** The key for the command line argument to manually change the chromedriver's executable's path */
    private static final String ARGUMENT_KEY_DRIVER_DIR = "--driver-executable";

    /** The HashMap object which the command line arguments are parsed into */
    private static Map<String, String> arguments;

    /**
     * The heart of this Fungram!
     *
     * After asking for credentials, logs into the Stud.IP system of the University of
     * Oldenburg and navigates to the "Schwarzes Brett"-section.
     * Closes and quits after arriving.
     */
    private Studiptroll() {
        // Initialize Console object from the command line
        Console console = System.console();

        // Ask user credentials
        String username = console.readLine("Username: ");
        char[] password = console.readPassword("Passwort: ");

        // Initialize path of the chromedriver executable
        initializeDriverExecutable();

        // Initialize the chromedriver options
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(initializeHeadlessMode());
        options.addArguments("--window-size=1920,1080");

        // Initialize the chrome driver
        ChromeDriver driver = new ChromeDriver(options);

        // Navigate to Website
        driver.get("http://www.uni-oldenburg.de");

        // Hover over the "studip"-button
        driver.findElement(By.id("unilogin")).click();

        // Log in
        driver.findElement(By.id("username")).sendKeys(username);
        driver.findElement(By.id("password")).sendKeys(new String(password));
        driver.findElement(By.id("unilogin_submit")).click();

        // To "Schwarzes Brett"
        driver.findElement(By.xpath("//li[@id='nav_schwarzesbrettplugin']")).click();

        // Some happy logging
        System.out.println("Erfolgreich zur Webseite gekommen :)");

        // Cleanup
        driver.close();
    }

    /**
     * Main function. Saves the command line arguments as a HashMap and
     * starts the actual program (let's get away from our static context for that)
     *
     * @param args The command line arguments passed when executing the jar
     */
    public static void main(String[] args) {
        arguments = argsToMap(args);
        new Studiptroll();
    }

    /**
     * Converts the command line arguments into a HashMap
     * @param args The command line arguments passed when executing the jar
     * @return The HashMap parsed from the arguments
     */
    private static Map<String, String> argsToMap(String[] args) {
        Map<String, String> map = new HashMap<String, String>();
        for (String s : args) {
            String[] parts = s.split("=");
            map.put(parts[0], parts[1]);
        }
        return map;
    }

    /**
     * Checks if the argument '--headless' was passed into the program and returns true, if
     * and only if the argument is 'true' (or any case-insensitive version of that)
     *
     * @return If Chrome should be started in headless mode
     */
    private boolean initializeHeadlessMode() {
        // Whether the program should run in headless mode or not
        String argumentHeadless;
        if ((argumentHeadless = arguments.get(ARGUMENT_KEY_HEADLESS)) != null) {
            return argumentHeadless.equalsIgnoreCase("true");
        }
        return false;
    }

    /**
     * Identifies (either from command line arguments or by a hardcoded path) the location
     * of the chromedriver executable and sets the appropriate system property to its path
     */
    private void initializeDriverExecutable() {
        String path;
        if (arguments.get(ARGUMENT_KEY_DRIVER_DIR) == null) {
            // standard path
            path = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "Geb" + File.separator + "studiptroll"
                    + File.separator + "drivers" + File.separator + "chromedriver-windows-32bit.exe";
        } else {
            path = arguments.get(ARGUMENT_KEY_DRIVER_DIR);
        }
        System.setProperty("webdriver.chrome.driver", path);
    }
}
