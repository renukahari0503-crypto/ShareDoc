package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PrintDocument
import com.example.ui.CustomerTab
import com.example.ui.PrintPortalViewModel
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavySky
import com.example.ui.theme.RoseError
import com.example.ui.theme.Slate50
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
import com.example.util.WhatsAppHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CustomerPortalScreen(
    viewModel: PrintPortalViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.customerTab.collectAsState()
    val uploadState by viewModel.uploadFormState.collectAsState()
    val documents by viewModel.documents.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = LocalContext.current

    // Document file picker contract
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val name = FileUtils.getFileNameFromUri(context, uri)
            var size = 0L
            try {
                context.contentResolver.openFileDescriptor(uri, "r")?.use {
                    size = it.statSize
                }
            } catch (e: Exception) {
                // Ignore fallback size
            }
            viewModel.onFileSelected(uri, name, size)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate100)
    ) {
        // Customer Sub Tabs
        TabRow(
            selectedTabIndex = currentTab.ordinal,
            containerColor = Color.White,
            contentColor = NavyPrimary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = currentTab == CustomerTab.UPLOAD,
                onClick = { viewModel.setCustomerTab(CustomerTab.UPLOAD) },
                text = { Text("Upload for Print", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("customer_tab_upload")
            )
            Tab(
                selected = currentTab == CustomerTab.MY_DOCS,
                onClick = { viewModel.setCustomerTab(CustomerTab.MY_DOCS) },
                text = { Text("Search & Download", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("customer_tab_search")
            )
        }

        when (currentTab) {
            CustomerTab.UPLOAD -> {
                CustomerUploadTabContent(
                    uploadState = uploadState,
                    onPickFile = { filePickerLauncher.launch("*/*") },
                    onUseSampleDoc = {
                        val sample = FileUtils.createSampleDocument(context, uploadState.customerPhoneOrName.ifBlank { "9866362137" }, "pdf")
                        val uri = FileUtils.getShareableUri(context, sample)
                        viewModel.onFileSelected(uri, sample.name, sample.length())
                    },
                    onCustomerPhoneOrNameChange = { viewModel.onCustomerPhoneOrNameChanged(it) },
                    onCopiesChange = { viewModel.setCopies(it) },
                    onColorModeChange = { viewModel.setColorMode(it) },
                    onPaperSizeChange = { viewModel.setPaperSize(it) },
                    onDoubleSidedChange = { viewModel.setDoubleSided(it) },
                    onNotesChange = { viewModel.setNotes(it) },
                    onSubmitUpload = { viewModel.submitUpload() },
                    onResetForm = { viewModel.resetUploadForm() },
                    onGoToSearch = {
                        viewModel.setCustomerTab(CustomerTab.MY_DOCS)
                        if (uploadState.customerPhoneOrName.isNotBlank()) {
                            viewModel.onSearchQueryChanged(uploadState.customerPhoneOrName)
                        }
                    }
                )
            }
            CustomerTab.MY_DOCS -> {
                CustomerSearchTabContent(
                    documents = documents,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
                    onDownload = { viewModel.downloadDocument(it) },
                    onShareWhatsApp = { viewModel.shareViaWhatsApp(it) }
                )
            }
        }
    }
}

@Composable
private fun CustomerUploadTabContent(
    uploadState: com.example.ui.UploadFormState,
    onPickFile: () -> Unit,
    onUseSampleDoc: () -> Unit,
    onCustomerPhoneOrNameChange: (String) -> Unit,
    onCopiesChange: (Int) -> Unit,
    onColorModeChange: (String) -> Unit,
    onPaperSizeChange: (String) -> Unit,
    onDoubleSidedChange: (Boolean) -> Unit,
    onNotesChange: (String) -> Unit,
    onSubmitUpload: () -> Unit,
    onResetForm: () -> Unit,
    onGoToSearch: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Success Card if just uploaded
        if (uploadState.uploadSuccess && uploadState.lastUploadedDoc != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("upload_success_card"),
                    colors = CardDefaults.cardColors(containerColor = EmeraldSuccess.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EmeraldSuccess))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Upload Successful!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Your document has been sent to Hari Prasad Dunna for printing.",
                            fontSize = 13.sp,
                            color = Slate700,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Auto-Named File:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate400
                                )
                                Text(
                                    text = uploadState.lastUploadedDoc.storedFileName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Status: In Print Queue (${uploadState.lastUploadedDoc.copies} copies, ${uploadState.lastUploadedDoc.colorMode})",
                                    fontSize = 12.sp,
                                    color = Slate600
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onGoToSearch,
                                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("view_in_my_docs_button")
                            ) {
                                Text("View in My Documents", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = onResetForm,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Upload Another", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Section 1: Choose File
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("file_picker_card"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "1. SELECT DOCUMENT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Upload document for printing or editing (PDF, DOCX, Images, etc.)",
                        fontSize = 13.sp,
                        color = Slate600
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (uploadState.selectedFileUri != null) {
                        Surface(
                            color = Slate100,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, NavyPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(NavyPrimary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = NavyPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = uploadState.originalFileName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = FileUtils.formatFileSize(uploadState.fileSize),
                                        fontSize = 11.sp,
                                        color = Slate400
                                    )
                                }
                                IconButton(onClick = onPickFile) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Change File",
                                        tint = NavyPrimary
                                    )
                                }
                            }
                        }
                    } else {
                        // Empty picker box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Slate50)
                                .border(1.5.dp, Slate300, RoundedCornerShape(12.dp))
                                .clickable { onPickFile() }
                                .padding(vertical = 24.dp, horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.FileUpload,
                                    contentDescription = null,
                                    tint = NavyPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap to Browse Device Files",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                                Text(
                                    text = "Supports PDF, Word DOCX, PNG, JPG",
                                    fontSize = 11.sp,
                                    color = Slate400
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick demo button
                        OutlinedButton(
                            onClick = onUseSampleDoc,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("use_sample_doc_button")
                        ) {
                            Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Use Sample Document for Testing", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Section 2: Customer Identity (Required for auto-naming)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("customer_info_card"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "2. CUSTOMER IDENTIFICATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Provide your phone number or name. The uploaded file is automatically named using this.",
                        fontSize = 13.sp,
                        color = Slate600
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uploadState.customerPhoneOrName,
                        onValueChange = onCustomerPhoneOrNameChange,
                        label = { Text("Phone Number or Full Name *") },
                        placeholder = { Text("e.g. 9866362137 or Hari Prasad") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_phone_or_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NavyPrimary,
                            focusedLabelColor = NavyPrimary
                        )
                    )

                    // Auto-naming preview banner
                    AnimatedVisibility(visible = uploadState.previewTargetName.isNotBlank()) {
                        Column {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = NavyPrimary.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = NavyPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Auto-naming convention applied:",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Slate600
                                        )
                                        Text(
                                            text = uploadState.previewTargetName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NavyPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (uploadState.errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uploadState.errorMessage,
                            color = RoseError,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Section 3: Print Options & Notes
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("print_options_card"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "3. PRINT PREFERENCES & INSTRUCTIONS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Copies Stepper
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Number of Copies:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate800
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Slate200)
                                    .clickable {
                                        if (uploadState.copies > 1) {
                                            onCopiesChange(uploadState.copies - 1)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Slate800, modifier = Modifier.size(16.dp))
                            }

                            Text(
                                text = "${uploadState.copies}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary,
                                modifier = Modifier.width(28.dp),
                                textAlign = TextAlign.Center
                            )

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(NavyPrimary)
                                    .clickable {
                                        onCopiesChange(uploadState.copies + 1)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = Slate200)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Color Mode Choice
                    Text(
                        text = "Color Mode:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate800
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SelectableChip(
                            label = "Black & White (₹2)",
                            isSelected = uploadState.colorMode == "Black & White",
                            onClick = { onColorModeChange("Black & White") },
                            modifier = Modifier.weight(1f)
                        )
                        SelectableChip(
                            label = "Full Color (₹10)",
                            isSelected = uploadState.colorMode == "Color",
                            onClick = { onColorModeChange("Color") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Paper Size Choice
                    Text(
                        text = "Paper Size:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate800
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("A4", "A3", "Legal").forEach { size ->
                            SelectableChip(
                                label = size,
                                isSelected = uploadState.paperSize == size,
                                onClick = { onPaperSizeChange(size) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Double sided switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Print Double-Sided (Back to Back)",
                            fontSize = 13.sp,
                            color = Slate800
                        )
                        Switch(
                            checked = uploadState.isDoubleSided,
                            onCheckedChange = onDoubleSidedChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NavyPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Special instructions / notes
                    OutlinedTextField(
                        value = uploadState.notes,
                        onValueChange = onNotesChange,
                        label = { Text("Special Editing or Print Instructions (Optional)") },
                        placeholder = { Text("e.g. Print page 1 to 5 only, need spiral binding") },
                        minLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upload_notes_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NavyPrimary,
                            focusedLabelColor = NavyPrimary
                        )
                    )
                }
            }
        }

        // Section 4: Submit Button
        item {
            val estimatedRate = if (uploadState.colorMode == "Color") 10.0 else 2.0
            val estimatedTotal = estimatedRate * uploadState.copies

            Button(
                onClick = onSubmitUpload,
                enabled = !uploadState.isUploading,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_upload_button")
            ) {
                if (uploadState.isUploading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Uploading & Syncing to Drive...", fontSize = 15.sp)
                } else {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Submit Print Job (Est. ₹${estimatedTotal.toInt()})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun SelectableChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isSelected) NavyPrimary else Slate100,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .border(
                1.dp,
                if (isSelected) NavyPrimary else Slate300,
                RoundedCornerShape(8.dp)
            )
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else Slate800,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CustomerSearchTabContent(
    documents: List<PrintDocument>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onDownload: (PrintDocument) -> Unit,
    onShareWhatsApp: (PrintDocument) -> Unit
) {
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
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SEARCH YOUR DOCUMENTS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Enter your phone number or name to find, download, or share your documents.",
                        fontSize = 13.sp,
                        color = Slate600
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = NavyPrimary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Slate400)
                                }
                            }
                        },
                        placeholder = { Text("Search by Phone Number or Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_search_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NavyPrimary,
                            focusedLabelColor = NavyPrimary
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
                        .padding(vertical = 20.dp),
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
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Slate400,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No documents found for '$searchQuery'" else "No documents in portal",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate800,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Check that you entered the exact phone number or name used during upload.",
                            fontSize = 12.sp,
                            color = Slate600,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(documents, key = { it.id }) { doc ->
                CustomerDocumentItemCard(
                    document = doc,
                    onDownload = { onDownload(doc) },
                    onShareWhatsApp = { onShareWhatsApp(doc) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun CustomerDocumentItemCard(
    document: PrintDocument,
    onDownload: () -> Unit,
    onShareWhatsApp: () -> Unit
) {
    val timeFormatted = remember(document.timestamp) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(document.timestamp))
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
            .testTag("customer_doc_card_${document.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header with status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
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
                            text = document.storedFileName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$timeFormatted • ${FileUtils.formatFileSize(document.fileSize)}",
                            fontSize = 11.sp,
                            color = Slate400
                        )
                    }
                }

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = when (document.status) {
                            "Ready" -> "READY FOR PICKUP"
                            "Printing" -> "PRINTING NOW"
                            "Pending" -> "IN QUEUE"
                            else -> document.status.uppercase(Locale.getDefault())
                        },
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Specs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${document.copies} copy • ${document.colorMode} (${document.paperSize})",
                    fontSize = 12.sp,
                    color = Slate700
                )
                Text(
                    text = "₹${document.calculateCost()}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp), color = Slate200)

            // Customer Action Buttons: Download & WhatsApp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDownload,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("customer_download_button_${document.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = NavyPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Download", fontSize = 12.sp, color = NavyPrimary)
                }

                Button(
                    onClick = onShareWhatsApp,
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("customer_whatsapp_button_${document.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp", fontSize = 12.sp, color = Color.White)
                }
            }
        }
    }
}
