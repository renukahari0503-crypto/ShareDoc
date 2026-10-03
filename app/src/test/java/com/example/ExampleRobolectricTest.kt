package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.PrintDocument
import com.example.data.model.ShopOwnerConfig
import com.example.util.FileUtils
import com.example.util.QRCodeGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Print Portal", appName)
    }

    @Test
    fun `test file naming convention using customer phone or name`() {
        val phoneTarget = FileUtils.generateTargetFileName("9866362137", "MyProject.pdf", "pdf")
        assertTrue("Generated filename should start with phone number", phoneTarget.startsWith("9866362137_"))
        assertTrue("Generated filename should end with extension", phoneTarget.endsWith(".pdf"))

        val nameTarget = FileUtils.generateTargetFileName("Hari Prasad", "Resume.docx", "docx")
        assertTrue("Generated filename should include sanitized name", nameTarget.startsWith("Hari_Prasad_"))
        assertTrue("Generated filename should end with docx", nameTarget.endsWith(".docx"))
    }

    @Test
    fun `test qr code generator produces valid bitmap`() {
        val qrBitmap = QRCodeGenerator.generateQRCode("https://printportal.app/upload?owner=9866362137", 200, 200)
        assertNotNull(qrBitmap)
        assertEquals(200, qrBitmap!!.width)
        assertEquals(200, qrBitmap.height)
    }

    @Test
    fun `test print document cost calculation`() {
        val bwDoc = PrintDocument(
            customerName = "Ravi",
            customerPhone = "9848022338",
            originalFileName = "Doc.pdf",
            storedFileName = "9848022338_Doc.pdf",
            filePath = "/tmp/test.pdf",
            fileSize = 1000,
            mimeType = "application/pdf",
            copies = 3,
            colorMode = "Black & White"
        )
        assertEquals(6.0, bwDoc.calculateCost(bwRatePerPage = 2.0, colorRatePerPage = 10.0), 0.01)

        val colorDoc = bwDoc.copy(colorMode = "Color", copies = 2)
        assertEquals(20.0, colorDoc.calculateCost(bwRatePerPage = 2.0, colorRatePerPage = 10.0), 0.01)
    }

    @Test
    fun `test shop owner configuration defaults`() {
        val config = ShopOwnerConfig()
        assertEquals("HARI PRASAD DUNNA", config.ownerName)
        assertEquals("+91 9866362137", config.ownerPhone)
    }

    @Test
    fun `test google drive folder qr code generation`() {
        val driveFolderUrl = "https://drive.google.com/drive/folders/1zX9_HariPrasadDunna_PrintHubFolder"
        val qrBitmap = QRCodeGenerator.generateQRCode(driveFolderUrl, 250, 250)
        assertNotNull(qrBitmap)
        assertEquals(250, qrBitmap!!.width)
        assertEquals(250, qrBitmap.height)
    }
}
