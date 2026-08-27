package com.example.projetofetec

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
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
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
            var isDarkTheme by remember { mutableStateOf(true) }
            var currentTab by remember { mutableStateOf(0) }

            SilentSOSTheme(darkTheme = isDarkTheme) {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { 
                                Text(
                                    "SILENT SOS", 
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                ) 
                            },
                            actions = {
                                IconButton(
                                    onClick = { isDarkTheme = !isDarkTheme },
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                        contentDescription = "Tema",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        )
                    },
                    bottomBar = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp, start = 24.dp, end = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                                    .shadow(16.dp, RoundedCornerShape(32.dp)),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                shape = RoundedCornerShape(32.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                tonalElevation = 8.dp
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    val items = listOf("Escudo", "Registros")
                                    items.forEachIndexed { index, label ->
                                        val isSelected = currentTab == index
                                        val icon = if (index == 0) Icons.Default.Shield else Icons.Default.History
                                        
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(32.dp))
                                                .clickable { currentTab = index },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = null,
                                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Text(
                                                    text = label,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(MaterialTheme.colorScheme.background)
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
            delay(2000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        SafetyStatusBanner(isAccessibilityEnabled && contacts.isNotEmpty())

        SectionTitle("Configurações Base", Icons.Default.Settings)
        
        StepCard(number = "01", title = "Identificação & Destinos", isDone = userName.isNotBlank() && contacts.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = userName,
                    onValueChange = { 
                        userName = it
                        prefs.edit().putString("user_name", it).apply()
                    },
                    label = { Text("Nome de Identificação") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newContactPhone,
                        onValueChange = { newContactPhone = it },
                        label = { Text("Novo Contato") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    Spacer(Modifier.width(8.dp))
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
                            .size(56.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                    ) {
                        Icon(Icons.Default.PersonAdd, null, tint = Color.White)
                    }
                }

                contacts.forEach { phone ->
                    ContactItem(phone) {
                        val newList = contacts.filter { it != phone }
                        contacts = newList
                        prefs.edit().putStringSet("emergency_contacts", newList.toSet()).apply()
                    }
                }
            }
        }

        StepCard(number = "02", title = "Mensagem SOS", isDone = true) {
            OutlinedTextField(
                value = emergencyMessage,
                onValueChange = { 
                    emergencyMessage = it
                    prefs.edit().putString("emergency_message", it).apply()
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }

        SectionTitle("Privilégios do Sistema", Icons.Default.Lock)

        StepCard(number = "03", title = "Acessos Críticos", isDone = hasLocationPerm && hasSmsPerm && hasAudioPerm) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PermissionRow("Localização GPS", hasLocationPerm)
                PermissionRow("Envio de SMS", hasSmsPerm)
                PermissionRow("Captura de Áudio", hasAudioPerm)
                
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
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Configurar Permissões", fontWeight = FontWeight.Bold)
                }
            }
        }

        StepCard(number = "04", title = "Monitor de Botões", isDone = isAccessibilityEnabled) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Necessário para detectar o acionamento via botões de volume.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onOpenAccessibility,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAccessibilityEnabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(if (isAccessibilityEnabled) Icons.Default.CheckCircle else Icons.Default.SettingsSuggest, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (isAccessibilityEnabled) "Serviço Ativado" else "Ativar Monitoramento", fontWeight = FontWeight.Bold)
                }
            }
        }

        SectionTitle("Arsenal de Segurança", Icons.Default.AutoAwesome)

        StepCard(number = "05", title = "Mecanismos de Defesa", isDone = true) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DefenseButton(
                    title = "Fake Shutdown",
                    description = "Simula o desligamento do aparelho.",
                    icon = Icons.Default.PowerSettingsNew,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    val intent = Intent(context, FakeShutdownActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun SafetyStatusBanner(isArmed: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .graphicsLayer {
                scaleX = if (isArmed) scale else 1f
                scaleY = if (isArmed) scale else 1f
            }
            .shadow(if (isArmed) 12.dp else 2.dp, RoundedCornerShape(24.dp)),
        color = if (isArmed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isArmed) Icons.Default.Shield else Icons.Default.ShieldMoon,
                contentDescription = null,
                tint = if (isArmed) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = if (isArmed) "SISTEMA PROTEGIDO" else "SISTEMA DESARMADO",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = if (isArmed) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isArmed) "Pronto para atuar" else "Conclua as configurações",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isArmed) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SectionTitle(text: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, start = 4.dp)
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun ContactItem(phone: String, onDelete: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Phone, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(12.dp))
            Text(
                phone, 
                style = MaterialTheme.typography.bodyMedium, 
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun PermissionRow(label: String, isGranted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isGranted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isGranted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DefenseButton(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, if (isDone) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = number,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = title, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isDone) {
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.Verified, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }
            content()
        }
    }
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
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "REGISTROS",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${history.size} alertas detectados",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (history.isNotEmpty()) {
                FilledIconButton(
                    onClick = { exportHistory(context, history) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.IosShare, null)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))

        if (history.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Inbox, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.height(16.dp))
                    Text("Tudo sob controle. Nenhum registro.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(history) { item ->
                    HistoryItemCard(item, mediaPlayer)
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(item: HistoryItem, mediaPlayer: MediaPlayer) {
    val context = LocalContext.current
    val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    val dateStr = sdf.format(Date(item.timestamp))
    var expanded by remember { mutableStateOf(false) }

    Surface(
        onClick = { expanded = !expanded },
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (item.triggerType == "Fake Shutdown") Icons.Default.PowerOff else Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = item.triggerType, 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = dateStr, 
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.weight(1f))
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            HorizontalDivider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Localização
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Place, null, modifier = Modifier.size(16.dp).padding(top = 2.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (expanded) "Localização exata:" else "Localização capturada", 
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (expanded) FontWeight.Bold else FontWeight.Normal
                        )
                        if (expanded) {
                            Text(
                                text = item.locationUrl,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.locationUrl))
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }

                // Contatos
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.People, null, modifier = Modifier.size(16.dp).padding(top = 2.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (expanded) "Enviado para ${item.contacts.size} contatos:" else "Enviado para ${item.contacts.size} contatos", 
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (expanded) FontWeight.Bold else FontWeight.Normal
                        )
                        if (expanded) {
                            item.contacts.forEach { phone ->
                                Text(
                                    text = "• $phone",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            if (item.audioPath != null) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        try {
                            mediaPlayer.reset()
                            mediaPlayer.setDataSource(item.audioPath)
                            mediaPlayer.prepare()
                            mediaPlayer.start()
                        } catch (e: Exception) {}
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Reproduzir Áudio", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun exportHistory(context: Context, history: List<HistoryItem>) {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    val report = StringBuilder("HISTÓRICO DE ALERTAS - SILENT SOS\n\n")
    history.forEach { item ->
        report.append("DATA: ${sdf.format(Date(item.timestamp))}\n")
        report.append("TIPO: ${item.triggerType}\n")
        report.append("LOCAL: ${item.locationUrl}\n")
        report.append("CONTATOS: ${item.contacts.joinToString(", ")}\n\n")
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, report.toString())
    }
    context.startActivity(Intent.createChooser(intent, "Compartilhar Histórico"))
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
