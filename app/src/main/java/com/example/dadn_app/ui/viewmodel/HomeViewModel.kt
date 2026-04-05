package com.example.dadn_app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dadn_app.data.local.AppDatabase
import com.example.dadn_app.data.local.ScanRecord
import com.example.dadn_app.data.repository.ScanRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = ScanRepository(AppDatabase.getInstance(app).scanDao())

    /** Observed by the UI — updates automatically whenever the DB changes. */
    val scans: StateFlow<List<ScanRecord>> = repo.scans.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun addScan(scan: ScanRecord) {
        viewModelScope.launch { repo.insert(scan) }
    }
}
