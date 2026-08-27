package com.example.projetofetec.services

import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class SilentSOSWearService : WearableListenerService() {

    companion object {
        private const val SOS_TRIGGER_PATH = "/trigger_sos"
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == SOS_TRIGGER_PATH) {
            Log.i("EmergencyAlert", "Gatilho recebido via Wear OS (Bluetooth)")
            
            // Dispara o serviço de emergência do telefone
            val intent = Intent(this, EmergencyForegroundService::class.java).apply {
                action = EmergencyForegroundService.ACTION_START_ALERT
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }
    }
}