package com.samsung.android.app.stepdiary.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.samsung.android.app.stepdiary.R
import com.samsung.android.app.stepdiary.ui.base.BaseActivity
import com.samsung.android.app.stepdiary.ui.google.GoogleFitActivity
import com.samsung.android.app.stepdiary.ui.samsung.StepDiaryActivity

class MainActivity : BaseActivity() {
    override fun getContentViewId(): Int {
        return R.layout.activty_main
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findViewById<Button>(R.id.googleFitButton).setOnClickListener {
            this.goToActivity(GoogleFitActivity::class.java)
        }
        findViewById<Button>(R.id.samSungHealthButton).setOnClickListener {
            this.goToActivity(StepDiaryActivity::class.java)
        }
    }
}

private fun BaseActivity.goToActivity(referenceClass: Class<*>) {
    startActivity(Intent(this,referenceClass))
}
