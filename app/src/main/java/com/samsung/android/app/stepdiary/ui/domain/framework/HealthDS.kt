package com.samsung.android.app.stepdiary.ui.domain.framework

import android.app.Activity

interface HealthDS {

    fun <T> connectAndAuthenticate(activity: Activity, permissionList: T, healthListener: HealthListener)

}
