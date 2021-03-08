package com.samsung.android.app.stepdiary.ui.domain.framework

interface HealthListener {

    fun onPermissionFailed()

    fun onPermissionSuccess()

    fun onConnectionSuccess()

    fun onConnectionFailed()
}