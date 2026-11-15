package com.example.easycooking.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easycooking.ui.components.PrimaryYellowButton
import com.example.easycooking.ui.theme.DarkText
import com.example.easycooking.ui.theme.DarkTextSecondary
import com.example.easycooking.ui.theme.LightGrayBg
import com.example.easycooking.ui.theme.PrimaryYellow

@Composable
fun WelcomeScreen(
    onGetStartedClick: () -> Unit,
    onContinueGuestClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
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
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(LightGrayBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Restaurant,
                    contentDescription = "Logo",
                    tint = DarkText,
                    modifier = Modifier.size(48.dp)
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
    onBackClick: () -> Unit,
    onSignInSuccess: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onCreateAccountClick: () -> Unit
) {
    var email by remember { mutableStateOf("alex@example.com") }
    var password by remember { mutableStateOf("************") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showPasswordErrorDialog by remember { mutableStateOf(false) }

    if (showPasswordErrorDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordErrorDialog = false },
            title = {
                Text(
                    text = "Warning Password Salah",
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
            },
            text = {
                Text(
                    text = "Alamat email atau kata sandi yang Anda masukkan tidak sesuai dengan database akun terdaftar. Silakan periksa kembali atau pilih Lupa Kata Sandi.",
                    color = DarkTextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showPasswordErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryYellow)
                ) {
                    Text("Coba Lagi", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPasswordErrorDialog = false
                    onForgotPasswordClick()
                }) {
                    Text("Lupa Kata Sandi?", color = DarkText)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = DarkText
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(28.dp))

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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = "Forgot Password?",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkTextSecondary,
                modifier = Modifier.clickable(onClick = onForgotPasswordClick)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        PrimaryYellowButton(
            text = "Sign In",
            onClick = {
                if (password.length < 4 || !email.contains("@")) {
                    showPasswordErrorDialog = true
                } else {
                    onSignInSuccess()
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Divider(modifier = Modifier.weight(1f))
            Text(
                text = "  or continue with  ",
                fontSize = 12.sp,
                color = DarkTextSecondary
            )
            Divider(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onSignInSuccess,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("Google", color = DarkText, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = onSignInSuccess,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Phone, contentDescription = null, tint = DarkText, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Phone", color = DarkText, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("New here? ", fontSize = 13.sp, color = DarkTextSecondary)
            Text(
                text = "Create Account",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                modifier = Modifier.clickable(onClick = onCreateAccountClick)
            )
        }
    }
}

@Composable
fun RegisterScreen(
    onBackClick: () -> Unit,
    onRegisterSuccess: () -> Unit,
    onSignInClick: () -> Unit
) {
    var fullName by remember { mutableStateOf("Alex Morgan") }
    var email by remember { mutableStateOf("alex@example.com") }
    var password by remember { mutableStateOf("************") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = DarkText
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Create Account",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )

        Spacer(modifier = Modifier.height(28.dp))

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

        Spacer(modifier = Modifier.height(28.dp))

        PrimaryYellowButton(
            text = "Create Account",
            onClick = onRegisterSuccess
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "By continuing, you agree to our Terms of Service and Privacy Policy.",
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = DarkTextSecondary,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Already have an account? ", fontSize = 13.sp, color = DarkTextSecondary)
            Text(
                text = "Sign In",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                modifier = Modifier.clickable(onClick = onSignInClick)
            )
        }
    }
}

@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit,
    onSendCodeClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    var recoveryStep by remember { mutableIntStateOf(0) }
    var email by remember { mutableStateOf("alex@kitchen.com") }
    var otpCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {
        IconButton(
            onClick = {
                if (recoveryStep > 0) recoveryStep -= 1 else onBackClick()
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = DarkText
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

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
                    imageVector = when (recoveryStep) {
                        3 -> Icons.Filled.Check
                        else -> Icons.Filled.Lock
                    },
                    contentDescription = null,
                    tint = if (recoveryStep == 3) Color(0xFF2E7D32) else Color(0xFFE65100),
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = when (recoveryStep) {
                    0 -> "Forgot Password?"
                    1 -> "Verifikasi Kode OTP"
                    2 -> "Buat Password Baru"
                    else -> "Password Telah Diubah!"
                },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when (recoveryStep) {
                    0 -> "Masukkan email terdaftar untuk menerima kode verifikasi pemulihan (OTP)."
                    1 -> "Kode OTP 6-digit telah dikirimkan ke $email. Silakan masukkan kode untuk memverifikasi identitas."
                    2 -> "Kredensial berhasil diverifikasi. Masukkan kata sandi baru untuk akun Anda."
                    else -> "Kata sandi akun Anda berhasil diperbarui. Silakan login kembali dengan password baru."
                },
                fontSize = 13.sp,
                color = DarkTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

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
                    onValueChange = { email = it },
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

                Spacer(modifier = Modifier.height(28.dp))

                PrimaryYellowButton(
                    text = "Send Verification Code",
                    onClick = { recoveryStep = 1 }
                )
            }
            1 -> {
                Text(
                    text = "Kode Verifikasi (OTP)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkText
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = otpCode,
                    onValueChange = { if (it.length <= 6) otpCode = it },
                    placeholder = { Text("Contoh: 482910", color = DarkTextSecondary) },
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

                Spacer(modifier = Modifier.height(28.dp))

                PrimaryYellowButton(
                    text = "Verifikasi Kode OTP",
                    onClick = { recoveryStep = 2 }
                )
            }
            2 -> {
                Text(
                    text = "Password Baru",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkText
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    placeholder = { Text("Masukkan password baru...", color = DarkTextSecondary) },
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

                Spacer(modifier = Modifier.height(28.dp))

                PrimaryYellowButton(
                    text = "Simpan Password Baru",
                    onClick = { recoveryStep = 3 }
                )
            }
            else -> {
                PrimaryYellowButton(
                    text = "Kembali ke Login",
                    onClick = onLoginClick
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Remember your password? ", fontSize = 13.sp, color = DarkTextSecondary)
            Text(
                text = "Log In",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                modifier = Modifier.clickable(onClick = onLoginClick)
            )
        }
    }
}
