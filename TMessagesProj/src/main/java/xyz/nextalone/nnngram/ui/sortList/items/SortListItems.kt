package xyz.nextalone.nnngram.ui.sortList.items

import xyz.nextalone.nnngram.config.ConfigManager

abstract class SortListItems {
    abstract val itemDefines: Array<String>
    abstract val itemNames: Array<String>
    abstract val define: String
    abstract val itemDefaultConfig: Boolean

    private fun getSavedOrder(): MutableList<Int> {
        val originToAdjusted: MutableList<Int> = (TextStyleItems.itemDefines.indices).toMutableList()
        val savedOrder = ConfigManager.getStringOrDefault(TextStyleItems.define, "")
        if (savedOrder?.isNotEmpty() == true) {
            val savedOrderList = savedOrder.split(",").map { it.toInt() }
            for (i in 0 until TextStyleItems.itemDefines.size) {
                originToAdjusted[i] = savedOrderList[i]
            }
        }
        return originToAdjusted
    }


    private fun getEnabledIndices(): MutableList<Int> {
        val enabledIndices = mutableListOf<Int>()
        for (i in TextStyleItems.itemDefines.indices) {
            if (ConfigManager.getBooleanOrDefault(TextStyleItems.itemDefines[i], itemDefaultConfig)) {
                enabledIndices.add(i)
            }
        }
        return enabledIndices
    }

    fun getEnabledOrder(): MutableList<Int> {
        val enabledIndices = getEnabledIndices()
        val savedOrder = getSavedOrder()
        val enabledOrder = mutableListOf<Int>()
        for (i in savedOrder) {
            if (enabledIndices.contains(i)) {
                enabledOrder.add(i)
            }
        }
        return enabledOrder
    }
}
