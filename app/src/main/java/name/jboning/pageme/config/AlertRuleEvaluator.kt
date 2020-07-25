package name.jboning.pageme.config

import name.jboning.pageme.CombinedSmsMessage
import name.jboning.pageme.config.model.AlertRule
import name.jboning.pageme.config.model.AlertRule.*

class AlertRuleEvaluator(private val rule: AlertRule, private val msg: CombinedSmsMessage) {
    fun matches(): Boolean {
        return evaluate(rule.expression)
    }

    private fun evaluate(expr: AlertExpression): Boolean {
        return when (expr) {
            is AlertBooleanExpression -> evaluateBoolean(expr)
            is AlertComparisonExpression -> evaluateComparison(expr)
            else -> throw java.lang.RuntimeException("!!!")
        }
    }

    private fun evaluateBoolean(expr: AlertBooleanExpression): Boolean {
        val results = expr.exprs.map {evaluate(it)}
        val reduced = when (expr.op) {
            BooleanOp.AND, BooleanOp.NAND -> results.all { it }
            BooleanOp.OR, BooleanOp.NOR -> results.any { it }
        }
        return when (expr.op) {
            BooleanOp.AND, BooleanOp.OR -> reduced
            BooleanOp.NAND, BooleanOp.NOR -> !reduced
        }
    }

    private fun evaluateComparison(expr: AlertComparisonExpression): Boolean {
        val inputField = when (expr.field) {
            InputField.SENDER -> msg.displayOriginatingAddress
            InputField.BODY -> msg.body
        }
        val data = when (expr.op) {
            ComparisonOp.LC_CONTAINS,
            ComparisonOp.LC_ENDS_WITH,
            ComparisonOp.LC_EQ,
            ComparisonOp.LC_MATCHES,
            ComparisonOp.LC_STARTS_WITH
                -> inputField.toLowerCase()
            ComparisonOp.CONTAINS,
            ComparisonOp.ENDS_WITH,
            ComparisonOp.EQ,
            ComparisonOp.MATCHES,
            ComparisonOp.STARTS_WITH
                -> inputField
        }
        return when(expr.op) {
            ComparisonOp.CONTAINS,
            ComparisonOp.LC_CONTAINS
                -> data.contains(expr.value)
            ComparisonOp.ENDS_WITH,
            ComparisonOp.LC_ENDS_WITH
                -> data.endsWith(expr.value)
            ComparisonOp.EQ,
            ComparisonOp.LC_EQ
                -> data == expr.value
            ComparisonOp.MATCHES,
            ComparisonOp.LC_MATCHES
                -> Regex(expr.value).containsMatchIn(data)
            ComparisonOp.STARTS_WITH,
            ComparisonOp.LC_STARTS_WITH
                -> data.startsWith(expr.value)
        }
    }

}