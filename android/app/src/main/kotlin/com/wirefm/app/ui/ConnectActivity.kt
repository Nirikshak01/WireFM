package com.wirefm.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.lifecycleScope
import com.wirefm.app.network.WireFMClient
import com.wirefm.app.ui.components.ManualConnectBottomSheet
import com.wirefm.app.ui.scanner.QrScannerActivity
import com.wirefm.app.ui.screens.SettingsScreen
import com.wirefm.app.ui.screens.WelcomeScreen
import com.wirefm.app.ui.theme.DarkCardBg
import com.wirefm.app.ui.theme.PillPrimary
import com.wirefm.app.ui.theme.TextPrimary
import com.wirefm.app.ui.theme.WireFMTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConnectActivity : ComponentActivity() {

    private var showManualSheet by mutableStateOf(false)
    private var showSettingsScreen by mutableStateOf(false)
    private var isConnecting by mutableStateOf(false)
    private var connectionStatusText by mutableStateOf("")
    private var themeMode by mutableStateOf("dark")

    private val qrScannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val qrText = result.data?.getStringExtra("qr_result")
            if (!qrText.isNullOrEmpty()) {
                handleScannedQr(qrText)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("wirefm", MODE_PRIVATE)
        themeMode = prefs.getString("theme_mode", "dark") ?: "dark"

        setContent {
            val isDark = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            WireFMTheme(darkTheme = isDark) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (showSettingsScreen) {
                        SettingsScreen(
                            currentThemeMode = themeMode,
                            onThemeModeChanged = { newMode ->
                                themeMode = newMode
                                prefs.edit().putString("theme_mode", newMode).apply()
                            },
                            onBack = { showSettingsScreen = false }
                        )
                    } else {
                        WelcomeScreen(
                            onScanClick = {
                                startQrScan()
                            },
                            onManualConnectClick = {
                                showManualSheet = true
                            },
                            onSettingsClick = {
                                showSettingsScreen = true
                            }
                        )
                    }

                    if (showManualSheet) {
                        ManualConnectBottomSheet(
                            initialIp = prefs.getString("last_ip", "") ?: "",
                            initialPort = prefs.getString("last_port", "8080") ?: "8080",
                            initialPass = prefs.getString("last_pass", "") ?: "",
                            onDismiss = { showManualSheet = false },
                            onConnect = { ip, port, pass ->
                                showManualSheet = false
                                attemptConnect(ip, port, pass)
                            }
                        )
                    }

                    if (isConnecting) {
                        val colors = WireFMTheme.colors
                        Dialog(onDismissRequest = {}) {
                            Box(
                                modifier = Modifier
                                    .size(170.dp)
                                    .background(colors.surface, shape = RoundedCornerShape(20.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    CircularProgressIndicator(
                                        color = colors.scanPillBg,
                                        modifier = Modifier.size(44.dp),
                                        strokeWidth = 3.5.dp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = connectionStatusText.ifEmpty { "Connecting..." },
                                        color = colors.textPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startQrScan() {
        qrScannerLauncher.launch(Intent(this, QrScannerActivity::class.java))
    }

    private fun handleScannedQr(raw: String) {
        try {
            val fullUrl = if (!raw.startsWith("http://") && !raw.startsWith("https://")) "http://$raw" else raw
            val url = java.net.URL(fullUrl)
            val ip = url.host
            val port = if (url.port > 0) url.port.toString() else "8080"
            val pass = url.query?.substringAfter("password=") ?: ""

            attemptConnect(ip, port, pass)
        } catch (e: Exception) {
            Toast.makeText(this, "Invalid QR code: $raw", Toast.LENGTH_SHORT).show()
        }
    }

    private fun attemptConnect(ip: String, port: String, pass: String) {
        isConnecting = true
        connectionStatusText = "Connecting to PC..."

        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                WireFMClient.testConnection(ip, port, pass)
            }

            if (result != null) {
                getSharedPreferences("wirefm", MODE_PRIVATE).edit()
                    .putString("last_ip", ip)
                    .putString("last_port", port)
                    .putString("last_pass", pass)
                    .apply()

                connectionStatusText = "Connected!"
                delay(500)
                isConnecting = false

                startActivity(Intent(this@ConnectActivity, FileManagerActivity::class.java).apply {
                    putExtra("ip", ip)
                    putExtra("port", port)
                    putExtra("pass", pass)
                    putExtra("hostname", result.hostname)
                    putExtra("user", result.user)
                    putExtra("os", result.os)
                    putExtra("root", result.root)
                })
                finish()
            } else {
                isConnecting = false
                Toast.makeText(
                    this@ConnectActivity,
                    "Cannot connect to $ip:$port. Check Wi-Fi and PIN.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
