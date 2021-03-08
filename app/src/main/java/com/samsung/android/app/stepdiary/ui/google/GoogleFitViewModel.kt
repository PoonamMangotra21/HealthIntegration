package com.samsung.android.app.stepdiary.ui.google

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.samsung.android.app.stepdiary.ui.domain.PermissionsEnum
import com.samsung.android.app.stepdiary.ui.domain.data.HealthRepository
import com.samsung.android.app.stepdiary.ui.domain.framework.HealthListener

class GoogleFitViewModel(private val healthRepository: HealthRepository) : ViewModel() {

    val permissionRequest = MutableLiveData<String>()

    fun onPermissionResult(permission: String, granted: Boolean) {
        TODO("whatever you need to do")
    }

    fun setUp(activity: Activity) {
        val list = listOf<PermissionsEnum>(PermissionsEnum.HEALTH_WEIGHT, PermissionsEnum.HEALTH_HEIGHT,
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