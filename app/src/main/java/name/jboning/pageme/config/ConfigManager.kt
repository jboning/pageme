package name.jboning.pageme.config

import android.content.Context
import name.jboning.pageme.R
import name.jboning.pageme.config.model.PagerConfig

class ConfigManager {
    fun getConfig(context: Context): PagerConfig {
        context.resources.openRawResource(R.raw.default_config).bufferedReader().use {
            return ConfigSerDes().parse(it.readText())
        }
    }
}