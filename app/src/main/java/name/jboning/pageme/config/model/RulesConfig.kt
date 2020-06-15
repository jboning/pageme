package name.jboning.pageme.config.model

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class RulesConfig(
    val alert_rules: ArrayList<AlertRule>
)