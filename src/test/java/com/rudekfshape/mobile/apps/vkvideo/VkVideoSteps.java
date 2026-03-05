package com.rudekfshape.mobile.apps.vkvideo;

import com.rudekfshape.mobile.utils.AdbUtils;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class VkVideoSteps {

    private static final Logger log = LoggerFactory.getLogger(VkVideoSteps.class);

    private final AndroidDriver driver;

    private static final String VK_PKG = "com.vk.vkvideo";


    public static final By FIRST_VIDEO = By.xpath("(//android.widget.ImageView[@resource-id='com.vk.vkvideo:id/preview'])[1]");

    private static final By PLAY_BUTTON = By.id("com.vk.vkvideo:id/play");
    private static final By SEEK_BAR = By.id("com.vk.vkvideo:id/seek_bar");
    private static final By CURRENT_PROGRESS = By.id("com.vk.vkvideo:id/current_progress");

    private static final By OFFLINE_MODE_TEXT = By.xpath("//*[@text='Offline mode' or contains(@text,'интернет') or contains(@text,'internet')]");
    private static final By ERROR_TEXT = By.xpath("//*[contains(@text,'No internet') or contains(@text,'Offline') or contains(@text,'нет интернета')]");

    private static final int CONTROLS_TAP_X = 200;
    private static final int CONTROLS_TAP_Y = 350;

    private long lastTapMs = 0L;
    private static final long TAP_DEBOUNCE_MS = 900;

    public VkVideoSteps(AndroidDriver driver) {
        this.driver = driver;
    }

    public void waitAppStable() {
        sleep(10_000);
    }

    public void openFirstVideoOrFail() {
        if (!isVisible(FIRST_VIDEO, 10)) {
            throw new AssertionError("No videos found on main screen");
        }

        click(FIRST_VIDEO, 10);

        // ждём загрузку плеера
        sleep(2500);

        // показываем контролы
        showControlsIfNeeded();
        waitControlsAppear(1800);

        // если есть кнопка play -> пробуем запустить
        if (isVisible(PLAY_BUTTON, 2)) {
            log.info("Video looks paused -> clicking play");
            safeClick(PLAY_BUTTON);
            sleep(1200);
        }
    }

    public boolean waitUntilPlaying(int timeoutSec) {
        log.info("Waiting until video starts playing (timeout {} sec)", timeoutSec);

        long end = System.currentTimeMillis() + timeoutSec * 1000L;

        while (System.currentTimeMillis() < end) {

            if (isAnyErrorVisible()) {
                log.info("Error detected on screen (offline / no internet)");
                return false;
            }

            // пробуем media_session (но у меня эта штука в 10% случаев считалась только(
            MediaSessionState ms = readMediaSessionState();
            log.info("MediaSession state: {}", ms);
            if (ms == MediaSessionState.PLAYING) {
                log.info("MediaSession reports PLAYING");
                return true;
            }

            showControlsIfNeeded();
            waitControlsAppear(1500);

            if (isVisible(PLAY_BUTTON, 1)) {
                log.info("Play button visible -> trying to start video");
                safeClick(PLAY_BUTTON);
                sleep(900);
            }

            // основной детект: двигается ли таймер/прогресс
            log.info("Checking timer/seek movement...");
            if (isSeekMoving(1800)) {
                log.info("Video playback detected via timer movement");
                return true;
            }

            sleep(600);
        }

        log.info("Timeout reached -> video did not start playing");
        return false;
    }

    public boolean isAnyErrorVisible() {
        return isVisible(OFFLINE_MODE_TEXT, 1) || isVisible(ERROR_TEXT, 1);
    }

    public void disableInternet() {
        AdbUtils.disableAllInternet();
        sleep(10000);
    }

    public void enableInternet() {
        AdbUtils.enableAllInternet();
        sleep(1500);
    }

    private MediaSessionState readMediaSessionState() {
        try {
            String dump = AdbUtils.execAndGetOutput("adb shell dumpsys media_session");
            if (dump == null || dump.isBlank()) return MediaSessionState.UNKNOWN;

            int idx = dump.indexOf(VK_PKG);
            if (idx < 0) return MediaSessionState.UNKNOWN;

            int from = Math.max(0, idx - 500);
            int to = Math.min(dump.length(), idx + 2500);
            String window = dump.substring(from, to);

            String lower = window.toLowerCase();
            if (containsAny(lower, "error", "state_error")) return MediaSessionState.ERROR;

            int s = window.indexOf("state=");
            if (s < 0) return MediaSessionState.UNKNOWN;

            int p = s + "state=".length();
            StringBuilder num = new StringBuilder();
            while (p < window.length()) {
                char c = window.charAt(p++);
                if (Character.isDigit(c)) num.append(c);
                else if (num.length() > 0) break;
            }
            if (num.isEmpty()) return MediaSessionState.UNKNOWN;

            int state = Integer.parseInt(num.toString());

            return switch (state) {
                case 3 -> MediaSessionState.PLAYING;
                case 2 -> MediaSessionState.PAUSED;
                case 1 -> MediaSessionState.STOPPED;
                default -> MediaSessionState.UNKNOWN;
            };

        } catch (Exception e) {
            return MediaSessionState.UNKNOWN;
        }
    }

    private static boolean containsAny(String haystack, String... needles) {
        for (String n : needles) if (haystack.contains(n)) return true;
        return false;
    }

    private enum MediaSessionState {
        PLAYING, PAUSED, STOPPED, ERROR, UNKNOWN
    }

    private void showControlsIfNeeded() {
        try {
            long now = System.currentTimeMillis();
            if (now - lastTapMs < TAP_DEBOUNCE_MS) return;

            lastTapMs = now;
            log.info("Tap to show controls at x={} y={}", CONTROLS_TAP_X, CONTROLS_TAP_Y);

            driver.executeScript("mobile: clickGesture", java.util.Map.of("x", CONTROLS_TAP_X, "y", CONTROLS_TAP_Y));
        } catch (Exception ignored) {

        }
    }

    private void waitControlsAppear(long timeoutMs) {
        long end = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < end) {
            if (!driver.findElements(CURRENT_PROGRESS).isEmpty()) return;
            if (!driver.findElements(PLAY_BUTTON).isEmpty()) return;
            if (!driver.findElements(SEEK_BAR).isEmpty()) return;
            sleep(120);
        }
    }


    private boolean isSeekMoving(long windowMs) {
        showControlsIfNeeded();
        sleep(windowMs);
        log.info("Первый пошел");
        showControlsIfNeeded();
        log.info("Кнопку отжал");
        waitControlsAppear(1000);
        log.info("Подождал и начал считывать");
        String a = readProgressSnapshotStable(2400);
        log.info("Progress before wait: {}", a);

        sleep(windowMs);

        showControlsIfNeeded();
        waitControlsAppear(1000);
        String b = readProgressSnapshotStable(2400);
        log.info("Progress after wait: {}", b);

        if (a == null || b == null) {
            log.info("Progress snapshot missing");
            return false;
        }

        boolean moved = !a.equals(b);
        if (moved) log.info("Seek moved: {} -> {}", a, b);
        else log.info("Seek did not move");

        return moved;
    }

    private String readProgressSnapshotStable(long timeoutMs) {
        long end = System.currentTimeMillis() + timeoutMs;
        String lastNonNull = null;

        while (System.currentTimeMillis() < end) {
            String s = readProgressSnapshotOnce();
            if (s != null) return s;
            sleep(120);
        }

        return lastNonNull;
    }

    private String readProgressSnapshotOnce() {

        String timer = readTimerText();
        if (timer != null && !timer.isBlank()) {
            log.info("Timer detected: {}", timer);
            return "timer=" + timer.trim();
        }

        //Seekbar
        try {
            List<WebElement> bars = driver.findElements(SEEK_BAR);
            if (bars.isEmpty()) {
                log.info("Seekbar not found");
                return null;
            }

            WebElement bar = bars.get(0);
            String txt = bar.getText();
            String desc = bar.getAttribute("content-desc");
            String value = bar.getAttribute("value");

            String snapshot = "text=" + safe(txt) + "|desc=" + safe(desc) + "|value=" + safe(value);
            log.info("Seekbar snapshot: {}", snapshot);
            return snapshot;

        } catch (Exception e) {
            log.info("Failed reading seekbar: {}", e.getMessage());
            return null;
        }
    }

    private String readTimerText() {
        try {
            List<WebElement> els = driver.findElements(CURRENT_PROGRESS);
            if (els.isEmpty()) return null;
            return els.get(0).getText();
        } catch (Exception e) {
            return null;
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private void safeClick(By locator) {
        try {
            driver.findElement(locator).click();
        } catch (Exception ignored) {
        }
    }

    private void click(By locator, int timeoutSec) {
        long end = System.currentTimeMillis() + timeoutSec * 1000L;
        while (System.currentTimeMillis() < end) {
            try {
                driver.findElement(locator).click();
                return;
            } catch (Exception ignored) {
                sleep(300);
            }
        }
        throw new AssertionError("Element not clickable: " + locator);
    }

    private boolean isVisible(By locator, int timeoutSec) {
        long end = System.currentTimeMillis() + timeoutSec * 1000L;
        while (System.currentTimeMillis() < end) {
            try {
                return !driver.findElements(locator).isEmpty();
            } catch (Exception ignored) {
                sleep(250);
            }
        }
        return false;
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}