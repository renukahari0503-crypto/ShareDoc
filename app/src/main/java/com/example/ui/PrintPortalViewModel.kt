package com.example.ui

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.drive.DriveSyncState
import com.example.data.drive.GoogleDriveService
import com.example.data.local.AppDatabase
import com.example.data.model.PrintDocument
import com.example.data.model.ShopOwnerConfig
import com.example.util.FileUtils
import com.example.util.WhatsAppHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class UserRole {
    SHOP_OWNER,
    CUSTOMER
}

enum class OwnerTab {
    QUEUE,
    QR_CODE,
    DRIVE_SYNC,
    SETTINGS
}

enum class CustomerTab {
    UPLOAD,
    MY_DOCS
}

data class UploadFormState(
    val selectedFileUri: Uri? = null,
    val originalFileName: String = "",
    val fileSize: Long = 0L,
    val customerPhoneOrName: String = "",
    val previewTargetName: String = "",
    val copies: Int = 1,
    val colorMode: String = "Black & White",
    val paperSize: String = "A4",
    val isDoubleSided: Boolean = false,
    val notes: String = "",
    val isUploading: Boolean = false,
    val uploadSuccess: Boolean = false,
    val lastUploadedDoc: PrintDocument? = null,
    val errorMessage: String? = null
)

class PrintPortalViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val dao = db.printDocumentDao()
    private val driveService = GoogleDriveService(application)

    private val _currentRole = MutableStateFlow(UserRole.SHOP_OWNER)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _ownerTab = MutableStateFlow(OwnerTab.QUEUE)
    val ownerTab: StateFlow<OwnerTab> = _ownerTab.asStateFlow()

    private val _customerTab = MutableStateFlow(CustomerTab.UPLOAD)
    val customerTab: StateFlow<CustomerTab> = _customerTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow("All")
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    private val _uploadFormState = MutableStateFlow(UploadFormState())
    val uploadFormState: StateFlow<UploadFormState> = _uploadFormState.asStateFlow()

    private val _ownerConfig = MutableStateFlow(ShopOwnerConfig())
    val ownerConfig: StateFlow<ShopOwnerConfig> = _ownerConfig.asStateFlow()

    val driveSyncState: StateFlow<DriveSyncState> = driveService.syncState

    // Live list of all documents with search and status filtering
    val documents: StateFlow<List<PrintDocument>> = combine(
        _searchQuery,
        _statusFilter
    ) { query, filter ->
        Pair(query.trim(), filter)
    }.flatMapLatest { (query, filter) ->
        if (query.isNotBlank()) {
            dao.searchDocuments(query)
        } else if (filter != "All") {
            dao.getDocumentsByStatus(filter)
        } else {
            dao.getAllDocuments()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Seed a demo initial item so the app is immediately alive and demonstrative
        viewModelScope.launch {
            val existing = dao.getDocumentById(1)
            if (existing == null) {
                val dummyFile = FileUtils.createSampleDocument(
                    getApplication(),
                    "9866362137",
                    "pdf"
                )
                dao.insertDocument(
                    PrintDocument(
                        customerName = "Ravi Kumar",
                        customerPhone = "+91 9848022338",
                        originalFileName = "Project_Proposal.pdf",
                        storedFileName = "RaviKumar_Project_Proposal.pdf",
                        filePath = dummyFile.absolutePath,
                        fileSize = 145000,
                        mimeType = "application/pdf",
                        copies = 2,
                        colorMode = "Black & White",
                        paperSize = "A4",
                        isDoubleSided = true,
                        notes = "Spiral binding required",
                        status = "Pending",
                        googleDriveFileId = "gdrive_demo_init_01",
                        isSyncedToDrive = true
                    )
                )
            }
        }
    }

    fun setRole(role: UserRole) {
        _currentRole.value = role
    }

    fun setOwnerTab(tab: OwnerTab) {
        _ownerTab.value = tab
    }

    fun setCustomerTab(tab: CustomerTab) {
        _customerTab.value = tab
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(filter: String) {
        _statusFilter.value = filter
    }

    fun onCustomerPhoneOrNameChanged(value: String) {
        val current = _uploadFormState.value
        val ext = current.originalFileName.substringAfterLast('.', "pdf")
        val preview = if (value.isNotBlank()) {
            FileUtils.generateTargetFileName(value, current.originalFileName, ext)
        } else ""
        _uploadFormState.value = current.copy(
            customerPhoneOrName = value,
            previewTargetName = preview,
            errorMessage = null
        )
    }

    fun onFileSelected(uri: Uri, name: String, size: Long) {
        val current = _uploadFormState.value
        val ext = name.substringAfterLast('.', "pdf")
        val preview = if (current.customerPhoneOrName.isNotBlank()) {
            FileUtils.generateTargetFileName(current.customerPhoneOrName, name, ext)
        } else {
            name
        }
        _uploadFormState.value = current.copy(
            selectedFileUri = uri,
            originalFileName = name,
            fileSize = size,
            previewTargetName = preview,
            errorMessage = null,
            uploadSuccess = false
        )
    }

    fun setCopies(copies: Int) {
        _uploadFormState.value = _uploadFormState.value.copy(copies = copies.coerceAtLeast(1))
    }

    fun setColorMode(mode: String) {
        _uploadFormState.value = _uploadFormState.value.copy(colorMode = mode)
    }

    fun setPaperSize(size: String) {
        _uploadFormState.value = _uploadFormState.value.copy(paperSize = size)
    }

    fun setDoubleSided(doubleSided: Boolean) {
        _uploadFormState.value = _uploadFormState.value.copy(isDoubleSided = doubleSided)
    }

    fun setNotes(notes: String) {
        _uploadFormState.value = _uploadFormState.value.copy(notes = notes)
    }

    fun resetUploadForm() {
        _uploadFormState.value = UploadFormState()
    }

    /**
     * Uploads the selected file or creates a sample document
     */
    fun submitUpload() {
        val state = _uploadFormState.value
        val identifier = state.customerPhoneOrName.trim()

        if (identifier.isBlank()) {
            _uploadFormState.value = state.copy(errorMessage = "Please enter your Phone Number or Name")
            return
        }

        viewModelScope.launch {
            _uploadFormState.value = state.copy(isUploading = true, errorMessage = null)
            try {
                val context = getApplication<Application>()
                val savedFile: File
                val origName: String

                if (state.selectedFileUri != null) {
                    val result = FileUtils.savePickedFile(context, state.selectedFileUri, identifier)
                    savedFile = result.first
                    origName = result.second
                } else {
                    // Create simulated sample test document if no file picked
                    savedFile = FileUtils.createSampleDocument(context, identifier, "pdf")
                    origName = "Document_${identifier}.pdf"
                }

                val isPhone = identifier.matches("^[0-9+ ]{7,15}$".toRegex())
                val custPhone = if (isPhone) identifier else ""
                val custName = if (!isPhone) identifier else "Customer"

                val doc = PrintDocument(
                    customerName = custName,
                    customerPhone = custPhone,
                    originalFileName = origName,
                    storedFileName = savedFile.name,
                    filePath = savedFile.absolutePath,
                    fileSize = savedFile.length(),
                    mimeType = FileUtils.getMimeType(savedFile.name),
                    copies = state.copies,
                    colorMode = state.colorMode,
                    paperSize = state.paperSize,
                    isDoubleSided = state.isDoubleSided,
                    notes = state.notes,
                    status = "Pending"
                )

                // Sync to Google Drive
                val driveFileId = driveService.syncDocumentToDrive(doc, savedFile)
                val finalDoc = doc.copy(googleDriveFileId = driveFileId, isSyncedToDrive = true)

                val newId = dao.insertDocument(finalDoc)
                val inserted = finalDoc.copy(id = newId)

                _uploadFormState.value = UploadFormState(
                    uploadSuccess = true,
                    lastUploadedDoc = inserted
                )
                Toast.makeText(context, "Document uploaded & saved as ${savedFile.name}", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                _uploadFormState.value = state.copy(
                    isUploading = false,
                    errorMessage = "Upload failed: ${e.localizedMessage}"
                )
            }
        }
    }

    fun updatePrintStatus(doc: PrintDocument, newStatus: String) {
        viewModelScope.launch {
            val updated = doc.copy(status = newStatus)
            dao.updateDocument(updated)
        }
    }

    fun deleteDocument(doc: PrintDocument) {
        viewModelScope.launch {
            val file = File(doc.filePath)
            if (file.exists()) {
                file.delete()
            }
            dao.deleteDocument(doc)
            Toast.makeText(getApplication(), "Document deleted", Toast.LENGTH_SHORT).show()
        }
    }

    fun downloadDocument(doc: PrintDocument) {
        val file = File(doc.filePath)
        val context = getApplication<Application>()
        if (file.exists()) {
            val success = FileUtils.downloadToPublicDownloads(context, file)
            if (success) {
                Toast.makeText(context, "Saved to Downloads: ${file.name}", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Failed to save file", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "File not found locally", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareViaWhatsApp(doc: PrintDocument) {
        val file = File(doc.filePath)
        val context = getApplication<Application>()
        val message = """
            *Customer Print Portal*
            📄 Document: ${doc.storedFileName}
            👤 Customer: ${doc.displayName}
            🖨️ Copies: ${doc.copies} | Mode: ${doc.colorMode} (${doc.paperSize})
            💰 Total: ₹${doc.calculateCost()}
            Owner: ${_ownerConfig.value.ownerName} (${_ownerConfig.value.ownerPhone})
        """.trimIndent()
        WhatsAppHelper.shareDocumentViaWhatsApp(context, file, message)
    }

    fun sendCustomerWhatsAppUpdate(doc: PrintDocument) {
        val context = getApplication<Application>()
        val phone = doc.customerPhone.ifBlank { _ownerConfig.value.ownerPhone }
        val msg = """
            Hello ${doc.displayName}!
            Your print order *${doc.storedFileName}* is *${doc.status}* at *${_ownerConfig.value.shopName}*.
            Total Amount: ₹${doc.calculateCost()}
            Copies: ${doc.copies} (${doc.colorMode})
            Thank you! - Hari Prasad Dunna
        """.trimIndent()
        WhatsAppHelper.openWhatsAppChat(context, phone, msg)
    }

    fun connectGoogleAccount(email: String, name: String) {
        driveService.connectGoogleAccount(email, name)
        _ownerConfig.value = _ownerConfig.value.copy(
            ownerEmail = email,
            ownerName = name,
            isGoogleDriveConnected = true
        )
        Toast.makeText(getApplication(), "Connected as $name ($email)", Toast.LENGTH_SHORT).show()
    }

    fun disconnectGoogleAccount() {
        driveService.disconnectGoogleAccount()
        _ownerConfig.value = _ownerConfig.value.copy(isGoogleDriveConnected = false)
        Toast.makeText(getApplication(), "Google Drive disconnected", Toast.LENGTH_SHORT).show()
    }

    fun resyncGoogleDrive() {
        viewModelScope.launch {
            driveService.resyncAll()
            Toast.makeText(getApplication(), "Drive synchronized", Toast.LENGTH_SHORT).show()
        }
    }
}
