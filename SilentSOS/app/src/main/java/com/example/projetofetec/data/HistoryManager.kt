package com.example.projetofetec.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class HistoryItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val locationUrl: String,
    val audioPath: String?,
    val contacts: List<String>,
    val triggerType: String // "Botão de Volume" ou "Fake Shutdown"
)

class HistoryManager(context: Context) {
    private val prefs = context.getSharedPreferences("sos_history", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveItem(item: HistoryItem) {
        val currentHistory = getHistory().toMutableList()
        currentHistory.add(0, item) // Adiciona no topo
        val json = gson.toJson(currentHistory)
        prefs.edit().putString("history_list", json).apply()
    }

    fun getHistory(): List<HistoryItem> {
        val json = prefs.getString("history_list", null) ?: return emptyList()
        val type = object : com.google.gson.reflect.TypeToken<List<HistoryItem>>() {}.type
        return gson.fromJson(json, type)
    }
}