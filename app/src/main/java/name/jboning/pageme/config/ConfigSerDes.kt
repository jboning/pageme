package name.jboning.pageme.config

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import name.jboning.pageme.config.model.AlertRule
import name.jboning.pageme.config.model.PagerConfig

class ConfigSerDes {
    private val configModule = SerializersModule {
        polymorphic(AlertRule.AlertExpression::class) {
            AlertRule.AlertBooleanExpression::class with AlertRule.AlertBooleanExpression.serializer()
            AlertRule.AlertComparisonExpression::class with AlertRule.AlertComparisonExpression.serializer()
        }
    }

    val json = Json(context = configModule)

    fun stringify(config: PagerConfig): String {
        return json.stringify(PagerConfig.serializer(), config)
    }

    fun parse(data: String): PagerConfig {
        return json.parse(PagerConfig.serializer(), data)
    }
}