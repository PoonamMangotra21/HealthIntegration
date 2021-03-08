package com.samsung.android.app.stepdiary.ui.domain.data

import android.app.Activity
import com.samsung.android.app.stepdiary.ui.domain.PermissionsEnum
import com.samsung.android.app.stepdiary.ui.domain.framework.HealthListener

interface HealthRepository {

    fun authenticationApp(activity: Activity, permissionList: List<PermissionsEnum>, healthListener: HealthListener)

}