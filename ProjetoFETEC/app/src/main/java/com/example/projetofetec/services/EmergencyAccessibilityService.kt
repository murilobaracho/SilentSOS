package com.example.projetofetec.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Build
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.util.Log

class EmergencyAccessibilityService : AccessibilityService() {

    private var pressCount = 0
    private var lastPressTime: Long = 0
    private val TRIGGER_COUNT = 5
    private val MAX_INTERVAL = 2000L // 2 segundos

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        val action = event.action

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP && action == KeyEvent.ACTION_DOWN) {
            val currentTime = System.currentTimeMillis()
            
            if (currentTime - lastPressTime > MAX_INTERVAL) {
                pressCount = 1
            } else {
                pressCount++
            }
            
            lastPressTime = currentTime
            Log.d("EmergencyAlert", "Botão pressionado: $pressCount")

            if (pressCount >= TRIGGER_COUNT) {
                pressCount = 0
                triggerEmergencyAlert()
            }
            
            return false 
        }
        return super.onKeyEvent(event)
    }

    private fun triggerEmergencyAlert() {
        Log.i("EmergencyAlert", "SEQUÊNCIA DETECTADA! Iniciando serviço de alerta.")
        val intent = Intent(this, EmergencyForegroundService::class.java).apply {
            action = EmergencyForegroundService.ACTION_START_ALERT
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
}