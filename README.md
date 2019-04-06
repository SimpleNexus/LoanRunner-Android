![Loan Runner](https://raw.githubusercontent.com/SimpleNexus/LoanRunner-Android/master/app/src/main/res/drawable-xhdpi/banner.png)

This is an Android port of [Loan Runner](https://www.simplenexus.com/sn/loan-runner-game/), a [PICO-8](https://www.lexaloffle.com/pico-8.php) game created by SimpleNexus, inspired by the chrome://dino game! This codebase can also serve as a starting point for porting your own web-exported PICO-8 game to Android.

## Controls
* Tap your device's screen to make Loan Runner jump!
* Includes gamepad & remote support for Android TV devices

## Download
<!--
[<img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png"
      alt="Google Play"
      height="80"
      align="middle">](https://play.google.com/store/apps/details?id=com.simplenexus.loanrunner)
-->

Grab the APK file from https://github.com/SimpleNexus/LoanRunner-Android/releases

## How to Build
Prerequisites:
* Windows / MacOS / Linux
* JDK 8
* Android SDK
* Internet connection (to download dependencies)

Once all the prerequisites are met, make sure that the `ANDROID_HOME` environment variable is set to your Android SDK directory, then run `./gradlew assembleDebug` at the base directory of the project to start the build. After the build completes, navigate to `app/build/outputs/apk/debug` where you will end up with an APK file ready to install on your Android device.
