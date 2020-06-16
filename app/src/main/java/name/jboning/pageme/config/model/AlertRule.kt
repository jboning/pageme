package name.jboning.pageme.config.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class AlertRule(
        val expression: AlertExpression,
        val name: String? = null,
        val notification_policy: String? = "default"
) {
    interface AlertExpression

    @Serializable
    @SerialName("bool")
    data class AlertBooleanExpression(
        val op: BooleanOp,
        val exprs: ArrayList<AlertExpression>
    ) : AlertExpression

    enum class BooleanOp {
        AND, OR, NAND, NOR
    }

    @Serializable
    @SerialName("cmp")
    data class AlertComparisonExpression(
        val field: InputField,
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