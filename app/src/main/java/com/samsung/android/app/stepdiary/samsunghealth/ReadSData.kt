package com.samsung.android.app.stepdiary.samsunghealth

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.samsung.android.app.stepdiary.StepCountReader
import com.samsung.android.app.stepdiary.util.CalendarHelper
import com.samsung.android.app.stepdiary.util.CalendarHelper.getTodayStartUtcTime
import com.samsung.android.sdk.healthdata.HealthConstants
import com.samsung.android.sdk.healthdata.HealthConstants.StepDailyTrend
import com.samsung.android.sdk.healthdata.HealthDataResolver
import com.samsung.android.sdk.healthdata.HealthDataResolver.ReadRequest
import com.samsung.android.sdk.healthdata.HealthDataStore


object ReadSData {

    fun readStepData(healthDataStore: HealthDataStore) {
        val healthDataResolver: HealthDataResolver = HealthDataResolver(healthDataStore, Handler(Looper.getMainLooper()))

        val filter: HealthDataResolver.Filter = HealthDataResolver.Filter.and(
                HealthDataResolver.Filter.eq(HealthConstants.StepDailyTrend.DAY_TIME, getTodayStartUtcTime()),
                HealthDataResolver.Filter.eq(HealthConstants.StepDailyTrend.SOURCE_TYPE, HealthConstants.StepDailyTrend.SOURCE_TYPE_ALL))
        val request = ReadRequest.Builder()
                .setDataType(StepDailyTrend.HEALTH_DATA_TYPE) // Set the data type
                .setFilter(filter) // Set a filter
                .build()
        try {
            healthDataResolver.read(request).setResultListener {
                it.iterator().forEach { data ->
                    val dayTime = data.getLong(StepDailyTrend.DAY_TIME)
                    val totalCount = data.getInt(StepDailyTrend.COUNT)
                    Log.e("ReadSData", "dayTime : $dayTime totalCount : $totalCount")
                }
            }
        } catch (e: Exception) {
            Log.e("ReadSData", "$e")
        }

    }

    fun readBloodGlucose(healthDataStore: HealthDataStore, healthDataType: String, dataType: String) {
        val healthDataResolver: HealthDataResolver = HealthDataResolver(healthDataStore, Handler(Looper.getMainLooper()))
        val request = ReadRequest.Builder()
                .setDataType(healthDataType)
                .setLocalTimeRange(HealthConstants.BloodGlucose.START_TIME, HealthConstants.BloodGlucose.TIME_OFFSET,
                        CalendarHelper.getStartTime(), CalendarHelper.getEndTime())
                .build()
        try {
            healthDataResolver.read(request).setResultListener {
                it.iterator().forEach { data ->
                    val totalCount = data.getFloat(dataType)
                    Log.e("ReadSData", "Glucose totalCount : $totalCount")
                }
            }
        } catch (e: Exception) {
            Log.e("ReadSData", "$e")
        }

    }

    fun requestDailyStepCount(startTime: Long, healthDataResolver: HealthDataResolver) {
        if (startTime >= StepCountReader.TODAY_START_UTC_TIME) {
            // Get today step count
            //  readStepCount(startTime,healthDataResolver)
        } else {
            // Get historical step count
            //readStepDailyTrend(startTime,healthDataResolver)
        }
    }

    /* private fun readStepCount(startTime: Long,healthDataResolver: HealthDataResolver) {
         // Get sum of step counts by device
         val request = HealthDataResolver.AggregateRequest.Builder()
                 .setDataType(HealthConstants.StepCount.HEALTH_DATA_TYPE)
                 .addFunction(HealthDataResolver.AggregateRequest.AggregateFunction.SUM, HealthConstants.StepCount.COUNT, StepCountReader.ALIAS_TOTAL_COUNT)
                 .addGroup(HealthConstants.StepCount.DEVICE_UUID, StepCountReader.ALIAS_DEVICE_UUID)
                 .setLocalTimeRange(HealthConstants.StepCount.START_TIME, HealthConstants.StepCount.TIME_OFFSET, startTime, startTime + StepCountReader.TIME_INTERVAL)
                 .setSort(StepCountReader.ALIAS_TOTAL_COUNT, HealthDataResolver.SortOrder.DESC)
                 .build()

         runCatching { healthDataResolver.aggregate(request) }
                 .onFailure { Log.e(StepDiaryActivity.TAG, "Getting step count fails.", it) }
                 .getOrNull()
                 ?.setResultListener {
                     it.use {
                         it.firstOrNull()
                                 .also { observer.onChanged(it?.getInt(StepCountReader.ALIAS_TOTAL_COUNT) ?: 0) }
                                 ?.let { readStepCountBinning(startTime, it.getString(StepCountReader.ALIAS_DEVICE_UUID)) } // for binning List
                                 ?: observer.onBinningDataChanged(emptyList())
                     }
                 }
     }

     private fun readStepDailyTrend(dayStartTime: Long,healthDataResolver: HealthDataResolver) {
         val request = HealthDataResolver.ReadRequest.Builder()
                 .setDataType(HealthConstants.StepDailyTrend.HEALTH_DATA_TYPE)
                 .setProperties(arrayOf(HealthConstants.StepDailyTrend.COUNT, HealthConstants.StepDailyTrend.BINNING_DATA))
                 .setFilter(HealthDataResolver.Filter.and(
                         HealthDataResolver.Filter.eq(HealthConstants.StepDailyTrend.DAY_TIME, dayStartTime),
                         HealthDataResolver.Filter.eq(HealthConstants.StepDailyTrend.SOURCE_TYPE, HealthConstants.StepDailyTrend.SOURCE_TYPE_ALL)))
                 .build()

         runCatching { healthDataResolver.read(request) }
                 .onFailure { Log.e(StepDiaryActivity.TAG, "Getting daily step trend fails.", it) }
                 //    java.lang.SecurityException: com.samsung.android.app.stepdiary does not match with registered signature. B4:48:B7:30:99
                 .getOrNull()
                 ?.setResultListener {
                     it.use {
                         it.firstOrNull().also {
                           //  observer.onChanged(it?.getInt(HealthConstants.StepDailyTrend.COUNT) ?: 0)
                           //  observer.onBinningDataChanged(
                                     it?.getBlob(HealthConstants.StepDailyTrend.BINNING_DATA)?.let { getBinningData(it) } ?: emptyList())
                         }
                     }
                 }
     }


     private fun getBinningData(zip: ByteArray): List<StepBinningData> {
         // decompress ZIP
         val binningDataList = HealthDataUtil.getStructuredDataList(zip, StepBinningData::class.java)
         return binningDataList.asSequence()
                 .withIndex()
                 .filter { it.value.count != 0 }
                 .onEach { it.value.time = String.format(Locale.US, "%02d:%02d", it.index / 6, it.index % 6 * 10) }
                 .map { it.value }
                 .toList()
     }

     private fun readStepCountBinning(startTime: Long, deviceUuid: String,healthDataResolver: HealthDataResolver) {

         // Get 10 minute binning data of a particular device
         val request = HealthDataResolver.AggregateRequest.Builder()
                 .setDataType(HealthConstants.StepCount.HEALTH_DATA_TYPE)
                 .addFunction(HealthDataResolver.AggregateRequest.AggregateFunction.SUM, HealthConstants.StepCount.COUNT, StepCountReader.ALIAS_TOTAL_COUNT)
                 .setTimeGroup(HealthDataResolver.AggregateRequest.TimeGroupUnit.MINUTELY, 10, HealthConstants.StepCount.START_TIME, HealthConstants.StepCount.TIME_OFFSET, StepCountReader.ALIAS_BINNING_TIME)
                 .setLocalTimeRange(HealthConstants.StepCount.START_TIME, HealthConstants.StepCount.TIME_OFFSET, startTime, startTime + StepCountReader.TIME_INTERVAL)
                 .setFilter(HealthDataResolver.Filter.eq(HealthConstants.StepCount.DEVICE_UUID, deviceUuid))
                 .setSort(StepCountReader.ALIAS_BINNING_TIME, HealthDataResolver.SortOrder.ASC)
                 .build()

         runCatching { healthDataResolver.aggregate(request) }
                 .onFailure { Log.e(StepDiaryActivity.TAG, "Getting step binning data fails.", it) }
                 .getOrNull()
                 ?.setResultListener {
                     it.use {
                         it.asSequence()
                                 .map { it.getString(StepCountReader.ALIAS_BINNING_TIME) to it.getInt(StepCountReader.ALIAS_TOTAL_COUNT) }
                                 .filter { it.first != null }
                                 .map { StepBinningData(it.first.split(" ")[1], it.second) }
                                 .toList()
                     }
                             .also { observer.onBinningDataChanged(it) }
                 }
     }*/

}