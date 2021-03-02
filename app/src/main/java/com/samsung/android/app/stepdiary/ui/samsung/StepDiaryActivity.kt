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
package com.samsung.android.app.stepdiary.ui.samsung

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.samsung.android.app.stepdiary.R
import com.samsung.android.app.stepdiary.StepBinningData
import com.samsung.android.app.stepdiary.StepCountObserver
import com.samsung.android.app.stepdiary.StepCountReader
import com.samsung.android.app.stepdiary.databinding.ActivitySamsungHealthBinding
import com.samsung.android.app.stepdiary.databinding.StepBinningListItemBinding
import com.samsung.android.sdk.healthdata.HealthConnectionErrorResult
import com.samsung.android.sdk.healthdata.HealthConstants.StepCount
import com.samsung.android.sdk.healthdata.HealthConstants.StepDailyTrend
import com.samsung.android.sdk.healthdata.HealthDataStore
import com.samsung.android.sdk.healthdata.HealthPermissionManager
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionKey
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionResult
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionType
import com.samsung.android.sdk.healthdata.HealthResultHolder
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class StepDiaryActivity : Activity() {

    private lateinit var binding: ActivitySamsungHealthBinding

    private val binningListAdapter: BinningListAdapter by lazy { BinningListAdapter() }

    // Create a HealthDataStore instance and set its listener
    private val healthDataStore: HealthDataStore by lazy { HealthDataStore(this, connectionListener) }

    private val stepCountReader: StepCountReader by lazy { StepCountReader(healthDataStore, stepCountObserver) }

    private var currentStartTime: Long = 0

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_samsung_health)
        // Get the start time of today in local
        currentStartTime = StepCountReader.TODAY_START_UTC_TIME
        binding.dateView.text = formattedTime(currentStartTime)
        binding.moveNext.setOnClickListener { onClickNextButton() }
        binding.moveBefore.setOnClickListener { onClickBeforeButton() }
        binding.binningList.adapter = binningListAdapter
        // Request the connection to the health data store
        healthDataStore.connectService()
    }

    public override fun onDestroy() {
        super.onDestroy()
        healthDataStore.disconnectService()
    }

    public override fun onResume() {
        super.onResume()
        stepCountReader.requestDailyStepCount(currentStartTime)
    }

    private fun onClickBeforeButton() {
        currentStartTime -= StepCountReader.TIME_INTERVAL
        binding.dateView.text = formattedTime(currentStartTime)
        stepCountReader.requestDailyStepCount(currentStartTime)
    }

    private fun onClickNextButton() {
        currentStartTime += StepCountReader.TIME_INTERVAL
        binding.dateView.text = formattedTime(currentStartTime)
        stepCountReader.requestDailyStepCount(currentStartTime)
    }

    private val connectionListener: HealthDataStore.ConnectionListener = object : HealthDataStore.ConnectionListener {
        override fun onConnected() {
            Log.d(TAG, "onConnected")
            if (checkPermissionsAcquired()) {
                stepCountReader.requestDailyStepCount(currentStartTime)
            } else {
                requestPermission()
            }
        }

        override fun onConnectionFailed(error: HealthConnectionErrorResult) {
            Log.d(TAG, "onConnectionFailed")
            showConnectionFailureDialog(error)
        }

        override fun onDisconnected() {
            Log.d(TAG, "onDisconnected")
            if (!isFinishing) {
                healthDataStore.connectService()
            }
        }
    }

    private fun formattedTime(time: Long): String {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd (E)", Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat.format(time)
    }

    private val stepCountObserver: StepCountObserver = object : StepCountObserver {
        override fun onChanged(count: Int) = updateTotalStepCountView(count.toString())

        override fun onBinningDataChanged(binningCountList: List<StepBinningData>) = updateBinningListView(binningCountList)
    }

    private fun updateTotalStepCountView(count: String) =
        runOnUiThread { binding.totalStepCount.text = count }

    private fun updateBinningListView(stepBinningDataList: List<StepBinningData>) {
        // the following code will be replaced with chart drawing code
        Log.d(TAG, "updateBinningChartView")
        stepBinningDataList.forEach { Log.d(TAG, "TIME : ${it.time}  COUNT : ${it.count}") }
        binningListAdapter.submitList(stepBinningDataList)
    }

    // Check whether the permissions that this application needs are acquired
    private fun checkPermissionsAcquired(): Boolean {
        val pmsManager = HealthPermissionManager(healthDataStore)
        pmsManager.isPermissionAcquired(permissionKeySet)
        // Check whether the permissions that this application needs are acquired
        return runCatching { pmsManager.isPermissionAcquired(permissionKeySet) }
            .onFailure { Log.e(TAG, "Permission request fails.", it) }
            .map { it.values.all { it } }
            .getOrDefault(false)
    }

    private fun requestPermission() {
        val pmsManager = HealthPermissionManager(healthDataStore)

        // Show user permission UI for allowing user to change options
        runCatching { pmsManager.requestPermissions(permissionKeySet, this) }
            .onFailure { Log.e(TAG, "Permission setting fails.", it) }
            .getOrNull()
            ?.setResultListener(mPermissionListener)
    }

    private val permissionKeySet: Set<PermissionKey> =
        setOf(
            PermissionKey(StepCount.HEALTH_DATA_TYPE, PermissionType.READ),
            PermissionKey(StepDailyTrend.HEALTH_DATA_TYPE, PermissionType.READ))

    private val mPermissionListener = HealthResultHolder.ResultListener<PermissionResult> { result ->
        // Show a permission alarm and clear step count if permissions are not acquired
        if (result.resultMap.values.any { !it }) {
            updateTotalStepCountView("")
            updateBinningListView(emptyList())
            showPermissionAlarmDialog()
        } else {
            // Get the daily step count of a particular day and display it
            stepCountReader.requestDailyStepCount(currentStartTime)
        }
    }

    private fun showPermissionAlarmDialog() {
        if (isFinishing) {
            return
        }
        AlertDialog.Builder(this@StepDiaryActivity)
            .setTitle(R.string.notice)
            .setMessage(R.string.msg_perm_acquired)
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    private fun showConnectionFailureDialog(error: HealthConnectionErrorResult) {
        if (isFinishing) {
            return
        }
        val alert = AlertDialog.Builder(this)
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
                error.resolve(this@StepDiaryActivity)
            }
        }
        if (error.hasResolution()) {
            alert.setNegativeButton(R.string.cancel, null)
        }
        alert.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.connect) {
            requestPermission()
        }
        return true
    }

    companion object {
        const val TAG = "StepDiary"

        private class BinningListAdapter : ListAdapter<StepBinningData, BinningListViewHolder>(
            object : DiffUtil.ItemCallback<StepBinningData>() {
                override fun areItemsTheSame(oldItem: StepBinningData, newItem: StepBinningData): Boolean =
                    oldItem === newItem

                override fun areContentsTheSame(oldItem: StepBinningData, newItem: StepBinningData): Boolean =
                    oldItem.time == newItem.time && oldItem.count == newItem.count

            }) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BinningListViewHolder =
                    BinningListViewHolder(StepBinningListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

            override fun onBindViewHolder(holder: BinningListViewHolder, position: Int) {
                holder.binding.data = getItem(position)
            }
        }

        private class BinningListViewHolder(val binding: StepBinningListItemBinding) : RecyclerView.ViewHolder(binding.root)
    }
}