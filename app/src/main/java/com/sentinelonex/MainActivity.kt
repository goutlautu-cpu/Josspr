package com.sentinelonex

import android.os.Bundle
import android.content.pm.PackageManager
import android.content.pm.ApplicationInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {
    private lateinit var output: TextView
    private val sensitive = setOf(
        "android.permission.RECORD_AUDIO",
        "android.permission.CAMERA",
        "android.permission.READ_SMS",
        "android.permission.SEND_SMS",
        "android.permission.READ_CONTACTS",
        "android.permission.READ_CALL_LOG",
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.ACCESS_BACKGROUND_LOCATION",
        "android.permission.REQUEST_INSTALL_PACKAGES"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLockScreen()
    }

    private fun showLockScreen() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(36, 60, 36, 36)
        }
        val title = TextView(this).apply {
            text = "SENTINEL ONE X\n\nProtección móvil"
            textSize = 26f
        }
        val status = TextView(this).apply { text = "\nVerificando biometría…" }
        val button = Button(this).apply { text = "Desbloquear con huella" }
        layout.addView(title); layout.addView(status); layout.addView(button)
        setContentView(layout)

        val manager = BiometricManager.from(this)
        val can = manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        if (can != BiometricManager.BIOMETRIC_SUCCESS) {
            status.text = "No hay biometría fuerte disponible o configurada.\nLa app no puede verificar la huella en este dispositivo."
            button.isEnabled = false
            return
        }
        button.setOnClickListener { authenticate(status) }
        authenticate(status)
    }

    private fun authenticate(status: TextView) {
        val executor: Executor = ContextCompat.getMainExecutor(this)
        val prompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                showScanner()
            }
            override fun onAuthenticationFailed() {
                status.text = "Huella no reconocida. Inténtalo nuevamente."
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                status.text = "Autenticación cancelada: $errString"
            }
        })
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Sentinel One X")
            .setSubtitle("Confirma tu identidad para abrir la aplicación")
            .setNegativeButtonText("Cancelar")
            .build()
        prompt.authenticate(info)
    }

    private fun showScanner() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 36, 28, 28)
        }
        val title = TextView(this).apply { text = "SENTINEL ONE X\n\nPanel de seguridad"; textSize = 24f }
        val scan = Button(this).apply { text = "Analizar aplicaciones instaladas" }
        val outputBox = ScrollView(this)
        output = TextView(this).apply { text = "Pulsa analizar para iniciar una revisión heurística."; textSize = 16f }
        outputBox.addView(output)
        layout.addView(title); layout.addView(scan); layout.addView(outputBox)
        setContentView(layout)
        scan.setOnClickListener { runScan() }
    }

    private fun runScan() {
        output.text = "Analizando…\n"
        val pm = packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        var inspected = 0
        val flagged = mutableListOf<String>()
        for (app in apps) {
            if (app.packageName == packageName) continue
            inspected++
            val label = pm.getApplicationLabel(app).toString()
            val requested = try {
                pm.getPackageInfo(app.packageName, PackageManager.GET_PERMISSIONS)
                    .requestedPermissions?.toSet() ?: emptySet()
            } catch (_: Exception) { emptySet<String>() }
            val matches = requested.intersect(sensitive)
            val isSideloaded = (app.flags and ApplicationInfo.FLAG_SYSTEM) == 0 &&
                try { pm.getInstallSourceInfo(app.packageName).installingPackageName == null } catch (_: Exception) { false }

            if (matches.size >= 4 || (isSideloaded && matches.size >= 2)) {
                flagged.add("$label (${app.packageName})\n• Permisos sensibles: ${matches.size}" +
                    (if (isSideloaded) "\n• Posible instalación fuera de tienda" else "") +
                    "\n• Revisa permisos: ${matches.joinToString()}\n")
            }
        }
        output.text = "Revisión terminada.\nApps visibles revisadas: $inspected\n" +
            "Elementos que requieren revisión: ${flagged.size}\n\n" +
            (if (flagged.isEmpty()) "No se encontraron coincidencias con estas reglas. Esto NO demuestra que el dispositivo esté libre de malware."
             else flagged.joinToString("\n") ) +
            "\n\nAviso: análisis heurístico básico. No analiza el código de las apps ni sustituye un antivirus profesional."
    }
}
