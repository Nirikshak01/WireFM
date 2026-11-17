package com.wirefm.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.lifecycleScope
import com.google.zxing.integration.android.IntentIntegrator
import com.wirefm.app.network.WireFMClient
import com.wirefm.app.ui.components.ManualConnectBottomSheet
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
    private var isConnecting by mutableStateOf(false)
    private var connectionStatusText by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WireFMTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    WelcomeScreen(
                        onScanClick = {
                            startQrScan()
                        },
                        onManualConnectClick = {
                            showManualSheet = true
                        },
                        onSettingsClick = {
                            Toast.makeText(this@ConnectActivity, "WireFM v2.0", Toast.LENGTH_SHORT).show()
                        }
                    )

                    if (showManualSheet) {
                        val prefs = getSharedPreferences("wirefm", MODE_PRIVATE)
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
                        Dialog(onDismissRequest = {}) {
                            Box(
                                modifier = Modifier
                                    .size(170.dp)
                                    .background(DarkCardBg, shape = RoundedCornerShape(20.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    CircularProgressIndicator(
                                        color = PillPrimary,
                                        modifier = Modifier.size(44.dp),
                                        strokeWidth = 3.5.dp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = connectionStatusText.ifEmpty { "Connecting..." },
                                        color = TextPrimary,
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
        IntentIntegrator(this).apply {
            setPrompt("Scan WireFM QR code on PC terminal")
            setOrientationLocked(false)
            setBeepEnabled(true)
            initiateScan()
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
                // Save prefs
                getSharedPreferences("wirefm", MODE_PRIVATE).edit()
                    .putString("last_ip", ip)
                    .putString("last_port", port)
                    .putString("last_pass", pass)
                    .apply()

                connectionStatusText = "Connected!"
                delay(600)
                isConnecting = false

                // Launch file manager
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            try {
                val raw = result.contents.trim()
                val fullUrl = if (!raw.startsWith("http://") && !raw.startsWith("https://")) "http://$raw" else raw
                val url = java.net.URL(fullUrl)
                val ip = url.host
                val port = if (url.port > 0) url.port.toString() else "8080"
                val pass = url.query?.substringAfter("password=") ?: ""

                attemptConnect(ip, port, pass)
            } catch (e: Exception) {
                Toast.makeText(this, "Invalid QR code format", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
