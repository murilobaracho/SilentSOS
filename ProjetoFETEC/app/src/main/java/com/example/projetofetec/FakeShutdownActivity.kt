package com.example.projetofetec

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projetofetec.services.EmergencyForegroundService
import com.example.projetofetec.ui.theme.SilentSOSTheme

class FakeShutdownActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Bloqueia o botão voltar
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Não faz nada para simular bloqueio
            }
        })

        setContent {
            SilentSOSTheme(darkTheme = true) {
                var isOff by remember { mutableStateOf(false) }
                
                if (isOff) {
                    // Tela preta total simulando desligado
                    // SAÍDA SECRETA: Toque longo de 3 segundos para sair
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = {
                                        stopAllAlerts()
                                        finish()
                                    }
                                )
                            }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.85f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(40.dp)
                        ) {
                            PowerMenuButton(
                                icon = Icons.Default.PowerSettingsNew,
                                label = "Desligar",
                                color = Color(0xFFE91E63),
                                onClick = { 
                                    isOff = true 
                                    triggerFakeShutdownAlert()
                                }
                            )
                            
                            PowerMenuButton(
                                icon = Icons.Default.Refresh,
                                label = "Reiniciar",
                                color = Color(0xFF4CAF50),
                                onClick = { /* Simula reinício? Melhor não fazer nada ou fechar */ }
                            )
                            
                            PowerMenuButton(
                                icon = Icons.Default.MedicalServices,
                                label = "Modo de Emergência",
                                color = Color(0xFF673AB7),
                                onClick = { /* Não faz nada */ }
                            )
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            Text(
                                text = "Toque para selecionar uma opção",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }

    private fun triggerFakeShutdownAlert() {
        val intent = Intent(this, EmergencyForegroundService::class.java).apply {
            action = EmergencyForegroundService.ACTION_START_FAKE_SHUTDOWN
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopAllAlerts() {
        val intent = Intent(this, EmergencyForegroundService::class.java).apply {
            action = EmergencyForegroundService.ACTION_STOP_ALERTS
        }
        startService(intent)
    }
}

@Composable
fun PowerMenuButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(120.dp)
    ) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = color,
            modifier = Modifier.size(70.dp),
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(35.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp
        )
    }
}
