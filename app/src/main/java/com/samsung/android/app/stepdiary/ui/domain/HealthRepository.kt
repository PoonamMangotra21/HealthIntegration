package com.samsung.android.app.stepdiary.ui.domain

class HealthRepository(private val healthDS: HealthDS) {

    fun authenticationApp(){
        healthDS.authenticateService()
    }


}