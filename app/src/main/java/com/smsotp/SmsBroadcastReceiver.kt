package com.smsotp
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

class SmsBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        if (intent?.action == SmsRetriever.SMS_RETRIEVED_ACTION) {

            val extras = intent.extras
            val smsRetrieverStatus = extras?.get(SmsRetriever.EXTRA_STATUS) as Status

            when (smsRetrieverStatus.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    extras.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT).also {
                        smsListener?.onSuccess(it)
                    }
                }

                CommonStatusCodes.TIMEOUT -> {
                    smsListener?.onFailure()
                }
            }
        }
    }

    companion object {
        private var smsListener: SmsBroadcastReceiverListener? = null
        fun getInstance(): SmsBroadcastReceiverListener {
            if (smsListener == null)
                smsListener = null
            return smsListener!!
        }

        fun setInstance(smsBroadcastReceiverListener: SmsBroadcastReceiverListener) {
            smsListener = smsBroadcastReceiverListener
        }
    }

    fun setSmsListenerInstance(smsBroadcastReceiverListener: SmsBroadcastReceiverListener) =
        setInstance(smsBroadcastReceiverListener)

    interface SmsBroadcastReceiverListener {
        fun onSuccess(intent: Intent?)
        fun onFailure()
    }
}