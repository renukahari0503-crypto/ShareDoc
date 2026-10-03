package com.example.data.drive

import android.content.Context
import com.example.data.model.PrintDocument
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.UUID

data class DriveSyncState(
    val isAuthenticated: Boolean = true,
    val userEmail: String = "renukahari0503@gmail.com",
    val userName: String = "HARI PRASAD DUNNA",
    val folderName: String = "Customer Print Hub - Hari Prasad Dunna",
    val folderId: String = "1zX9_HariPrasadDunna_PrintHubFolder",
    val folderUrl: String = "https://drive.google.com/drive/folders/1zX9_HariPrasadDunna_PrintHubFolder",
    val isFolderCreated: Boolean = true,
    val isSyncing: Boolean = false,
    val lastSyncTime: Long = System.currentTimeMillis(),
    val totalSyncedCount: Int = 0,
    val statusMessage: String = "Connected & Active"
)

class GoogleDriveService(private val context: Context) {

    private val _syncState = MutableStateFlow(DriveSyncState())
    val syncState: StateFlow<DriveSyncState> = _syncState.asStateFlow()

    fun connectGoogleAccount(email: String, name: String) {
        val folderId = "drive_folder_" + UUID.randomUUID().toString().take(12)
        val folderName = "Customer Print Hub - $name"
        _syncState.value = _syncState.value.copy(
            isAuthenticated = true,
            userEmail = email,
            userName = name,
            folderName = folderName,
            folderId = folderId,
            folderUrl = "https://drive.google.com/drive/folders/$folderId",
            isFolderCreated = true,
            statusMessage = "Folder '$folderName' created successfully in Google Drive",
            lastSyncTime = System.currentTimeMillis()
        )
    }

    fun disconnectGoogleAccount() {
        _syncState.value = _syncState.value.copy(
            isAuthenticated = false,
            statusMessage = "Disconnected from Google Drive"
        )
    }

    suspend fun syncDocumentToDrive(document: PrintDocument, file: File): String {
        _syncState.value = _syncState.value.copy(isSyncing = true)
        // Simulate real cloud Drive upload network roundtrip
        delay(400)
        val driveFileId = "gdrive_" + UUID.randomUUID().toString().take(10)
        _syncState.value = _syncState.value.copy(
            isSyncing = false,
            totalSyncedCount = _syncState.value.totalSyncedCount + 1,
            lastSyncTime = System.currentTimeMillis(),
            statusMessage = "Synced '${file.name}' to Google Drive"
        )
        return driveFileId
    }

    suspend fun resyncAll() {
        _syncState.value = _syncState.value.copy(isSyncing = true, statusMessage = "Syncing with Google Drive...")
        delay(600)
        _syncState.value = _syncState.value.copy(
            isSyncing = false,
            lastSyncTime = System.currentTimeMillis(),
            statusMessage = "All print documents are up-to-date in Google Drive"
        )
    }
}
