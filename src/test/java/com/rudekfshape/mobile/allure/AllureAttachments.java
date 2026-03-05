package com.rudekfshape.mobile.allure;

import com.codeborne.selenide.Screenshots;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.ByteArrayInputStream;

public final class AllureAttachments {

    private AllureAttachments() {}

    public static void screenshot(WebDriver driver, String name) {
        try {
            if (driver instanceof TakesScreenshot ts) {
                byte[] bytes = ts.getScreenshotAs(OutputType.BYTES);
                Allure.addAttachment(name, "image/png", new ByteArrayInputStream(bytes), ".png");
            } else {
                var file = Screenshots.takeScreenShotAsFile();
                if (file != null) {
                    Allure.addAttachment(name, "image/png", file.toURI().toURL().openStream(), ".png");
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void pageSource(WebDriver driver, String name) {
        try {
            String source = driver.getPageSource();
            Allure.addAttachment(name, "text/xml", source);
        } catch (Exception ignored) {
        }
    }

    public static void text(String name, String content) {
        try {
            Allure.addAttachment(name, "text/plain", content == null ? "" : content);
        } catch (Exception ignored) {
        }
    }
}
