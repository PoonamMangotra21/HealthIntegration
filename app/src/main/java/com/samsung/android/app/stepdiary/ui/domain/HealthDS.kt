package com.samsung.android.app.stepdiary.ui.domain

interface HealthDS {

    fun connectService()

    fun authenticateService()

    fun isPermissionGranted()

    fun askPermission()

}