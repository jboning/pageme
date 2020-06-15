package name.jboning.pageme.config

import android.content.Context
import android.util.Log
import name.jboning.pageme.R
import name.jboning.pageme.config.model.AlertRule
import name.jboning.pageme.config.model.AnnoyerPolicy
import name.jboning.pageme.config.model.RulesConfig
import java.io.FileNotFoundException

class ConfigManager {
    companion object {
        private val RULES_PATH = "default.rules.pageme.json"
    }

    fun getRules(context: Context): ArrayList<AlertRule> {
        val stream = try {
            Log.d("ConfigManager", "Loading rules from file...")
            context.openFileInput(RULES_PATH)
        } catch (e: FileNotFoundException) {
            Log.d("ConfigManager", "No rules found")
            return arrayListOf()
        }
        stream.bufferedReader().use {
            return ConfigSerDes().json.parse(RulesConfig.serializer(), it.readText()).alert_rules
        }
    }

    fun saveRules(context: Context, rules: ArrayList<AlertRule>) {
        context.openFileOutput(RULES_PATH, Context.MODE_PRIVATE).use {
            it.write(ConfigSerDes().json.stringify(RulesConfig.serializer(), RulesConfig(alert_rules = rules)).toByteArray())
        }
    }

    fun getAnnoyerPolicy(context: Context): AnnoyerPolicy {
        context.resources.openRawResource(R.raw.default_annoyer_policy).bufferedReader().use {
            return ConfigSerDes().json.parse(AnnoyerPolicy.serializer(), it.readText())
        }
    }
}