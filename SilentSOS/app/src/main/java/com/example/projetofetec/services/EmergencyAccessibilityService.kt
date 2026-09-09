package com.example.projetofetec.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.util.Log

class EmergencyAccessibilityService : AccessibilityService() {

    private var pressCount = 0
    private var lastPressTime: Long = 0
    private val TRIGGER_COUNT = 5
    private val MAX_INTERVAL = 600L // 0.6 segundos entre cliques

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS or
                    AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 100
        }
        this.serviceInfo = info
        Log.i("EmergencyAlert", "Serviço de Acessibilidade Conectado e Configurado.")
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        val action = event.action
        val repeatCount = event.repeatCount

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP && action == KeyEvent.ACTION_DOWN && repeatCount == 0) {
            val currentTime = SystemClock.elapsedRealtime()
            val delta = currentTime - lastPressTime
            
            if (delta > MAX_INTERVAL) {
                pressCount = 1
            } else {
                pressCount++
            }
            
            lastPressTime = currentTime
            Log.d("EmergencyAlert", "Botão pressionado: $pressCount (Intervalo: ${delta}ms)")

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