package com.rudekfshape.mobile.tests;

import com.rudekfshape.mobile.config.Config;
import com.rudekfshape.mobile.apps.vkvideo.VkVideoSteps;
import io.qameta.allure.Description;
import io.qameta.allure.Owner;
import io.qameta.allure.Step;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class VkVideoPlaybackTest extends BaseMobileTest {

    @Override
    protected String appPackage() {
        return Config.require("vkvideo.appPackage");
    }

    @Override
    protected String appActivity() {
        return Config.require("vkvideo.appActivity");
    }

    @Test
    @Description("Checks VK Video playback: positive (internet ON) and negative (internet OFF) are both handled inside one test")
    void vk_video_should_play_with_internet_and_not_play_without_internet() {
        VkVideoSteps vk = new VkVideoSteps(driver);

        step("Open first video and verify it starts playing (internet ON)", () -> {
            vk.waitAppStable();
            vk.openFirstVideoOrFail();
            assertTrue(vk.waitUntilPlaying(20), "Expected video to start playing with internet");
        });

        step("Disable internet and verify NEW playback does NOT start (internet OFF)", () -> {
            driver.terminateApp("com.vk.vkvideo");
            driver.activateApp("com.vk.vkvideo");

            vk.waitAppStable();
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            vk.disableInternet();
            vk.openFirstVideoOrFail();

            boolean started = vk.waitUntilPlaying(30); // можно было waitUntilStopped но суть та же)
            boolean hasError = vk.isAnyErrorVisible();
            vk.enableInternet();
            assertTrue(!started || hasError, "Expected video NOT to start and show error with internet OFF");
        });

        step("Re-enable internet (cleanup)", vk::enableInternet);
    }

    @Step("{0}")
    private static void step(String name, Runnable action) {
        action.run();
    }
}
