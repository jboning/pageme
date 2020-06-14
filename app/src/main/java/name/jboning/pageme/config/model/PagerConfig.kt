package name.jboning.pageme.config.model

import java.util.*

data class PagerConfig(
    val notification_policies: HashMap<String, NotificationPolicy>,
    val alert_rules: ArrayList<AlertRule>
)