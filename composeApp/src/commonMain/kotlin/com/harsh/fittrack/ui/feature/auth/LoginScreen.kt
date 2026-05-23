package com.harsh.fittrack.ui.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.harsh.fittrack.feature.auth.AuthState
import com.harsh.fittrack.feature.auth.AuthViewModel
import com.harsh.fittrack.resources.Res
import com.harsh.fittrack.resources.app_name
import com.harsh.fittrack.resources.app_tagline_short
import com.harsh.fittrack.resources.login_create_account
import com.harsh.fittrack.resources.login_email
import com.harsh.fittrack.resources.login_legal_footer
import com.harsh.fittrack.resources.login_name
import com.harsh.fittrack.resources.login_password
import com.harsh.fittrack.resources.login_sign_in
import com.harsh.fittrack.resources.login_switch_to_login
import com.harsh.fittrack.resources.login_switch_to_register
import com.harsh.fittrack.ui.component.FitTrackLogo
import com.harsh.fittrack.ui.theme.FitTrackTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    onSignedIn: () -> Unit,
    @Suppress("UNUSED_PARAMETER") showAppleSignIn: Boolean = false,
) {
    val vm: AuthViewModel = koinViewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    val isLoading = state is AuthState.Loading
    val error = (state as? AuthState.Error)?.message

    var isRegisterMode by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val focusManager = LocalFocusManager.current

    LaunchedEffect(state) {
        if (state is AuthState.SignedIn) onSignedIn()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FitTrackTheme.colors.surface)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = FitTrackTheme.spacing.containerMargin),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(48.dp))

        FitTrackLogo()

        Spacer(Modifier.height(FitTrackTheme.spacing.sm))

        Text(
            text = stringResource(Res.string.app_name),
            style = FitTrackTheme.typography.headlineLarge,
            color = FitTrackTheme.colors.primary,
            fontWeight = FontWeight.ExtraBold,
        )

        Text(
            text = stringResource(Res.string.app_tagline_short),
            style = FitTrackTheme.typography.bodyMedium,
            color = FitTrackTheme.colors.onSurfaceVariant,
        )

        Spacer(Modifier.height(40.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(FitTrackTheme.spacing.sm),
        ) {
            if (isRegisterMode) {
                AuthTextField(
                    value = name,
                    onValueChange = { name = it; vm.clearError() },
                    label = stringResource(Res.string.login_name),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    enabled = !isLoading,
                )
            }

            AuthTextField(
                value = email,
                onValueChange = { email = it; vm.clearError() },
                label = stringResource(Res.string.login_email),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                enabled = !isLoading,
            )

            AuthTextField(
                value = password,
                onValueChange = { password = it; vm.clearError() },
                label = stringResource(Res.string.login_password),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    submitForm(vm, isRegisterMode, name, email, password)
                }),
                enabled = !isLoading,
            )
        }

        if (error != null) {
            Spacer(Modifier.height(FitTrackTheme.spacing.sm))
            Text(
                text = error,
                style = FitTrackTheme.typography.bodySmall,
                color = FitTrackTheme.colors.error,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(FitTrackTheme.spacing.md))

        Button(
            onClick = {
                focusManager.clearFocus()
                submitForm(vm, isRegisterMode, name, email, password)
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FitTrackTheme.colors.primary),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = FitTrackTheme.colors.onPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.height(20.dp),
                )
            } else {
                Text(
                    text = stringResource(if (isRegisterMode) Res.string.login_create_account else Res.string.login_sign_in),
                    style = FitTrackTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        TextButton(
            onClick = {
                isRegisterMode = !isRegisterMode
                vm.clearError()
                name = ""
                password = ""
            },
            enabled = !isLoading,
        ) {
            Text(
                text = stringResource(if (isRegisterMode) Res.string.login_switch_to_login else Res.string.login_switch_to_register),
                style = FitTrackTheme.typography.bodySmall,
                color = FitTrackTheme.colors.primary,
            )
        }

        Spacer(Modifier.weight(1f))

        Text(
            text = stringResource(Res.string.login_legal_footer),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = FitTrackTheme.spacing.md),
            style = FitTrackTheme.typography.labelSmall,
            color = FitTrackTheme.colors.onSurfaceVariant.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
        )
    }
}

private fun submitForm(vm: AuthViewModel, isRegisterMode: Boolean, name: String, email: String, password: String) {
    if (isRegisterMode) vm.register(name, email, password)
    else vm.login(email, password)
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        enabled = enabled,
        singleLine = true,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = FitTrackTheme.colors.primary,
            unfocusedBorderColor = FitTrackTheme.colors.outline,
            focusedLabelColor = FitTrackTheme.colors.primary,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}
