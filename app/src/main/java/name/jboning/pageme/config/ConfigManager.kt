package name.jboning.pageme.config

import name.jboning.pageme.config.model.AlertRule
import name.jboning.pageme.config.model.PagerConfig

val DEFAULT_CONFIG = PagerConfig(
        notification_policies = hashMapOf(),
        alert_rules = arrayListOf(
                // BAMRU
                AlertRule(
                        expression = AlertRule.AlertBooleanExpression(
                                op = AlertRule.BooleanOp.AND,
                                exprs = arrayListOf(
                                        AlertRule.AlertComparisonExpression(
                                                input_field = AlertRule.InputField.SENDER,
                                                op = AlertRule.ComparisonOp.MATCHES,
                                                value = "^(4157122678|8312267814|8312267820|8312267823|8312267824)$"
                                        ),
                                        AlertRule.AlertBooleanExpression(
                                                op = AlertRule.BooleanOp.NOR,
                                                exprs = arrayListOf(
                                                        // transit page
                                                        AlertRule.AlertComparisonExpression(
                                                                input_field = AlertRule.InputField.BODY,
                                                                op = AlertRule.ComparisonOp.MATCHES,
                                                                value = "(Have you left home yet?|Are you home yet?)$"
                                                        ),
                                                        // response confirmation
                                                        AlertRule.AlertComparisonExpression(
                                                                input_field = AlertRule.InputField.BODY,
                                                                op = AlertRule.ComparisonOp.MATCHES,
                                                                value = "(^(Departure time recorded" +
                                                                        "|Departure time cleared" +
                                                                        "|Return time recorded" +
                                                                        "|Return time cleared))" +
                                                                        "|(^(RSVP|Response).*(recorded\\.|successful\\.))$"
                                                        )
                                                )
                                        )
                                )
                        ),
                        notification_policy = "default"
                ),
                // SMC alert
                AlertRule(
                        expression = AlertRule.AlertComparisonExpression(
                                input_field = AlertRule.InputField.SENDER,
                                op = AlertRule.ComparisonOp.EQ,
                                value = "89361"
                        ),
                        notification_policy = "default"
                ),
                // Pagerduty
                AlertRule(
                        expression = AlertRule.AlertComparisonExpression(
                                input_field = AlertRule.InputField.BODY,
                                op = AlertRule.ComparisonOp.STARTS_WITH,
                                value = "ALRT"
                        ),
                        notification_policy = "default"
                ),
                // Testing
                AlertRule(
                        expression = AlertRule.AlertComparisonExpression(
                                input_field = AlertRule.InputField.BODY,
                                op = AlertRule.ComparisonOp.LC_MATCHES,
                                value = "test alert|test page"
                        ),
                        notification_policy = "default"
                )
        )
)

class ConfigManager {
    fun getConfig(): PagerConfig {
        return DEFAULT_CONFIG
    }
}