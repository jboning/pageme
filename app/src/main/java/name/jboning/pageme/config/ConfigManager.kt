package name.jboning.pageme.config

import android.content.Context
import android.util.Log
import kotlinx.serialization.json.JsonDecodingException
import name.jboning.pageme.R
import name.jboning.pageme.config.model.AlertRule
import name.jboning.pageme.config.model.AnnoyerPolicy
import name.jboning.pageme.config.model.RulesConfig
import java.io.FileNotFoundException

class ConfigManager {
    companion object {
        private const val RULES_PATH = "default.rules.pageme.json"
    }

    var rulesStatus: ConfigStatus? = null

    enum class ConfigStatus {
        ABSENT, INVALID, VALID
    }

    fun getRules(context: Context): ArrayList<AlertRule> {
        val stream = try {
            Log.d("ConfigManager", "Loading rules from file...")
            context.openFileInput(RULES_PATH)
        } catch (e: FileNotFoundException) {
            Log.d("ConfigManager", "No rules found")
            rulesStatus = ConfigStatus.ABSENT
            return getDefaultRules(context)
        }
        val result = stream.bufferedReader().use {
            try {
                ConfigSerDes().json.parse(RulesConfig.serializer(), it.readText()).alert_rules
            } catch (e: JsonDecodingException) {
                rulesStatus = ConfigStatus.INVALID
                return arrayListOf()
            }
        }
        rulesStatus = ConfigStatus.VALID
        return result
    }

    fun getDefaultRules(context: Context): ArrayList<AlertRule> {
        context.resources.openRawResource(R.raw.default_rules).bufferedReader().use {
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