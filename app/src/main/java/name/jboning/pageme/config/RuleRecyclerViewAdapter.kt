package name.jboning.pageme.config

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import name.jboning.pageme.R

import name.jboning.pageme.config.model.AlertRule

class RuleRecyclerViewAdapter(
        private val values: List<AlertRule>)
    : RecyclerView.Adapter<RuleRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_rule, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.numberView.text = position.toString()
        holder.nameView.text = item.name ?: "(unnamed)"
        holder.contentText.text = item.toString()
        holder.contentView.removeAllViews()
        holder.contentView.addView(AlertRuleRenderer(holder.contentView.context).render(item))
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val numberView: TextView = view.findViewById(R.id.rule_number)
        val nameView: TextView = view.findViewById(R.id.rule_name)
        val contentView: ViewGroup = view.findViewById(R.id.rule_contents)
        val contentText: TextView = view.findViewById(R.id.rule_contents_text)

        override fun toString(): String {
            return super.toString() + " '" + contentText.text + "'"
        }
    }
}