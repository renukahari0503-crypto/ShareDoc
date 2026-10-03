package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PrintDocument
import kotlinx.coroutines.flow.Flow

@Dao
interface PrintDocumentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: PrintDocument): Long

    @Update
    suspend fun updateDocument(document: PrintDocument)

    @Delete
    suspend fun deleteDocument(document: PrintDocument)

    @Query("SELECT * FROM print_documents ORDER BY timestamp DESC")
    fun getAllDocuments(): Flow<List<PrintDocument>>

    @Query("""
        SELECT * FROM print_documents 
        WHERE customerPhone LIKE '%' || :query || '%' 
           OR customerName LIKE '%' || :query || '%' 
           OR storedFileName LIKE '%' || :query || '%'
           OR originalFileName LIKE '%' || :query || '%'
        ORDER BY timestamp DESC
    """)
    fun searchDocuments(query: String): Flow<List<PrintDocument>>

    @Query("SELECT * FROM print_documents WHERE status = :status ORDER BY timestamp DESC")
    fun getDocumentsByStatus(status: String): Flow<List<PrintDocument>>

    @Query("SELECT * FROM print_documents WHERE id = :id LIMIT 1")
    suspend fun getDocumentById(id: Long): PrintDocument?

    @Query("SELECT COUNT(*) FROM print_documents")
    fun getTotalDocumentCount(): Flow<Int>
}
