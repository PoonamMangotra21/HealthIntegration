package com.samsung.android.app.stepdiary.ui.samsung

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.samsung.android.app.stepdiary.ui.domain.PermissionsEnum
import com.samsung.android.app.stepdiary.ui.domain.data.HealthRepository
import com.samsung.android.app.stepdiary.ui.domain.framework.HealthListener

class StepDiaryViewModel(private val healthRepository: HealthRepository) : ViewModel() {

    fun setUp(activity: Activity) {
        val list = listOf(PermissionsEnum.HEALTH_WEIGHT, PermissionsEnum.HEALTH_HEIGHT,
                PermissionsEnum.HEALTH_STEPS_COUNT, PermissionsEnum.HEALTH_STEPS_DAILY_TREND,
                PermissionsEnum.HEALTH_BLOOD_GLUCOSE, PermissionsEnum.HEALTH_HEART_POINTS)
        healthRepository.authenticationApp(activity, list, object : HealthListener {
            override fun onPermissionFailed() {

            }

            override fun onPermissionSuccess() {

            }

            override fun onConnectionSuccess() {

            }

            override fun onConnectionFailed() {

            }
        })
    }
}