package com.activepulse.tracker

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PrivacyPolicyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val textView = TextView(this)
        textView.text = "Privacy Policy: We respect your health data privacy. This app uses Health Connect to read steps and workout data only for tracking goals. Data is not shared."
        setContentView(textView)
    }
}