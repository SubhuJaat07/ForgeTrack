import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
  TextInput,
  useColorScheme,
  Alert,
  Modal,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import Animated, { FadeIn, FadeInDown } from 'react-native-reanimated';
import { Ionicons, Feather, FontAwesome5 } from '@expo/vector-icons';
import { useClientStore } from '@/stores/useClientStore';
import { Client } from '@/types';

export default function ClientsScreen() {
  const colorScheme = useColorScheme();
  const isDark = colorScheme === 'dark';
  const { clients, addClient, updateClient, deleteClient, searchClients } = useClientStore();
  const [search, setSearch] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editingClient, setEditingClient] = useState<Client | null>(null);
  const [form, setForm] = useState({
    name: '',
    email: '',
    phone: '',
    company: '',
    address: '',
    notes: '',
  });

  const filteredClients = search ? searchClients(search) : clients;

  const openCreateModal = () => {
    setEditingClient(null);
    setForm({ name: '', email: '', phone: '', company: '', address: '', notes: '' });
    setShowModal(true);
  };

  const openEditModal = (client: Client) => {
    setEditingClient(client);
    setForm({
      name: client.name,
      email: client.email,
      phone: client.phone,
      company: client.company,
      address: client.address,
      notes: client.notes,
    });
    setShowModal(true);
  };

  const handleSave = () => {
    if (!form.name.trim()) {
      Alert.alert('Error', 'Please enter a client name');
      return;
    }

    if (editingClient) {
      updateClient(editingClient.id, form);
    } else {
      const client: Client = {
        id: `client_${Date.now()}`,
        ...form,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };
      addClient(client);
    }
    setShowModal(false);
  };

  const handleDelete = (id: string) => {
    Alert.alert('Delete Client?', 'This will not delete associated jobs.', [
      { text: 'Cancel', style: 'cancel' },
      { text: 'Delete', style: 'destructive', onPress: () => deleteClient(id) },
    ]);
  };

  const textColor = isDark ? '#e2e8f0' : '#1e293b';
  const mutedColor = isDark ? '#94a3b8' : '#64748b';
  const cardBg = isDark ? '#1a1a2e' : '#ffffff';
  const inputBg = isDark ? '#252542' : '#f1f5f9';

  const renderClient = ({ item, index }: { item: Client; index: number }) => (
    <Animated.View entering={FadeInDown.delay(index * 50).duration(300)}>
      <TouchableOpacity
        style={[styles.clientCard, { backgroundColor: cardBg }]}
        onPress={() => openEditModal(item)}
        activeOpacity={0.7}
      >
        <View style={styles.avatar}>
          <Text style={{ fontSize: 18, fontWeight: '700', color: '#fff' }}>
            {item.name.charAt(0).toUpperCase()}
          </Text>
        </View>
        <View style={{ flex: 1, marginLeft: 12 }}>
          <Text style={{ fontSize: 15, fontWeight: '600', color: textColor }} numberOfLines={1}>
            {item.name}
          </Text>
          {item.company ? (
            <Text style={{ fontSize: 12, color: '#6C5CE7', fontWeight: '500' }} numberOfLines={1}>
              {item.company}
            </Text>
          ) : null}
          <Text style={{ fontSize: 12, color: mutedColor, marginTop: 2 }} numberOfLines={1}>
            {item.phone} {item.email ? `• ${item.email}` : ''}
          </Text>
        </View>
        <TouchableOpacity onPress={() => handleDelete(item.id)} style={styles.deleteBtn}>
          <Ionicons name="trash-outline" size={18} color="#ff6b6b" />
        </TouchableOpacity>
      </TouchableOpacity>
    </Animated.View>
  );

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: isDark ? '#0f0f1a' : '#f8fafc' }]}>
      <Animated.View entering={FadeIn.duration(400)} style={styles.header}>
        <View>
          <Text style={{ fontSize: 26, fontWeight: '800', color: textColor }}>Clients</Text>
          <Text style={{ fontSize: 13, color: mutedColor }}>{clients.length} total clients</Text>
        </View>
        <TouchableOpacity style={styles.createBtn} onPress={openCreateModal}>
          <Ionicons name="add" size={22} color="#fff" />
        </TouchableOpacity>
      </Animated.View>

      <View style={[styles.searchBox, { backgroundColor: cardBg }]}>
        <Feather name="search" size={18} color={mutedColor} />
        <TextInput
          style={[styles.searchInput, { color: textColor }]}
          placeholder="Search clients..."
          placeholderTextColor={mutedColor}
          value={search}
          onChangeText={setSearch}
        />
      </View>

      <FlatList
        data={filteredClients}
        keyExtractor={(item) => item.id}
        renderItem={renderClient}
        contentContainerStyle={styles.list}
        ListEmptyComponent={
          <View style={styles.emptyContainer}>
            <Text style={{ fontSize: 48 }}>👥</Text>
            <Text style={{ fontSize: 16, fontWeight: '600', color: textColor, marginTop: 12 }}>
              No clients yet
            </Text>
            <Text style={{ fontSize: 13, color: mutedColor, marginTop: 4 }}>
              Add your first client to get started
            </Text>
          </View>
        }
        showsVerticalScrollIndicator={false}
      />

      {/* Add/Edit Modal */}
      <Modal visible={showModal} animationType="slide" transparent>
        <View style={styles.modalOverlay}>
          <View style={[styles.modal, { backgroundColor: cardBg }]}>
            <View style={styles.modalHeader}>
              <TouchableOpacity onPress={() => setShowModal(false)}>
                <Ionicons name="close" size={24} color={textColor} />
              </TouchableOpacity>
              <Text style={{ fontSize: 18, fontWeight: '700', color: textColor }}>
                {editingClient ? 'Edit Client' : 'New Client'}
              </Text>
              <TouchableOpacity onPress={handleSave}>
                <Text style={{ color: '#6C5CE7', fontWeight: '700', fontSize: 16 }}>Save</Text>
              </TouchableOpacity>
            </View>
            <ScrollView style={styles.formContent} showsVerticalScrollIndicator={false}>
              <Text style={styles.label}>Name *</Text>
              <TextInput
                style={[styles.input, { backgroundColor: inputBg, color: textColor }]}
                placeholder="Client name"
                placeholderTextColor={mutedColor}
                value={form.name}
                onChangeText={(t) => setForm({ ...form, name: t })}
              />
              <Text style={styles.label}>Company</Text>
              <TextInput
                style={[styles.input, { backgroundColor: inputBg, color: textColor }]}
                placeholder="Company name"
                placeholderTextColor={mutedColor}
                value={form.company}
                onChangeText={(t) => setForm({ ...form, company: t })}
              />
              <Text style={styles.label}>Email</Text>
              <TextInput
                style={[styles.input, { backgroundColor: inputBg, color: textColor }]}
                placeholder="email@example.com"
                placeholderTextColor={mutedColor}
                value={form.email}
                onChangeText={(t) => setForm({ ...form, email: t })}
                keyboardType="email-address"
                autoCapitalize="none"
              />
              <Text style={styles.label}>Phone</Text>
              <TextInput
                style={[styles.input, { backgroundColor: inputBg, color: textColor }]}
                placeholder="+1 234 567 890"
                placeholderTextColor={mutedColor}
                value={form.phone}
                onChangeText={(t) => setForm({ ...form, phone: t })}
                keyboardType="phone-pad"
              />
              <Text style={styles.label}>Address</Text>
              <TextInput
                style={[styles.input, { backgroundColor: inputBg, color: textColor }]}
                placeholder="Client address"
                placeholderTextColor={mutedColor}
                value={form.address}
                onChangeText={(t) => setForm({ ...form, address: t })}
              />
              <Text style={styles.label}>Notes</Text>
              <TextInput
                style={[styles.input, styles.textArea, { backgroundColor: inputBg, color: textColor }]}
                placeholder="Notes about this client..."
                placeholderTextColor={mutedColor}
                value={form.notes}
                onChangeText={(t) => setForm({ ...form, notes: t })}
                multiline
                numberOfLines={3}
              />
            </ScrollView>
          </View>
        </View>
      </Modal>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingTop: 16,
    paddingBottom: 12,
  },
  createBtn: {
    width: 44,
    height: 44,
    borderRadius: 14,
    backgroundColor: '#6C5CE7',
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: '#6C5CE7',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
    elevation: 4,
  },
  searchBox: {
    flexDirection: 'row',
    alignItems: 'center',
    marginHorizontal: 20,
    paddingHorizontal: 14,
    paddingVertical: 12,
    borderRadius: 14,
    gap: 10,
    marginBottom: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 2,
  },
  searchInput: { flex: 1, fontSize: 15, fontWeight: '500' },
  list: { paddingHorizontal: 20, paddingBottom: 20 },
  clientCard: {
    flexDirection: 'row',
    alignItems: 'center',
    borderRadius: 14,
    padding: 14,
    marginBottom: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 2,
  },
  avatar: {
    width: 44,
    height: 44,
    borderRadius: 14,
    backgroundColor: '#6C5CE7',
    alignItems: 'center',
    justifyContent: 'center',
  },
  deleteBtn: {
    width: 36,
    height: 36,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 10,
  },
  emptyContainer: { alignItems: 'center', paddingTop: 80 },
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.5)',
    justifyContent: 'flex-end',
  },
  modal: {
    height: '85%',
    borderRadius: 24,
    overflow: 'hidden',
  },
  modalHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingVertical: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#2d2d50',
  },
  formContent: { paddingHorizontal: 20, paddingTop: 16 },
  label: { fontSize: 13, fontWeight: '600', color: '#64748b', marginBottom: 6, marginTop: 12 },
  input: { fontSize: 15, paddingVertical: 10, paddingHorizontal: 12, borderRadius: 10 },
  textArea: { height: 80, textAlignVertical: 'top' },
});
