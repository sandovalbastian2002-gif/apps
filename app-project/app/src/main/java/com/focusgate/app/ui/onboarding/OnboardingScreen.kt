package com.focusgate.app.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.focusgate.app.ui.theme.AccentMint
import com.focusgate.app.util.PermissionUtils

@Composable
fun OnboardingScreen(onContinue: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var accessibilityGranted by remember { mutableStateOf(PermissionUtils.isAccessibilityServiceEnabled(context)) }
    var usageAccessGranted by remember { mutableStateOf(PermissionUtils.isUsageAccessGranted(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                accessibilityGranted = PermissionUtils.isAccessibilityServiceEnabled(context)
                usageAccessGranted = PermissionUtils.isUsageAccessGranted(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Bienvenido a FocusGate", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "Para interceptar Instagram, FocusGate necesita dos permisos de Android. " +
                "No lee el contenido de otras apps ni recopila tu actividad: solo observa qué " +
                "aplicación está abierta en cada momento.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))

        PermissionCard(
            title = "Servicio de accesibilidad (obligatorio)",
            description = "Permite detectar cuándo abres Instagram para mostrarte el desafío antes de dejarte continuar. Es el mecanismo central del bloqueo.",
            granted = accessibilityGranted,
            onRequestClick = { context.startActivity(PermissionUtils.accessibilitySettingsIntent()) }
        )

        Spacer(Modifier.height(12.dp))

        PermissionCard(
            title = "Acceso al uso de apps (opcional)",
            description = "Permite mostrar en Estadísticas cuánto tiempo pasas en Instagram. Si no lo activas, el resto de la app funciona igual.",
            granted = usageAccessGranted,
            onRequestClick = { context.startActivity(PermissionUtils.usageAccessSettingsIntent(context)) }
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onContinue,
            enabled = accessibilityGranted,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text(if (accessibilityGranted) "Continuar" else "Activa el servicio de accesibilidad para continuar")
        }
    }
}

@Composable
private fun PermissionCard(
    title: String,
    description: String,
    granted: Boolean,
    onRequestClick: () -> Unit
) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (granted) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (granted) AccentMint else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 10.dp))
            }
            Spacer(Modifier.height(6.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (!granted) {
                Spacer(Modifier.height(10.dp))
                OutlinedButton(onClick = onRequestClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Abrir configuración")
                }
            }
        }
    }
}
