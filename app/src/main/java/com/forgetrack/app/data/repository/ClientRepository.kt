package com.forgetrack.app.data.repository

import com.forgetrack.app.data.local.dao.ClientDao
import com.forgetrack.app.data.model.Client
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientRepository @Inject constructor(private val clientDao: ClientDao) {

    fun getAllClients(): Flow<List<Client>> = clientDao.getAllClients()

    fun getClientById(id: String): Flow<Client?> = clientDao.getClientById(id)

    fun searchClients(query: String): Flow<List<Client>> = clientDao.searchClients(query)

    suspend fun insertClient(client: Client) = clientDao.insertClient(client)

    suspend fun updateClient(client: Client) = clientDao.updateClient(client)

    suspend fun deleteClient(id: String) = clientDao.deleteClientById(id)
}
