package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PrintDocument
import com.example.ui.OwnerTab
import com.example.ui.PrintPortalViewModel
import com.example.ui.UserRole
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavySky
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.WhatsAppGreen
import com.example.util.FileUtils
import com.example.util.QRCodeGenerator
import com.example.util.WhatsAppHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OwnerDashboardScreen(
    viewModel: PrintPortalViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.ownerTab.collectAsState()
    val documents by viewModel.documents.collectAsState()
    val ownerConfig by viewModel.ownerConfig.collectAsState()
    val driveSyncState by viewModel.driveSyncState.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate100)
    ) {
        // Tab Navigation
        ScrollableTabRow(
            selectedTabIndex = currentTab.ordinal,
            containerColor = Color.White,
            contentColor = NavyPrimary,
            edgePadding = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = currentTab == OwnerTab.QUEUE,
                onClick = { viewModel.setOwnerTab(OwnerTab.QUEUE) },
                text = { Text("Print Queue (${documents.size})", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_owner_queue")
            )
            Tab(
                selected = currentTab == OwnerTab.QR_CODE,
                onClick = { viewModel.setOwnerTab(OwnerTab.QR_CODE) },
                text = { Text("Customer QR Code", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_owner_qrcode")
            )
            Tab(
                selected = currentTab == OwnerTab.DRIVE_SYNC,
                onClick = { viewModel.setOwnerTab(OwnerTab.DRIVE_SYNC) },
                text = { Text("Google Drive", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_owner_drive")
            )
            Tab(
                selected = currentTab == OwnerTab.SETTINGS,
                onClick = { viewModel.setOwnerTab(OwnerTab.SETTINGS) },
                text = { Text("Shop Info", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_owner_settings")
            )
        }

        // Tab Content
        when (currentTab) {
            OwnerTab.QUEUE -> {
                OwnerQueueContent(
                    documents = documents,
                    statusFilter = statusFilter,
                    onStatusFilterChange = { viewModel.setStatusFilter(it) },
                    onUpdateStatus = { doc, status -> viewModel.updatePrintStatus(doc, status) },
                    onDownload = { viewModel.downloadDocument(it) },
                    onShare = { viewModel.shareViaWhatsApp(it) },
                    onNotifyCustomer = { viewModel.sendCustomerWhatsAppUpdate(it) },
                    onDelete = { viewModel.deleteDocument(it) },
                    onSwitchToCustomer = { viewModel.setRole(UserRole.CUSTOMER) }
                )
            }
            OwnerTab.QR_CODE -> {
                OwnerQRCodeContent(
                    ownerConfig = ownerConfig,
                    driveFolderUrl = driveSyncState.folderUrl,
                    onTestCustomerFlow = { viewModel.setRole(UserRole.CUSTOMER) }
                )
            }
            OwnerTab.DRIVE_SYNC -> {
                OwnerDriveSyncContent(
                    driveState = driveSyncState,
                    onResync = { viewModel.resyncGoogleDrive() },
                    onConnectAccount = { email, name -> viewModel.connectGoogleAccount(email, name) },
                    onDisconnect = { viewModel.disconnectGoogleAccount() }
                )
            }
            OwnerTab.SETTINGS -> {
                OwnerSettingsContent(ownerConfig = ownerConfig)
            }
        }
    }
}

@Composable
private fun OwnerQueueContent(
    documents: List<PrintDocument>,
    statusFilter: String,
    onStatusFilterChange: (String) -> Unit,
    onUpdateStatus: (PrintDocument, String) -> Unit,
    onDownload: (PrintDocument) -> Unit,
    onShare: (PrintDocument) -> Unit,
    onNotifyCustomer: (PrintDocument) -> Unit,
    onDelete: (PrintDocument) -> Unit,
    onSwitchToCustomer: () -> Unit
) {
    val pendingCount = documents.count { it.status == "Pending" }
    val printingCount = documents.count { it.status == "Printing" }
    val readyCount = documents.count { it.status == "Ready" }
    val totalCost = documents.sumOf { it.calculateCost() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            // Metrics Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "Pending",
                    value = pendingCount.toString(),
                    color = AmberWarning,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Printing",
                    value = printingCount.toString(),
                    color = NavySky,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Ready",
                    value = readyCount.toString(),
                    color = EmeraldSuccess,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Est. Total",
                    value = "₹${totalCost.toInt()}",
                    color = NavyPrimary,
                    modifier = Modifier.weight(1.2f)
                )
            }
        }

        item {
            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val filters = listOf("All", "Pending", "Printing", "Ready", "Delivered")
                items(filters) { filter ->
                    FilterChip(
                        selected = statusFilter == filter,
                        onClick = { onStatusFilterChange(filter) },
                        label = { Text(filter, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NavyPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        if (documents.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Slate400,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No print jobs found",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate800
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Customers can scan the shop QR code to upload documents",
                            fontSize = 13.sp,
                            color = Slate600,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onSwitchToCustomer,
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            modifier = Modifier.testTag("empty_queue_upload_test_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simulate Customer Upload")
                        }
                    }
                }
            }
        } else {
            items(documents, key = { it.id }) { doc ->
                PrintJobCard(
                    document = doc,
                    onUpdateStatus = { onUpdateStatus(doc, it) },
                    onDownload = { onDownload(doc) },
                    onShare = { onShare(doc) },
                    onNotifyCustomer = { onNotifyCustomer(doc) },
                    onDelete = { onDelete(doc) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Slate600,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun PrintJobCard(
    document: PrintDocument,
    onUpdateStatus: (String) -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    onNotifyCustomer: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val timeFormatted = remember(document.timestamp) {
        SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(document.timestamp))
    }

    val statusColor = when (document.status) {
        "Pending" -> AmberWarning
        "Printing" -> NavySky
        "Ready" -> EmeraldSuccess
        "Delivered" -> Slate600
        else -> Slate700
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("print_job_card_${document.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Customer ID + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(NavyPrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = NavyPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = document.displayName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = timeFormatted,
                            fontSize = 11.sp,
                            color = Slate400
                        )
                    }
                }

                // Status pill
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = document.status.uppercase(Locale.getDefault()),
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // File Information (Requested auto-naming)
            Surface(
                color = Slate100,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Auto-Named File:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400
                    )
                    Text(
                        text = document.storedFileName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NavyPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (document.originalFileName != document.storedFileName) {
                        Text(
                            text = "Original: ${document.originalFileName} (${FileUtils.formatFileSize(document.fileSize)})",
                            fontSize = 11.sp,
                            color = Slate600,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Print Specifications
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SpecificationPill(label = "${document.copies} copy")
                    SpecificationPill(label = document.colorMode)
                    SpecificationPill(label = document.paperSize)
                    if (document.isDoubleSided) {
                        SpecificationPill(label = "2-Sided")
                    }
                }

                Text(
                    text = "₹${document.calculateCost()}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
            }

            if (document.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Note: ${document.notes}",
                    fontSize = 11.sp,
                    color = Slate700
                )
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp), color = Slate200)

            // Actions row: Status changer, Download, WhatsApp notify, Share, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Next status advance button
                val nextStatus = when (document.status) {
                    "Pending" -> "Printing"
                    "Printing" -> "Ready"
                    "Ready" -> "Delivered"
                    else -> "Pending"
                }

                Button(
                    onClick = { onUpdateStatus(nextStatus) },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                    modifier = Modifier.testTag("advance_status_${document.id}")
                ) {
                    Text(
                        text = when (document.status) {
                            "Pending" -> "Start Print"
                            "Printing" -> "Mark Ready"
                            "Ready" -> "Delivered"
                            else -> "Reopen"
                        },
                        fontSize = 12.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // WhatsApp notify customer
                    IconButton(
                        onClick = onNotifyCustomer,
                        modifier = Modifier.testTag("whatsapp_customer_${document.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Notify via WhatsApp",
                            tint = WhatsAppGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Download
                    IconButton(
                        onClick = onDownload,
                        modifier = Modifier.testTag("download_doc_${document.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download Document",
                            tint = NavyPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Share
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.testTag("share_doc_${document.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Document",
                            tint = Slate600,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Delete
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_doc_${document.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Document",
                            tint = Slate400,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecificationPill(label: String) {
    Surface(
        color = Slate200,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Slate700,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun OwnerQRCodeContent(
    ownerConfig: com.example.data.model.ShopOwnerConfig,
    driveFolderUrl: String,
    onTestCustomerFlow: () -> Unit
) {
    val context = LocalContext.current
    val portalPayload = "https://printportal.app/upload?owner=9866362137&name=HariPrasadDunna"
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(portalPayload) {
        qrBitmap = QRCodeGenerator.generateQRCode(portalPayload, 600, 600)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("owner_qr_code_card"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CUSTOMER SELF-SERVICE QR",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = ownerConfig.shopName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Scan to upload documents, search & download",
                        fontSize = 13.sp,
                        color = Slate600,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // QR Display
                    Box(
                        modifier = Modifier
                            .size(230.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(2.dp, Slate200, RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap!!.asImageBitmap(),
                                contentDescription = "Customer Upload QR Code",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            CircularProgressIndicator(color = NavyPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Counter stand instructions
                    Surface(
                        color = Slate100,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "HOW CUSTOMERS USE THIS:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "1. Customer scans QR code with phone camera or QR scanner\n" +
                                       "2. Customer uploads document & enters Phone Number or Name\n" +
                                       "3. Document is auto-named and synced to your Google Drive\n" +
                                       "4. Customer searches & downloads or sends via WhatsApp",
                                fontSize = 12.sp,
                                color = Slate700,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onTestCustomerFlow,
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("test_customer_view_button")
                        ) {
                            Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open Customer View", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                WhatsAppHelper.openWhatsAppChat(
                                    context,
                                    ownerConfig.ownerPhone,
                                    "Customer Print Portal Link: $portalPayload"
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("share_qr_whatsapp_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share QR Link", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun OwnerDriveSyncContent(
    driveState: com.example.data.drive.DriveSyncState,
    onResync: () -> Unit,
    onConnectAccount: (String, String) -> Unit,
    onDisconnect: () -> Unit
) {
    var showAccountDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("google_drive_status_card"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (driveState.isAuthenticated) EmeraldSuccess.copy(alpha = 0.15f) else Slate200),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (driveState.isAuthenticated) Icons.Default.CloudDone else Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = if (driveState.isAuthenticated) EmeraldSuccess else Slate600
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Google Drive Integration",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Text(
                                    text = if (driveState.isAuthenticated) "Connected & Auto-Syncing" else "Not Connected",
                                    fontSize = 12.sp,
                                    color = if (driveState.isAuthenticated) EmeraldSuccess else Slate600
                                )
                            }
                        }

                        if (driveState.isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = NavyPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = Slate200)
                    Spacer(modifier = Modifier.height(14.dp))

                    DriveDetailItem(label = "Google Account", value = "${driveState.userName} (${driveState.userEmail})")
                    DriveDetailItem(label = "Dedicated Drive Folder", value = driveState.folderName)
                    DriveDetailItem(label = "Folder ID", value = driveState.folderId)
                    DriveDetailItem(label = "Folder Web URL", value = driveState.folderUrl)
                    DriveDetailItem(label = "Status", value = driveState.statusMessage)

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onResync,
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("resync_drive_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sync All Now")
                        }

                        OutlinedButton(
                            onClick = { showAccountDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Account Settings")
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavyDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "AUTOMATIC FOLDER WORKFLOW",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavySky,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Whenever a customer uploads a print job through the portal, it is immediately placed in your dedicated Google Drive folder: '${driveState.folderName}'. You can access files from your PC or printer directly from Google Drive!",
                        fontSize = 13.sp,
                        color = Slate200,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showAccountDialog) {
        var emailInput by remember { mutableStateOf(driveState.userEmail) }
        var nameInput by remember { mutableStateOf(driveState.userName) }

        AlertDialog(
            onDismissRequest = { showAccountDialog = false },
            title = { Text("Google Account Setup") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Configure the shop owner Google account for Drive storage:")
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Owner Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Google Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConnectAccount(emailInput, nameInput)
                        showAccountDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Save & Connect")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DriveDetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label.uppercase(Locale.getDefault()),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Slate400,
            letterSpacing = 0.5.sp
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Slate800,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun OwnerSettingsContent(ownerConfig: com.example.data.model.ShopOwnerConfig) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "SHOP & OWNER DETAILS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    DriveDetailItem(label = "App Owner", value = ownerConfig.ownerName)
                    DriveDetailItem(label = "Phone / WhatsApp", value = ownerConfig.ownerPhone)
                    DriveDetailItem(label = "Owner Email", value = ownerConfig.ownerEmail)
                    DriveDetailItem(label = "Shop Name", value = ownerConfig.shopName)

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = Slate200)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "PRINTING RATES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Black & White (A4):", color = Slate700, fontSize = 13.sp)
                        Text("₹${ownerConfig.bwPricePerPage} / page", fontWeight = FontWeight.Bold, color = NavyPrimary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Full Color (A4):", color = Slate700, fontSize = 13.sp)
                        Text("₹${ownerConfig.colorPricePerPage} / page", fontWeight = FontWeight.Bold, color = NavyPrimary)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
