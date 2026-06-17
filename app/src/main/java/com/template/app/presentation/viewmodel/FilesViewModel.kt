package com.template.app.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.utils.Resource
import com.template.app.domain.model.VelaBreadcrumb
import com.template.app.domain.model.VelaDiskUsage
import com.template.app.domain.model.VelaFileInfo
import com.template.app.domain.repository.VelaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class FilesState(
    val currentPath: String = "",
    val parentPath: String? = null,
    val files: List<VelaFileInfo> = emptyList(),
    val disks: List<VelaDiskUsage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val isUploading: Boolean = false,
    val isPerformingAction: Boolean = false,
    val showHidden: Boolean = false,
    val breadcrumbs: List<VelaBreadcrumb> = emptyList()
)

@HiltViewModel
class FilesViewModel @Inject constructor(
    private val repository: VelaRepository
) : ViewModel() {

    private val _currentPath = MutableStateFlow<String?>(null)
    
    private val _state = MutableStateFlow(FilesState())
    val state: StateFlow<FilesState> = _state.asStateFlow()

    init {
        loadDisks()
        
        repository.observeDisks()
            .onEach { disks -> _state.update { it.copy(disks = disks) } }
            .launchIn(viewModelScope)

        @OptIn(ExperimentalCoroutinesApi::class)
        _currentPath
            .filterNotNull()
            .distinctUntilChanged()
            .flatMapLatest { path -> 
                Log.d("FilesViewModel", "Observing path: $path")
                repository.observeFiles(path) 
            }
            .onEach { files -> 
                Log.d("FilesViewModel", "Observer emitted ${files.size} files for current path")
                _state.update { it.copy(files = files) } 
            }
            .launchIn(viewModelScope)
    }

    fun loadFiles(path: String?, showHidden: Boolean = _state.value.showHidden) {
        val requestedPath = path ?: ""
        
        // Step 1: Set the loading state but don't switch the observer yet 
        // if we are already observing this path (to prevent flickering)
        _state.update { it.copy(isLoading = true, error = null) }
        
        // Step 2: Update the observer to the new target path immediately to show cache
        _currentPath.value = requestedPath

        viewModelScope.launch {
            val result = repository.listFiles(path, showHidden)
            if (result is Resource.Success) {
                val data = result.data
                val actualPath = data.currentPath
                
                // Step 3: Sync internal state with the ACTUAL path returned by the server
                // This ensures that if the server redirects us (e.g. following a symlink), 
                // our observer and UI stay in sync.
                if (actualPath != _currentPath.value) {
                    _currentPath.value = actualPath
                }

                _state.update { it.copy(
                    currentPath = actualPath,
                    parentPath = data.parentPath,
                    showHidden = data.showHidden
                ) }
                
                updateBreadcrumbs(actualPath)
            } else if (result is Resource.Error) {
                _state.update { it.copy(error = result.message) }
            }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun updateBreadcrumbs(path: String) {
        if (path.isBlank()) {
            _state.update { it.copy(breadcrumbs = emptyList()) }
            return
        }
        val result = repository.getFileTree(path, maxDepth = 1)
        if (result is Resource.Success) {
            _state.update { it.copy(breadcrumbs = result.data.breadcrumbs) }
        }
    }

    private fun loadDisks() {
        viewModelScope.launch {
            repository.getDiskUsage()
        }
    }

    fun navigateTo(path: String) {
        loadFiles(path)
    }

    fun navigateUp() {
        val parent = _state.value.parentPath
        if (parent != null) {
            loadFiles(parent)
        } else {
            val current = _state.value.currentPath
            if (current.isNotBlank() && current != "/") {
                val manualParent = current.substringBeforeLast("/", "").ifBlank { "/" }
                loadFiles(manualParent)
            } else if (current.isNotBlank()) {
                loadFiles(null)
            }
        }
    }

    fun toggleShowHidden() {
        loadFiles(_state.value.currentPath, !_state.value.showHidden)
    }

    fun setSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun createDirectory(name: String) {
        viewModelScope.launch {
            _state.update { it.copy(isPerformingAction = true) }
            val current = _state.value.currentPath
            val path = if (current.isEmpty()) name else if (current.endsWith("/")) "$current$name" else "$current/$name"
            val result = repository.makeDirectory(path)
            if (result is Resource.Success) {
                loadFiles(_currentPath.value)
            } else if (result is Resource.Error) {
                _state.update { it.copy(error = result.message) }
            }
            _state.update { it.copy(isPerformingAction = false) }
        }
    }

    fun deleteFile(file: VelaFileInfo) {
        viewModelScope.launch {
            _state.update { it.copy(isPerformingAction = true) }
            val result = repository.deleteFile(file.path)
            if (result is Resource.Success) {
                loadFiles(_currentPath.value)
            } else if (result is Resource.Error) {
                _state.update { it.copy(error = result.message) }
            }
            _state.update { it.copy(isPerformingAction = false) }
        }
    }

    fun renameFile(file: VelaFileInfo, newName: String) {
        viewModelScope.launch {
            _state.update { it.copy(isPerformingAction = true) }
            val parentPath = file.path.substringBeforeLast("/", "")
            val newPath = if (parentPath.isEmpty()) newName else "$parentPath/$newName"
            val result = repository.renameFile(file.path, newPath)
            if (result is Resource.Success) {
                loadFiles(_currentPath.value)
            } else if (result is Resource.Error) {
                _state.update { it.copy(error = result.message) }
            }
            _state.update { it.copy(isPerformingAction = false) }
        }
    }

    fun uploadFile(localFile: File) {
        viewModelScope.launch {
            _state.update { it.copy(isUploading = true) }
            val result = repository.uploadFile(_state.value.currentPath, localFile)
            if (result is Resource.Success) {
                loadFiles(_currentPath.value)
                localFile.delete()
            } else if (result is Resource.Error) {
                _state.update { it.copy(error = result.message) }
            }
            _state.update { it.copy(isUploading = false) }
        }
    }

    fun openFile(file: VelaFileInfo) {
        viewModelScope.launch {
            repository.openFile(file.path)
        }
    }

    fun zipFiles(files: List<VelaFileInfo>, outputName: String) {
        viewModelScope.launch {
            _state.update { it.copy(isPerformingAction = true) }
            val paths = files.map { it.path }
            val current = _state.value.currentPath
            val outputPath = if (current.isEmpty()) outputName else if (current.endsWith("/")) "$current$outputName" else "$current/$outputName"
            val result = repository.zipFiles(paths, outputPath)
            if (result is Resource.Success) {
                loadFiles(_currentPath.value)
            } else if (result is Resource.Error) {
                _state.update { it.copy(error = result.message) }
            }
            _state.update { it.copy(isPerformingAction = false) }
        }
    }

    fun unzipFile(file: VelaFileInfo) {
        viewModelScope.launch {
            _state.update { it.copy(isPerformingAction = true) }
            val destination = file.path.removeSuffix(".zip")
            val result = repository.unzipFile(file.path, destination)
            if (result is Resource.Success) {
                loadFiles(_currentPath.value)
            } else if (result is Resource.Error) {
                _state.update { it.copy(error = result.message) }
            }
            _state.update { it.copy(isPerformingAction = false) }
        }
    }
}
