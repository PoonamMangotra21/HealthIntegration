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

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
import com.samsung.android.app.stepdiary.ui.base.BaseViewModelFactory
import com.samsung.android.app.stepdiary.ui.domain.data.HealthRepoImplementation
import com.samsung.android.app.stepdiary.ui.domain.framework.SamSungHealthImplementation
import com.samsung.android.app.stepdiary.util.CalendarHelper

class StepDiaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySamsungHealthBinding

    private val binningListAdapter: BinningListAdapter by lazy { BinningListAdapter() }

    private val stepDiaryViewModel by viewModels<StepDiaryViewModel> {
        BaseViewModelFactory {
            StepDiaryViewModel(HealthRepoImplementation(SamSungHealthImplementation()))
        }
    }

    private var currentStartTime: Long = 0

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_samsung_health)
        // Get the start time of today in local
        currentStartTime = StepCountReader.TODAY_START_UTC_TIME
        binding.dateView.text = CalendarHelper.formattedTime(currentStartTime)
        binding.moveNext.setOnClickListener { onClickNextButton() }
        binding.moveBefore.setOnClickListener { onClickBeforeButton() }
        binding.binningList.adapter = binningListAdapter

        stepDiaryViewModel.setUp(this)
    }

    public override fun onDestroy() {
        super.onDestroy()
        //  sHealthConnectionHelper?.disconnectSHealth()
    }

    public override fun onResume() {
        super.onResume()
        //  stepCountReader.requestDailyStepCount(currentStartTime)
        //  sHealthConnectionHelper?.readStepCount()
    }

    private fun onClickBeforeButton() {
        currentStartTime -= StepCountReader.TIME_INTERVAL
        binding.dateView.text = CalendarHelper.formattedTime(currentStartTime)
        //  sHealthConnectionHelper?.readWeight()
        //  stepCountReader.requestDailyStepCount(currentStartTime)
    }

    private fun onClickNextButton() {
        currentStartTime += StepCountReader.TIME_INTERVAL
        binding.dateView.text = CalendarHelper.formattedTime(currentStartTime)
        // sHealthConnectionHelper?.readHeight()
        //  stepCountReader.requestDailyStepCount(currentStartTime)
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