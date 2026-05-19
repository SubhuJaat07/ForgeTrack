package com.forgetrack.app.ui.screens.clients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgetrack.app.data.model.Client
import com.forgetrack.app.data.repository.ClientRepository
import com.forgetrack.app.util.generateId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ClientsViewModel @Inject constructor(
    private val clientRepository: ClientRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    /**
     * Clients list automatically filtered by the current search query.
     * Falls back to full list when query is blank.
     */
    val clients: StateFlow<List<Client>> = combine(
        clientRepository.getAllClients(),
        _searchQuery.debounce(200)
    ) { allClients, query ->
        if (query.isBlank()) {
            allClients
        } else {
            val q = query.lowercase().trim()
            allClients.filter {
                it.name.lowercase().contains(q) ||
                    it.company.lowercase().contains(q) ||
                    it.email.lowercase().contains(q) ||
                    it.phone.lowercase().contains(q) ||
                    it.address.lowercase().contains(q)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /**
     * Adds a new client to the repository.
     * Generates a unique ID if one is not already provided.
     */
    fun addClient(uiState: ClientUiState) {
        viewModelScope.launch {
            val client = Client(
                id = uiState.id.ifBlank { generateId() },
                name = uiState.name.trim(),
                company = uiState.company.trim(),
                email = uiState.email.trim(),
                phone = uiState.phone.trim(),
                address = uiState.address.trim(),
                notes = uiState.notes.trim(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            clientRepository.insertClient(client)
        }
    }

    /**
     * Updates an existing client. The updatedAt timestamp is refreshed.
     */
    fun updateClient(client: Client) {
        viewModelScope.launch {
            clientRepository.updateClient(
                client.copy(updatedAt = System.currentTimeMillis())
            )
        }
    }

    /**
     * Permanently deletes a client from the repository.
     */
    fun deleteClient(client: Client) {
        viewModelScope.launch {
            clientRepository.deleteClient(client.id)
        }
    }

    /**
     * Updates the search query used to filter the clients list.
     * A 200ms debounce prevents excessive recomputation while typing.
     */
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}
