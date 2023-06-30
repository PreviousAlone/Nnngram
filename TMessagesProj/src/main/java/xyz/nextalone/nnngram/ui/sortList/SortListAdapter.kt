package xyz.nextalone.nnngram.ui.sortList

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Cells.TextCheckCell
import xyz.nextalone.nnngram.config.ConfigManager
import xyz.nextalone.nnngram.config.ConfigManager.getBooleanOrFalse
import xyz.nextalone.nnngram.config.ConfigManager.toggleBoolean
import xyz.nextalone.nnngram.ui.sortList.items.TextStyleItems
import java.util.Collections


abstract class SortListAdapter(
    private val itemDefines: Array<String>,
    private val itemNames: Array<String>,
    private val define: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), ItemTouchHelperAdapter {
    private var originToAdjusted: MutableList<Int> = (itemDefines.indices).toMutableList()

    init {
        val savedOrder = ConfigManager.getStringOrDefault(define, "") ?: ""
        if (savedOrder.isNotEmpty()) {
            val savedOrderList = savedOrder.split(",").map { it.toInt() }
            for (i in itemDefines.indices) {
                originToAdjusted[i] = savedOrderList[i]
            }
        }
    }


    override fun onItemMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(originToAdjusted, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(originToAdjusted, i, i - 1)
            }
        }
        ConfigManager.putString(define, originToAdjusted.joinToString(","))
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val textCheckCell = TextCheckCell(parent.context)
        textCheckCell.background = Theme.getSelectorDrawable(false)
        return object : RecyclerView.ViewHolder(textCheckCell) {}
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val textCheckCell = holder.itemView as TextCheckCell

        val itemIndex = originToAdjusted[position]

        textCheckCell.setTextAndCheck(itemNames[itemIndex], getBooleanOrFalse(itemDefines[itemIndex]), false)
        textCheckCell.setOnClickListener {
            toggleBoolean(itemDefines[itemIndex])
            textCheckCell.isChecked = getBooleanOrFalse(itemDefines[itemIndex])
        }
    }

    override fun getItemCount(): Int = itemDefines.size

    fun reset() {
        originToAdjusted = (itemDefines.indices).toMutableList()
        ConfigManager.putString(define, originToAdjusted.joinToString(","))
        for (i in itemDefines.indices) {
            ConfigManager.putBoolean(itemDefines[i], true)
        }
        notifyDataSetChanged()
    }
}

class TextStyleListAdapter : SortListAdapter(TextStyleItems.itemDefines, TextStyleItems.itemNames, TextStyleItems.define)
