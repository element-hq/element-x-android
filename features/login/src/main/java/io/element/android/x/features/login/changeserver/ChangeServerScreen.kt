@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.x.features.login.changeserver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import io.element.android.x.designsystem.ElementXTheme
import io.element.android.x.designsystem.components.VectorIcon
import io.element.android.x.features.login.R
import io.element.android.x.features.login.error.changeServerError

@Composable
fun ChangeServerScreen(
    viewModel: ChangeServerViewModel = mavericksViewModel(),
    onChangeServerSuccess: () -> Unit = { }
) {
    val state: ChangeServerViewState by viewModel.collectAsState()
    ChangeServerContent(
        state = state,
        onChangeServer = viewModel::setServer,
        onChangeServerSubmit = viewModel::setServerSubmit,
        onChangeServerSuccess = onChangeServerSuccess
    )
}


@Composable
fun ChangeServerContent(
    state: ChangeServerViewState,
    onChangeServer: (String) -> Unit = {},
    onChangeServerSubmit: () -> Unit = {},
    onChangeServerSuccess: () -> Unit = {},
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        val scrollState = rememberScrollState()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(
                        state = scrollState,
                    )
                    .padding(horizontal = 16.dp)
            )
            {
                val isError = state.changeServerAction is Fail
                Box(
                    modifier = Modifier
                        .padding(top = 99.dp)
                        .size(width = 81.dp, height = 73.dp)
                        .align(Alignment.CenterHorizontally)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(32.dp)
                        )
                ) {
                    VectorIcon(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(width = 48.dp, height = 48.dp),
                        // TODO Update with design input
                        resourceId = R.drawable.ic_baseline_dataset_24,
                    )
                }
                Text(
                    text = "Your server",
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 56.dp)
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 38.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                )
                Text(
                    text = "A server is a home for all your data.\n" +
                            "You choose your server and it’s easy to make one.", // TODO "Learn more.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 16.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                OutlinedTextField(
                    value = state.homeserver,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 200.dp),
                    onValueChange = onChangeServer,
                    label = {
                        Text(text = "Server")
                    },
                    isError = isError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onChangeServerSubmit() }
                    )
                )
                if (state.changeServerAction is Fail) {
                    Text(
                        text = changeServerError(
                            state.homeserver,
                            state.changeServerAction.error
                        ),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                Button(
                    onClick = onChangeServerSubmit,
                    enabled = state.submitEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 44.dp)
                ) {
                    Text(text = "Continue")
                }
                if (state.changeServerAction is Success) {
                    onChangeServerSuccess()
                }
            }
            if (state.changeServerAction is Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
@Preview
fun ChangeServerContentPreview() {
    ElementXTheme {
        ChangeServerContent(
            state = ChangeServerViewState(homeserver = "matrix.org"),
        )
    }
}
