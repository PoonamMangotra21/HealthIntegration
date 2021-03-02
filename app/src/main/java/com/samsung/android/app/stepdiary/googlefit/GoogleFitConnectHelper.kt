package com.samsung.android.app.stepdiary.googlefit

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.data.HealthDataTypes.AGGREGATE_BLOOD_GLUCOSE_SUMMARY
import com.google.android.gms.fitness.data.HealthDataTypes.TYPE_BLOOD_GLUCOSE
import com.google.android.gms.fitness.request.DataReadRequest


enum class FitActionRequestCode {
    SUBSCRIBE,
    STEP_COUNT,
    HEART_RATE
}

const val TAG = "GoogleFitConnectHelper"

class GoogleFitConnectHelper(private val activity: Activity) {

    var totalSteps: MutableLiveData<Int>? = MutableLiveData()
    var totalHeartPoints: MutableLiveData<Float>? = MutableLiveData()

    private val fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
            .addDataType(DataType.TYPE_HEART_POINTS)
            .build()

    internal fun checkPermissionsAndRun(fitActionRequestCode: FitActionRequestCode) {
        if (permissionApproved()) {
            fitSignIn(fitActionRequestCode)
        } else {
            requestRuntimePermissions(fitActionRequestCode)
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
    internal fun fitSignIn(requestCode: FitActionRequestCode) {

        if (oAuthPermissionsApproved()) {
            performActionForRequestCode(requestCode)
        } else {
            requestCode.let {
                GoogleSignIn.requestPermissions(
                        activity,
                        requestCode.ordinal,
                        getGoogleAccount(), fitnessOptions)
            }
        }
    }

    /**
     * Runs the desired method, based on the specified request code. The request code is typically
     * passed to the Fit sign-in flow, and returned with the success callback. context allows the
     * caller to specify which method, post-sign-in, should be called.
     *
     * @param requestCode The code corresponding to the action to perform.
     */
    internal fun performActionForRequestCode(requestCode: FitActionRequestCode) = when (requestCode) {
        FitActionRequestCode.STEP_COUNT -> readStepCountData()
        FitActionRequestCode.HEART_RATE -> readHeartPoints()
        FitActionRequestCode.SUBSCRIBE -> subscribe()
    }

    internal fun oAuthErrorMsg(requestCode: Int, resultCode: Int) {
        val message = """
            There was an error signing into Fit. Check the troubleshooting section of the README
            for potential issues.
            Request code was: $requestCode
            Result code was: $resultCode
        """.trimIndent()
        Log.e(TAG, message)
    }

    private fun oAuthPermissionsApproved() = GoogleSignIn.hasPermissions(getGoogleAccount(), fitnessOptions)

    /**
     * Gets a Google account for use in creating the Fitness client. context is achieved by either
     * using the last signed-in account, or if necessary, prompting the user to sign in.
     * `getAccountForExtension` is recommended over `getLastSignedInAccount` as the latter can
     * return `null` if there has been no sign in before.
     */
    private fun getGoogleAccount() = GoogleSignIn.getAccountForExtension(activity, fitnessOptions)

    /** Records step data by requesting a subscription to background step data.  */
    private fun subscribe() {
        // To create a subscription, invoke the Recording API. As soon as the subscription is
        // active, fitness data will start recording.
        Fitness.getRecordingClient(activity, getGoogleAccount())
                .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.i(TAG, "Successfully subscribed!")
                    } else {
                        Log.w(TAG, "There was a problem subscribing.", task.exception)
                    }
                }
    }

    /**
     * Reads the current daily step total, computed from midnight of the current day on the device's
     * current timezone.
     */
    private fun readHeartPoints() {
        Fitness.getHistoryClient(activity, getGoogleAccount())
                .readDailyTotal(DataType.TYPE_HEART_POINTS)
                .addOnSuccessListener { dataSet ->
                    val total = when {
                        dataSet.isEmpty -> 0
                        else -> dataSet.dataPoints.first().getValue(Field.FIELD_INTENSITY).toString()
                    }
                    Log.i(TAG, "Heart Points: $total")
                    totalHeartPoints?.postValue(total as Float?)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "There was a problem getting the heart points.", e)
                }
    }

    // create git
    private fun getHeightWeight(){
        Fitness.getHistoryClient(activity, getGoogleAccount())
                .readDailyTotal(DataType.TYPE_WEIGHT) //TYPE_HEIGHT
                .addOnSuccessListener {dataSet->
                    Log.i(TAG, "Total steps: ${dataSet.dataPoints.first().getValue(Field.FIELD_AVERAGE)} ${dataSet.dataPoints.first().getValue(Field.FIELD_MIN)} ${dataSet.dataPoints.first().getValue(Field.FIELD_MAX)}")
                    val total = when {
                        dataSet.isEmpty -> 0
                        else -> dataSet.dataPoints.first().getValue(Field.FIELD_AVERAGE).toString() // if Field.FIELD_WEIGHT     java.lang.IllegalArgumentException: weight(f) not a field of DataType{com.google.weight.summary[average(f), max(f), min(f)]}
                    }
                }.addOnFailureListener {
                    Log.e("dataPointE",it.toString())
                }
    }

    private fun getBloodGlocose(){
      /*  val readRequest = DataReadRequest.Builder()
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .aggregate(TYPE_BLOOD_GLUCOSE, AGGREGATE_BLOOD_GLUCOSE_SUMMARY)
                .bucketByTime(1, TimeUnit.DAYS)
                .build()
*/

    }

    private fun readStepCountData() {
        Fitness.getHistoryClient(activity, getGoogleAccount())
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener { dataSet ->
                    val total = when {
                        dataSet.isEmpty -> 0
                        else -> dataSet.dataPoints.first().getValue(Field.FIELD_STEPS).asInt()
                    }
                    Log.i(TAG, "Total steps: $total")
                    totalSteps?.value= total
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "There was a problem getting the step count.", e)
                }
    }

    private val runningQOrLater =
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    private fun permissionApproved(): Boolean {
        val approved = if (runningQOrLater) {
            PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.ACTIVITY_RECOGNITION)  //Allows an application to recognize physical activity. for apps that need to detect the user's step count or classify the user's physical activity, such as walking, biking, or moving in a vehicle.


        } else {
            true
        }
        return approved
    }

    private fun requestRuntimePermissions(requestCode: FitActionRequestCode) {
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

}