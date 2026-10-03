package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.io.File
import java.net.URLEncoder

object WhatsAppHelper {

    /**
     * Shares a document file and accompanying details via WhatsApp (or chooser if not installed).
     */
    fun shareDocumentViaWhatsApp(
        context: Context,
        file: File,
        message: String
    ) {
        try {
            if (!file.exists()) {
                Toast.makeText(context, "File does not exist", Toast.LENGTH_SHORT).show()
                return
            }

            val fileUri = FileUtils.getShareableUri(context, file)
            val mimeType = FileUtils.getMimeType(file.name)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_TEXT, message)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage("com.whatsapp")
            }

            // Check if WhatsApp is installed
            val packageManager = context.packageManager
            if (shareIntent.resolveActivity(packageManager) != null) {
                context.startActivity(shareIntent)
            } else {
                // Fallback to standard share chooser
                val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                    type = mimeType
                    putExtra(Intent.EXTRA_STREAM, fileUri)
                    putExtra(Intent.EXTRA_TEXT, message)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(fallbackIntent, "Share Document via..."))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Could not open WhatsApp: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Opens a direct WhatsApp chat with prefilled message
     */
    fun openWhatsAppChat(
        context: Context,
        rawPhone: String,
        message: String = ""
    ) {
        try {
            val cleanPhone = rawPhone.replace("[^0-9]".toRegex(), "")
            val encodedMessage = URLEncoder.encode(message, "UTF-8")
            val url = if (cleanPhone.isNotBlank()) {
                "https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMessage"
            } else {
                "https://api.whatsapp.com/send?text=$encodedMessage"
            }
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Unable to launch WhatsApp", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Dials a phone number
     */
    fun callPhoneNumber(context: Context, rawPhone: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${rawPhone.trim()}")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Unable to dial phone", Toast.LENGTH_SHORT).show()
        }
    }
}
