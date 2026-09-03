package com.example.projetofetec.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class WearTriggerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.example.silentsos.TRIGGER_ALERT") {
            Log.i("EmergencyAlert", "Gatilho Externo (Wear OS/Siri) recebido!")
            val serviceIntent = Intent(context, EmergencyForegroundService::class.java).apply {
                action = EmergencyForegroundService.ACTION_START_ALERT
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }
}