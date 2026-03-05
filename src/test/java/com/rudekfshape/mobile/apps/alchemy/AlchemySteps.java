package com.rudekfshape.mobile.apps.alchemy;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlchemySteps {

    private static final Logger log = LoggerFactory.getLogger(AlchemySteps.class);

    private final AndroidDriver driver;


    public static final By PLAY_BUTTON =
            By.xpath("//x2.f1/android.view.View/android.view.View/android.view.View/android.view.View[5]/android.widget.Button");
    public static final By HINT_COUNT =
            By.xpath("(//android.widget.TextView)[1]");
    public static final By ADD_HINT_BUTTON =
            By.xpath("//x2.f1/android.view.View/android.view.View/android.view.View/android.view.View[1]/android.view.View[1]/android.view.View[2]");
    public static final By WATCH_AD_BUTTON =
            By.xpath("//x2.f1/android.view.View/android.view.View/android.view.View/android.view.View[2]/android.view.View/android.view.View/android.view.View/android.view.View[3]/android.view.View[2]/android.view.View/android.widget.Button");
    public static final By SKIP_AD_BUTTON =
            By.xpath("//android.widget.RelativeLayout/android.widget.FrameLayout/android.widget.FrameLayout/android.view.ViewGroup/android.view.ViewGroup/android.view.ViewGroup[2]/android.view.ViewGroup[1]/android.view.ViewGroup[2]/android.view.ViewGroup[2]/android.widget.ImageView");

    public static final By SKIP_AD_BUTTON_TWO =
            By.xpath("//android.widget.RelativeLayout[@content-desc=\"pageIndex: 2\"]/android.widget.FrameLayout/android.widget.FrameLayout/android.view.ViewGroup/android.view.ViewGroup/android.view.ViewGroup[2]/android.view.ViewGroup[1]/android.view.ViewGroup[2]/android.view.ViewGroup[2]/android.widget.ImageView");

    public static final By CLOSE_AD_BUTTON =
            By.xpath("//android.widget.RelativeLayout[contains(@content-desc,'pageIndex')]/android.widget.FrameLayout/android.widget.FrameLayout/android.view.ViewGroup/android.view.ViewGroup/android.view.ViewGroup[1]/android.widget.ImageView");

    public static final By CLOSE_AD_BUTTON_ID =
            By.id("com.ilyin.alchemy:id/bigo_ad_btn_close");


    public static final By CLOSE_AD_MENU_BUTTON =
            By.xpath("//android.view.View[@content-desc=\"Закрыть меню навигации\"]");

    public static final By AFTER_AD_SCREEN =
            By.xpath("//x2.f1/android.view.View/android.view.View/android.view.View/android.view.View[2]/android.view.View/android.view.View/android.view.View");

    public AlchemySteps(AndroidDriver driver) {
        this.driver = driver;
        log.info("AlchemySteps initialized");
    }

    public void tapPlay() {
        sleep(3_000); //Экран у игрушки может перекрываться play играми в моменте и в 5% запусков из-за этого крашит.
        log.info("STEP: tapPlay()");
        driver.findElement(PLAY_BUTTON).click();
        log.info("Play button clicked");
    }

    public void tapAddHints() {
        log.info("STEP: tapAddHints()");
        driver.findElement(ADD_HINT_BUTTON).click();
        log.info("Add hints button clicked, waiting 10 seconds"); // даем подгрузить рекламу
        sleep(10_000);
    }

    public void tapWatchAdForHint() {
        log.info("STEP: tapWatchAdForHint()");
        driver.findElement(WATCH_AD_BUTTON).click();
        log.info("Watch ad button clicked");
    }


    public boolean closerAd() {
        log.info("STEP: closerAd()");
        sleep(15000);

        for (int attempt = 3; attempt != 0; ) {

            for (int clickAttempt = 0; clickAttempt != 10; clickAttempt++) {
                log.info("Try click skip button");
                skipAd();

                sleep(5000);
                log.info("Try click close button");
                if (closeAd()) {
                    sleep(2000);
                    if (isPresent(AFTER_AD_SCREEN)) {
                        return true;
                    }
                }


            }
            attempt--;
            sleep(2_000);
        }

        return false;
    }

    private void skipAd() {
        if (!safeClickOnce(SKIP_AD_BUTTON, "Skip ad (xpath)")) {
            safeClickOnce(SKIP_AD_BUTTON_TWO, "Skip ad (xpath)");
        }
    }

    private boolean closeAd(){
        if (safeClickOnce(CLOSE_AD_BUTTON_ID, "close Ad Click by Id")){
            return true;
        } else {
            return safeClickOnce(CLOSE_AD_BUTTON, "close Ad Click");
        }
    }


    public void closeAdMenu() {
        log.info("STEP: closeAdMenu()");
        driver.findElement(CLOSE_AD_MENU_BUTTON).click();
        log.info("Ad menu closed");
    }


    public int readHintCount() {
        log.info("STEP: readHintCount()");
        String text = driver.findElement(HINT_COUNT).getText();
        int count = Integer.parseInt(text.trim());
        log.info("Hint count read: {}", count);
        return count;
    }

    //helpers

    private boolean isPresent(By locator) {
        try {
            return !driver.findElements(locator).isEmpty();
        } catch (WebDriverException e) {
            log.warn("isPresent() WebDriverException: {}", e.getMessage());
            return false;
        }
    }

    private boolean safeClickOnce(By locator, String name) {
        try {
            if (driver.findElements(locator).isEmpty()) return false;
            driver.findElement(locator).click();
            log.info("'{}' clicked. Locator: {}", name, locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        } catch (WebDriverException e) {
            log.warn("Click '{}' failed (WebDriverException): {}", name, e.getMessage());
            return false;
        }
    }

    private static void sleep(long ms) {
        log.info("Sleeping {} ms", ms);
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            log.error("Sleep interrupted", e);
            Thread.currentThread().interrupt();
        }
    }
}