package com.template.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.sync.DataSyncManager
import com.template.app.core.utils.AppEventManager
import com.template.app.domain.usecase.GetSettingsUseCase
import com.template.app.presentation.ui.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val dataSyncManager: DataSyncManager,
    val appEventManager: AppEventManager
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            getSettingsUseCase().collectLatest { settings ->
                val isValid = settings.baseUrl.isNotBlank() && settings.apiToken.isNotBlank()
                
                // Set initial destination only once
                if (_startDestination.value == null) {
                    _startDestination.value = if (isValid) Routes.MAIN else Routes.ONBOARDING
                }

                if (isValid) {
                    dataSyncManager.startSync()
                } else {
                    dataSyncManager.stopSync()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        dataSyncManager.stopSync()
    }
}
