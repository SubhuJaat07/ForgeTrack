import { create } from 'zustand';
import { Client } from '@/types';

interface ClientStore {
  clients: Client[];
  addClient: (client: Client) => void;
  updateClient: (id: string, updates: Partial<Client>) => void;
  deleteClient: (id: string) => void;
  getClientById: (id: string) => Client | undefined;
  searchClients: (query: string) => Client[];
}

export const useClientStore = create<ClientStore>((set, get) => ({
  clients: [],

  addClient: (client) =>
    set((state) => ({ clients: [client, ...state.clients] })),

  updateClient: (id, updates) =>
    set((state) => ({
      clients: state.clients.map((client) =>
        client.id === id
          ? { ...client, ...updates, updatedAt: new Date().toISOString() }
          : client
      ),
    })),

  deleteClient: (id) =>
    set((state) => ({
      clients: state.clients.filter((client) => client.id !== id),
    })),

  getClientById: (id) => get().clients.find((client) => client.id === id),

  searchClients: (query) => {
    const q = query.toLowerCase();
    return get().clients.filter(
      (client) =>
        client.name.toLowerCase().includes(q) ||
        client.company.toLowerCase().includes(q) ||
        client.email.toLowerCase().includes(q) ||
        client.phone.includes(q)
    );
  },
}));
