/* Copyright 2019 SimpleNexus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.simplenexus.loanrunner

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity: AppCompatActivity() {

    private val listener = ActionBar.OnMenuVisibilityListener {
        pico8("SetPaused", it.toString())
    }

    private var lastDpadKey = KeyEvent.KEYCODE_UNKNOWN

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        title = ""

        webView.apply {
            settings.javaScriptEnabled = true

            // Load the HTML data for Loan Runner into the WebView
            loadUrl("file:///android_asset/loanrunner.html")

            // Listen for when the user touches the screen, and translate the
            // touch into the correct keypress to allow our runner to jump.

            setOnTouchListener { _, event ->
                when(event.action) {
                    KeyEvent.ACTION_DOWN, KeyEvent.ACTION_UP -> {
                        dispatchKeyEvent(KeyEvent(event.action, KeyEvent.KEYCODE_Z))
                    }
                }

                true
            }
        }

        // Add a menu visibility listener which automatically pauses and
        // unpauses the game when the menu is opened or closed.

        supportActionBar?.addOnMenuVisibilityListener(listener)
    }

    override fun onDestroy() {
        supportActionBar?.removeOnMenuVisibilityListener(listener)
        super.onDestroy()
    }

    // Bit of a hack to force the WebView to resize on a config change,
    // thus triggering the CSS media queries to resize the game window.

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)

        newConfig?.let {
            val density = it.densityDpi / 160f
            val width = it.screenWidthDp * density
            val height = it.screenHeightDp * density

            webView.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)

            Handler().postDelayed({
                webView.layoutParams = FrameLayout.LayoutParams(width.toInt(), height.toInt())
            }, 50)
        }
    }

    // Initializes the options menu in the lower-right hand corner of the screen.

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Handler().postDelayed({
            when(item.itemId) {
                R.id.reset -> pico8("Reset")
                R.id.sound -> pico8("ToggleSound")
                R.id.about -> openSNWebsite()
            }
        }, 50)

        return true
    }

    // Listens for button presses from a gamepad or remote, and translates them
    // into the corresponding keypresses to dispatch to the WebView.

    // Loan Runner only uses the Z key (which we map to the A button), so we're
    // only handling that (along with P for accessing the in-game pause menu).
    // However, PICO-8 also lets games use the X key, which we don't handle here.

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        when(event?.action) {
            KeyEvent.ACTION_DOWN, KeyEvent.ACTION_UP -> {
                when(event.keyCode) {
                    KeyEvent.KEYCODE_BUTTON_A,
                    KeyEvent.KEYCODE_BUTTON_B,
                    KeyEvent.KEYCODE_BUTTON_R1, // R1 maps to C on the 8bitdo M30
                    KeyEvent.KEYCODE_DPAD_CENTER -> {
                        webView.dispatchKeyEvent(KeyEvent(event.action, KeyEvent.KEYCODE_Z))
                        return true
                    }

                    KeyEvent.KEYCODE_BUTTON_START -> {
                        webView.dispatchKeyEvent(KeyEvent(event.action, KeyEvent.KEYCODE_P))
                        return true
                    }
                }
            }
        }

        return super.dispatchKeyEvent(event)
    }

    // Listens for D-Pad inputs from a gamepad, and translates them into
    // the corresponding keypresses to dispatch to the WebView.

    // Currently we're only listening to Y-axis inputs (up and down), since
    // those are the only ones that Loan Runner recognizes.

    override fun dispatchGenericMotionEvent(ev: MotionEvent?): Boolean {
        if(ev?.action == MotionEvent.ACTION_MOVE) {
            when(ev.getAxisValue(MotionEvent.AXIS_HAT_Y)) {
                -1f -> {
                    lastDpadKey = KeyEvent.KEYCODE_DPAD_UP
                    webView.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, lastDpadKey))
                    return true
                }

                0f -> {
                    webView.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, lastDpadKey))
                    return true
                }

                1f -> {
                    lastDpadKey = KeyEvent.KEYCODE_DPAD_DOWN
                    webView.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, lastDpadKey))
                    return true
                }
            }
        }

        return super.dispatchGenericMotionEvent(ev)
    }

    // Sends commands to the PICO-8 JavaScript runtime inside the WebView,
    // depending on which options the user selects from the menu.

    private fun pico8(action: String, param: String = "")
            = webView.loadUrl("javascript:Module.pico8$action($param);")

    // Opens the SimpleNexus website in a Chrome Custom Tab, so the user can
    // learn more about the cool stuff we work on every single day!

    private fun openSNWebsite() {
        val customTabs = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .addDefaultShareMenuItem()
                .build()

        try {
            customTabs.launchUrl(this, Uri.parse("https://www.simplenexus.com"))
        } catch (e: ActivityNotFoundException) {}
    }
}