package com.samsung.android.app.stepdiary.samsunghealth

import android.app.Activity
import android.util.Log
import com.samsung.android.app.stepdiary.ui.samsung.StepDiaryActivity
import com.samsung.android.app.stepdiary.util.AlertHelper
import com.samsung.android.sdk.healthdata.*

class SHealthConnectionHelper(private val activity: Activity, private val permissionKeySet: HashSet<HealthPermissionManager.PermissionKey>) {

    private var healthDataStore: HealthDataStore? = null

    fun connectSHealth() {
        healthDataStore = HealthDataStore(activity, object : HealthDataStore.ConnectionListener {
            override fun onConnected() {
                Log.e("SHealthConnectionHelper", "onConnected Called")
                if (checkPermissionsAcquired()) {
                    readStepCount()
                } else {
                    requestPermission()
                }
            }

            override fun onConnectionFailed(p0: HealthConnectionErrorResult) {
                Log.e("SHealthConnectionHelper", "onConnectionFailed Called $p0")
                AlertHelper.showConnectionFailureDialog(activity, p0)
            }

            override fun onDisconnected() {
                Log.e("SHealthConnectionHelper", "onDisconnected Called")
                healthDataStore?.connectService()
            }
        })
        healthDataStore?.connectService()
        //    android.app.ServiceConnectionLeaked: Activity com.samsung.android.app.stepdiary.ui.samsung.StepDiaryActivity has leaked ServiceConnection com.samsung.android.sdk.healthdata.HealthDataStore$a@bb98136 that was originally bound here
    }

    fun disconnectSHealth() {
        healthDataStore?.disconnectService()
    }

    fun readStepCount() {
        if (healthDataStore != null)
            ReadSData.readStepData(healthDataStore!!)
    }

    fun readBloodGlucose() {
        if (healthDataStore != null)
            ReadSData.readBloodGlucose(healthDataStore!!, HealthConstants.BloodGlucose.HEALTH_DATA_TYPE, HealthConstants.BloodGlucose.GLUCOSE)
    }

    fun readHeight() {
        if (healthDataStore != null)
            ReadSData.readBloodGlucose(healthDataStore!!, HealthConstants.Height.HEIGHT, HealthConstants.Height.HEIGHT)
    }

    fun readWeight() {
        if (healthDataStore != null)
            ReadSData.readBloodGlucose(healthDataStore!!, HealthConstants.Weight.WEIGHT, HealthConstants.Weight.WEIGHT)
    }


    // Check whether the permissions that this application needs are acquired
    private fun checkPermissionsAcquired(): Boolean {
        val pmsManager = HealthPermissionManager(healthDataStore)
        pmsManager.isPermissionAcquired(permissionKeySet)
        // Check whether the permissions that this application needs are acquired
        return runCatching { pmsManager.isPermissionAcquired(permissionKeySet) }
                .onFailure { Log.e(StepDiaryActivity.TAG, "Permission request fails.", it) }
                .map { it.values.all { it } }
                .getOrDefault(false)
    }

    private fun requestPermission() {
        val pmsManager = HealthPermissionManager(healthDataStore)

        // Show user permission UI for allowing user to change options
        runCatching { pmsManager.requestPermissions(permissionKeySet, activity) }
                .onFailure { Log.e(StepDiaryActivity.TAG, "Permission setting fails.", it) }
                .getOrNull()
                ?.setResultListener(mPermissionListener)
    }


    private val mPermissionListener = HealthResultHolder.ResultListener<HealthPermissionManager.PermissionResult> { result ->
        // Show a permission alarm and clear step count if permissions are not acquired
        if (result.resultMap.values.any { !it }) {
            /*updateTotalStepCountView("")
            updateBinningListView(emptyList())
           */
            AlertHelper.showPermissionAlarmDialog(activity)
        } else {
            // Get the daily step count of a particular day and display it
            readStepCount()
            //  stepCountReader.requestDailyStepCount(currentStartTime)
        }
    }

}