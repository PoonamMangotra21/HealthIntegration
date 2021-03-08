package com.samsung.android.app.stepdiary.util

import android.app.Activity
import android.app.AlertDialog
import com.samsung.android.app.stepdiary.R
import com.samsung.android.sdk.healthdata.HealthConnectionErrorResult

object AlertHelper {

    fun showPermissionAlarmDialog(activity: Activity) {
        AlertDialog.Builder(activity)
                .setTitle(R.string.notice)
                .setMessage(R.string.msg_perm_acquired)
                .setPositiveButton(R.string.ok, null)
                .show()
    }

    fun showConnectionFailureDialog(activity: Activity, error: HealthConnectionErrorResult) {
        val alert = AlertDialog.Builder(activity)
        if (error.hasResolution()) {
            when (error.errorCode) {
                HealthConnectionErrorResult.PLATFORM_NOT_INSTALLED -> alert.setMessage(R.string.msg_req_install)
                HealthConnectionErrorResult.OLD_VERSION_PLATFORM -> alert.setMessage(R.string.msg_req_upgrade)
                HealthConnectionErrorResult.PLATFORM_DISABLED -> alert.setMessage(R.string.msg_req_enable)
                HealthConnectionErrorResult.USER_AGREEMENT_NEEDED -> alert.setMessage(R.string.msg_req_agree)
                else -> alert.setMessage(R.string.msg_req_available)
            }
        } else {
            alert.setMessage(R.string.msg_conn_not_available)
        }
        alert.setPositiveButton(R.string.ok) { _, _ ->
            if (error.hasResolution()) {
                try{
                    error.resolve(activity)
                }catch (e:Exception){
                    alert.setMessage(R.string.msg_req_install)
                }
            }
        }
        if (error.hasResolution()) {
            alert.setNegativeButton(R.string.cancel, null)
        }
        alert.show()
    }
}