package com.samsung.android.app.stepdiary.googlefit

import android.app.Activity
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field

object ReadData {

    fun accessData(activity:Activity,account : GoogleSignInAccount, dataType: DataType, field: Field): String {
        var data = "0"
        Fitness.getHistoryClient(activity, account)
                .readDailyTotal(dataType)
                .addOnSuccessListener { dataSet ->
                    val total = when {
                        dataSet.isEmpty -> "0"
                        else -> dataSet.dataPoints.first().getValue(field).toString()
                    }
                    Log.i(TAG, "Data: $total")
                    data = total
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "There was a problem getting details", e)
                }
        return data
    }
}