package name.jboning.pageme.config.model

import java.util.*

data class AlertRule(
    val expression: AlertExpression,
    val notification_policy: String
) {
    interface AlertExpression

    data class AlertBooleanExpression(
        val op: BooleanOp,
        val exprs: ArrayList<AlertExpression>
    ) : AlertExpression

    enum class BooleanOp {
        AND, OR, NAND, NOR
    }

    data class AlertComparisonExpression(
        val input_field: InputField,
        val op: ComparisonOp,
        val value: String
    ) : AlertExpression

    enum class InputField {
        SENDER, BODY
    }

    enum class ComparisonOp {
        EQ,
        STARTS_WITH,
        ENDS_WITH,
        CONTAINS,
        MATCHES,
        LC_EQ,
        LC_STARTS_WITH,
        LC_ENDS_WITH,
        LC_CONTAINS,
        LC_MATCHES,
    }
}