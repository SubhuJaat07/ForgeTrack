import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  useColorScheme,
  Switch,
  Alert,
  Linking,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import Animated, { FadeIn, FadeInDown } from 'react-native-reanimated';
import { Ionicons, Feather, FontAwesome5, MaterialIcons } from '@expo/vector-icons';
import { useSettingsStore } from '@/stores/useSettingsStore';
import { useJobStore } from '@/stores/useJobStore';
import { useClientStore } from '@/stores/useClientStore';
import { ThemeMode, NotificationSetting } from '@/types';
import { exportAllData } from '@/utils/storage';
import * as Sharing from 'expo-sharing';
import { requestNotificationPermission } from '@/utils/notifications';

export default function SettingsScreen() {
  const colorScheme = useColorScheme();
  const isDark = colorScheme === 'dark';
  const settings = useSettingsStore();
  const { jobs } = useJobStore();
  const { clients } = useClientStore();

  const textColor = isDark ? '#e2e8f0' : '#1e293b';
  const mutedColor = isDark ? '#94a3b8' : '#64748b';
  const cardBg = isDark ? '#1a1a2e' : '#ffffff';
  const inputBg = isDark ? '#252542' : '#f1f5f9';

  const handleThemeChange = (theme: ThemeMode) => {
    settings.setTheme(theme);
  };

  const handleNotificationChange = async (enabled: boolean) => {
    if (enabled) {
      const granted = await requestNotificationPermission();
      if (granted) {
        settings.setNotifications('all');
      } else {
        Alert.alert('Permission Denied', 'Please enable notifications in Settings');
      }
    } else {
      settings.setNotifications('none');
    }
  };

  const handleExport = async () => {
    try {
      const filePath = await exportAllData(jobs, clients);
      const canShare = await Sharing.isAvailableAsync();
      if (canShare) {
        await Sharing.shareAsync(filePath);
      }
    } catch (error) {
      Alert.alert('Export Failed', 'Could not export data. Please try again.');
    }
  };

  const handleClearData = () => {
    Alert.alert(
      'Clear All Data?',
      'This will delete all jobs, clients, and settings. This action cannot be undone.',
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Clear Everything',
          style: 'destructive',
          onPress: () => {
            Alert.alert('Data Cleared', 'All data has been cleared.');
          },
        },
      ]
    );
  };

  const currencyOptions = ['USD', 'EUR', 'GBP', 'INR', 'CAD', 'AUD'];

  const SettingRow = ({
    icon,
    iconColor,
    title,
    subtitle,
    children,
  }: {
    icon: React.ReactNode;
    iconColor: string;
    title: string;
    subtitle?: string;
    children?: React.ReactNode;
  }) => (
    <View style={[styles.settingRow, { borderBottomColor: isDark ? '#2d2d50' : '#e2e8f0' }]}>
      <View style={styles.settingLeft}>
        <View style={[styles.settingIcon, { backgroundColor: `${iconColor}20` }]}>
          {icon}
        </View>
        <View style={{ marginLeft: 12 }}>
          <Text style={{ fontSize: 15, fontWeight: '600', color: textColor }}>{title}</Text>
          {subtitle && (
            <Text style={{ fontSize: 12, color: mutedColor, marginTop: 1 }}>{subtitle}</Text>
          )}
        </View>
      </View>
      {children && <View style={styles.settingRight}>{children}</View>}
    </View>
  );

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: isDark ? '#0f0f1a' : '#f8fafc' }]}>
      <ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>
        {/* Header */}
        <Animated.View entering={FadeIn.duration(400)} style={styles.header}>
          <Text style={{ fontSize: 26, fontWeight: '800', color: textColor }}>Settings</Text>
        </Animated.View>

        {/* Profile Card */}
        <Animated.View entering={FadeInDown.delay(100).duration(500)}>
          <View style={[styles.profileCard, { backgroundColor: cardBg }]}>
            <View style={styles.avatar}>
              <Text style={{ fontSize: 28, fontWeight: '800', color: '#fff' }}>
                {(settings.userName || 'U').charAt(0).toUpperCase()}
              </Text>
            </View>
            <View style={{ flex: 1, marginLeft: 16 }}>
              <Text style={{ fontSize: 18, fontWeight: '700', color: textColor }}>
                {settings.userName || 'Set your name'}
              </Text>
              <Text style={{ fontSize: 13, color: mutedColor }}>
                {settings.userRole} {settings.userCompany ? `• ${settings.userCompany}` : ''}
              </Text>
            </View>
            <TouchableOpacity onPress={() => settings.setOnboarded(false)}>
              <Feather name="edit-2" size={18} color="#6C5CE7" />
            </TouchableOpacity>
          </View>
        </Animated.View>

        {/* Appearance */}
        <Animated.View entering={FadeInDown.delay(200).duration(500)}>
          <Text style={styles.sectionTitle}>Appearance</Text>
          <View style={[styles.sectionCard, { backgroundColor: cardBg }]}>
            <SettingRow
              icon={<FontAwesome5 name="palette" size={18} color="#6C5CE7" />}
              iconColor="#6C5CE7"
              title="Theme"
              subtitle="App appearance"
            >
              <View style={styles.themeOptions}>
                {(['system', 'light', 'dark'] as const).map((t) => (
                  <TouchableOpacity
                    key={t}
                    style={[
                      styles.themeChip,
                      {
                        backgroundColor: settings.theme === t ? '#6C5CE7' : inputBg,
                      },
                    ]}
                    onPress={() => handleThemeChange(t)}
                  >
                    <Text
                      style={{
                        fontSize: 12,
                        fontWeight: '600',
                        color: settings.theme === t ? '#fff' : mutedColor,
                        textTransform: 'capitalize',
                      }}
                    >
                      {t}
                    </Text>
                  </TouchableOpacity>
                ))}
              </View>
            </SettingRow>
          </View>
        </Animated.View>

        {/* Notifications */}
        <Animated.View entering={FadeInDown.delay(300).duration(500)}>
          <Text style={styles.sectionTitle}>Notifications</Text>
          <View style={[styles.sectionCard, { backgroundColor: cardBg }]}>
            <SettingRow
              icon={<Feather name="bell" size={18} color="#fdcb6e" />}
              iconColor="#fdcb6e"
              title="Push Notifications"
              subtitle="Job reminders and updates"
            >
              <Switch
                value={settings.notifications !== 'none'}
                onValueChange={handleNotificationChange}
                trackColor={{ false: '#2d2d50', true: '#6C5CE7' }}
                thumbColor="#fff"
              />
            </SettingRow>
          </View>
        </Animated.View>

        {/* Job Settings */}
        <Animated.View entering={FadeInDown.delay(400).duration(500)}>
          <Text style={styles.sectionTitle}>Job Settings</Text>
          <View style={[styles.sectionCard, { backgroundColor: cardBg }]}>
            <SettingRow
              icon={<MaterialIcons name="picture-as-pdf" size={18} color="#ff6b6b" />}
              iconColor="#ff6b6b"
              title="Auto Generate PDF"
              subtitle="Create PDF report on job completion"
            >
              <Switch
                value={settings.autoGeneratePdf}
                onValueChange={settings.setAutoGeneratePdf}
                trackColor={{ false: '#2d2d50', true: '#6C5CE7' }}
                thumbColor="#fff"
              />
            </SettingRow>
            <SettingRow
              icon={<FontAwesome5 name="dollar-sign" size={18} color="#00b894" />}
              iconColor="#00b894"
              title="Currency"
              subtitle="Default currency for revenue"
            >
              <View style={styles.currencyPicker}>
                {currencyOptions.map((c) => (
                  <TouchableOpacity
                    key={c}
                    style={[
                      styles.currencyChip,
                      {
                        backgroundColor: settings.currency === c ? '#6C5CE7' : inputBg,
                      },
                    ]}
                    onPress={() => settings.setCurrency(c)}
                  >
                    <Text
                      style={{
                        fontSize: 12,
                        fontWeight: '600',
                        color: settings.currency === c ? '#fff' : mutedColor,
                      }}
                    >
                      {c}
                    </Text>
                  </TouchableOpacity>
                ))}
              </View>
            </SettingRow>
          </View>
        </Animated.View>

        {/* Data */}
        <Animated.View entering={FadeInDown.delay(500).duration(500)}>
          <Text style={styles.sectionTitle}>Data</Text>
          <View style={[styles.sectionCard, { backgroundColor: cardBg }]}>
            <TouchableOpacity style={[styles.actionRow, { borderBottomColor: isDark ? '#2d2d50' : '#e2e8f0' }]} onPress={handleExport}>
              <View style={{ flexDirection: 'row', alignItems: 'center', gap: 12 }}>
                <View style={[styles.settingIcon, { backgroundColor: '#74b9ff20' }]}>
                  <Feather name="download" size={18} color="#74b9ff" />
                </View>
                <View>
                  <Text style={{ fontSize: 15, fontWeight: '600', color: textColor }}>Export Data</Text>
                  <Text style={{ fontSize: 12, color: mutedColor }}>Download all jobs & clients</Text>
                </View>
              </View>
              <Feather name="chevron-right" size={18} color={mutedColor} />
            </TouchableOpacity>
            <TouchableOpacity style={styles.actionRow} onPress={handleClearData}>
              <View style={{ flexDirection: 'row', alignItems: 'center', gap: 12 }}>
                <View style={[styles.settingIcon, { backgroundColor: '#ff6b6b20' }]}>
                  <Feather name="trash-2" size={18} color="#ff6b6b" />
                </View>
                <View>
                  <Text style={{ fontSize: 15, fontWeight: '600', color: '#ff6b6b' }}>Clear All Data</Text>
                  <Text style={{ fontSize: 12, color: mutedColor }}>Delete everything</Text>
                </View>
              </View>
              <Feather name="chevron-right" size={18} color={mutedColor} />
            </TouchableOpacity>
          </View>
        </Animated.View>

        {/* Info */}
        <Animated.View entering={FadeInDown.delay(600).duration(500)}>
          <Text style={styles.sectionTitle}>About</Text>
          <View style={[styles.sectionCard, { backgroundColor: cardBg }]}>
            <SettingRow
              icon={<Feather name="info" size={18} color="#6C5CE7" />}
              iconColor="#6C5CE7"
              title="Version"
              subtitle="ForgeTrack v1.0.0"
            />
            <SettingRow
              icon={<FontAwesome5 name="github" size={18} color="#94a3b8" />}
              iconColor="#94a3b8"
              title="GitHub"
              subtitle="View source code"
            >
              <TouchableOpacity onPress={() => Linking.openURL('https://github.com/SubhuJaat07/ForgeTrack')}>
                <Feather name="external-link" size={18} color="#6C5CE7" />
              </TouchableOpacity>
            </SettingRow>
          </View>
        </Animated.View>

        <View style={{ height: 40 }} />
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  content: { paddingHorizontal: 20, paddingTop: 16 },
  header: { marginBottom: 20 },
  profileCard: {
    flexDirection: 'row',
    alignItems: 'center',
    borderRadius: 16,
    padding: 16,
    marginBottom: 24,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 8,
    elevation: 3,
  },
  avatar: {
    width: 56,
    height: 56,
    borderRadius: 18,
    backgroundColor: '#6C5CE7',
    alignItems: 'center',
    justifyContent: 'center',
  },
  sectionTitle: {
    fontSize: 14,
    fontWeight: '700',
    color: '#64748b',
    textTransform: 'uppercase',
    letterSpacing: 0.5,
    marginBottom: 8,
    marginLeft: 4,
  },
  sectionCard: {
    borderRadius: 16,
    overflow: 'hidden',
    marginBottom: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 8,
    elevation: 3,
  },
  settingRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 14,
    borderBottomWidth: 1,
  },
  settingLeft: { flexDirection: 'row', alignItems: 'center', flex: 1 },
  settingIcon: {
    width: 36,
    height: 36,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
  },
  settingRight: {},
  themeOptions: { flexDirection: 'row', gap: 6 },
  themeChip: { paddingHorizontal: 10, paddingVertical: 6, borderRadius: 8 },
  currencyPicker: { flexDirection: 'row', flexWrap: 'wrap', gap: 4 },
  currencyChip: { paddingHorizontal: 8, paddingVertical: 5, borderRadius: 6 },
  actionRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 14,
    borderBottomWidth: 1,
  },
});
