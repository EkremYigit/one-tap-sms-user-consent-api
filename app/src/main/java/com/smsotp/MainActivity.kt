package com.smsotp

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    lateinit var smsBroadcastReceiver: SmsBroadcastReceiver
    lateinit var textView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView =findViewById(R.id.textView)
        startSmsUserConsent()
    }

    private fun registerToSmsBroadcastReceiver() {
        smsBroadcastReceiver = SmsBroadcastReceiver().also {
            it.setSmsListenerInstance(object  : SmsBroadcastReceiver.SmsBroadcastReceiverListener {
                override fun onSuccess(intent: Intent?) {
                    intent?.let {intent2 ->  smsOtpResult.launch(intent2) }
                }
                override fun onFailure() {
                }
            })
        }
        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        this.registerReceiver(smsBroadcastReceiver,intentFilter,SmsRetriever.SEND_PERMISSION, null)
    }

    private fun startSmsUserConsent() {
        SmsRetriever.getClient(applicationContext).also {
            it.startSmsUserConsent(null)
                .addOnSuccessListener {
                    registerToSmsBroadcastReceiver()
                }
                .addOnFailureListener {ex->
                    Snackbar.make(textView,ex.message?:"Error while listening.",Snackbar.LENGTH_LONG).show()
                }
        }
    }

    private val smsOtpResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val message = result.data?.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                val code = message?.let { fetchVerificationCode(it) }
                if (code != null) {
                    Snackbar.make(textView,code,Snackbar.LENGTH_LONG).show()
                    startSmsUserConsent() // to fire api again to retrieve next message.
                }
            }
        }

    private fun fetchVerificationCode(message: String): String {
        // 6 -> is OTP's length, it can be change by your usage.
        return Regex("(\\d{6})").find(message)?.value ?: ""
    }
}