package com.rudekfshape.mobile.tests;

import com.rudekfshape.mobile.apps.alchemy.AlchemySteps;
import com.rudekfshape.mobile.config.Config;
import io.qameta.allure.Description;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("TODO: finalize locators + stabilize rewarded-ad flow (can be flaky on emulators)")
public class AlchemyHintsTest extends BaseMobileTest {

    @Override
    protected String appPackage() {
        return Config.require("alchemy.appPackage");
    }

    @Override
    protected String appActivity() {
        return Config.require("alchemy.appActivity");
    }

    @Test
    @Description("Алхимия: Играть -> Получить подсказку -> Реклама -> Проверить, что подсказок стало 4")
    void hints_should_increase_to_four_after_rewarded_ad() {
        AlchemySteps alchemy = new AlchemySteps(driver);
        alchemy.tapPlay();

        int before = alchemy.readHintCount();

        alchemy.tapAddHints();

        alchemy.tapWatchAdForHint();
        assertTrue(alchemy.closerAd());
        alchemy.closeAdMenu();

        int after = alchemy.readHintCount();

        assertTrue(after > before, "Hint count should increase");
        assertEquals(4, after, "Hint count should be 4");
    }
}