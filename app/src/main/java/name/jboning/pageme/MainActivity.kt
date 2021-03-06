package name.jboning.pageme

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.CompoundButton
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.observe
import name.jboning.pageme.DurationPickerFragment.OnDurationSetListener
import name.jboning.pageme.config.ConfigManager
import name.jboning.pageme.config.RulesActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), OnDurationSetListener {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val silenceSwitch = findViewById<CompoundButton>(R.id.silenceSwitch)
        silenceSwitch.setOnClickListener {
            if (silenceSwitch.isChecked) {
                showAudioEditDialog()
            } else {
                onDurationCleared()
            }
        }
        viewModel.silenced.observe(this) {
            silenceSwitch.isChecked = it
            renderStatus()
        }

        viewModel.rules.observe(this) {
            findViewById<TextView>(R.id.num_rules_text).text = "" + it.size + " rules configured"
        }

        findViewById<FrameLayout>(R.id.rules_entry).setOnClickListener {
            startActivity(Intent(this, RulesActivity::class.java))
        }

        permissionsCheck()
    }

    private fun renderStatus() {
        findViewById<TextView>(R.id.status).text = when (viewModel.rulesStatus.value) {
            ConfigManager.ConfigStatus.VALID -> "Monitoring for pages"
            ConfigManager.ConfigStatus.INVALID -> "Invalid rule configuration"
            ConfigManager.ConfigStatus.ABSENT -> "No rules configured"
            null -> "Something went wrong!"
        }
        findViewById<TextView>(R.id.status_subheader).text = when (viewModel.rulesStatus.value) {
            ConfigManager.ConfigStatus.INVALID -> "Open a .pageme.json file to load configuration."
            ConfigManager.ConfigStatus.ABSENT -> "Monitoring using default (test) rules. Open a .pageme.json file to load configuration."
            else ->
                if(viewModel.silenced.value!!) {
                    "Silenced until " + SimpleDateFormat("HH:mm").format(Date(viewModel.silenceUntil.value!!)) + "."
                } else {
                    ""
                }
        }
    }

    private fun showAudioEditDialog() {
        val f: DialogFragment = DurationPickerFragment()
        f.show(supportFragmentManager, "duration")
    }

    override fun onDurationSet(minutes: Int) {
        viewModel.setSilenceUntil(System.currentTimeMillis() + 1000 * 60 * minutes)
    }

    private fun onDurationCleared() {
        viewModel.setSilenceUntil(-1)
    }

    override fun onDoneWithDurationSelection() {
        // Ensure that the silence switch is in the proper state (in case no duration was selected)
        findViewById<CompoundButton>(R.id.silenceSwitch).isChecked = viewModel.silenced.value!!
    }

    private fun permissionsCheck() {
        Log.d("MainActivity", "checking permissions")
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MainActivity", "requesting permissions")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.VIBRATE),
                    0)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsCheck()
    }

}