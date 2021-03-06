package com.samsung.android.app.stepdiary.googlefit

import android.app.Activity
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.*
import com.google.android.gms.fitness.data.Field.FIELD_MEAL_TYPE
import com.google.android.gms.fitness.data.Field.MEAL_TYPE_BREAKFAST
import com.google.android.gms.fitness.data.HealthDataTypes.TYPE_BLOOD_GLUCOSE
import com.google.android.gms.fitness.data.HealthFields.*
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.result.DataReadResponse
import com.samsung.android.app.stepdiary.util.CalendarHelper
import java.text.DateFormat
import java.text.DateFormat.getTimeInstance
import java.util.concurrent.TimeUnit


enum class FitActionRequestCode {
    SUBSCRIBE,
    STEP_COUNT,
    HEART_RATE,
    WEIGHT,
    HEIGHT,
    GLUCOSE
}

/*
* Manifest Permission Approval  (activity)
*
*
* */
const val TAG = "GoogleFitConnectHelper"

class GoogleFitConnectHelper(private val activity: Activity, private var fitnessOptions: FitnessOptions) {

    internal fun checkPermissionsAndRun(fitActionRequestCode: FitActionRequestCode) {
        GoogleFitAuth.checkPermissionsAndRun(activity, fitActionRequestCode, fitnessOptions)
    }

    /**
     * Runs the desired method, based on the specified request code. The request code is typically
     * passed to the Fit sign-in flow, and returned with the success callback. context allows the
     * caller to specify which method, post-sign-in, should be called.
     *
     * @param requestCode The code corresponding to the action to perform.
     */
    private fun performActionForRequestCode(requestCode: FitActionRequestCode) = when (requestCode) {
        FitActionRequestCode.STEP_COUNT -> readStepCountData()
        FitActionRequestCode.HEART_RATE -> readHeartPoints()
        FitActionRequestCode.HEIGHT -> readHeight()
        FitActionRequestCode.WEIGHT -> readWeight()
        FitActionRequestCode.GLUCOSE -> readBloodGlucose()
        FitActionRequestCode.SUBSCRIBE -> subscribe()
    }

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

    private fun readStepCountData() {
        ReadData.accessData(activity, getGoogleAccount(), DataType.TYPE_STEP_COUNT_DELTA, Field.FIELD_STEPS).toInt()
    }

    private fun readHeartPoints() {
        ReadData.accessData(activity, getGoogleAccount(), DataType.TYPE_HEART_POINTS, Field.FIELD_INTENSITY).toFloat()
    }

    private fun readWeight() {
        ReadData.accessData(activity, getGoogleAccount(), DataType.TYPE_WEIGHT, Field.FIELD_MIN).toFloat()
        ReadData.accessData(activity, getGoogleAccount(), DataType.TYPE_WEIGHT, Field.FIELD_AVERAGE).toFloat()
        ReadData.accessData(activity, getGoogleAccount(), DataType.TYPE_WEIGHT, Field.FIELD_MAX).toFloat()
    }

    private fun readHeight() {
        ReadData.accessData(activity, getGoogleAccount(), DataType.TYPE_HEIGHT, Field.FIELD_MIN).toFloat()
        ReadData.accessData(activity, getGoogleAccount(), DataType.TYPE_HEIGHT, Field.FIELD_AVERAGE).toFloat()
        ReadData.accessData(activity, getGoogleAccount(), DataType.TYPE_HEIGHT, Field.FIELD_MAX).toFloat()
    }

    fun insertWeight(value: Float) {
        InsertData.insertHeightWeight(activity, getGoogleAccount(), DataType.TYPE_WEIGHT, value)
    }

    fun insertHeight(value: Float) {
        InsertData.insertHeightWeight(activity, getGoogleAccount(), DataType.TYPE_HEIGHT, value)
    }

    fun insertStep(value: Int) {
        InsertData.insertSteps(activity, getGoogleAccount(), DataType.TYPE_STEP_COUNT_DELTA, Field.FIELD_STEPS, value)
    }

    fun insertHeartPoint(value: Float) {
        InsertData.insertHP(activity, getGoogleAccount(), DataType.TYPE_HEART_POINTS, Field.FIELD_INTENSITY, value) // float value required
    }

    fun writeGlucose() {
        val startTime: Long = CalendarHelper.getStartTime()
        val dataSource: DataSource = DataSource.Builder()
                .setAppPackageName(activity)
                .setDataType(TYPE_BLOOD_GLUCOSE)
                .setType(DataSource.TYPE_RAW)
                .build()
        val bloodGlucose = DataPoint.builder(dataSource)
                .setTimestamp(startTime, TimeUnit.MILLISECONDS)
                .setField(FIELD_BLOOD_GLUCOSE_LEVEL, 5.0f) // 90 mg/dL
                .setField(FIELD_TEMPORAL_RELATION_TO_MEAL, FIELD_TEMPORAL_RELATION_TO_MEAL_BEFORE_MEAL)
                .setField(FIELD_MEAL_TYPE, MEAL_TYPE_BREAKFAST)
                .setField(FIELD_TEMPORAL_RELATION_TO_SLEEP, TEMPORAL_RELATION_TO_SLEEP_ON_WAKING)
                .setField(FIELD_BLOOD_GLUCOSE_SPECIMEN_SOURCE, BLOOD_GLUCOSE_SPECIMEN_SOURCE_CAPILLARY_BLOOD)
                .build()
        val dataSet = DataSet.builder(dataSource)
                .add(bloodGlucose)
                .build()
        Fitness.getHistoryClient(activity, getGoogleAccount())
                .insertData(dataSet)
                .addOnSuccessListener {
                    Log.e(TAG, "written successfully")
                }
                .addOnFailureListener {
                    Log.e(TAG, "Exc Reading: $it")
                }
    }

    // create git
    private fun readBloodGlucose() {
        val startTime = CalendarHelper.getStartTime()
        val endTime: Long = CalendarHelper.getEndTime()
        val dataSource: DataSource = DataSource.Builder()
                .setAppPackageName(activity)
                .setDataType(TYPE_BLOOD_GLUCOSE)
                .setType(DataSource.TYPE_RAW)
                .build()
        val readRequest = DataReadRequest.Builder() //.aggregate(DataType.TYPE_HEART_RATE_BPM, DataType.AGGREGATE_HEART_RATE_SUMMARY)
                //   .aggregate(HealthDataTypes.TYPE_BLOOD_GLUCOSE, HealthDataTypes.AGGREGATE_BLOOD_GLUCOSE_SUMMARY)
                .aggregate(dataSource)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()

        Fitness.getHistoryClient(activity, getGoogleAccount())
                .readData(readRequest)
                .addOnSuccessListener {
                    printData(it)
                }
                .addOnFailureListener {
                    Log.e("getBloodGlucose", it.toString())

                }
    }

    fun printData(dataReadResult: DataReadResponse) {
        // If the DataReadRequest object specified aggregated data, dataReadResult will be returned
        // as buckets containing DataSets, instead of just DataSets.
        Log.v(TAG, "Number of returned buckets of DataSets is: " + dataReadResult.buckets.size)
        if (dataReadResult.buckets.size > 0) {
            for (bucket in dataReadResult.buckets) {
                val dataSets = bucket.dataSets
                Log.e(TAG, "Datasets: $dataSets")
                for (dataSet in dataSets) {
                    dumpDataSet(dataSet)
                }
            }
        } else if (dataReadResult.dataSets.size > 0) {
            print("Number of returned DataSets is: " + dataReadResult.dataSets.size)
            for (dataSet in dataReadResult.dataSets) {
                dumpDataSet(dataSet)
            }
        }
    }

    // [START parse_dataset]
    private fun dumpDataSet(dataSet: DataSet) {
        Log.v(TAG, "Name: " + dataSet.dataType.name)
        Log.v(TAG, "Fields: " + dataSet.dataSource.dataType.fields.size)
        Log.v(TAG, "Data Point Values :" + dataSet.dataPoints)
        val dateFormat: DateFormat = getTimeInstance()
        for (dp in dataSet.dataPoints) {
            Log.v(TAG, "Data Point:")
            Log.v(TAG, "Type: " + dataSet.dataType.name)
            Log.v(TAG, "Start: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)))
            Log.v(TAG, "End: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)))
            for (field in dp.dataType.fields) {
                Log.v(TAG, "Field: " + field.name.toString() + ", Value : " + dp.getValue(field))
            }
        }
    }

}