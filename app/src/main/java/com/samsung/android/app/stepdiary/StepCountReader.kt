/*
 * Copyright (C) 2014 Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Mobile Communication Division,
 * Digital Media & Communications Business, Samsung Electronics Co., Ltd.
 *
 * This software and its documentation are confidential and proprietary
 * information of Samsung Electronics Co., Ltd.  No part of the software and
 * documents may be copied, reproduced, transmitted, translated, or reduced to
 * any electronic medium or machine-readable form without the prior written
 * consent of Samsung Electronics.
 *
 * Samsung Electronics makes no representations with respect to the contents,
 * and assumes no responsibility for any errors that might appear in the
 * software and documents. This publication and the contents hereof are subject
 * to change without notice.
 */
package com.samsung.android.app.stepdiary

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.samsung.android.app.stepdiary.ui.samsung.StepDiaryActivity.Companion.TAG
import com.samsung.android.sdk.healthdata.HealthConstants.StepCount
import com.samsung.android.sdk.healthdata.HealthConstants.StepDailyTrend
import com.samsung.android.sdk.healthdata.HealthDataResolver
import com.samsung.android.sdk.healthdata.HealthDataResolver.AggregateRequest
import com.samsung.android.sdk.healthdata.HealthDataResolver.AggregateRequest.AggregateFunction
import com.samsung.android.sdk.healthdata.HealthDataResolver.AggregateRequest.TimeGroupUnit
import com.samsung.android.sdk.healthdata.HealthDataResolver.Filter
import com.samsung.android.sdk.healthdata.HealthDataResolver.ReadRequest
import com.samsung.android.sdk.healthdata.HealthDataResolver.SortOrder
import com.samsung.android.sdk.healthdata.HealthDataStore
import com.samsung.android.sdk.healthdata.HealthDataUtil
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class StepCountReader(
    store: HealthDataStore,
    private val observer: StepCountObserver
) {
    private val healthDataResolver: HealthDataResolver = HealthDataResolver(store, Handler(Looper.getMainLooper()))

    // Get the daily total step count of a specified day
    fun requestDailyStepCount(startTime: Long) {
        if (startTime >= TODAY_START_UTC_TIME) {
            // Get today step count
            readStepCount(startTime)
        } else {
            // Get historical step count
            readStepDailyTrend(startTime)
        }
    }

    private fun readStepCount(startTime: Long) {
        // Get sum of step counts by device
        val request = AggregateRequest.Builder()
            .setDataType(StepCount.HEALTH_DATA_TYPE)
            .addFunction(AggregateFunction.SUM, StepCount.COUNT, ALIAS_TOTAL_COUNT)
            .addGroup(StepCount.DEVICE_UUID, ALIAS_DEVICE_UUID)
            .setLocalTimeRange(StepCount.START_TIME, StepCount.TIME_OFFSET, startTime, startTime + TIME_INTERVAL)
            .setSort(ALIAS_TOTAL_COUNT, SortOrder.DESC)
            .build()

        runCatching { healthDataResolver.aggregate(request) }
            .onFailure { Log.e(TAG, "Getting step count fails.", it) }
            .getOrNull()
            ?.setResultListener {
                it.use {
                    it.firstOrNull()
                        .also { observer.onChanged(it?.getInt(ALIAS_TOTAL_COUNT) ?: 0) }
                        ?.let { readStepCountBinning(startTime, it.getString(ALIAS_DEVICE_UUID)) }
                        ?: observer.onBinningDataChanged(emptyList())
                }
            }
    }

    private fun readStepDailyTrend(dayStartTime: Long) {
        val request = ReadRequest.Builder()
            .setDataType(StepDailyTrend.HEALTH_DATA_TYPE)
            .setProperties(arrayOf(StepDailyTrend.COUNT, StepDailyTrend.BINNING_DATA))
            .setFilter(Filter.and(
                Filter.eq(StepDailyTrend.DAY_TIME, dayStartTime),
                Filter.eq(StepDailyTrend.SOURCE_TYPE, StepDailyTrend.SOURCE_TYPE_ALL)))
            .build()

        runCatching { healthDataResolver.read(request) }
            .onFailure { Log.e(TAG, "Getting daily step trend fails.", it) }
                //    java.lang.SecurityException: com.samsung.android.app.stepdiary does not match with registered signature. B4:48:B7:30:99
            .getOrNull()
            ?.setResultListener {
                it.use {
                    it.firstOrNull().also {
                        observer.onChanged(it?.getInt(StepDailyTrend.COUNT) ?: 0)
                        observer.onBinningDataChanged(
                            it?.getBlob(StepDailyTrend.BINNING_DATA)?.let { getBinningData(it) } ?: emptyList())
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

    private fun readStepCountBinning(startTime: Long, deviceUuid: String) {

        // Get 10 minute binning data of a particular device
        val request = AggregateRequest.Builder()
            .setDataType(StepCount.HEALTH_DATA_TYPE)
            .addFunction(AggregateFunction.SUM, StepCount.COUNT, ALIAS_TOTAL_COUNT)
            .setTimeGroup(TimeGroupUnit.MINUTELY, 10, StepCount.START_TIME, StepCount.TIME_OFFSET, ALIAS_BINNING_TIME)
            .setLocalTimeRange(StepCount.START_TIME, StepCount.TIME_OFFSET, startTime, startTime + TIME_INTERVAL)
            .setFilter(Filter.eq(StepCount.DEVICE_UUID, deviceUuid))
            .setSort(ALIAS_BINNING_TIME, SortOrder.ASC)
            .build()

        runCatching { healthDataResolver.aggregate(request) }
            .onFailure { Log.e(TAG, "Getting step binning data fails.", it) }
            .getOrNull()
            ?.setResultListener {
                it.use {
                    it.asSequence()
                        .map { it.getString(ALIAS_BINNING_TIME) to it.getInt(ALIAS_TOTAL_COUNT) }
                        .filter { it.first != null }
                        .map { StepBinningData(it.first.split(" ")[1], it.second) }
                        .toList()
                }
                    .also { observer.onBinningDataChanged(it) }
            }
    }

    companion object {
        val TODAY_START_UTC_TIME = todayStartUtcTime
        val TIME_INTERVAL = TimeUnit.DAYS.toMillis(1)
        private const val ALIAS_TOTAL_COUNT = "count"
        private const val ALIAS_DEVICE_UUID = "deviceuuid"
        private const val ALIAS_BINNING_TIME = "binning_time"

        private val todayStartUtcTime: Long
            get() {
                val today = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                today[Calendar.HOUR_OF_DAY] = 0
                today[Calendar.MINUTE] = 0
                today[Calendar.SECOND] = 0
                today[Calendar.MILLISECOND] = 0
                return today.timeInMillis
            }
    }
}
