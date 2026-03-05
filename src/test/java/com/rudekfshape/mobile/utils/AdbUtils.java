package com.rudekfshape.mobile.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public final class AdbUtils {

    private static final Logger log = LoggerFactory.getLogger(AdbUtils.class);

    private AdbUtils() {
    }

    public static void disableAnimations() {
        execBestEffort("adb shell settings put global transition_animation_scale 0.0");
        execBestEffort("adb shell settings put global window_animation_scale 0.0");
        execBestEffort("adb shell settings put global animator_duration_scale 0.0");
    }

    public static void disableAllInternet() {
        // Works well on emulator API 30
        execBestEffort("adb shell svc wifi disable");
        execBestEffort("adb shell svc data disable");
        // airplane mode sometimes helps; some images ignore it
        execBestEffort("adb shell settings put global airplane_mode_on 1");
        execBestEffort("adb shell am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true");
    }

    public static void enableAllInternet() {
        execBestEffort("adb shell settings put global airplane_mode_on 0");
        execBestEffort("adb shell am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false");
        execBestEffort("adb shell svc wifi enable");
        execBestEffort("adb shell svc data enable");
    }

    public static void forceStop(String appPackage) {
        if (appPackage == null || appPackage.isBlank()) return;
        execBestEffort("adb shell am force-stop " + appPackage);
    }

    public static void startActivity(String appPackage, String appActivity) {
        if (appPackage == null || appPackage.isBlank()) return;
        if (appActivity == null || appActivity.isBlank()) return;
        execBestEffort("adb shell am start -n " + appPackage + "/" + appActivity);
    }

    public static String execAndGetOutput(String command) {
        try {
            log.info("ADB: {}", command);
            Process p = Runtime.getRuntime().exec(command);
            String out = new String(p.getInputStream().readAllBytes());
            String err = new String(p.getErrorStream().readAllBytes());
            p.waitFor();
            return (out + "\n" + err).trim();
        } catch (IOException | InterruptedException e) {
            log.warn("ADB command failed: {} ({})", command, e.getMessage());
            Thread.currentThread().interrupt();
            return "";
        }
    }

    private static void execBestEffort(String command) {
        try {
            log.info("ADB: {}", command);
            Process p = Runtime.getRuntime().exec(command);
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            log.warn("ADB command failed: {} ({})", command, e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
