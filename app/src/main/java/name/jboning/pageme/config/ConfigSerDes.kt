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
        polymorphic(AlertRule.ReplyOption::class) {
            AlertRule.FixedReplyOption::class with AlertRule.FixedReplyOption.serializer()
            AlertRule.PatternReplyOption::class with AlertRule.PatternReplyOption.serializer()
        }
    }

    val json = Json(context = configModule)
}