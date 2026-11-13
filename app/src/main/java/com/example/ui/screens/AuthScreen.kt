package com.example.ui.screens

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.ui.components.GlassActionButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassSurfaceBox
import com.example.ui.components.GlassTier
import com.example.ui.theme.GlassCyanAccent
import com.example.ui.theme.GlassCyanBorder
import com.example.ui.theme.GlassCyanFill
import com.example.ui.theme.GlassRoseAccent
import com.example.ui.theme.GlassRoseBorder
import com.example.ui.theme.GlassRoseFill
import com.example.ui.theme.GlassRoyalBlueBorder
import com.example.ui.theme.GlassRoyalBlueFill
import com.example.ui.theme.TextBodyTranslucent
import com.example.ui.theme.TextPrimaryWhite
import com.example.viewmodel.VocabularyViewModel
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AuthScreen(
    viewModel: VocabularyViewModel,
    onSignedIn: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            tier = GlassTier.TIER_2,
            cornerRadius = 28.dp,
            modifier = Modifier
                .widthIn(max = 460.dp)
                .fillMaxWidth()
                .testTag("auth_screen_container")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp)
            ) {
                GlassSurfaceBox(
                    tier = GlassTier.TIER_3,
                    cornerRadius = 24.dp,
                    tintColor = GlassCyanFill,
                    borderColor = GlassCyanBorder,
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(92.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "App Logo",
                        tint = GlassCyanAccent,
                        modifier = Modifier.size(50.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "4000 Essential Words",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimaryWhite,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Master the most useful English vocabulary with audio pronunciation, interactive quizzes, and cloud progress sync.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextBodyTranslucent,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (errorMessage != null) {
                    GlassSurfaceBox(
                        tier = GlassTier.TIER_3,
                        cornerRadius = 14.dp,
                        tintColor = GlassRoseFill,
                        borderColor = GlassRoseBorder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = GlassRoseAccent,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        )
                    }
                }

                GlassActionButton(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        coroutineScope.launch {
                            try {
                                val credentialManager = CredentialManager.create(context)
                                val clientId = context.getString(R.string.default_web_client_id)
                                val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
                                val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

                                val result = credentialManager.getCredential(context as Activity, request)
                                val credential = result.credential
                                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                                    Firebase.auth.signInWithCredential(authCredential).await()
                                    viewModel.refreshAuthUser()
                                    isLoading = false
                                    onSignedIn()
                                } else {
                                    isLoading = false
                                    errorMessage = "Unexpected sign-in credential type."
                                }
                            } catch (e: GetCredentialCancellationException) {
                                isLoading = false
                                Log.w("Auth", "Sign-in cancelled", e)
                            } catch (e: Exception) {
                                isLoading = false
                                errorMessage = e.localizedMessage ?: "Google Sign-In failed."
                                Log.e("Auth", "Sign-in error", e)
                            }
                        }
                    },
                    tintColor = GlassRoyalBlueFill,
                    borderColor = GlassRoyalBlueBorder,
                    cornerRadius = 16.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("google_sign_in_button"),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = TextPrimaryWhite,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Login,
                            contentDescription = null,
                            tint = GlassCyanAccent
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Sign in with Google",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = TextPrimaryWhite
                        )
                    }
                }
            }
        }
    }
}
