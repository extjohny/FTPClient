package ru.abdulkhalikov.ftpclient.presentation.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.abdulkhalikov.ftpclient.domain.FTPConnectionStatus
import ru.abdulkhalikov.ftpclient.presentation.ConnectionViewModel

private val CORNER_RADIUS = 5.dp
private val TEXT_FIELD_SPACER = 20.dp

@Composable
fun ConnectionScreen(
    modifier: Modifier = Modifier,
    onSuccessConnection: () -> Unit
) {
    val viewModel: ConnectionViewModel = viewModel()
    val screenState = viewModel.screenState.collectAsState()

    LoginForm(
        modifier = modifier,
        viewModel = viewModel
    )
    when (val currentState = screenState.value) {
        is FTPConnectionStatus.Error -> {
            val context = LocalContext.current
            Toast.makeText(
                context,
                currentState.error,
                Toast.LENGTH_SHORT
            ).show()
        }

        FTPConnectionStatus.Initial -> {

        }

        FTPConnectionStatus.Loading -> {
            CircularProgressIndicator()
        }

        FTPConnectionStatus.Success -> {
            onSuccessConnection()
        }
    }
}

@Composable
fun LoginForm(
    modifier: Modifier = Modifier,
    viewModel: ConnectionViewModel,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var host by rememberSaveable { mutableStateOf("31.24.251.233") }
        var port by rememberSaveable { mutableIntStateOf(21) }
        var username by rememberSaveable { mutableStateOf("user311585") }
        var password by rememberSaveable { mutableStateOf("DzKp2Xpn1a16") }

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            value = host,
            onValueChange = { host = it },
            shape = RoundedCornerShape(CORNER_RADIUS),
            placeholder = {
                Text("Server IP")
            }
        )
        Spacer(modifier = Modifier.height(TEXT_FIELD_SPACER))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            value = port.toString(),
            onValueChange = { port = it.toInt() },
            shape = RoundedCornerShape(CORNER_RADIUS),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = {
                Text("Port")
            }
        )
        Spacer(modifier = Modifier.height(TEXT_FIELD_SPACER))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            value = username,
            onValueChange = { username = it },
            shape = RoundedCornerShape(CORNER_RADIUS),
            placeholder = {
                Text("Username")
            }
        )
        Spacer(modifier = Modifier.height(TEXT_FIELD_SPACER))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            value = password,
            onValueChange = { password = it },
            shape = RoundedCornerShape(CORNER_RADIUS),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            placeholder = {
                Text("Password")
            }
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(CORNER_RADIUS),
            onClick = {
                viewModel.connect(host, port, username, password)
            }
        ) {
            Text("Connect")
        }
    }
}