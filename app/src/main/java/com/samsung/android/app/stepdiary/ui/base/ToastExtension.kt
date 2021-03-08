package com.samsung.android.app.stepdiary.ui.base

import android.app.Activity
import android.widget.Toast

fun String.showToastMessage(activity: Activity) {
    Toast.makeText(activity, this, Toast.LENGTH_SHORT).show()
}