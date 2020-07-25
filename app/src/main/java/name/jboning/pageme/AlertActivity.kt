package name.jboning.pageme

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import name.jboning.pageme.annoyer.AnnoyerService
import name.jboning.pageme.config.ConfigSerDes
import name.jboning.pageme.config.model.AlertRule
import org.json.JSONObject
import java.lang.AssertionError

class AlertActivity : AppCompatActivity() {
    private val viewModel: AlertViewModel by viewModels()
    private var message: CombinedSmsMessage? = null
    private var alertRule: AlertRule? = null
    private var replyButtons: MutableList<Button> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        message = CombinedSmsMessage.fromJson(JSONObject(intent.getStringExtra(EXTRA_SMS)!!))
        alertRule = ConfigSerDes().json.parse(AlertRule.serializer(), intent.getStringExtra(EXTRA_ALERT_RULE)!!)

        setContentView(R.layout.activity_alert)
        val messageView = findViewById<View>(R.id.message) as TextView
        messageView.text = message!!.body
        val senderView = findViewById<View>(R.id.sender) as TextView
        senderView.text = "from " + message!!.displayOriginatingAddress

        val swipe = findViewById<View>(R.id.swipeConfirm) as SwipeConfirm
        val hasAcknowledged = viewModel.hasAcknowledged.value == true
        swipe.setOnConfirmedListener {
            viewModel.setAcknowledged()
            stopService(Intent(this, AnnoyerService::class.java))
        }
        swipe.setConfirmed(hasAcknowledged)
        if (!hasAcknowledged) {
            startService(Intent(this, AnnoyerService::class.java))
        }

        val replyContainer: ViewGroup = findViewById(R.id.reply_container)
        alertRule!!.reply_options?.forEach { replyOption ->
            val replyContent = when (replyOption) {
                is AlertRule.FixedReplyOption -> replyOption.reply
                is AlertRule.PatternReplyOption -> TODO()
                else -> throw AssertionError("wtf")
            }
            val replyLabel = when (replyOption) {
                is AlertRule.FixedReplyOption -> replyContent
                is AlertRule.PatternReplyOption -> "${replyOption.label} ('${replyContent}')"
                else -> throw AssertionError("wtf")
            }
            val replyButton = Button(this).apply {
                text = replyLabel
                setOnClickListener {
                    sendReply(replyLabel, replyContent)
                }
                isEnabled = false
            }
            replyButtons.add(replyButton)
            replyContainer.addView(replyButton)
        }

        viewModel.hasAcknowledged.observe(this, Observer { renderReplySection() })
        viewModel.repliedWith.observe(this, Observer { renderReplySection() })
    }

    private fun sendReply(replyLabel: String, replyContent: String) {
        Log.d("AlertActivity", "Sending to ${message!!.displayOriginatingAddress}")
        SmsManager.getDefault().sendTextMessage(message!!.originatingAddress, null, replyContent, null, null);
        viewModel.setRepliedWith(replyLabel)
    }

    private fun renderReplySection() {
        if (alertRule?.reply_options?.isNotEmpty() != true) {
            // no replies configured, nothing to do
            return
        }
        if (viewModel.hasAcknowledged.value != true) {
            // 0: default state
        } else if (viewModel.repliedWith.value == null) {
            // 1: acknowledged, ready to reply
            replyButtons.forEach {
                it.isEnabled = true
            }
        } else {
            // 2: replied
            replyButtons.forEach {
                it.isEnabled = false
            }
            findViewById<TextView>(R.id.reply_text).apply {
                text = "Replied: " + viewModel.repliedWith.value
                visibility = View.VISIBLE
            }
        }
    }

    override fun onBackPressed() {
        // Only react to back button once the user has acknowledged the alert.
        if (viewModel.hasAcknowledged.value == true) {
            super.onBackPressed()
        }
    }

    companion object {
        private const val EXTRA_SMS = "sms"
        private const val EXTRA_ALERT_RULE = "alert_rule"

        @JvmStatic
        fun getIntent(context: Context, msg: CombinedSmsMessage, rule: AlertRule): Intent {
            val intent = Intent(context, AlertActivity::class.java)
            intent.putExtra(EXTRA_SMS, msg.toJson().toString())
            intent.putExtra(EXTRA_ALERT_RULE, ConfigSerDes().json.stringify(AlertRule.serializer(), rule))
            return intent
        }
    }
}