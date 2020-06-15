package name.jboning.pageme.config

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import name.jboning.pageme.R

class ImportConfigActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent!!.data
        if (uri == null) {
            finish()
            return
        }

        setContentView(R.layout.activity_import_config)

        val button = findViewById<Button>(R.id.import_button)
        button.setOnClickListener { lifecycleScope.launch { importConfig() } }
    }

    private suspend fun importConfig() {
        val success: Boolean = withContext(Dispatchers.IO) {
            val uri = intent?.data ?: return@withContext false
            val inputStream = contentResolver.openInputStream(uri) ?: return@withContext false
            val config = inputStream.bufferedReader().use {
                ConfigSerDes().parse(it.readText())
            }
            ConfigManager().saveConfig(this@ImportConfigActivity, config)
            true
        }
        if (success) {
            Toast.makeText(applicationContext, "Success", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(applicationContext, "Error importing config", Toast.LENGTH_SHORT).show()
        }
    }
}