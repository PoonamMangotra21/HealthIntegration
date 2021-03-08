package com.samsung.android.app.stepdiary.ui.domain.framework

import android.app.Activity
import com.samsung.android.app.stepdiary.samsunghealth.SHealthConnectionHelper
import com.samsung.android.sdk.healthdata.HealthPermissionManager

class SamSungHealthImplementation : HealthDS {

    override fun <T> connectAndAuthenticate(activity: Activity, permissionList: T, healthListener: HealthListener) {
        val sHealthConnectionHelper = SHealthConnectionHelper(activity, permissionList as HashSet<HealthPermissionManager.PermissionKey>)
        sHealthConnectionHelper.connectSHealth()
    }

}

