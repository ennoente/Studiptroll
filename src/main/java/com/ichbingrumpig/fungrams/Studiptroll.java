package com.ichbingrumpig.fungrams;

import com.mashape.unirest.http.Headers;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.http.cookie.Cookie;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * This project tries to combine the fun of Selenium's browser automation with
 * some useful automation of the Stud.IP website.
 *
 * Initially, the program asks for a username and password and logs into the Stud.IP
 * system of the University of Oldenburg (http://www.uni-oldenburg.de).
 * Once logged in, the driver navigates to the "Schwarzes Brett" section.
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
     * Oldenburg.
     * Once arrived, a time is asked (
     * Closes and quits after arriving.
     */
    private Studiptroll() {
        String username = "error";
        char[] password = null;

        // Initialize Console object from the command line
        Console console = System.console();

        if (console == null) {
            System.out.println("Retrieving user data from configs file");
            File configsFile = new File(System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "configs.properties");

            try {
                Properties props = new Properties();
                props.load(new FileInputStream(configsFile));
                username = (String) props.get("username");
                password = props.get("password").toString().toCharArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Ask user credentials
            username = console.readLine("Username: ");
            password = console.readPassword("Passwort: ");
        }

        String httpRequest = "POST /plugins.php/uollayoutplugin/login?cancel_login=1 HTTP/1.1" + "\r\n" +
                "Host: elearning.uni-oldenburg.de" + "\r\n" +
                "Content-Type: application/x-www-form-urlencoded" + "\r\n" + "\r\n" +
                "username=" + username + "&password=" + new String(password);

        String url = "https://elearning.uni-oldenburg.de/plugins.php/uollayoutplugin/login?cancel_login=1";

        String bodyString = "username=" + username + "&password=" + new String(password);

        try {


            HttpResponse<String> response = Unirest.post(url)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .body(bodyString)
                    .asString();

            Headers headers = response.getHeaders();
            System.out.println("Location-Header:" + headers.get("Location"));


            List<String> cookieHeaders = headers.get("Set-Cookie");
            for (String s : cookieHeaders) {
                System.out.println("Cookie-Header:" + s);
            }

            System.out.println("Response-Body: '" + response.getBody() + "'");

            System.out.println("__________");
            System.out.println("Sending response now...");


            String vLocation = headers.getFirst("Location");

            List<HttpCookie> cookie = HttpCookie.parse(cookieHeaders.get(0));
            String name = cookie.get(0).getName();
            String value = cookie.get(0).getValue();
            String cookiesString = name + "=" + value;
            for (int i = 1; i < cookieHeaders.size(); i++) {
                cookie = HttpCookie.parse(cookieHeaders.get(i));
                name = cookie.get(0).getName();
                value = cookie.get(0).getValue();

                cookiesString = cookiesString.concat("; " + name + "=" + value);
            }
            System.out.println("Request Cookies-Value-String: '" + cookiesString + "'");

            HttpResponse<String> response2 = Unirest.post(vLocation)
                    .header("Cookie", cookiesString)
                    .asString();

            System.out.println("Response2-Headers: '" + response2.getHeaders().toString() + "'");
            System.out.println("Response2-Body: '" + response2.getBody() + "'");

            Headers headers2 = response2.getHeaders();
            String vLocationAfterResponse2 = headers2.getFirst("Location");
            System.out.println("Response2-Location: '" + vLocationAfterResponse2 + "'");
            System.out.println("Response2-SetCookie-Headers: '" + headers2.get("Set-Cookie") + "'");

            System.out.println("______________________");
            System.out.println("Sending response now...");

            String url3 = "https://elearning.uni-oldenburg.de/dispatch.php/start";
            HttpResponse<String> response3 = Unirest.get(url3)
                    .header("Cookie", cookiesString)
                    .asString();

            System.out.println(response3.getHeaders());
            //System.out.println("Response3-Body: '" + response3.getBody() + "'");


            System.out.println("______________________________");
            System.out.println("Going to 'Planer' now...");

            HttpResponse<String> planerResponse = Unirest.get("https://elearning.uni-oldenburg.de/plugins.php/planerplugin/planer")
                    .asString();

            System.out.println("Planer-Response-Body: '" + planerResponse.getBody() + "'");




        } catch (UnirestException e) {
            e.printStackTrace();
        }
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
        Map<String, String> map = new HashMap<>();
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
