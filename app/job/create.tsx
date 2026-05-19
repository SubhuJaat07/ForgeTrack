import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TextInput,
  TouchableOpacity,
  useColorScheme,
  Alert,
  KeyboardAvoidingView,
  Platform,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { router } from 'expo-router';
import Animated, { FadeIn } from 'react-native-reanimated';
import { Ionicons, Feather } from '@expo/vector-icons';
import { useJobStore } from '@/stores/useJobStore';
import { useClientStore } from '@/stores/useClientStore';
import { useLocation } from '@/hooks/useLocation';
import { useSettingsStore } from '@/stores/useSettingsStore';
import { Job, JobPriority } from '@/types';

const priorityOptions: { label: string; value: JobPriority; color: string }[] = [
  { label: 'Low', value: 'low', color: '#94a3b8' },
  { label: 'Medium', value: 'medium', color: '#74b9ff' },
  { label: 'High', value: 'high', color: '#fdcb6e' },
  { label: 'Urgent', value: 'urgent', color: '#ff6b6b' },
];

export default function CreateJobScreen() {
  const colorScheme = useColorScheme();
  const isDark = colorScheme === 'dark';
  const { addJob } = useJobStore();
  const { clients } = useClientStore();
  const { getCurrentPosition, address, location } = useLocation();
  const { currency } = useSettingsStore();

  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [selectedClient, setSelectedClient] = useState('');
  const [priority, setPriority] = useState<JobPriority>('medium');
  const [scheduledDate, setScheduledDate] = useState(new Date().toISOString().split('T')[0]);
  const [scheduledTime, setScheduledTime] = useState('09:00');
  const [revenue, setRevenue] = useState('');
  const [cost, setCost] = useState('');
  const [locationAddress, setLocationAddress] = useState(address);
  const [tags, setTags] = useState('');

  const handleCreate = () => {
    if (!title.trim()) {
      Alert.alert('Error', 'Please enter a job title');
      return;
    }

    const job: Job = {
      id: `job_${Date.now()}`,
      title: title.trim(),
      description: description.trim(),
      clientId: selectedClient || 'unknown',
      clientName: clients.find((c) => c.id === selectedClient)?.name || 'Walk-in Client',
      status: 'scheduled',
      priority,
      scheduledDate,
      scheduledTime: new Date(`${scheduledDate}T${scheduledTime}`).toISOString(),
      location: {
        latitude: location?.coords.latitude || 0,
        longitude: location?.coords.longitude || 0,
        address: locationAddress || 'Current location',
      },
      photos: [],
      voiceNotes: [],
      notes: [],
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      revenue: parseFloat(revenue) || 0,
      cost: parseFloat(cost) || 0,
      tags: tags.split(',').map((t) => t.trim()).filter(Boolean),
    };

    addJob(job);
    Alert.alert('Success', 'Job created successfully!', [
      {
        text: 'View Job',
        onPress: () => router.replace(`/job/${job.id}`),
      },
      {
        text: 'Back to Jobs',
        onPress: () => router.back(),
      },
    ]);
  };

  const fetchLocation = async () => {
    await getCurrentPosition();
    setLocationAddress(address);
  };

  const cardBg = isDark ? '#1a1a2e' : '#ffffff';
  const textColor = isDark ? '#e2e8f0' : '#1e293b';
  const mutedColor = isDark ? '#94a3b8' : '#64748b';
  const inputBg = isDark ? '#252542' : '#f1f5f9';

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: isDark ? '#0f0f1a' : '#f8fafc' }]}>
      <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : 'height'} style={{ flex: 1 }}>
        <ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>
          {/* Header */}
          <Animated.View entering={FadeIn.duration(400)} style={styles.header}>
            <TouchableOpacity onPress={() => router.back()} style={styles.closeBtn}>
              <Ionicons name="close" size={24} color={textColor} />
            </TouchableOpacity>
            <Text style={{ fontSize: 18, fontWeight: '700', color: textColor }}>New Job</Text>
            <TouchableOpacity onPress={handleCreate} style={styles.createBtn}>
              <Text style={{ color: '#fff', fontWeight: '700', fontSize: 15 }}>Create</Text>
            </TouchableOpacity>
          </Animated.View>

          {/* Title */}
          <View style={[styles.card, { backgroundColor: cardBg }]}>
            <Text style={styles.label}>Job Title *</Text>
            <TextInput
              style={[styles.input, { backgroundColor: inputBg, color: textColor }]}
              placeholder="e.g., HVAC Installation"
              placeholderTextColor={mutedColor}
              value={title}
              onChangeText={setTitle}
            />
          </View>

          {/* Description */}
          <View style={[styles.card, { backgroundColor: cardBg }]}>
            <Text style={styles.label}>Description</Text>
            <TextInput
              style={[styles.input, styles.textArea, { backgroundColor: inputBg, color: textColor }]}
              placeholder="Job details, scope of work..."
              placeholderTextColor={mutedColor}
              value={description}
              onChangeText={setDescription}
              multiline
              numberOfLines={3}
            />
          </View>

          {/* Client */}
          <View style={[styles.card, { backgroundColor: cardBg }]}>
            <Text style={styles.label}>Client</Text>
            {clients.length > 0 ? (
              <View style={styles.clientList}>
                {clients.slice(0, 5).map((client) => (
                  <TouchableOpacity
                    key={client.id}
                    style={[
                      styles.clientChip,
                      {
                        backgroundColor: selectedClient === client.id ? '#6C5CE720' : inputBg,
                        borderColor: selectedClient === client.id ? '#6C5CE7' : 'transparent',
                      },
                    ]}
                    onPress={() => setSelectedClient(client.id)}
                  >
                    <Text
                      style={{
                        fontSize: 13,
                        fontWeight: '500',
                        color: selectedClient === client.id ? '#6C5CE7' : textColor,
                      }}
                    >
                      {client.name}
                    </Text>
                  </TouchableOpacity>
                ))}
              </View>
            ) : (
              <Text style={{ fontSize: 13, color: mutedColor, fontStyle: 'italic' }}>
                No clients yet. Add clients from the Clients tab.
              </Text>
            )}
          </View>

          {/* Priority */}
          <View style={[styles.card, { backgroundColor: cardBg }]}>
            <Text style={styles.label}>Priority</Text>
            <View style={styles.priorityRow}>
              {priorityOptions.map((opt) => (
                <TouchableOpacity
                  key={opt.value}
                  style={[
                    styles.priorityChip,
                    {
                      backgroundColor: priority === opt.value ? `${opt.color}20` : inputBg,
                      borderColor: priority === opt.value ? opt.color : 'transparent',
                    },
                  ]}
                  onPress={() => setPriority(opt.value)}
                >
                  <Text
                    style={{
                      fontSize: 13,
                      fontWeight: '600',
                      color: priority === opt.value ? opt.color : textColor,
                    }}
                  >
                    {opt.label}
                  </Text>
                </TouchableOpacity>
              ))}
            </View>
          </View>

          {/* Date & Time */}
          <View style={styles.row}>
            <View style={[styles.card, styles.halfCard, { backgroundColor: cardBg }]}>
              <Text style={styles.label}>Date</Text>
              <TextInput
                style={[styles.input, { backgroundColor: inputBg, color: textColor }]}
                value={scheduledDate}
                onChangeText={setScheduledDate}
                placeholder="YYYY-MM-DD"
                placeholderTextColor={mutedColor}
              />
            </View>
            <View style={[styles.card, styles.halfCard, { backgroundColor: cardBg }]}>
              <Text style={styles.label}>Time</Text>
              <TextInput
                style={[styles.input, { backgroundColor: inputBg, color: textColor }]}
                value={scheduledTime}
                onChangeText={setScheduledTime}
                placeholder="HH:MM"
                placeholderTextColor={mutedColor}
              />
            </View>
          </View>

          {/* Location */}
          <View style={[styles.card, { backgroundColor: cardBg }]}>
            <Text style={styles.label}>Location</Text>
            <TouchableOpacity
              style={[styles.locationBtn, { backgroundColor: inputBg }]}
              onPress={fetchLocation}
            >
              <Feather name="map-pin" size={16} color="#6C5CE7" />
              <Text
                style={[styles.locationText, { color: locationAddress ? textColor : mutedColor }]}
                numberOfLines={1}
              >
                {locationAddress || 'Tap to get current location'}
              </Text>
            </TouchableOpacity>
          </View>

          {/* Revenue & Cost */}
          <View style={styles.row}>
            <View style={[styles.card, styles.halfCard, { backgroundColor: cardBg }]}>
              <Text style={styles.label}>Revenue ({currency})</Text>
              <TextInput
                style={[styles.input, { backgroundColor: inputBg, color: textColor }]}
                placeholder="0.00"
                placeholderTextColor={mutedColor}
                value={revenue}
                onChangeText={setRevenue}
                keyboardType="decimal-pad"
              />
            </View>
            <View style={[styles.card, styles.halfCard, { backgroundColor: cardBg }]}>
              <Text style={styles.label}>Cost ({currency})</Text>
              <TextInput
                style={[styles.input, { backgroundColor: inputBg, color: textColor }]}
                placeholder="0.00"
                placeholderTextColor={mutedColor}
                value={cost}
                onChangeText={setCost}
                keyboardType="decimal-pad"
              />
            </View>
          </View>

          {/* Tags */}
          <View style={[styles.card, { backgroundColor: cardBg }]}>
            <Text style={styles.label}>Tags (comma separated)</Text>
            <TextInput
              style={[styles.input, { backgroundColor: inputBg, color: textColor }]}
              placeholder="e.g., HVAC, plumbing, electrical"
              placeholderTextColor={mutedColor}
              value={tags}
              onChangeText={setTags}
            />
          </View>

          <View style={{ height: 40 }} />
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  content: { paddingHorizontal: 20, paddingTop: 16 },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 20,
  },
  closeBtn: { width: 44, height: 44, alignItems: 'center', justifyContent: 'center' },
  createBtn: {
    backgroundColor: '#6C5CE7',
    paddingHorizontal: 20,
    paddingVertical: 10,
    borderRadius: 12,
  },
  card: {
    borderRadius: 14,
    padding: 14,
    marginBottom: 12,
  },
  label: {
    fontSize: 13,
    fontWeight: '600',
    color: '#64748b',
    marginBottom: 8,
  },
  input: {
    fontSize: 15,
    paddingVertical: 10,
    paddingHorizontal: 12,
    borderRadius: 10,
  },
  textArea: {
    height: 80,
    textAlignVertical: 'top',
  },
  clientList: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
  clientChip: {
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 10,
    borderWidth: 1,
  },
  priorityRow: { flexDirection: 'row', gap: 8 },
  priorityChip: {
    paddingHorizontal: 14,
    paddingVertical: 8,
    borderRadius: 10,
    borderWidth: 1,
  },
  row: { flexDirection: 'row', gap: 12 },
  halfCard: { flex: 1 },
  locationBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 12,
    paddingHorizontal: 12,
    borderRadius: 10,
    gap: 10,
  },
  locationText: { fontSize: 14, flex: 1 },
});
