package com.example.easycooking.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easycooking.R
import com.example.easycooking.ui.components.PrimaryYellowButton
import com.example.easycooking.ui.theme.AmberBadgeBg
import com.example.easycooking.ui.theme.AmberBadgeText
import com.example.easycooking.ui.theme.DarkText
import com.example.easycooking.ui.theme.DarkTextSecondary
import com.example.easycooking.ui.theme.GreenSuccessText
import com.example.easycooking.ui.theme.LightGrayBg
import com.example.easycooking.ui.theme.PrimaryYellow
import com.example.easycooking.viewmodel.CookingViewModel
import kotlinx.coroutines.launch

@Composable
fun WelcomeScreen(
    onGetStartedClick: () -> Unit,
    onContinueGuestClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .clip(CircleShape)
                    .background(PrimaryYellow),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_chef_hat),
                    contentDescription = "EasyCooking Chef Hat Logo",
                    modifier = Modifier.size(62.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "EasyCooking",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PrimaryYellowButton(
                text = "Get Started",
                onClick = onGetStartedClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onContinueGuestClick) {
                Text(
                    text = "Continue as Guest",
                    color = DarkTextSecondary,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun LoginScreen(
    viewModel: CookingViewModel,
    onBackClick: () -> Unit,
    onSignInSuccess: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onCreateAccountClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val rememberedEmail by viewModel.rememberedEmail.collectAsState()
    val savedRememberMe by viewModel.rememberMe.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    var didPrefillRemembered by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(rememberedEmail, savedRememberMe) {
        if (!didPrefillRemembered && (rememberedEmail.isNotBlank() || savedRememberMe)) {
            didPrefillRemembered = true
            if (email.isBlank() && rememberedEmail.isNotBlank()) {
                email = rememberedEmail
            }
            rememberMe = savedRememberMe
        }
    }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = {
                Text(
                    text = "Sign In Notice",
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
            },
            text = {
                Text(
                    text = errorMessage ?: "",
                    color = DarkTextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { errorMessage = null },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryYellow,
                        contentColor = DarkText
                    )
                ) {
                    Text("Try Again", fontWeight = FontWeight.Bold, color = DarkText)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    errorMessage = null
                    onForgotPasswordClick()
                }) {
                    Text("Forgot Password?", color = DarkText)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = DarkText
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(LightGrayBg)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = "Kitchen Pass",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF7D5800)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Welcome Back",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )
        Text(
            text = "Sign in to access your saved recipes.",
            fontSize = 14.sp,
            color = DarkTextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Email Address",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = DarkText
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            singleLine = true,
            placeholder = { Text("you@example.com", color = DarkTextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = {
                Icon(Icons.Filled.Email, contentDescription = null, tint = DarkTextSecondary)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = DarkText,
                unfocusedTextColor = DarkText,
                cursorColor = DarkText,
                focusedContainerColor = LightGrayBg,
                unfocusedContainerColor = LightGrayBg,
                focusedBorderColor = PrimaryYellow,
                unfocusedBorderColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Password",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = DarkText
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = DarkTextSecondary)
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "Toggle password",
                        tint = DarkTextSecondary
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = DarkText,
                unfocusedTextColor = DarkText,
                cursorColor = DarkText,
                focusedContainerColor = LightGrayBg,
                unfocusedContainerColor = LightGrayBg,
                focusedBorderColor = PrimaryYellow,
                unfocusedBorderColor = Color.Transparent
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { rememberMe = !rememberMe }
            ) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = PrimaryYellow,
                        checkmarkColor = DarkText
                    )
                )
                Text(
                    text = "Remember Me",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = DarkText
                )
            }
            Text(
                text = "Forgot Password?",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkTextSecondary,
                modifier = Modifier
                    .clickable(onClick = onForgotPasswordClick)
                    .padding(vertical = 8.dp, horizontal = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        PrimaryYellowButton(
            text = "Sign In",
            onClick = {
                coroutineScope.launch {
                    val result = viewModel.login(email, password, rememberMe)
                    if (result.isSuccess) {
                        onSignInSuccess()
                    } else {
                        errorMessage = result.exceptionOrNull()?.message ?: "Sign in failed."
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("New here? ", fontSize = 13.sp, color = DarkTextSecondary)
            Text(
                text = "Create Account",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                modifier = Modifier
                    .clickable(onClick = onCreateAccountClick)
                    .padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun RegisterScreen(
    viewModel: CookingViewModel,
    onBackClick: () -> Unit,
    onRegisterSuccess: () -> Unit,
    onSignInClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = {
                Text(
                    text = "Registration Failed",
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
            },
            text = {
                Text(
                    text = errorMessage ?: "",
                    color = DarkTextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { errorMessage = null },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryYellow,
                        contentColor = DarkText
                    )
                ) {
                    Text("OK", fontWeight = FontWeight.Bold, color = DarkText)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = DarkText
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Create Account",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Full Name",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = DarkText
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            singleLine = true,
            placeholder = { Text("Chef Name", color = DarkTextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = {
                Icon(Icons.Filled.Person, contentDescription = null, tint = DarkTextSecondary)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = DarkText,
                unfocusedTextColor = DarkText,
                cursorColor = DarkText,
                focusedContainerColor = LightGrayBg,
                unfocusedContainerColor = LightGrayBg,
                focusedBorderColor = PrimaryYellow,
                unfocusedBorderColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Email Address",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = DarkText
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            singleLine = true,
            placeholder = { Text("you@example.com", color = DarkTextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = {
                Icon(Icons.Filled.Email, contentDescription = null, tint = DarkTextSecondary)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = DarkText,
                unfocusedTextColor = DarkText,
                cursorColor = DarkText,
                focusedContainerColor = LightGrayBg,
                unfocusedContainerColor = LightGrayBg,
                focusedBorderColor = PrimaryYellow,
                unfocusedBorderColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Password (min 6 characters)",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = DarkText
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = DarkTextSecondary)
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "Toggle password",
                        tint = DarkTextSecondary
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = DarkText,
                unfocusedTextColor = DarkText,
                cursorColor = DarkText,
                focusedContainerColor = LightGrayBg,
                unfocusedContainerColor = LightGrayBg,
                focusedBorderColor = PrimaryYellow,
                unfocusedBorderColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        PrimaryYellowButton(
            text = "Create Account",
            onClick = {
                coroutineScope.launch {
                    val result = viewModel.register(fullName, email, password)
                    if (result.isSuccess) {
                        onRegisterSuccess()
                    } else {
                        errorMessage = result.exceptionOrNull()?.message ?: "Registration failed."
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Already have an account? ", fontSize = 13.sp, color = DarkTextSecondary)
            Text(
                text = "Sign In",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                modifier = Modifier
                    .clickable(onClick = onSignInClick)
                    .padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun ForgotPasswordScreen(
    viewModel: CookingViewModel,
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var recoveryStep by remember { mutableIntStateOf(0) }
    var email by remember { mutableStateOf("") }
    var generatedOtp by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        IconButton(
            onClick = {
                errorMessage = null
                if (recoveryStep > 0) recoveryStep -= 1 else onBackClick()
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = DarkText
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(LightGrayBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (recoveryStep == 3) Icons.Filled.Check else Icons.Filled.Lock,
                    contentDescription = null,
                    tint = if (recoveryStep == 3) GreenSuccessText else AmberBadgeText,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = when (recoveryStep) {
                    0 -> "Forgot Password?"
                    1 -> "Verify OTP Code"
                    2 -> "Create New Password"
                    else -> "Password Updated!"
                },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when (recoveryStep) {
                    0 -> "Enter your registered email address to generate a recovery code."
                    1 -> "Enter the 6-digit verification code for ${email.trim()}."
                    2 -> "Verification succeeded. Choose a new password (min 6 characters)."
                    else -> "Your password has been updated. You can now sign in with your new credentials."
                },
                fontSize = 13.sp,
                color = DarkTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFEE2E2))
                    .padding(12.dp)
            ) {
                Text(
                    text = errorMessage ?: "",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFB91C1C)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        when (recoveryStep) {
            0 -> {
                Text(
                    text = "Email Address",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkText
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = null },
                    singleLine = true,
                    placeholder = { Text("you@example.com", color = DarkTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(Icons.Filled.Email, contentDescription = null, tint = DarkTextSecondary)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DarkText,
                        unfocusedTextColor = DarkText,
                        cursorColor = DarkText,
                        focusedContainerColor = LightGrayBg,
                        unfocusedContainerColor = LightGrayBg,
                        focusedBorderColor = PrimaryYellow,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryYellowButton(
                    text = "Generate Recovery Code",
                    onClick = {
                        coroutineScope.launch {
                            val check = viewModel.checkEmailExists(email)
                            if (check.isSuccess) {
                                errorMessage = null
                                generatedOtp = (100000..999999).random().toString()
                                otpCode = ""
                                recoveryStep = 1
                            } else {
                                errorMessage = check.exceptionOrNull()?.message ?: "Account not found."
                            }
                        }
                    }
                )
            }
            1 -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(AmberBadgeBg)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Local recovery OTP code: $generatedOtp",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberBadgeText
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Verification Code (OTP)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkText
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = otpCode,
                    onValueChange = { if (it.length <= 6) otpCode = it; errorMessage = null },
                    singleLine = true,
                    placeholder = { Text("Enter 6-digit code", color = DarkTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DarkText,
                        unfocusedTextColor = DarkText,
                        cursorColor = DarkText,
                        focusedContainerColor = LightGrayBg,
                        unfocusedContainerColor = LightGrayBg,
                        focusedBorderColor = PrimaryYellow,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryYellowButton(
                    text = "Verify OTP Code",
                    onClick = {
                        if (otpCode.trim() == generatedOtp && generatedOtp.isNotEmpty()) {
                            errorMessage = null
                            recoveryStep = 2
                        } else {
                            errorMessage = "Invalid verification code. Please check the code above."
                        }
                    }
                )
            }
            2 -> {
                Text(
                    text = "New Password",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkText
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it; errorMessage = null },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    placeholder = { Text("Minimum 6 characters", color = DarkTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = DarkTextSecondary)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DarkText,
                        unfocusedTextColor = DarkText,
                        cursorColor = DarkText,
                        focusedContainerColor = LightGrayBg,
                        unfocusedContainerColor = LightGrayBg,
                        focusedBorderColor = PrimaryYellow,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryYellowButton(
                    text = "Save New Password",
                    onClick = {
                        coroutineScope.launch {
                            val res = viewModel.resetPassword(email, newPassword)
                            if (res.isSuccess) {
                                errorMessage = null
                                recoveryStep = 3
                            } else {
                                errorMessage = res.exceptionOrNull()?.message ?: "Could not update password."
                            }
                        }
                    }
                )
            }
            else -> {
                PrimaryYellowButton(
                    text = "Back to Sign In",
                    onClick = onLoginClick
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Remember your password? ", fontSize = 13.sp, color = DarkTextSecondary)
            Text(
                text = "Sign In",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                modifier = Modifier
                    .clickable(onClick = onLoginClick)
                    .padding(horizontal = 4.dp)
            )
        }
    }
}
