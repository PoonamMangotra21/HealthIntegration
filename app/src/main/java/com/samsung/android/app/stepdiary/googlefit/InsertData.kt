package com.samsung.android.app.stepdiary.googlefit

import android.app.Activity
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.*
import com.samsung.android.app.stepdiary.util.CalendarHelper
import java.util.concurrent.TimeUnit

object InsertData {

    fun insertHeightWeight(activity:Activity,googleAccount:GoogleSignInAccount, dataType: DataType,value: Float){
        val dataSource = getValueSource(activity,dataType)

        val dataPoint = getValueDataPoint(dataSource,value)

        writeToGoogleGit(activity,googleAccount,dataSource,dataPoint)
    }

    fun  insertStepsCount(activity:Activity,googleAccount:GoogleSignInAccount, dataType: DataType,value: Int){
        val dataSource = getDataSource(activity,dataType)

        val dataPoint = getStepsDataPoint(dataSource,value)

        writeToGoogleGit(activity,googleAccount,dataSource,dataPoint)
    }

    private fun writeToGoogleGit(activity: Activity, googleAccount: GoogleSignInAccount, dataSource: DataSource, dataPoint: DataPoint) {
        val dataSet = DataSet.builder(dataSource)
                .add(dataPoint)
                .build()

        Fitness.getHistoryClient(activity, googleAccount)
                .insertData(dataSet)
                .addOnSuccessListener {
                    Log.e(TAG, "written successfully")
                }
                .addOnFailureListener {
                    Log.e(TAG, "Exc Reading: $it")
                }
    }

    private fun getValueSource(activity: Activity, dataType: DataType) : DataSource{
        return DataSource.Builder()
                .setAppPackageName(activity)
                .setDataType(dataType)
                .setType(DataSource.TYPE_RAW)
                .build()
    }

    private fun getDataSource(activity: Activity, dataType: DataType) : DataSource{
       return DataSource.Builder()
                .setAppPackageName(activity)
                .setDataType(dataType)
                .setStreamName("$TAG - step count")
                .setType(DataSource.TYPE_RAW)
                .build()
    }

    private fun getValueDataPoint(dataSource : DataSource, value: Float) : DataPoint{
        val startTime: Long = CalendarHelper.getStartTime()
        return DataPoint.builder(dataSource)
                .setTimeInterval(startTime, startTime, TimeUnit.MILLISECONDS)
                .setFloatValues(value)
                .build()
    }

    private fun getStepsDataPoint(dataSource : DataSource, value: Int) : DataPoint{
        val startTime: Long = CalendarHelper.getStartTime()
        val endTime: Long = CalendarHelper.getEndTime()
        return DataPoint.builder(dataSource)
                .setField(Field.FIELD_STEPS, value)
                .setTimeInterval(startTime, endTime, TimeUnit.SECONDS)
                .build()
    }
}