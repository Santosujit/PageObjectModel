package com.qa.opencart.factory;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * 
 * @author naveenautomationlabs
 *
 */
public class DriverFactory {
	private static final Logger LOGGER = Logger.getLogger(String.valueOf(DriverFactory.class));//what does it mean?

	WebDriver driver;
	Properties prop;//using prop you can access config.properties
	public static String highlight;//flash() method takes a WebElement and highlights it
	OptionsManager optionsManager;//accessing OptionsManager class, chromeoptions, firefoxoptions

	public static ThreadLocal<WebDriver> tlDriver = new ThreadLocal<>();
	
//	ThreadLocal is class coming from Java 1.8
//	You have to create object of ThreadLocal
//	It has two methods set(), and get()
	
//	you have to create an object of ThreadLocal having WebDriver as generic
//	so one thrreadlocal copy of the webdribver is given to each thread
//	there would be no dead-lock, no driver will be locked by any thread
//	In sequential mode of execution login page driver is passed to accounts page class, so no need of threadlocal concept
//	But in parallel mode of execution ThreadLocal concept is needed  or else multiple threads will use the same driver
//	and the driver will be locked giving rise to dead lock

//	ThrreadLocal has two methods, set and get
	/**
	 * This method is used to initialize the webdriver on the basis of given browser
	 * name..
	 * 
	 * @param browserName
	 * @return
	 */
	
	//I will give you the browser name, below method initilizes the browser
	
	public WebDriver init_driver(String browserName, String browserVersion) {
		

		// String browserName = prop.getProperty("browser");
		LOGGER.info("browser name is: " + browserName);

		highlight = prop.getProperty("highlight").trim();
		optionsManager = new OptionsManager(prop);//prop object stores all info from config.properties

		if (browserName.equals("chrome")) {

			LOGGER.info("setup chrome browser");
			WebDriverManager.chromedriver().setup();
			if (Boolean.parseBoolean(prop.getProperty("remote"))) {
				init_remoteDriver("chrome", browserVersion);//remote execution
			} else {
				tlDriver.set(new ChromeDriver(optionsManager.getChromeOptions()));//optionsManager.getChromeOptions()==co
//				Here driver is initialised with Threadlocal
//				Each thread will get a threadlocal copy of the driver
			}
		}

		else if (browserName.equals("firefox")) {
			LOGGER.info("setup FF browser");
			WebDriverManager.firefoxdriver().setup();
			if (Boolean.parseBoolean(prop.getProperty("remote"))) {
				init_remoteDriver("firefox", browserVersion);
			} else {
				tlDriver.set(new FirefoxDriver(optionsManager.getFirefoxOptions()));
			}

		}

		else if (browserName.equals("safari")) {
			LOGGER.info("setup safari browser");
			tlDriver.set(new SafariDriver());
		} else {
			System.out.println("Please pass the correct browser name : " + browserName);
			LOGGER.info("Please pass the correct browser name : \" + browserName");
		}

		getDriver().manage().window().fullscreen();
		getDriver().manage().deleteAllCookies();

		return getDriver();

	}

	private void init_remoteDriver(String browser, String browserVersion) {

		System.out.println("Running test on remote grid server: " + browser);

		if (browser.equals("chrome")) {
			DesiredCapabilities cap = DesiredCapabilities.chrome();
			cap.setCapability("browserName", "chrome");
			cap.setCapability("browserVersion", browserVersion);
			cap.setCapability("enableVNC", true);
			cap.setCapability(ChromeOptions.CAPABILITY, optionsManager.getChromeOptions());

			try {
				tlDriver.set(new RemoteWebDriver(new URL(prop.getProperty("huburl")), cap));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}

		}

		else if (browser.equals("firefox")) {
			DesiredCapabilities cap = DesiredCapabilities.firefox();
			cap.setCapability("browserName", "firefox");
			cap.setCapability("browserVersion", browserVersion);
			cap.setCapability("enableVNC", true);
			cap.setCapability(ChromeOptions.CAPABILITY, optionsManager.getFirefoxOptions());

			try {
				tlDriver.set(new RemoteWebDriver(new URL(prop.getProperty("huburl")), cap));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}

	}

	public static synchronized WebDriver getDriver() {
		return tlDriver.get();
	}
	
//	Each thread will get a threadlocal copy of the driver one by one

	/**
	 * This method is used to initialize the properties from config file.
	 * 
	 * @return returns Properties prop
	 */
	public Properties init_prop() {
		FileInputStream ip = null;
		prop = new Properties();
		String env = System.getProperty("env");
		LOGGER.info("Running on Environment -->: " + env);
		System.out.println("Running on Environment -->: " + env);

		if (env == null) {
			try {
				ip = new FileInputStream("./src/test/resources/config/config.properties");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {

			try {
				switch (env) {
				case "qa":
					ip = new FileInputStream("./src/test/resources/config/qa.config.properties");
					break;
				case "stage":
					ip = new FileInputStream("./src/test/resources/config/stage.config.properties");
					break;
				case "dev":
					ip = new FileInputStream("./src/test/resources/config/dev.config.properties");
					break;
				case "prod":
					ip = new FileInputStream("./src/test/resources/config/config.properties");
					break;

				default:
					break;
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				LOGGER.error("File Not found at the given location....");

			}
		}

		try {
			prop.load(ip);
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}

		return prop;
	}

	/**
	 * take sceenshot Ashot
	 */
	public String getScreenshot() {
		String src = ((TakesScreenshot) getDriver()).getScreenshotAs(OutputType.BASE64);
		File srcFile = new File(src);
		String path = System.getProperty("user.dir") + "/screenshots/" + System.currentTimeMillis() + ".png";
		File destination = new File(path);
		try {
			FileUtils.copyFile(srcFile, destination);
		} catch (IOException e) {
			LOGGER.error("some exception is coming while creating the screenshot");
			e.printStackTrace();
		}
		return path;
	}

}