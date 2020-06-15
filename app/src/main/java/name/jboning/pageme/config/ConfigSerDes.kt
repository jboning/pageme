package name.jboning.pageme.config

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import name.jboning.pageme.config.model.AlertRule

class ConfigSerDes {
    private val configModule = SerializersModule {
        polymorphic(AlertRule.AlertExpression::class) {
            AlertRule.AlertBooleanExpression::class with AlertRule.AlertBooleanExpression.serializer()
            AlertRule.AlertComparisonExpression::class with AlertRule.AlertComparisonExpression.serializer()
        }
    }

    val json = Json(context = configModule)
}