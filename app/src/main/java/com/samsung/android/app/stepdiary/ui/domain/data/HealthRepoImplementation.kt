package com.samsung.android.app.stepdiary.ui.domain.data

import android.app.Activity
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.HealthDataTypes
import com.samsung.android.app.stepdiary.ui.domain.PermissionsEnum
import com.samsung.android.app.stepdiary.ui.domain.framework.GoogleFitHealthImplementation
import com.samsung.android.app.stepdiary.ui.domain.framework.HealthDS
import com.samsung.android.app.stepdiary.ui.domain.framework.HealthListener
import com.samsung.android.app.stepdiary.ui.domain.framework.SamSungHealthImplementation
import com.samsung.android.sdk.healthdata.HealthConstants
import com.samsung.android.sdk.healthdata.HealthPermissionManager

class HealthRepoImplementation(private val healthDS: HealthDS) : HealthRepository {

    override fun authenticationApp(activity: Activity, permissionList: List<PermissionsEnum>, healthListener: HealthListener) {
        if (healthDS is SamSungHealthImplementation) {
            val permissions = getSamsungHealthPermissions(permissionList)
            healthDS.connectAndAuthenticate(activity, permissions, healthListener)
        } else if (healthDS is GoogleFitHealthImplementation) {
            val permissions = getGoogleFitPermissions(permissionList)
            healthDS.connectAndAuthenticate(activity, permissions, healthListener)
        }
    }

    private fun getSamsungHealthPermissions(permissionList: List<PermissionsEnum>): MutableSet<HealthPermissionManager.PermissionKey> {
        val permissionKeySet: MutableSet<HealthPermissionManager.PermissionKey> = HashSet()

        permissionList.forEach {
            when (it) {
                PermissionsEnum.HEALTH_HEIGHT -> {
                    permissionKeySet.add(HealthPermissionManager.PermissionKey(HealthConstants.Height.HEIGHT, HealthPermissionManager.PermissionType.READ))
                    permissionKeySet.add(HealthPermissionManager.PermissionKey(HealthConstants.Height.HEIGHT, HealthPermissionManager.PermissionType.WRITE))
                }
                PermissionsEnum.HEALTH_WEIGHT -> {
                    permissionKeySet.add(HealthPermissionManager.PermissionKey(HealthConstants.Weight.WEIGHT, HealthPermissionManager.PermissionType.READ))
                    permissionKeySet.add(HealthPermissionManager.PermissionKey(HealthConstants.Weight.WEIGHT, HealthPermissionManager.PermissionType.WRITE))
                }
                PermissionsEnum.HEALTH_STEPS_COUNT -> {
                    permissionKeySet.add(HealthPermissionManager.PermissionKey(HealthConstants.StepCount.HEALTH_DATA_TYPE, HealthPermissionManager.PermissionType.READ))
                    permissionKeySet.add(HealthPermissionManager.PermissionKey(HealthConstants.StepCount.HEALTH_DATA_TYPE, HealthPermissionManager.PermissionType.WRITE))
                }
                PermissionsEnum.HEALTH_HEART_POINTS -> {
                    permissionKeySet.add(HealthPermissionManager.PermissionKey(HealthConstants.HeartRate.HEART_RATE, HealthPermissionManager.PermissionType.READ))
                    permissionKeySet.add(HealthPermissionManager.PermissionKey(HealthConstants.HeartRate.HEART_RATE, HealthPermissionManager.PermissionType.WRITE))
                }
                PermissionsEnum.HEALTH_BLOOD_GLUCOSE -> {
                    permissionKeySet.add(HealthPermissionManager.PermissionKey(HealthConstants.BloodGlucose.GLUCOSE, HealthPermissionManager.PermissionType.READ))
                    permissionKeySet.add(HealthPermissionManager.PermissionKey(HealthConstants.BloodGlucose.GLUCOSE, HealthPermissionManager.PermissionType.WRITE))
                }
                PermissionsEnum.HEALTH_STEPS_DAILY_TREND -> {
                    permissionKeySet.add(HealthPermissionManager.PermissionKey(HealthConstants.StepDailyTrend.HEALTH_DATA_TYPE, HealthPermissionManager.PermissionType.READ))
                    permissionKeySet.add(HealthPermissionManager.PermissionKey(HealthConstants.StepDailyTrend.HEALTH_DATA_TYPE, HealthPermissionManager.PermissionType.WRITE))
                }
            }
        }
        return permissionKeySet
    }

    private fun getGoogleFitPermissions(permissionList: List<PermissionsEnum>): FitnessOptions {
        val fitnessOptions = FitnessOptions.builder()
        permissionList.forEach {
            when (it) {
                PermissionsEnum.HEALTH_HEIGHT -> {
                    fitnessOptions.addDataType(DataType.TYPE_HEIGHT, FitnessOptions.ACCESS_READ)
                    fitnessOptions.addDataType(DataType.TYPE_HEIGHT, FitnessOptions.ACCESS_WRITE)
                }
                PermissionsEnum.HEALTH_WEIGHT -> {
                    fitnessOptions.addDataType(DataType.TYPE_WEIGHT, FitnessOptions.ACCESS_READ)
                    fitnessOptions.addDataType(DataType.TYPE_WEIGHT, FitnessOptions.ACCESS_WRITE)
                }
                PermissionsEnum.HEALTH_STEPS_COUNT -> {
                    fitnessOptions.addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                    fitnessOptions.addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
                }
                PermissionsEnum.HEALTH_HEART_POINTS -> {
                    fitnessOptions.addDataType(DataType.TYPE_HEART_POINTS, FitnessOptions.ACCESS_READ)
                    fitnessOptions.addDataType(DataType.TYPE_HEART_POINTS, FitnessOptions.ACCESS_WRITE)
                }
                PermissionsEnum.HEALTH_BLOOD_GLUCOSE -> {
                    fitnessOptions.addDataType(HealthDataTypes.TYPE_BLOOD_GLUCOSE, FitnessOptions.ACCESS_READ)
                    fitnessOptions.addDataType(HealthDataTypes.TYPE_BLOOD_GLUCOSE, FitnessOptions.ACCESS_WRITE)
                }
                PermissionsEnum.HEALTH_STEPS_DAILY_TREND -> {
                    fitnessOptions.addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE, FitnessOptions.ACCESS_READ)
                    fitnessOptions.addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE, FitnessOptions.ACCESS_WRITE)
                }
            }
        }
        fitnessOptions.build()
        return fitnessOptions.build()
    }
}