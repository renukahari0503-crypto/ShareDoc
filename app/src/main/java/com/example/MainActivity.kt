package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.PrintPortalViewModel
import com.example.ui.UserRole
import com.example.ui.components.AppTopBar
import com.example.ui.components.BottomOwnerBanner
import com.example.ui.screens.CustomerPortalScreen
import com.example.ui.screens.OwnerDashboardScreen
import com.example.ui.theme.CustomerPrintPortalTheme
import com.example.ui.theme.Slate100

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CustomerPrintPortalTheme {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen(
    viewModel: PrintPortalViewModel = viewModel()
) {
    val currentRole by viewModel.currentRole.collectAsState()
    val ownerConfig by viewModel.ownerConfig.collectAsState()
    val driveState by viewModel.driveSyncState.collectAsState()

    // Handle back button when in Customer mode to return to Shop Owner mode
    BackHandler(enabled = currentRole == UserRole.CUSTOMER) {
        viewModel.setRole(UserRole.SHOP_OWNER)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppTopBar(
                currentRole = currentRole,
                onRoleChange = { viewModel.setRole(it) },
                isDriveConnected = driveState.isAuthenticated,
                modifier = Modifier.statusBarsPadding()
            )
        },
        bottomBar = {
            // Requested down below banner: [ HARI PRASAD DUNNA| +91 9866362137]
            BottomOwnerBanner(
                ownerName = ownerConfig.ownerName,
                ownerPhone = ownerConfig.ownerPhone,
                modifier = Modifier.navigationBarsPadding()
            )
        },
        containerColor = Slate100
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Slate100)
        ) {
            when (currentRole) {
                UserRole.SHOP_OWNER -> {
                    OwnerDashboardScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                UserRole.CUSTOMER -> {
                    CustomerPortalScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
