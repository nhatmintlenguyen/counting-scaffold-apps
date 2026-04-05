package com.example.dadn_app.data.repository

import com.example.dadn_app.data.local.ScanDao
import com.example.dadn_app.data.local.ScanRecord
import kotlinx.coroutines.flow.Flow

class ScanRepository(private val dao: ScanDao) {

    /** Live list of scans from the DB, newest first. */
    val scans: Flow<List<ScanRecord>> = dao.getAll()

    suspend fun insert(scan: ScanRecord) = dao.insert(scan)
}
