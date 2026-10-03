package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "print_documents")
data class PrintDocument(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerName: String,
    val customerPhone: String,
    val originalFileName: String,
    val storedFileName: String,
    val filePath: String,
    val fileSize: Long,
    val mimeType: String,
    val timestamp: Long = System.currentTimeMillis(),
    val copies: Int = 1,
    val colorMode: String = "Black & White", // "Black & White" or "Color"
    val paperSize: String = "A4",           // "A4", "A3", "Letter"
    val isDoubleSided: Boolean = false,
    val notes: String = "",
    val status: String = "Pending",          // "Pending", "Printing", "Ready", "Delivered"
    val googleDriveFileId: String? = null,
    val googleDriveFolderId: String? = "drive_folder_customer_print_hub",
    val isSyncedToDrive: Boolean = true
) {
    val displayName: String
        get() = if (customerPhone.isNotBlank()) {
            customerPhone
        } else if (customerName.isNotBlank()) {
            customerName
        } else {
            "Guest"
        }

    fun calculateCost(bwRatePerPage: Double = 2.0, colorRatePerPage: Double = 10.0): Double {
        val rate = if (colorMode.contains("Color", ignoreCase = true)) colorRatePerPage else bwRatePerPage
        // estimated 1 page minimum if unknown
        return rate * copies
    }
}
