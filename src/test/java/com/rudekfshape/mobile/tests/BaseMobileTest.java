package com.rudekfshape.mobile.tests;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.logevents.SelenideLogger;
import com.rudekfshape.mobile.allure.AllureAttachments;
import com.rudekfshape.mobile.driver.AndroidDriverFactory;
import com.rudekfshape.mobile.utils.AdbUtils;
import io.appium.java_client.android.AndroidDriver;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

public abstract class BaseMobileTest {

    protected AndroidDriver driver;

    protected abstract String appPackage();

    protected abstract String appActivity();

    @BeforeAll
    static void globalSetup() {
        SelenideLogger.addListener("AllureSelenide", new AllureSelenide().screenshots(false).savePageSource(false));

        Configuration.browserSize = null;
        Configuration.timeout = 15_000;

        AdbUtils.disableAnimations();
    }

    @BeforeEach
    void setUp(TestInfo testInfo) {
        driver = AndroidDriverFactory.createForApp(appPackage(), appActivity());
        WebDriverRunner.setWebDriver(driver);

        AdbUtils.startActivity(appPackage(), appActivity());
    }

    @AfterEach
    void tearDown(TestInfo testInfo) {
        try {
            AllureAttachments.screenshot(driver, "Screenshot (after test)");
            AllureAttachments.pageSource(driver, "Page Source (after test)");
            // Extra diagnostics for flaky media apps
            AllureAttachments.text("dumpsys media_session", AdbUtils.execAndGetOutput("adb shell dumpsys media_session"));
            AllureAttachments.text("dumpsys activity top", AdbUtils.execAndGetOutput("adb shell dumpsys activity activities | head -n 200"));
        } finally {
            try {
                if (driver != null) driver.quit();
            } finally {
                AdbUtils.forceStop(appPackage());
                WebDriverRunner.closeWebDriver();
            }
        }
    }
}
