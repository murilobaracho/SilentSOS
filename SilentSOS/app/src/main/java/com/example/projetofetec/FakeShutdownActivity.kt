package com.example.projetofetec

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projetofetec.services.EmergencyForegroundService
import com.example.projetofetec.ui.theme.SilentSOSTheme

class FakeShutdownActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {}
        })

        setContent {
            SilentSOSTheme(darkTheme = true) {
                var isOff by remember { mutableStateOf(false) }
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (isOff) {
                        // Simulação de tela desligada
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
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
                        // Menu de energia estilo moderno (Pixel/Samsung inspired)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(32.dp),
                            modifier = Modifier.padding(bottom = 80.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(24.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                PowerOption(
                                    icon = Icons.Default.Emergency,
                                    label = "Emergência",
                                    color = Color(0xFFD32F2F),
                                    onClick = { /* Faz nada ou SOS manual */ }
                                )
                                PowerOption(
                                    icon = Icons.Default.Refresh,
                                    label = "Reiniciar",
                                    color = Color(0xFF388E3C),
                                    onClick = { 
                                        isOff = true
                                        triggerFakeShutdownAlert()
                                    }
                                )
                                PowerOption(
                                    icon = Icons.Default.PowerSettingsNew,
                                    label = "Desligar",
                                    color = Color(0xFF1976D2),
                                    onClick = { 
                                        isOff = true 
                                        triggerFakeShutdownAlert()
                                    }
                                )
                            }
                            
                            Spacer(Modifier.height(20.dp))
                            
                            Text(
                                text = "Selecione uma opção",
                                color = Color.White.copy(alpha = 0.5f),
                                style = MaterialTheme.typography.bodyMedium
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
fun PowerOption(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(90.dp)
    ) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = Color(0xFF2C2C2E), // Dark grey button background
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = label,
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Normal
        )
    }
}
