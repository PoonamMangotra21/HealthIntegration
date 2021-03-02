package com.samsung.android.app.stepdiary.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.lang.Exception

class NoContentException : Exception(){
    override val message: String?
        get() = "No Content View Attached Exception"
}

abstract class BaseActivity : AppCompatActivity() {


    abstract fun getContentViewId() : Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (getContentViewId() == 0)
            throw NoContentException()
        else
            setContentView(getContentViewId())
    }

}