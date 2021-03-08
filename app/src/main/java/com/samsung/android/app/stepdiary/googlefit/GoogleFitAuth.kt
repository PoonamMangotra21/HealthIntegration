package com.samsung.android.app.stepdiary.googlefit

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.FitnessOptions

object GoogleFitAuth {

    internal fun checkPermissionsAndRun(activity: Activity, fitActionRequestCode: FitActionRequestCode, fitnessOptions: FitnessOptions) {
        if (permissionApproved(activity)) {
            fitSignIn(activity, fitActionRequestCode, fitnessOptions)
        } else {
            requestRuntimePermissions(activity, fitActionRequestCode)
        }
    }

    private val runningQOrLater =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    private fun permissionApproved(context: Context): Boolean {
        val approved = if (runningQOrLater) {
            PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACTIVITY_RECOGNITION)  //Allows an application to recognize physical activity. for apps that need to detect the user's step count or classify the user's physical activity, such as walking, biking, or moving in a vehicle.
        } else {
            true
        }
        return approved
    }

    private fun requestRuntimePermissions(activity: Activity, requestCode: FitActionRequestCode) {
        val shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACTIVITY_RECOGNITION)
//https://developers.google.com/fit/android/authorization#requesting_android_permissions
        // Provide an additional rationale to the user. context would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        requestCode.let {
            if (shouldProvideRationale) {
                Log.i(TAG, "Displaying permission rationale to provide additional context.")
            } else {
                Log.i(TAG, "Requesting permission")
                // Request permission. It's possible context can be auto answered if device policy
                // sets the permission in a given state or the user denied the permission
                // previously and checked "Never ask again".
                ActivityCompat.requestPermissions(activity,
                        arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                        requestCode.ordinal)
            }
        }
    }

    // normalizing model  for generic
    // setup authentication and permission
    /**
     * Checks that the user is signed in, and if so, executes the specified function. If the user is
     * not signed in, initiates the sign in flow, specifying the post-sign in function to execute.
     *
     * @param requestCode The request code corresponding to the action to perform after sign in.
     */
    private fun fitSignIn(activity: Activity, requestCode: FitActionRequestCode, fitnessOptions: FitnessOptions) {
        if (oAuthPermissionsApproved(activity, fitnessOptions)) {
            //performActionForRequestCode(requestCode)
        } else {
            requestCode.let {
                GoogleSignIn.requestPermissions(
                        activity,
                        requestCode.ordinal,
                        getGoogleAccount(activity, fitnessOptions), fitnessOptions)
            }
        }
    }

    private fun oAuthPermissionsApproved(activity: Activity, fitnessOptions: FitnessOptions) = GoogleSignIn.hasPermissions(getGoogleAccount(activity, fitnessOptions), fitnessOptions)

    /**
     * Gets a Google account for use in creating the Fitness client. context is achieved by either
     * using the last signed-in account, or if necessary, prompting the user to sign in.
     * `getAccountForExtension` is recommended over `getLastSignedInAccount` as the latter can
     * return `null` if there has been no sign in before.
     */
    private fun getGoogleAccount(activity: Activity, fitnessOptions: FitnessOptions) = GoogleSignIn.getAccountForExtension(activity, fitnessOptions)

}