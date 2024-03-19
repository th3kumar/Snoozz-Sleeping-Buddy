package com.fridayhouse.snoozz.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.fridayhouse.snoozz.R

class AboutActivity : ParentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        window.statusBarColor = ContextCompat.getColor(this, R.color.action_bar)
        //tryAppUpdateFlow()
    }
}