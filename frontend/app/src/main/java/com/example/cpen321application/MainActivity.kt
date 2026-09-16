package com.example.cpen321application

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cpen321application.ui.theme.CPEN321ApplicationTheme
import androidx.compose.runtime.LaunchedEffect
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import androidx.compose.runtime.DisposableEffect
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CPEN321ApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    M1App(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun M1App(modifier: Modifier = Modifier) {

    var currentScreen by remember {
        mutableStateOf("home")
    }

    when (currentScreen) {

        "home" -> HomeScreen(
            modifier = modifier,
            onLoginClick = {
                currentScreen = "login"
            },
            onLiveUpdatesClick = {
                currentScreen = "live"
            },
            onTimerClick = {
                currentScreen = "timer"
            }
        )

        "login" -> LoginServerScreen(
            apiBaseUrl = BuildConfig.API_BASE_URL,
            modifier = modifier,
            onBackClick = {
                currentScreen = "home"
            }
        )

        "live" -> LiveUpdatesScreen(
            modifier = modifier,
            onBackClick = {
                currentScreen = "home"
            }
        )

        "timer" -> TimerScreen(
            modifier = modifier,
            onBackClick = {
                currentScreen = "home"
            }
        )
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onLoginClick: () -> Unit,
    onLiveUpdatesClick: () -> Unit,
    onTimerClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(
            16.dp,
            Alignment.CenterVertically
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("CPEN 321 M1")

        Button(
            onClick = onLoginClick
        ) {
            Text("Login + Server")
        }

        Button(
            onClick = onLiveUpdatesClick
        ) {
            Text("Live Updates")
        }

        Button(
            onClick = onTimerClick
        ) {
            Text("Timer")
        }
    }
}

@Composable
fun LoginServerScreen(
    apiBaseUrl: String,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val credentialManager = remember {
        CredentialManager.create(context)
    }

    var signedInUserName by remember {
        mutableStateOf<String?>(null)
    }

    var signInStatus by remember {
        mutableStateOf("Please sign in with Google")
    }

    var serverIp by remember { mutableStateOf("Not loaded") }
    var serverTime by remember { mutableStateOf("Not loaded") }
    var ownerName by remember { mutableStateOf("Not loaded") }

    val clientTime = remember {
        getClientLocalTime()
    }

    val clientIp = remember {
        getClientIpAddress()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Login + Server")

        if (signedInUserName == null) {

            Text(signInStatus)

            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            val googleOption =
                                GetSignInWithGoogleOption.Builder(
                                    serverClientId = BuildConfig.GOOGLE_CLIENT_ID
                                ).build()

                            val request =
                                GetCredentialRequest.Builder()
                                    .addCredentialOption(googleOption)
                                    .build()

                            val result =
                                credentialManager.getCredential(
                                    request = request,
                                    context = context
                                )

                            val credential = result.credential

                            if (
                                credential is CustomCredential &&
                                credential.type ==
                                GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                            ) {
                                val googleCredential =
                                    GoogleIdTokenCredential.createFrom(
                                        credential.data
                                    )

                                signedInUserName =
                                    googleCredential.displayName
                                        ?: googleCredential.id

                                signInStatus = "Signed in successfully"

                                val backendInfo =
                                    fetchBackendInfo(apiBaseUrl)

                                serverIp = backendInfo.serverIp
                                serverTime = backendInfo.serverTime
                                ownerName = backendInfo.ownerName
                            } else {
                                signInStatus =
                                    "Unexpected credential type"
                            }

                        } catch (e: GetCredentialException) {
                            signInStatus =
                                "Sign-in failed: ${e.message}"
                        } catch (e: Exception) {
                            signInStatus =
                                "Error: ${e.message}"
                        }
                    }
                }
            ) {
                Text("Sign in with Google")
            }

        } else {

            Text("Server IP address: $serverIp")

            Text("Client IP address: $clientIp")

            Text("Server local time: $serverTime")

            Text("Client local time: $clientTime")

            Text("Your name: $ownerName")

            Text("Logged-in user: $signedInUserName")
        }

        Button(
            onClick = onBackClick
        ) {
            Text("Back")
        }
    }
}

@Composable
fun PlaceholderScreen(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(
            16.dp,
            Alignment.CenterVertically
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(title)

        Text("Not implemented yet")

        Button(
            onClick = onBackClick
        ) {
            Text("Back")
        }
    }
}

@Composable
fun LiveUpdatesScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit
) {
    var connectionStatus by remember {
        mutableStateOf("Connecting...")
    }

    // Create 256 white cells: 16 × 16
    val pixels = remember {
        mutableStateListOf<Color>().apply {
            repeat(16 * 16) {
                add(Color.White)
            }
        }
    }

    val mainHandler = remember {
        Handler(Looper.getMainLooper())
    }

    DisposableEffect(Unit) {

        val client = OkHttpClient()

        val request = Request.Builder()
            .url("ws://10.0.2.2:3000/live")
            .build()

        val listener = object : WebSocketListener() {

            override fun onOpen(
                webSocket: WebSocket,
                response: Response
            ) {
                mainHandler.post {
                    connectionStatus = "Connected"
                }
            }

            override fun onMessage(
                webSocket: WebSocket,
                text: String
            ) {
                try {
                    val json = JSONObject(text)

                    val x = json.getInt("x")
                    val y = json.getInt("y")
                    val colorHex = json.getString("color")

                    if (x in 0..15 && y in 0..15) {

                        val index = y * 16 + x

                        val newColor = Color(
                            android.graphics.Color.parseColor(colorHex)
                        )

                        mainHandler.post {
                            pixels[index] = newColor
                        }
                    }

                } catch (e: Exception) {
                    mainHandler.post {
                        connectionStatus =
                            "Pixel error: ${e.message}"
                    }
                }
            }

            override fun onFailure(
                webSocket: WebSocket,
                t: Throwable,
                response: Response?
            ) {
                mainHandler.post {
                    connectionStatus =
                        "Connection failed: ${t.message}"
                }
            }
        }

        val webSocket =
            client.newWebSocket(request, listener)

        onDispose {
            webSocket.close(1000, "Leaving screen")
            client.dispatcher.executorService.shutdown()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(
            16.dp,
            Alignment.CenterVertically
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Live Updates")

        Text("Status: $connectionStatus")

        PixelGrid(pixels)

        Button(
            onClick = onBackClick
        ) {
            Text("Back")
        }
    }
}

@Composable
fun PixelGrid(
    pixels: List<Color>
) {
    Canvas(
        modifier = Modifier.size(256.dp)
    ) {

        val cellWidth = size.width / 16
        val cellHeight = size.height / 16

        for (y in 0 until 16) {
            for (x in 0 until 16) {

                val index = y * 16 + x

                drawRect(
                    color = pixels[index],
                    topLeft = androidx.compose.ui.geometry.Offset(
                        x * cellWidth,
                        y * cellHeight
                    ),
                    size = androidx.compose.ui.geometry.Size(
                        cellWidth,
                        cellHeight
                    )
                )
            }
        }
    }
}

@Composable
fun TimerScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit
) {
    var minutesInput by remember {
        mutableStateOf("0")
    }

    var secondsInput by remember {
        mutableStateOf("10")
    }

    var remainingSeconds by remember {
        mutableStateOf(0)
    }

    var isRunning by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    var showSurprise by remember {
        mutableStateOf(false)
    }

    var surpriseEmoji by remember {
        mutableStateOf("🎉")
    }

    var surpriseMessage by remember {
        mutableStateOf("")
    }

    val surpriseEmojis = listOf(
        "🎉",
        "🚀",
        "⭐",
        "🎁",
        "🔥",
        "😎"
    )

    val surpriseMessages = listOf(
        "Time for a short break!",
        "You made it!",
        "Grab some water!",
        "Small steps still count.",
        "Surprise! Keep going!",
        "Mission completed."
    )

    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
        }

        if (isRunning && remainingSeconds == 0) {
            isRunning = false

            surpriseEmoji = surpriseEmojis.random()
            surpriseMessage = surpriseMessages.random()
            showSurprise = true
        }
    }

    val displayMinutes = remainingSeconds / 60
    val displaySeconds = remainingSeconds % 60

    val formattedTime =
        String.format(
            "%02d:%02d",
            displayMinutes,
            displaySeconds
        )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(
            16.dp,
            Alignment.CenterVertically
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Timer")

        OutlinedTextField(
            value = minutesInput,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() }) {
                    minutesInput = newValue
                }
            },
            label = {
                Text("Minutes")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            enabled = !isRunning
        )

        OutlinedTextField(
            value = secondsInput,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() }) {
                    secondsInput = newValue
                }
            },
            label = {
                Text("Seconds")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            enabled = !isRunning
        )

        Button(
            onClick = {
                val minutes =
                    minutesInput.toIntOrNull() ?: 0

                val seconds =
                    secondsInput.toIntOrNull() ?: 0

                if (minutes == 0 && seconds == 0) {
                    errorMessage =
                        "Please enter a timer greater than 0."
                } else if (seconds !in 0..59) {
                    errorMessage =
                        "Seconds must be between 0 and 59."
                } else {
                    errorMessage = ""
                    showSurprise = false

                    remainingSeconds =
                        minutes * 60 + seconds

                    isRunning = true
                }
            },
            enabled = !isRunning
        ) {
            Text("Start Timer")
        }

        Text(
            text = formattedTime
        )

        if (showSurprise) {

            Text(
                text = surpriseEmoji
            )

            Text(
                text = "Surprise!"
            )

            Text(
                text = surpriseMessage
            )

            Button(
                onClick = {
                    showSurprise = false
                    remainingSeconds = 0
                    minutesInput = "0"
                    secondsInput = "10"
                }
            ) {
                Text("Reset Timer")
            }
        }

        if (errorMessage.isNotEmpty()) {
            Text(errorMessage)
        }

        Button(
            onClick = onBackClick
        ) {
            Text("Back")
        }
    }
}

private data class BackendInfo(
    val serverIp: String,
    val serverTime: String,
    val ownerName: String
)

//Function to call backend
private suspend fun fetchBackendInfo(
    apiBaseUrl: String
): BackendInfo = withContext(Dispatchers.IO) {

    try {
        val ipJson = getJson(apiBaseUrl, "/server-ip")
        val timeJson = getJson(apiBaseUrl, "/server-time")
        val nameJson = getJson(apiBaseUrl, "/name")

        BackendInfo(
            serverIp = ipJson.getString("ip"),
            serverTime = timeJson.getString("time"),
            ownerName =
                "${nameJson.getString("firstName")} ${nameJson.getString("lastName")}"
        )

    } catch (e: Exception) {

        BackendInfo(
            serverIp = "Error",
            serverTime = "Error",
            ownerName = "Error: ${e.message}"
        )
    }
}

//Adding helper to perform an HTTP GET
private fun getJson(
    apiBaseUrl: String,
    endpoint: String
): JSONObject {

    val url =
        URL("${apiBaseUrl.trimEnd('/')}$endpoint")

    val connection =
        url.openConnection() as HttpURLConnection

    connection.requestMethod = "GET"
    connection.connectTimeout = 5_000
    connection.readTimeout = 5_000

    try {
        val responseCode = connection.responseCode

        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw Exception("HTTP $responseCode")
        }

        val body =
            connection.inputStream
                .bufferedReader()
                .use { it.readText() }

        return JSONObject(body)

    } finally {
        connection.disconnect()
    }
}

private fun getClientLocalTime(): String {
    val now = java.time.ZonedDateTime.now()

    val hours = String.format("%02d", now.hour)
    val minutes = String.format("%02d", now.minute)
    val seconds = String.format("%02d", now.second)

    val offsetSeconds = now.offset.totalSeconds
    val sign = if (offsetSeconds >= 0) "+" else "-"

    val absoluteMinutes =
        kotlin.math.abs(offsetSeconds / 60)

    val offsetHours =
        String.format("%02d", absoluteMinutes / 60)

    val offsetMinutes =
        String.format("%02d", absoluteMinutes % 60)

    return "$hours:$minutes:$seconds GMT$sign$offsetHours:$offsetMinutes"
}

private fun getClientIpAddress(): String {
    try {
        val interfaces = java.net.NetworkInterface.getNetworkInterfaces()

        while (interfaces.hasMoreElements()) {
            val networkInterface = interfaces.nextElement()
            val addresses = networkInterface.inetAddresses

            while (addresses.hasMoreElements()) {
                val address = addresses.nextElement()

                if (
                    !address.isLoopbackAddress &&
                    address is java.net.Inet4Address &&
                    address.isSiteLocalAddress
                ) {
                    return address.hostAddress ?: "Unavailable"
                }
            }
        }
    } catch (e: Exception) {
        return "Error: ${e.message}"
    }

    return "Unavailable"
}