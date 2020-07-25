package name.jboning.pageme.config

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import name.jboning.pageme.config.model.AlertRule
import java.lang.AssertionError

class AlertRuleRenderer(private val context: Context, private val rule: AlertRule) {
    companion object {
        private val INDENT_DP = 16
    }

    private val density = context.resources.displayMetrics.density

    fun renderExpression(): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            renderExpressionInto(rule.expression, 0, this)
        }
    }

    private fun renderExpressionInto(expr: AlertRule.AlertExpression, depth: Int, container: ViewGroup) {
        when (expr) {
            is AlertRule.AlertBooleanExpression -> renderBooleanInto(expr, depth, container)
            is AlertRule.AlertComparisonExpression -> renderComparisonInto(expr, depth, container)
        }
    }

    private fun renderBooleanInto(expr: AlertRule.AlertBooleanExpression, depth: Int, container: ViewGroup) {
        var innerDepth = depth + 1
        when (expr.op) {
            AlertRule.BooleanOp.NAND, AlertRule.BooleanOp.NOR -> {
                addTextView(container, innerDepth, "NOT")
                innerDepth += 1
            }
            AlertRule.BooleanOp.AND, AlertRule.BooleanOp.OR -> {}
        }
        expr.exprs.forEachIndexed { i, subExpr ->
            if (i != 0) {
                addTextView(container, innerDepth, when (expr.op) {
                    AlertRule.BooleanOp.AND, AlertRule.BooleanOp.NAND -> "AND"
                    AlertRule.BooleanOp.OR, AlertRule.BooleanOp.NOR -> "OR"
                })
            }
            renderExpressionInto(subExpr, innerDepth, container)
        }
    }

    private fun renderComparisonInto(expr: AlertRule.AlertComparisonExpression, depth: Int, container: ViewGroup) {
        val field = when (expr.field) {
            AlertRule.InputField.BODY -> "Message"
            AlertRule.InputField.SENDER -> "Sender"
        }
        val lc = when (expr.op) {
            AlertRule.ComparisonOp.CONTAINS,
            AlertRule.ComparisonOp.ENDS_WITH,
            AlertRule.ComparisonOp.EQ,
            AlertRule.ComparisonOp.MATCHES,
            AlertRule.ComparisonOp.STARTS_WITH
                -> ""
            AlertRule.ComparisonOp.LC_CONTAINS,
            AlertRule.ComparisonOp.LC_ENDS_WITH,
            AlertRule.ComparisonOp.LC_EQ,
            AlertRule.ComparisonOp.LC_MATCHES,
            AlertRule.ComparisonOp.LC_STARTS_WITH
                -> " (lowercased)"
        }
        val op = when (expr.op) {
            AlertRule.ComparisonOp.CONTAINS,
            AlertRule.ComparisonOp.LC_CONTAINS
                -> "contains"
            AlertRule.ComparisonOp.ENDS_WITH,
            AlertRule.ComparisonOp.LC_ENDS_WITH
                -> "ends with"
            AlertRule.ComparisonOp.EQ,
            AlertRule.ComparisonOp.LC_EQ
                -> "equals"
            AlertRule.ComparisonOp.MATCHES,
            AlertRule.ComparisonOp.LC_MATCHES
                -> "matches regex"
            AlertRule.ComparisonOp.STARTS_WITH,
            AlertRule.ComparisonOp.LC_STARTS_WITH
                -> "starts with"
        }
        addTextView(container, maxOf(depth, 1), "${field}${lc} ${op} '${expr.value}'")
    }

    fun renderReplies(): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            rule.reply_options?.forEach {
                renderReplyInto(it, this)
            }
        }
    }

    private fun renderReplyInto(reply: AlertRule.ReplyOption, container: ViewGroup) {
        val text = when (reply) {
            is AlertRule.FixedReplyOption -> reply.reply
            is AlertRule.PatternReplyOption -> "${reply.label}: from pattern '${reply.pattern}'"
            else -> throw AssertionError("wtf")  // for some reason the compiler thinks the list of cases isn't exhaustive
        }
        addTextView(container, 1, text)
    }

    private fun addTextView(container: ViewGroup, depth: Int, text: String) {
        container.addView(TextView(container.context).apply {
            layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                leftMargin = (depth * density * INDENT_DP).toInt()
            }
            setText(text)
        })
    }
}