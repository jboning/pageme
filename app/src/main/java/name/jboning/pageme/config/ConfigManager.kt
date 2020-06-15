package name.jboning.pageme.config

import android.content.Context
import android.util.Log
import name.jboning.pageme.R
import name.jboning.pageme.config.model.PagerConfig
import java.io.FileNotFoundException

class ConfigManager {
    companion object {
        private val CONFIG_PATH = "config.pageme.json"
    }

    fun getConfig(context: Context): PagerConfig {
        val stream = try {
            Log.d("ConfigManager", "Loading config from file...")
            context.openFileInput(CONFIG_PATH)
        } catch (e: FileNotFoundException) {
            Log.d("ConfigManager", "Failed, falling back to default config")
            context.resources.openRawResource(R.raw.default_config)
        }
        stream.bufferedReader().use {
            return ConfigSerDes().parse(it.readText())
        }
    }

    fun saveConfig(context: Context, config: PagerConfig) {
        context.openFileOutput(CONFIG_PATH, Context.MODE_PRIVATE).use {
            it.write(ConfigSerDes().stringify(config).toByteArray())
        }
    }
}