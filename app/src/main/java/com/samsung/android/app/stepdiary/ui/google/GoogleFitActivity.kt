package com.samsung.android.app.stepdiary.ui.google

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import androidx.lifecycle.Observer
import com.google.android.gms.fitness.data.DataSource
import com.google.android.gms.fitness.data.DataType
import com.google.android.material.snackbar.Snackbar
import com.samsung.android.app.stepdiary.BuildConfig
import com.samsung.android.app.stepdiary.R
import com.samsung.android.app.stepdiary.googlefit.FitActionRequestCode
import com.samsung.android.app.stepdiary.googlefit.GoogleFitConnectHelper
import com.samsung.android.app.stepdiary.ui.base.BaseActivity
import java.util.*
import java.util.concurrent.TimeUnit

class GoogleFitActivity : BaseActivity() {

    private var googleFitConnectHelper : GoogleFitConnectHelper ? = null

    override fun getContentViewId(): Int {
        return R.layout.activity_google_fit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        googleFitConnectHelper = GoogleFitConnectHelper(this)

        // initializeLogging()
        googleFitConnectHelper?.checkPermissionsAndRun(FitActionRequestCode.SUBSCRIBE)
      /*  googleFitConnectHelper?.totalHeartPoints?.observe(this, Observer {
            findViewById<TextView>(R.id.total_heart_rate).text = "$it"
        })*/

        val editText = findViewById<TextView>(R.id.weightET)

        findViewById<TextView>(R.id.addWH).setOnClickListener {
            if (!editText.text.isNullOrEmpty()){
                googleFitConnectHelper?.insertWeight(editText.text.toString().toFloat())
                editText.text = ""
            }
        }

        findViewById<TextView>(R.id.text_heart).setOnClickListener {
            googleFitConnectHelper?.checkPermissionsAndRun(FitActionRequestCode.HEART_RATE)
        }

        findViewById<TextView>(R.id.text_desc).setOnClickListener {
            googleFitConnectHelper?.checkPermissionsAndRun(FitActionRequestCode.STEP_COUNT)
        }

        findViewById<TextView>(R.id.text_height).setOnClickListener {
            googleFitConnectHelper?.checkPermissionsAndRun(FitActionRequestCode.HEIGHT)
        }

        findViewById<TextView>(R.id.text_weight).setOnClickListener {
            googleFitConnectHelper?.checkPermissionsAndRun(FitActionRequestCode.WEIGHT)
        }
    }

    /**
     * Handles the callback from the OAuth sign in flow, executing the post sign in function
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (resultCode) {
            RESULT_OK -> {
                val postSignInAction = FitActionRequestCode.values()[requestCode]
                postSignInAction.let {
                    googleFitConnectHelper?.performActionForRequestCode(postSignInAction)
                }
            }
            else -> googleFitConnectHelper?.oAuthErrorMsg(requestCode, resultCode)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when {
            grantResults.isEmpty() -> {
                // If user interaction was interrupted, the permission request
                // is cancelled and you receive empty arrays.
                Log.i("MainActivity", "User interaction was cancelled.")
            }
            grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                // Permission was granted.
                val fitActionRequestCode = FitActionRequestCode.values()[requestCode]
                fitActionRequestCode.let {
                    googleFitConnectHelper?.fitSignIn(fitActionRequestCode)
                }
            }
            else -> {
                // Permission denied.

                // In this Activity we've chosen to notify the user that they
                // have rejected a core permission for the app since it makes the Activity useless.
                // We're communicating this message in a Snackbar since this is a sample app, but
                // core permissions would typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.

                Snackbar.make(
                        findViewById(R.id.mainL),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings) {
                            // Build intent that displays the App settings screen.
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts("package",
                                    BuildConfig.APPLICATION_ID, null)
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        .show()
            }
        }
    }

}