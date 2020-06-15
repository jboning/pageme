package name.jboning.pageme

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.CompoundButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.observe
import name.jboning.pageme.DurationPickerFragment.OnDurationSetListener
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

        permissionsCheck()
    }

    private fun renderStatus() {
        findViewById<TextView>(R.id.status).text = if (viewModel.silenced.value!!) {
            "silenced until " + SimpleDateFormat("HH:mm").format(Date(viewModel.silenceUntil.value!!))
        } else {
            ""
        }
    }

    private fun showAudioEditDialog() {
        val f: DialogFragment = DurationPickerFragment()
        f.show(supportFragmentManager, "duration")
    }

    override fun onDurationSet(minutes: Int) {
        viewModel.setSilenceUntil(System.currentTimeMillis() + 1000 * 60 * minutes)
    }

    override fun onDurationCleared() {
        viewModel.setSilenceUntil(-1)
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