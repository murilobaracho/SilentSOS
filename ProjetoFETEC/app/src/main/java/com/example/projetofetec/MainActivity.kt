package com.example.projetofetec

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.projetofetec.data.HistoryItem
import com.example.projetofetec.data.HistoryManager
import com.example.projetofetec.services.EmergencyAccessibilityService
import com.example.projetofetec.ui.theme.SilentSOSTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // true = Dark, false = Light. Iniciando como false (Light) por padrão.
            var isDarkTheme by remember { mutableStateOf(false) }
            var currentTab by remember { mutableStateOf(0) }

            SilentSOSTheme(darkTheme = isDarkTheme) {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { 
                                Text("Silent SOS", fontWeight = FontWeight.ExtraBold) 
                            },
                            actions = {
                                IconButton(
                                    onClick = { isDarkTheme = !isDarkTheme },
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            shape = CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                        contentDescription = "Alternar Tema",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = Color.Transparent
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Shield, null) },
                                label = { Text("Segurança") },
                                selected = currentTab == 0,
                                onClick = { currentTab = 0 }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.History, null) },
                                label = { Text("Histórico") },
                                selected = currentTab == 1,
                                onClick = { currentTab = 1 }
                            )
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        if (currentTab == 0) {
                            EmergencyControlPanel(
                                onOpenAccessibility = { openAccessibilitySettings() }
                            )
                        } else {
                            HistoryScreen()
                        }
                    }
                }
            }
        }
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }
}

@Composable
fun EmergencyControlPanel(
    onOpenAccessibility: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("emergency_prefs", Context.MODE_PRIVATE) }
    
    var userName by remember { mutableStateOf(prefs.getString("user_name", "") ?: "") }
    var emergencyMessage by remember { mutableStateOf(prefs.getString("emergency_message", "ALERTA! Preciso de ajuda urgente.") ?: "ALERTA! Preciso de ajuda urgente.") }
    
    var contacts by remember { 
        mutableStateOf(prefs.getStringSet("emergency_contacts", emptySet())?.toList() ?: emptyList()) 
    }
    
    var newContactPhone by remember { mutableStateOf("") }
    
    var hasLocationPerm by remember { mutableStateOf(checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) }
    var hasSmsPerm by remember { mutableStateOf(checkPermission(context, Manifest.permission.SEND_SMS)) }
    var hasAudioPerm by remember { mutableStateOf(checkPermission(context, Manifest.permission.RECORD_AUDIO)) }
    var isAccessibilityEnabled by remember { mutableStateOf(isAccessibilityServiceEnabled(context, EmergencyAccessibilityService::class.java)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasLocationPerm = results[Manifest.permission.ACCESS_FINE_LOCATION] ?: hasLocationPerm
        hasSmsPerm = results[Manifest.permission.SEND_SMS] ?: hasSmsPerm
        hasAudioPerm = results[Manifest.permission.RECORD_AUDIO] ?: hasAudioPerm
    }

    LaunchedEffect(Unit) {
        while(true) {
            isAccessibilityEnabled = isAccessibilityServiceEnabled(context, EmergencyAccessibilityService::class.java)
            delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // SEÇÃO 1: PERFIL & CONTATOS
        SectionTitle("👤 Perfil & Contatos")
        StepCard(number = "1", title = "Configuração Inicial", isDone = userName.isNotBlank() && contacts.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = userName,
                    onValueChange = { 
                        userName = it
                        prefs.edit().putString("user_name", it).apply()
                    },
                    label = { Text("Seu Nome") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newContactPhone,
                        onValueChange = { newContactPhone = it },
                        label = { Text("Número (+55...)") },
                        leadingIcon = { Icon(Icons.Default.Phone, null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    IconButton(
                        onClick = {
                            if (newContactPhone.isNotBlank()) {
                                val newList = contacts + newContactPhone
                                contacts = newList
                                prefs.edit().putStringSet("emergency_contacts", newList.toSet()).apply()
                                newContactPhone = ""
                            }
                        },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(50.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, null, tint = Color.White)
                    }
                }

                contacts.forEach { phone ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ContactPhone, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(phone, fontSize = 14.sp)
                            Spacer(Modifier.weight(1f))
                            IconButton(onClick = {
                                val newList = contacts.filter { it != phone }
                                contacts = newList
                                prefs.edit().putStringSet("emergency_contacts", newList.toSet()).apply()
                            }) {
                                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // SEÇÃO 2: MENSAGEM
        SectionTitle("📝 Mensagem de Emergência")
        StepCard(number = "2", title = "Texto do Alerta", isDone = true) {
            OutlinedTextField(
                value = emergencyMessage,
                onValueChange = { 
                    emergencyMessage = it
                    prefs.edit().putString("emergency_message", it).apply()
                },
                label = { Text("O que será enviado") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // SEÇÃO 3: AUTORIZAÇÕES
        SectionTitle("🔐 Autorizações & Acesso")
        StepCard(number = "3", title = "Permissões do Sistema", isDone = hasLocationPerm && hasSmsPerm && hasAudioPerm) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PermissionBadge("Localização", hasLocationPerm)
                PermissionBadge("Envio de SMS", hasSmsPerm)
                PermissionBadge("Gravação de Áudio", hasAudioPerm)
                
                Button(
                    onClick = {
                        val permissions = mutableListOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.SEND_SMS,
                            Manifest.permission.RECORD_AUDIO
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        permissionLauncher.launch(permissions.toTypedArray())
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Conceder Permissões")
                }
            }
        }

        StepCard(number = "4", title = "Gatilho de Hardware", isDone = isAccessibilityEnabled) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PermissionBadge("Monitor de Botões", isAccessibilityEnabled)
                Button(
                    onClick = onOpenAccessibility,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAccessibilityEnabled) MaterialTheme.colorScheme.secondary else Color(0xFFD32F2F)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isAccessibilityEnabled) "Serviço Ativado" else "Ativar Monitor de Botão")
                }
            }
        }

        // SEÇÃO 4: RECURSOS PRO
        SectionTitle("🚀 Recursos Avançados")
        StepCard(number = "5", title = "Inteligência Extra", isDone = true) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        val intent = Intent(context, FakeShutdownActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PowerOff, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Fake Shutdown")
                }

                var showTimerDialog by remember { mutableStateOf(false) }
                Button(
                    onClick = { showTimerDialog = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Timer, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Safety Timer")
                }

                if (showTimerDialog) {
                    AlertDialog(
                        onDismissRequest = { showTimerDialog = false },
                        title = { Text("Safety Timer") },
                        text = { Text("O SOS será disparado se você não desativar o tempo a tempo.") },
                        confirmButton = {
                            Button(onClick = { showTimerDialog = false }) { Text("Iniciar") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showTimerDialog = false }) { Text("Sair") }
                        }
                    )
                }
            }
        }

        // STATUS FINAL
        AnimatedVisibility(
            visible = isAccessibilityEnabled && contacts.isNotEmpty(),
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            val isDark = isSystemInDarkTheme()
            Card(
                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1B5E20) else Color(0xFF2E7D32)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("SISTEMA ARMADO: 5x Volume+ para socorro.", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
    )
}

@Composable
fun HistoryScreen() {
    val context = LocalContext.current
    val historyManager = remember { HistoryManager(context) }
    val history = remember { historyManager.getHistory() }
    val mediaPlayer = remember { MediaPlayer() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Histórico",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            if (history.isNotEmpty()) {
                IconButton(
                    onClick = { exportHistory(context, history) },
                    modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Exportar",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        if (history.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nenhum alerta registrado.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(history) { item ->
                    HistoryCard(item, mediaPlayer)
                }
            }
        }
    }
}

private fun exportHistory(context: Context, history: List<HistoryItem>) {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    val report = StringBuilder("HISTÓRICO DE ALERTAS - SILENT SOS\n")
    report.append("Gerado em: ${sdf.format(Date())}\n\n")

    history.forEach { item ->
        report.append("--------------------------------\n")
        report.append("DATA/HORA: ${sdf.format(Date(item.timestamp))}\n")
        report.append("TIPO: ${item.triggerType}\n")
        report.append("LOCALIZAÇÃO: ${item.locationUrl}\n")
        report.append("CONTATOS: ${item.contacts.joinToString(", ")}\n")
        if (item.audioPath != null) {
            report.append("ÁUDIO: Arquivo salvo localmente\n")
        }
        report.append("\n")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Relatório Silent SOS")
        putExtra(Intent.EXTRA_TEXT, report.toString())
    }
    context.startActivity(Intent.createChooser(intent, "Compartilhar Histórico"))
}

@Composable
fun HistoryCard(item: HistoryItem, mediaPlayer: MediaPlayer) {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    val dateStr = sdf.format(Date(item.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (item.triggerType == "Fake Shutdown") Icons.Default.PowerOff else Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(text = item.triggerType, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text(text = dateStr, style = MaterialTheme.typography.labelSmall)
            }
            
            Spacer(Modifier.height(8.dp))
            
            Text("📍 Localização:", style = MaterialTheme.typography.labelMedium)
            Text(
                text = item.locationUrl, 
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text("📞 Enviado para:", style = MaterialTheme.typography.labelMedium)
            Text(
                text = item.contacts.joinToString(", "), 
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (item.audioPath != null) {
                Button(
                    onClick = {
                        try {
                            mediaPlayer.reset()
                            mediaPlayer.setDataSource(item.audioPath)
                            mediaPlayer.prepare()
                            mediaPlayer.start()
                        } catch (e: Exception) {
                            // Erro ao tocar
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Ouvir Gravação", fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun StepCard(
    number: String,
    title: String,
    isDone: Boolean = false,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (isDone) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                val isDark = isSystemInDarkTheme()
                Surface(
                    color = if (isDone) (if (isDark) Color(0xFF81C784) else Color(0xFF4CAF50)) else MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(number, color = if (isDone && isDark) Color.Black else Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (isDone) {
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4CAF50))
                }
            }
            content()
        }
    }
}

@Composable
fun PermissionBadge(label: String, isGranted: Boolean) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isGranted) {
        if (isDark) Color(0xFF1B5E20) else Color(0xFFE8F5E9)
    } else {
        if (isDark) Color(0xFFC62828).copy(alpha = 0.2f) else Color(0xFFFFEBEE)
    }
    
    val contentColor = if (isGranted) {
        if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
    } else {
        if (isDark) Color(0xFFEF9A9A) else Color(0xFFC62828)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = if (isGranted) "Ativo" else "Pendente",
                style = MaterialTheme.typography.labelSmall,
                color = contentColor
            )
        }
    }
}

private fun checkPermission(context: Context, permission: String): Boolean {
    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

private fun isAccessibilityServiceEnabled(context: Context, service: Class<out AccessibilityService>): Boolean {
    val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
    for (enabledService in enabledServices) {
        val enabledServiceInfo = enabledService.resolveInfo.serviceInfo
        if (enabledServiceInfo.packageName == context.packageName &&
            enabledServiceInfo.name == service.name
        ) {
            return true
        }
    }
    return false
}
