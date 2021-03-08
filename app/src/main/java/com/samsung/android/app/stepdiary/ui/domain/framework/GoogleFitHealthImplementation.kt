package com.samsung.android.app.stepdiary.ui.domain.framework

import android.app.Activity
import com.google.android.gms.fitness.FitnessOptions
import com.samsung.android.app.stepdiary.googlefit.FitActionRequestCode
import com.samsung.android.app.stepdiary.googlefit.GoogleFitConnectHelper

class GoogleFitHealthImplementation : HealthDS {


    override fun <T> connectAndAuthenticate(activity: Activity, permissionList: T, healthListener: HealthListener) {
        val googleFitConnectHelper = GoogleFitConnectHelper(activity, (permissionList as FitnessOptions))

        // initializeLogging()
        googleFitConnectHelper.checkPermissionsAndRun(FitActionRequestCode.SUBSCRIBE)
    }

}