package com.fridayhouse.snoozz.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.fridayhouse.snoozz.R

class AboutActivity : ParentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        //tryAppUpdateFlow()
    }
}