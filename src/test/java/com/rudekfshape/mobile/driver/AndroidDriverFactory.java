package com.rudekfshape.mobile.driver;

import com.rudekfshape.mobile.config.Config;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URI;
import java.net.URL;
import java.time.Duration;

public final class AndroidDriverFactory {

    private AndroidDriverFactory() {}

    public static AndroidDriver createForApp(String appPackage, String appActivity) {
        DesiredCapabilities caps = new DesiredCapabilities();

        caps.setCapability("platformName", Config.get("android.platformName"));
        caps.setCapability("appium:automationName", Config.get("android.automationName"));
        caps.setCapability("appium:deviceName", Config.get("android.deviceName"));
        caps.setCapability("appium:platformVersion", Config.get("android.platformVersion"));
        caps.setCapability("appium:noReset", Config.getBool("android.noReset", true));
        caps.setCapability("appium:newCommandTimeout", Config.getInt("android.newCommandTimeoutSec", 3600));

        if (appPackage != null && !appPackage.isBlank()) {
            caps.setCapability("appium:appPackage", appPackage);
        }
        if (appActivity != null && !appActivity.isBlank()) {
            caps.setCapability("appium:appActivity", appActivity);
        }

        try {
            URL serverUrl = URI.create(Config.require("appium.serverUrl")).toURL();
            AndroidDriver driver = new AndroidDriver(serverUrl, caps);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(Config.getInt("android.implicitWaitSec", 10)));
            return driver;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create AndroidDriver. Check Appium server and config.", e);
        }
    }
}
