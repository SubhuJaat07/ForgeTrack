import React, { useState, useEffect, useRef } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  TextInput,
  useColorScheme,
  Alert,
  Image,
  FlatList,
  SafeAreaView,
  Linking,
} from 'react-native';
import { SafeAreaView as RNSafeArea } from 'react-native-safe-area-context';
import { router, useLocalSearchParams } from 'expo-router';
import Animated, { FadeIn, FadeInDown } from 'react-native-reanimated';
import {
  Ionicons,
  Feather,
  MaterialIcons,
  FontAwesome5,
} from '@expo/vector-icons';
import { useJobStore } from '@/stores/useJobStore';
import { useTimer } from '@/hooks/useTimer';
import { useLocation } from '@/hooks/useLocation';
import { useSettingsStore } from '@/stores/useSettingsStore';
import { SignaturePad } from '@/components/SignaturePad';
import { StatusBadge } from '@/components/ui/StatusBadge';
import { AnimatedButton } from '@/components/ui/AnimatedButton';
import { ProgressBar } from '@/components/ui/ProgressBar';
import { generateJobReport } from '@/utils/pdfGenerator';
import { formatCurrency, formatDuration, getStatusColor } from '@/utils/theme';
import { getCurrentLocation, reverseGeocode } from '@/utils/geofencing';
import { sendLocalNotification } from '@/utils/notifications';
import { Job, JobPhoto, PhotoType } from '@/types';
import * as ImagePicker from 'expo-image-picker';
import * as Speech from 'expo-speech';
import * as FileSystem from 'expo-file-system';

export default function JobDetailScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const colorScheme = useColorScheme();
  const isDark = colorScheme === 'dark';
  const { jobs, updateJob, deleteJob } = useJobStore();
  const timer = useTimer();
  const { getCurrentPosition, address } = useLocation();
  const { currency, autoGeneratePdf } = useSettingsStore();

  const [job, setJob] = useState<Job | undefined>(() => jobs.find((j) => j.id === id));
  const [activeTab, setActiveTab] = useState<'details' | 'photos' | 'notes'>('details');
  const [noteText, setNoteText] = useState('');
  const [showSignature, setShowSignature] = useState(false);
  const [isRecording, setIsRecording] = useState(false);
  const [transcription, setTranscription] = useState('');
  const [photoType, setPhotoType] = useState<PhotoType>('before');
  const [showPhotoPicker, setShowPhotoPicker] = useState(false);
  const [checkedIn, setCheckedIn] = useState(false);

  useEffect(() => {
    const found = jobs.find((j) => j.id === id);
    setJob(found);
  }, [jobs, id]);

  if (!job) {
    return (
      <SafeAreaView style={[styles.container, { backgroundColor: isDark ? '#0f0f1a' : '#f8fafc' }]}>
        <View style={styles.notFound}>
          <Text style={{ fontSize: 48 }}>😕</Text>
          <Text style={{ fontSize: 18, fontWeight: '600', color: isDark ? '#e2e8f0' : '#1e293b', marginTop: 16 }}>
            Job not found
          </Text>
          <TouchableOpacity onPress={() => router.back()} style={styles.backBtn}>
            <Text style={{ color: '#6C5CE7', fontWeight: '600' }}>Go Back</Text>
          </TouchableOpacity>
        </View>
      </SafeAreaView>
    );
  }

  const textColor = isDark ? '#e2e8f0' : '#1e293b';
  const mutedColor = isDark ? '#94a3b8' : '#64748b';
  const cardBg = isDark ? '#1a1a2e' : '#ffffff';
  const inputBg = isDark ? '#252542' : '#f1f5f9';

  const handleStartJob = () => {
    updateJob(job!.id, { status: 'in-progress' });
    timer.start(job!.id);
    setJob({ ...job!, status: 'in-progress' });
    sendLocalNotification('Job Started', `"${job!.title}" is now in progress`);
  };

  const handleCompleteJob = async () => {
    const duration = timer.elapsedSeconds;
    timer.stop();
    
    const updates: Partial<Job> = {
      status: 'completed',
      completedAt: new Date().toISOString(),
      totalDuration: duration,
    };

    updateJob(job!.id, updates);
    setJob({ ...job!, ...updates });

    if (autoGeneratePdf) {
      try {
        await generateJobReport({ ...job!, ...updates });
      } catch (e) {
        // PDF might not work in all environments
      }
    }

    sendLocalNotification('Job Completed', `"${job!.title}" has been completed!`);
    Alert.alert('Job Completed!', 'Great work! The job has been marked as completed.', [
      { text: 'OK', onPress: () => router.back() },
    ]);
  };

  const handleCancelJob = () => {
    Alert.alert('Cancel Job?', 'Are you sure you want to cancel this job?', [
      { text: 'Keep Job', style: 'cancel' },
      {
        text: 'Cancel Job',
        style: 'destructive',
        onPress: () => {
          updateJob(job!.id, { status: 'cancelled' });
          timer.stop();
          router.back();
        },
      },
    ]);
  };

  const handleCheckIn = async () => {
    const loc = await getCurrentPosition();
    if (loc) {
      const addr = await reverseGeocode(loc.coords.latitude, loc.coords.longitude);
      updateJob(job!.id, {
        location: {
          ...job!.location,
          latitude: loc.coords.latitude,
          longitude: loc.coords.longitude,
          address: addr,
          checkInTime: new Date().toISOString(),
        },
      });
      setCheckedIn(true);
      sendLocalNotification('Checked In', `You checked in at ${addr}`);
    }
  };

  const handleCheckOut = async () => {
    const loc = await getCurrentLocation();
    if (loc) {
      const addr = await reverseGeocode(loc.coords.latitude, loc.coords.longitude);
      updateJob(job!.id, {
        location: {
          ...job!.location,
          checkOutTime: new Date().toISOString(),
        },
      });
      setCheckedIn(false);
    }
  };

  const pickImage = async () => {
    const { status } = await ImagePicker.requestCameraPermissionsAsync();
    if (status !== 'granted') {
      Alert.alert('Permission needed', 'Camera permission is required to take photos');
      return;
    }

    let result: ImagePicker.ImagePickerResult;

    if (showPhotoPicker) {
      result = await ImagePicker.launchCameraAsync({
        quality: 0.8,
        allowsEditing: true,
      });
    } else {
      result = await ImagePicker.launchImageLibraryAsync({
        quality: 0.8,
        allowsEditing: true,
      });
    }

    if (!result.canceled && result.assets[0]) {
      const photo: JobPhoto = {
        id: `photo_${Date.now()}`,
        uri: result.assets[0].uri,
        type: photoType,
        annotation: '',
        timestamp: new Date().toISOString(),
      };
      const newPhotos = [...job!.photos, photo];
      updateJob(job!.id, { photos: newPhotos });
      setJob({ ...job!, photos: newPhotos });
    }
    setShowPhotoPicker(false);
  };

  const addNote = () => {
    if (!noteText.trim()) return;
    const newNotes = [...job!.notes, noteText.trim()];
    updateJob(job!.id, { notes: newNotes });
    setJob({ ...job!, notes: newNotes });
    setNoteText('');
  };

  const handleVoiceRecord = async () => {
    setIsRecording(true);
    // Simulate voice recording - in production, use expo-av for actual recording
    setTimeout(() => {
      setIsRecording(false);
      setTranscription('Voice note captured - tap to edit transcription');
      const voiceNote = {
        id: `voice_${Date.now()}`,
        uri: '',
        transcription: 'Voice note captured',
        duration: 5,
        timestamp: new Date().toISOString(),
      };
      const newVoiceNotes = [...(job!.voiceNotes || []), voiceNote];
      updateJob(job!.id, { voiceNotes: newVoiceNotes });
      setJob({ ...job!, voiceNotes: newVoiceNotes });
    }, 2000);
  };

  const speakText = (text: string) => {
    Speech.speak(text, { language: 'en-US', rate: 1 });
  };

  const handleSignature = (signatureData: string) => {
    updateJob(job!.id, { signatureData });
    setJob({ ...job!, signatureData });
    setShowSignature(false);
    Alert.alert('Success', 'Signature captured!');
  };

  const handleDeleteJob = () => {
    Alert.alert('Delete Job?', 'This action cannot be undone.', [
      { text: 'Cancel', style: 'cancel' },
      {
        text: 'Delete',
        style: 'destructive',
        onPress: () => {
          deleteJob(job!.id);
          timer.stop();
          router.back();
        },
      },
    ]);
  };

  const handleGeneratePdf = async () => {
    try {
      await generateJobReport(job!);
    } catch (e) {
      Alert.alert('Error', 'Failed to generate PDF report');
    }
  };

  const profit = job.revenue - job.cost;
  const profitMargin = job.revenue > 0 ? (profit / job.revenue) * 100 : 0;

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: isDark ? '#0f0f1a' : '#f8fafc' }]}>
      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => router.back()} style={styles.headerBtn}>
          <Ionicons name="arrow-back" size={24} color={textColor} />
        </TouchableOpacity>
        <View style={{ flex: 1 }}>
          <Text style={{ fontSize: 18, fontWeight: '700', color: textColor }} numberOfLines={1}>
            {job.title}
          </Text>
          <Text style={{ fontSize: 12, color: mutedColor }}>{job.clientName}</Text>
        </View>
        <TouchableOpacity onPress={handleDeleteJob} style={styles.headerBtn}>
          <Ionicons name="trash-outline" size={22} color="#ff6b6b" />
        </TouchableOpacity>
      </View>

      <ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>
        {/* Status & Timer */}
        <Animated.View entering={FadeIn.duration(400)}>
          <View style={[styles.statusCard, { backgroundColor: cardBg }]}>
            <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
              <StatusBadge label={job.status} type="status" />
              <StatusBadge label={job.priority} type="priority" />
            </View>

            {job.status === 'in-progress' && timer.isRunning && (
              <View style={styles.timerSection}>
                <Text style={{ fontSize: 36, fontWeight: '800', color: '#6C5CE7', fontFamily: 'monospace' }}>
                  {String(Math.floor(timer.elapsedSeconds / 3600)).padStart(2, '0')}:
                  {String(Math.floor((timer.elapsedSeconds % 3600) / 60)).padStart(2, '0')}:
                  {String(timer.elapsedSeconds % 60).padStart(2, '0')}
                </Text>
                <Text style={{ fontSize: 13, color: mutedColor }}>Time elapsed</Text>
              </View>
            )}
          </View>
        </Animated.View>

        {/* Action Buttons */}
        <Animated.View entering={FadeInDown.delay(100).duration(400)} style={styles.actionRow}>
          {job.status === 'scheduled' && (
            <AnimatedButton title="Start Job" onPress={handleStartJob} variant="success" size="lg" style={{ flex: 1 }} />
          )}
          {job.status === 'in-progress' && (
            <>
              <AnimatedButton title="Complete" onPress={handleCompleteJob} variant="success" size="md" style={{ flex: 1 }} />
              <AnimatedButton title="Cancel" onPress={handleCancelJob} variant="danger" size="md" />
            </>
          )}
          {job.status === 'completed' && (
            <AnimatedButton title="Generate PDF" onPress={handleGeneratePdf} variant="primary" size="md" style={{ flex: 1 }} />
          )}
        </Animated.View>

        {/* GPS Check-in */}
        <Animated.View entering={FadeInDown.delay(200).duration(400)}>
          <View style={[styles.sectionCard, { backgroundColor: cardBg }]}>
            <Text style={{ fontSize: 15, fontWeight: '700', color: textColor, marginBottom: 12 }}>
              📍 GPS Check-in
            </Text>
            <Text style={{ fontSize: 13, color: mutedColor, marginBottom: 8 }} numberOfLines={2}>
              {job.location.address}
            </Text>
            {job.location.checkInTime && (
              <Text style={{ fontSize: 12, color: '#00b894' }}>
                ✓ Checked in: {new Date(job.location.checkInTime).toLocaleTimeString()}
              </Text>
            )}
            <View style={{ flexDirection: 'row', gap: 10, marginTop: 10 }}>
              {!checkedIn ? (
                <AnimatedButton title="Check In" onPress={handleCheckIn} variant="primary" size="sm" />
              ) : (
                <AnimatedButton title="Check Out" onPress={handleCheckOut} variant="secondary" size="sm" />
              )}
            </View>
          </View>
        </Animated.View>

        {/* Quick Actions Grid */}
        <Animated.View entering={FadeInDown.delay(300).duration(400)}>
          <View style={styles.quickActionsGrid}>
            <TouchableOpacity
              style={[styles.quickAction, { backgroundColor: cardBg }]}
              onPress={() => { setPhotoType('before'); setShowPhotoPicker(true); }}
            >
              <FontAwesome5 name="camera" size={20} color="#6C5CE7" />
              <Text style={{ fontSize: 11, fontWeight: '600', color: textColor, marginTop: 6 }}>Before Photo</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={[styles.quickAction, { backgroundColor: cardBg }]}
              onPress={() => { setPhotoType('after'); setShowPhotoPicker(true); }}
            >
              <FontAwesome5 name="camera-retro" size={20} color="#00b894" />
              <Text style={{ fontSize: 11, fontWeight: '600', color: textColor, marginTop: 6 }}>After Photo</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={[styles.quickAction, { backgroundColor: cardBg }]}
              onPress={handleVoiceRecord}
            >
              <FontAwesome5 name="microphone" size={20} color={isRecording ? '#ff6b6b' : '#fdcb6e'} />
              <Text style={{ fontSize: 11, fontWeight: '600', color: textColor, marginTop: 6 }}>
                {isRecording ? 'Recording...' : 'Voice Note'}
              </Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={[styles.quickAction, { backgroundColor: cardBg }]}
              onPress={() => setShowSignature(true)}
            >
              <FontAwesome5 name="signature" size={20} color="#ff6b6b" />
              <Text style={{ fontSize: 11, fontWeight: '600', color: textColor, marginTop: 6 }}>Signature</Text>
            </TouchableOpacity>
          </View>
        </Animated.View>

        {/* Tab Selector */}
        <View style={styles.tabBar}>
          {(['details', 'photos', 'notes'] as const).map((tab) => (
            <TouchableOpacity
              key={tab}
              style={[styles.tab, { borderBottomColor: activeTab === tab ? '#6C5CE7' : 'transparent' }]}
              onPress={() => setActiveTab(tab)}
            >
              <Text
                style={{
                  fontSize: 14,
                  fontWeight: '600',
                  color: activeTab === tab ? '#6C5CE7' : mutedColor,
                  textTransform: 'capitalize',
                }}
              >
                {tab}
              </Text>
            </TouchableOpacity>
          ))}
        </View>

        {/* Tab Content */}
        {activeTab === 'details' && (
          <Animated.View entering={FadeIn.duration(300)}>
            <View style={[styles.detailsCard, { backgroundColor: cardBg }]}>
              <DetailRow label="Description" value={job.description || 'No description'} />
              <DetailRow label="Client" value={job.clientName} />
              <DetailRow label="Scheduled" value={`${new Date(job.scheduledDate).toLocaleDateString()} at ${new Date(job.scheduledTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`} />
              <DetailRow label="Revenue" value={formatCurrency(job.revenue)} color="#00b894" />
              <DetailRow label="Cost" value={formatCurrency(job.cost)} color="#ff6b6b" />
              <DetailRow label="Profit" value={formatCurrency(profit)} color={profit >= 0 ? '#00b894' : '#ff6b6b'} />
              
              {job.revenue > 0 && (
                <View style={{ marginTop: 8 }}>
                  <Text style={{ fontSize: 12, color: mutedColor, marginBottom: 4 }}>Profit Margin</Text>
                  <ProgressBar progress={profitMargin / 100} color={profit >= 0 ? '#00b894' : '#ff6b6b'} height={8} />
                  <Text style={{ fontSize: 12, color: mutedColor, marginTop: 4 }}>{profitMargin.toFixed(1)}%</Text>
                </View>
              )}

              {job.totalDuration && (
                <DetailRow label="Duration" value={formatDuration(job.totalDuration)} />
              )}
              {job.tags.length > 0 && (
                <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 6, marginTop: 8 }}>
                  {job.tags.map((tag, i) => (
                    <View key={i} style={[styles.tag, { backgroundColor: '#6C5CE720' }]}>
                      <Text style={{ fontSize: 11, color: '#6C5CE7', fontWeight: '600' }}>#{tag}</Text>
                    </View>
                  ))}
                </View>
              )}
            </View>
          </Animated.View>
        )}

        {activeTab === 'photos' && (
          <Animated.View entering={FadeIn.duration(300)}>
            {job.photos.length === 0 ? (
              <View style={[styles.emptySection, { backgroundColor: cardBg }]}>
                <Text style={{ fontSize: 36 }}>📷</Text>
                <Text style={{ fontSize: 14, color: mutedColor, marginTop: 8 }}>No photos yet</Text>
                <Text style={{ fontSize: 12, color: mutedColor }}>Use quick actions to add before/after photos</Text>
              </View>
            ) : (
              <View style={styles.photosGrid}>
                {job.photos.map((photo) => (
                  <View key={photo.id} style={[styles.photoCard, { backgroundColor: cardBg }]}>
                    <View
                      style={{
                        width: '100%',
                        height: 100,
                        borderRadius: 8,
                        backgroundColor: isDark ? '#252542' : '#f1f5f9',
                        alignItems: 'center',
                        justifyContent: 'center',
                      }}
                    >
                      <Ionicons name="image" size={32} color={mutedColor} />
                    </View>
                    <Text style={{ fontSize: 11, fontWeight: '600', color: textColor, marginTop: 6, textTransform: 'uppercase' }}>
                      {photo.type}
                    </Text>
                    <Text style={{ fontSize: 10, color: mutedColor }}>
                      {new Date(photo.timestamp).toLocaleTimeString()}
                    </Text>
                  </View>
                ))}
              </View>
            )}
          </Animated.View>
        )}

        {activeTab === 'notes' && (
          <Animated.View entering={FadeIn.duration(300)}>
            {/* Voice Notes */}
            {job.voiceNotes && job.voiceNotes.length > 0 && (
              <View style={[styles.notesSection, { backgroundColor: cardBg }]}>
                <Text style={{ fontSize: 14, fontWeight: '700', color: textColor, marginBottom: 8 }}>Voice Notes</Text>
                {job.voiceNotes.map((vn) => (
                  <TouchableOpacity
                    key={vn.id}
                    style={styles.voiceNoteItem}
                    onPress={() => speakText(vn.transcription)}
                  >
                    <FontAwesome5 name="volume-up" size={16} color="#6C5CE7" />
                    <Text style={{ fontSize: 13, color: textColor, flex: 1 }}>{vn.transcription}</Text>
                    <Text style={{ fontSize: 11, color: mutedColor }}>{vn.duration}s</Text>
                  </TouchableOpacity>
                ))}
              </View>
            )}

            {/* Text Notes */}
            <View style={[styles.notesSection, { backgroundColor: cardBg }]}>
              <Text style={{ fontSize: 14, fontWeight: '700', color: textColor, marginBottom: 8 }}>Text Notes</Text>
              {job.notes.map((note, i) => (
                <View key={i} style={styles.noteItem}>
                  <Text style={{ fontSize: 13, color: textColor }}>{note}</Text>
                </View>
              ))}
              <View style={styles.noteInputRow}>
                <TextInput
                  style={[styles.noteInput, { backgroundColor: inputBg, color: textColor }]}
                  placeholder="Add a note..."
                  placeholderTextColor={mutedColor}
                  value={noteText}
                  onChangeText={setNoteText}
                />
                <TouchableOpacity style={styles.noteSendBtn} onPress={addNote}>
                  <Ionicons name="send" size={18} color="#fff" />
                </TouchableOpacity>
              </View>
            </View>
          </Animated.View>
        )}

        {/* Signature Preview */}
        {job.signatureData && (
          <View style={[styles.sectionCard, { backgroundColor: cardBg }]}>
            <Text style={{ fontSize: 15, fontWeight: '700', color: textColor, marginBottom: 8 }}>✍️ Client Signature</Text>
            <View style={{ backgroundColor: '#f8fafc', borderRadius: 12, padding: 8, alignItems: 'center' }}>
              <Text style={{ fontSize: 13, color: '#00b894', fontWeight: '600' }}>Signature captured ✓</Text>
            </View>
          </View>
        )}

        <View style={{ height: 40 }} />
      </ScrollView>

      <SignaturePad
        visible={showSignature}
        onConfirm={handleSignature}
        onCancel={() => setShowSignature(false)}
      />
    </SafeAreaView>
  );
}

function DetailRow({ label, value, color }: { label: string; value: string; color?: string }) {
  const colorScheme = useColorScheme();
  const isDark = colorScheme === 'dark';
  return (
    <View style={{ marginBottom: 12 }}>
      <Text style={{ fontSize: 12, fontWeight: '600', color: '#64748b', marginBottom: 2 }}>{label}</Text>
      <Text style={{ fontSize: 14, color: color || (isDark ? '#e2e8f0' : '#1e293b'), fontWeight: '500' }}>{value}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingTop: 16,
    paddingBottom: 8,
    gap: 8,
  },
  headerBtn: { width: 40, height: 40, alignItems: 'center', justifyContent: 'center' },
  content: { paddingHorizontal: 20, paddingTop: 8 },
  statusCard: {
    borderRadius: 16,
    padding: 16,
    marginBottom: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 8,
    elevation: 3,
  },
  timerSection: { alignItems: 'center', marginTop: 16 },
  actionRow: { flexDirection: 'row', gap: 10, marginBottom: 16 },
  sectionCard: {
    borderRadius: 16,
    padding: 16,
    marginBottom: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 8,
    elevation: 3,
  },
  quickActionsGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: 12, marginBottom: 16 },
  quickAction: {
    flex: 1,
    minWidth: '45%',
    alignItems: 'center',
    paddingVertical: 16,
    borderRadius: 14,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 2,
  },
  tabBar: {
    flexDirection: 'row',
    borderBottomWidth: 1,
    borderBottomColor: '#2d2d50',
    marginBottom: 16,
  },
  tab: {
    flex: 1,
    paddingBottom: 10,
    alignItems: 'center',
    borderBottomWidth: 2,
  },
  detailsCard: {
    borderRadius: 16,
    padding: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 8,
    elevation: 3,
  },
  tag: {
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 6,
  },
  emptySection: {
    borderRadius: 16,
    padding: 32,
    alignItems: 'center',
  },
  photosGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: 12 },
  photoCard: {
    width: '47%',
    borderRadius: 12,
    padding: 8,
    alignItems: 'center',
  },
  notesSection: {
    borderRadius: 16,
    padding: 16,
    marginBottom: 12,
  },
  voiceNoteItem: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 10,
    backgroundColor: '#6C5CE710',
    borderRadius: 10,
    marginBottom: 8,
    gap: 10,
  },
  noteItem: {
    padding: 10,
    backgroundColor: '#f1f5f9',
    borderRadius: 10,
    marginBottom: 8,
  },
  noteInputRow: { flexDirection: 'row', gap: 8, marginTop: 8 },
  noteInput: {
    flex: 1,
    fontSize: 14,
    paddingVertical: 10,
    paddingHorizontal: 12,
    borderRadius: 10,
  },
  noteSendBtn: {
    width: 42,
    height: 42,
    borderRadius: 12,
    backgroundColor: '#6C5CE7',
    alignItems: 'center',
    justifyContent: 'center',
  },
  notFound: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  backBtn: { marginTop: 16, paddingVertical: 8 },
});

// Handle image picking effect
const pickImageAndAdd = async (
  useCamera: boolean,
  photoType: PhotoType,
  job: Job,
  updateJob: (id: string, updates: Partial<Job>) => void,
  setJob: (j: Job) => void
) => {
  const { status } = await ImagePicker.requestCameraPermissionsAsync();
  if (status !== 'granted') return;

  const result = useCamera
    ? await ImagePicker.launchCameraAsync({ quality: 0.8, allowsEditing: true })
    : await ImagePicker.launchImageLibraryAsync({ quality: 0.8, allowsEditing: true });

  if (!result.canceled && result.assets[0]) {
    const photo: JobPhoto = {
      id: `photo_${Date.now()}`,
      uri: result.assets[0].uri,
      type: photoType,
      annotation: '',
      timestamp: new Date().toISOString(),
    };
    const newPhotos = [...job.photos, photo];
    updateJob(job.id, { photos: newPhotos });
    setJob({ ...job, photos: newPhotos });
  }
};
