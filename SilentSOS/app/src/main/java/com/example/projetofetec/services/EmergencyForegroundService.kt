package com.example.projetofetec.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.media.MediaRecorder
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.example.projetofetec.data.HistoryItem
import com.example.projetofetec.data.HistoryManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EmergencyForegroundService : Service() {

    private var isProcessing = false
    private var mediaRecorder: MediaRecorder? = null
    private var currentAudioPath: String? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isRecurringMode = false
    private var currentTriggerType = "Botão de Volume"

    companion object {
        const val ACTION_START_ALERT = "START_ALERT"
        const val ACTION_START_FAKE_SHUTDOWN = "START_FAKE_SHUTDOWN"
        const val ACTION_STOP_ALERTS = "STOP_ALERTS"
        const val CHANNEL_ID = "EmergencyChannel"
        const val NOTIFICATION_ID = 1
        private const val LOCATION_INTERVAL = 5 * 60 * 1000L // 5 minutos
    }

    private val recurringLocationTask = object : Runnable {
        override fun run() {
            if (isRecurringMode) {
                Log.i("EmergencyAlert", "Disparando atualização periódica de localização...")
                fetchLocationAndSendAlert(onlyLocation = true)
                handler.postDelayed(this, LOCATION_INTERVAL)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_ALERT -> {
                currentTriggerType = "Botão de Volume"
                if (!isProcessing) startEmergencyProcess(recurring = false)
            }
            ACTION_START_FAKE_SHUTDOWN -> {
                currentTriggerType = "Fake Shutdown"
                if (!isProcessing) startEmergencyProcess(recurring = true)
            }
            ACTION_STOP_ALERTS -> {
                stopRecurringAlerts()
            }
        }
        return START_STICKY
    }

    private fun stopRecurringAlerts() {
        Log.i("EmergencyAlert", "Alertas recorrentes interrompidos pelo usuário.")
        isRecurringMode = false
        isProcessing = false
        handler.removeCallbacks(recurringLocationTask)
        updateNotification("Proteção em standby.")
        vibrate(100)
    }

    private fun startEmergencyProcess(recurring: Boolean) {
        isProcessing = true
        isRecurringMode = recurring
        vibrate(200) // Vibração curta para confirmar detecção
        createNotificationChannel()
        val notification = createNotification("Silent SOS: Proteção Ativa")
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, 
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        startRecording() // Inicia gravação de 1 minuto
        fetchLocationAndSendAlert(onlyLocation = false)

        if (recurring) {
            handler.postDelayed(recurringLocationTask, LOCATION_INTERVAL)
        }
    }

    private fun startRecording() {
        try {
            val audioFile = File(getExternalFilesDir(null), "emergency_record_${System.currentTimeMillis()}.3gp")
            currentAudioPath = audioFile.absolutePath
            
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            
            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFile.absolutePath)
                prepare()
                start()
            }
            
            Log.i("EmergencyAlert", "Gravação de áudio iniciada: ${audioFile.name}")
            
            // Para após 60 segundos
            Handler(Looper.getMainLooper()).postDelayed({
                stopRecording()
            }, 60000)
            
        } catch (e: Exception) {
            Log.e("EmergencyAlert", "Erro ao iniciar gravação: ${e.message}")
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            Log.i("EmergencyAlert", "Gravação de áudio finalizada.")
        } catch (e: Exception) {
            Log.e("EmergencyAlert", "Erro ao parar gravação: ${e.message}")
        }
    }

    private fun vibrate(duration: Long) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }

    private fun fetchLocationAndSendAlert(onlyLocation: Boolean) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location: Location? ->
                    val loc = location ?: Location("").apply { latitude = 0.0; longitude = 0.0 }
                    sendAlert(loc, onlyLocation)
                }
        } catch (e: SecurityException) {
            Log.e("EmergencyAlert", "Permissão de localização negada: ${e.message}")
            if (!onlyLocation) stopSelf()
        }
    }

    private fun sendAlert(location: Location, onlyLocation: Boolean) {
        val prefs = getSharedPreferences("emergency_prefs", MODE_PRIVATE)
        val userName = prefs.getString("user_name", "Usuário") ?: "Usuário"
        val contactSet = prefs.getStringSet("emergency_contacts", emptySet()) ?: emptySet()
        val customMessage = if (onlyLocation) 
            "ATUALIZAÇÃO DE LOCALIZAÇÃO" 
            else 
            (prefs.getString("emergency_message", "Preciso de ajuda!") ?: "Preciso de ajuda!")
        
        val timeStamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        
        // Mensagem simplificada para evitar bloqueio de SPAM das operadoras
        val message = """
            $customMessage
            ID: $userName
            Local: https://maps.google.com/?q=${location.latitude},${location.longitude}
            Hora: $timeStamp
        """.trimIndent()

        if (contactSet.isNotEmpty()) {
            // 1. Enviar SMS para TODOS os contatos
            for (rawPhone in contactSet) {
                val cleanPhone = rawPhone.replace(Regex("[^0-9+]"), "")
                if (cleanPhone.isNotEmpty()) {
                    sendSMS(cleanPhone, message)
                }
            }
            
            // 2. Preparar WhatsApp apenas no primeiro envio completo
            if (!onlyLocation) {
                val firstContact = contactSet.first().replace("+", "")
                val uri = Uri.parse("https://api.whatsapp.com/send?phone=${firstContact}&text=${Uri.encode(message)}")
                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("EmergencyAlert", "WhatsApp não instalado")
                }
            }
        }

        if (!onlyLocation) {
            // Salvar no Histórico apenas se for o envio completo (primeiro)
            val historyManager = HistoryManager(this)
            historyManager.saveItem(HistoryItem(
                locationUrl = "https://maps.google.com/?q=${location.latitude},${location.longitude}",
                audioPath = currentAudioPath,
                contacts = contactSet.toList(),
                triggerType = currentTriggerType
            ))

            updateNotification("Alerta processado. Verifique SMS e WhatsApp.")
            isProcessing = false
        }
    }

    private fun sendSMS(phoneNumber: String, message: String) {
        val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
        Log.d("EmergencyAlert", "Disparando SMS para: $cleanNumber")
        Log.d("EmergencyAlert", "Conteúdo da mensagem: $message")
        
        val sentAction = "SMS_SENT_ACTION"
        val deliveryAction = "SMS_DELIVERY_ACTION"

        // Receiver para o ENVIO (Saiu do seu celular)
        val sentReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val status = when (resultCode) {
                    android.app.Activity.RESULT_OK -> "✓ SAIU DO APARELHO"
                    else -> "✗ ERRO NO APARELHO: $resultCode"
                }
                Log.i("EmergencyAlert", "Status Envio: $status")
                unregisterReceiver(this)
            }
        }

        // Receiver para a ENTREGA (Chegou no celular da outra pessoa)
        val deliveryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.i("EmergencyAlert", "Status Entrega: O CELULAR DESTINO RECEBEU A MENSAGEM!")
                unregisterReceiver(this)
            }
        }
        
        val flags = PendingIntent.FLAG_IMMUTABLE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(sentReceiver, IntentFilter(sentAction), Context.RECEIVER_EXPORTED)
            registerReceiver(deliveryReceiver, IntentFilter(deliveryAction), Context.RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(sentReceiver, IntentFilter(sentAction))
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(deliveryReceiver, IntentFilter(deliveryAction))
        }

        val sentIntent = PendingIntent.getBroadcast(this, 0, Intent(sentAction).setPackage(packageName), flags)
        val deliveryIntent = PendingIntent.getBroadcast(this, 0, Intent(deliveryAction).setPackage(packageName), flags)

        try {
            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                this.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            
            val parts = smsManager.divideMessage(message)
            val sentIntents = ArrayList<PendingIntent>()
            val deliveryIntents = ArrayList<PendingIntent>()
            
            for (part in parts) {
                sentIntents.add(sentIntent)
                deliveryIntents.add(deliveryIntent)
            }
            
            smsManager.sendMultipartTextMessage(cleanNumber, null, parts, sentIntents, deliveryIntents)
            Log.i("EmergencyAlert", "SMS entregue à rede celular...")
        } catch (e: Exception) {
            Log.e("EmergencyAlert", "Erro: ${e.message}")
            try { unregisterReceiver(sentReceiver) } catch(ex: Exception) {}
            try { unregisterReceiver(deliveryReceiver) } catch(ex: Exception) {}
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Serviço de Emergência",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(content: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Alerta de Emergência Ativo")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .build()
    }

    private fun updateNotification(content: String) {
        vibrate(500) // Vibração mais longa para confirmar o envio
        val notification = createNotification(content)
        val manager = getSystemService(NotificationManager::class.java)
        
        // No Android 13+, a falta da permissão POST_NOTIFICATIONS apenas impede a exibição,
        // mas não deve derrubar o serviço. O check idealmente seria feito antes.
        try {
            manager.notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            Log.e("EmergencyAlert", "Sem permissão para postar notificações")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        isRecurringMode = false
        handler.removeCallbacks(recurringLocationTask)
        stopRecording()
        super.onDestroy()
    }
}