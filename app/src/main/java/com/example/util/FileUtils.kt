package com.example.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileUtils {

    /**
     * Resolves display name from Content URI
     */
    fun getFileNameFromUri(context: Context, uri: Uri): String {
        var name = "document"
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    name = it.getString(index)
                }
            }
        }
        return name
    }

    /**
     * Resolves file extension from name or mime type
     */
    fun getFileExtension(context: Context, uri: Uri, fallbackName: String): String {
        val extensionFromName = fallbackName.substringAfterLast('.', "")
        if (extensionFromName.isNotBlank() && extensionFromName != fallbackName) {
            return extensionFromName
        }
        val mimeType = context.contentResolver.getType(uri)
        if (mimeType != null) {
            val extensionFromMime = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
            if (!extensionFromMime.isNullOrBlank()) {
                return extensionFromMime
            }
        }
        return "pdf"
    }

    /**
     * Computes the automatic file name according to user specification:
     * "The uploaded file is automatically named using the provided phone number or name."
     */
    fun generateTargetFileName(
        customerPhoneOrName: String,
        originalFileName: String,
        extension: String
    ): String {
        val cleanIdentifier = customerPhoneOrName
            .trim()
            .replace("[^a-zA-Z0-9_+]".toRegex(), "_")
            .ifBlank { "CustomerDoc" }
        
        val ext = if (extension.startsWith(".")) extension else ".$extension"
        val timeTag = SimpleDateFormat("MMdd_HHmm", Locale.getDefault()).format(Date())
        return "${cleanIdentifier}_${timeTag}$ext"
    }

    /**
     * Copies picked URI into internal storage and returns the local file
     */
    fun savePickedFile(
        context: Context,
        uri: Uri,
        customerPhoneOrName: String
    ): Pair<File, String> {
        val originalName = getFileNameFromUri(context, uri)
        val ext = getFileExtension(context, uri, originalName)
        val targetFileName = generateTargetFileName(customerPhoneOrName, originalName, ext)

        val uploadsDir = File(context.filesDir, "customer_uploads")
        if (!uploadsDir.exists()) {
            uploadsDir.mkdirs()
        }

        val destinationFile = File(uploadsDir, targetFileName)
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(destinationFile)

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        return Pair(destinationFile, originalName)
    }

    /**
     * Creates a dummy/sample test document if the user wants to test upload without device files
     */
    fun createSampleDocument(context: Context, customerPhoneOrName: String, docType: String = "pdf"): File {
        val uploadsDir = File(context.filesDir, "customer_uploads")
        if (!uploadsDir.exists()) {
            uploadsDir.mkdirs()
        }
        val targetFileName = generateTargetFileName(customerPhoneOrName, "SampleDoc.$docType", docType)
        val file = File(uploadsDir, targetFileName)
        file.writeText(
            """
            =========================================
            CUSTOMER PRINT PORTAL - PRINT JOB
            =========================================
            Customer: $customerPhoneOrName
            Created: ${Date()}
            Shop Owner: HARI PRASAD DUNNA (+91 9866362137)
            Folder: Customer Print Hub (Google Drive)
            Status: Ready for printing
            =========================================
            This document was generated for print testing.
            =========================================
            """.trimIndent()
        )
        return file
    }

    /**
     * Gets a sharable Uri through Android FileProvider
     */
    fun getShareableUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    /**
     * Saves/copies a document to device's public Downloads directory
     */
    fun downloadToPublicDownloads(context: Context, file: File): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
                    put(MediaStore.MediaColumns.MIME_TYPE, getMimeType(file.name))
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/CustomerPrintPortal")
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        file.inputStream().use { input ->
                            input.copyTo(out)
                        }
                    }
                    true
                } else {
                    false
                }
            } else {
                @Suppress("DEPRECATION")
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val targetDir = File(downloadsDir, "CustomerPrintPortal")
                if (!targetDir.exists()) targetDir.mkdirs()
                val target = File(targetDir, file.name)
                file.copyTo(target, overwrite = true)
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getMimeType(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "")
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.lowercase(Locale.getDefault()))
            ?: "application/octet-stream"
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        val index = digitGroups.coerceIn(0, units.size - 1)
        val value = bytes / Math.pow(1024.0, index.toDouble())
        return DecimalFormat("#,##0.#").format(value) + " " + units[index]
    }
}
