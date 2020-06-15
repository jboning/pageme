package name.jboning.pageme.config.model

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class PagerConfig(
    val notification_policies: HashMap<String, NotificationPolicy>,
    val alert_rules: ArrayList<AlertRule>
)