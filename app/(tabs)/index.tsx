import React, { useState, useEffect, useCallback } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  useColorScheme,
  RefreshControl,
  StatusBar,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { router } from 'expo-router';
import Animated, {
  FadeIn,
  FadeInDown,
  useSharedValue,
  useAnimatedStyle,
  withSpring,
  withRepeat,
  withTiming,
  interpolateColor,
} from 'react-native-reanimated';
import { BarChart, DonutChart } from '@/components/Charts';
import { ThemedCard } from '@/components/ui/ThemedView';
import { ThemedText } from '@/components/ui/ThemedText';
import { StatusBadge } from '@/components/ui/StatusBadge';
import { ProgressBar } from '@/components/ui/ProgressBar';
import { CardSkeleton } from '@/components/ui/SkeletonLoader';
import { useJobStore } from '@/stores/useJobStore';
import { useTimerStore } from '@/stores/useTimerStore';
import { useSettingsStore } from '@/stores/useSettingsStore';
import { Job } from '@/types';
import { formatCurrency, formatDuration, getStatusColor } from '@/utils/theme';
import { Feather, MaterialIcons, Ionicons } from '@expo/vector-icons';

export default function DashboardScreen() {
  const colorScheme = useColorScheme();
  const isDark = colorScheme === 'dark';
  const { jobs, getTodayJobs, getWeekJobs } = useJobStore();
  const { activeJobId, isRunning, elapsedSeconds } = useTimerStore();
  const { userName, isOnboarded } = useSettingsStore();
  const [refreshing, setRefreshing] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isOnboarded) {
      router.replace('/onboarding');
      return;
    }
    const timer = setTimeout(() => setLoading(false), 1000);
    return () => clearTimeout(timer);
  }, [isOnboarded]);

  const todayJobs = getTodayJobs();
  const weekJobs = getWeekJobs();
  const completedToday = todayJobs.filter((j) => j.status === 'completed').length;
  const activeJob = jobs.find((j) => j.id === activeJobId);
  const weekRevenue = weekJobs.reduce((sum, j) => j.revenue, 0);
  const totalWeekJobs = weekJobs.length;

  const onRefresh = useCallback(() => {
    setRefreshing(true);
    setTimeout(() => setRefreshing(false), 1000);
  }, []);

  const greeting = () => {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good Morning';
    if (hour < 17) return 'Good Afternoon';
    return 'Good Evening';
  };

  const weeklyData = [
    { label: 'Mon', value: 3, color: '#6C5CE7' },
    { label: 'Tue', value: 5, color: '#00b894' },
    { label: 'Wed', value: 2, color: '#74b9ff' },
    { label: 'Thu', value: 7, color: '#fdcb6e' },
    { label: 'Fri', value: 4, color: '#ff6b6b' },
    { label: 'Sat', value: 1, color: '#a29bfe' },
    { label: 'Sun', value: 0, color: '#636e72' },
  ];

  const statusData = [
    { value: totalWeekJobs > 0 ? weekJobs.filter((j) => j.status === 'completed').length : 4, color: '#00b894', label: 'Completed' },
    { value: totalWeekJobs > 0 ? weekJobs.filter((j) => j.status === 'in-progress').length : 2, color: '#6C5CE7', label: 'In Progress' },
    { value: totalWeekJobs > 0 ? weekJobs.filter((j) => j.status === 'scheduled').length : 3, color: '#74b9ff', label: 'Scheduled' },
  ];

  if (loading) {
    return (
      <SafeAreaView style={[styles.container, { backgroundColor: isDark ? '#0f0f1a' : '#f8fafc' }]}>
        <ScrollView contentContainerStyle={styles.content}>
          <CardSkeleton />
          <View style={{ height: 16 }} />
          <CardSkeleton />
          <View style={{ height: 16 }} />
          <CardSkeleton />
        </ScrollView>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: isDark ? '#0f0f1a' : '#f8fafc' }]}>
      <StatusBar barStyle={isDark ? 'light-content' : 'dark-content'} />
      <ScrollView
        contentContainerStyle={styles.content}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor="#6C5CE7" />
        }
        showsVerticalScrollIndicator={false}
      >
        {/* Header */}
        <Animated.View entering={FadeIn.duration(500)} style={styles.header}>
          <View>
            <Text style={{ fontSize: 14, color: isDark ? '#94a3b8' : '#64748b', fontWeight: '500' }}>
              {greeting()} {userName ? userName.split(' ')[0] : 'Worker'} 👋
            </Text>
            <Text style={{ fontSize: 26, fontWeight: '800', color: isDark ? '#e2e8f0' : '#1e293b', marginTop: 2 }}>
              Dashboard
            </Text>
          </View>
          <TouchableOpacity
            style={styles.createBtn}
            onPress={() => router.push('/job/create')}
          >
            <Ionicons name="add" size={20} color="#fff" />
          </TouchableOpacity>
        </Animated.View>

        {/* Active Timer Banner */}
        {isRunning && activeJob && (
          <Animated.View
            entering={FadeInDown.duration(400)}
            style={[styles.activeJobBanner, { backgroundColor: '#6C5CE720', borderColor: '#6C5CE740' }]}
          >
            <View style={{ flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' }}>
              <View>
                <Text style={{ fontSize: 12, color: '#6C5CE7', fontWeight: '600' }}>⏱ Active Job</Text>
                <Text style={{ fontSize: 18, fontWeight: '700', color: isDark ? '#e2e8f0' : '#1e293b' }}>
                  {activeJob.title}
                </Text>
                <Text style={{ fontSize: 13, color: '#6C5CE7', fontWeight: '500' }}>
                  {formatDuration(elapsedSeconds)}
                </Text>
              </View>
              <TouchableOpacity
                style={[styles.resumeBtn, { backgroundColor: '#6C5CE7' }]}
                onPress={() => router.push(`/job/${activeJob.id}`)}
              >
                <Text style={{ color: '#fff', fontWeight: '600', fontSize: 13 }}>View</Text>
              </TouchableOpacity>
            </View>
          </Animated.View>
        )}

        {/* Stats Grid */}
        <Animated.View entering={FadeInDown.delay(100).duration(500)} style={styles.statsGrid}>
          <View style={[styles.statCard, { backgroundColor: isDark ? '#1a1a2e' : '#ffffff' }]}>
            <View style={[styles.statIcon, { backgroundColor: '#6C5CE720' }]}>
              <Feather name="briefcase" size={18} color="#6C5CE7" />
            </View>
            <Text style={{ fontSize: 24, fontWeight: '800', color: isDark ? '#e2e8f0' : '#1e293b' }}>
              {todayJobs.length}
            </Text>
            <Text style={{ fontSize: 12, color: isDark ? '#94a3b8' : '#64748b', fontWeight: '500' }}>
              Today's Jobs
            </Text>
          </View>

          <View style={[styles.statCard, { backgroundColor: isDark ? '#1a1a2e' : '#ffffff' }]}>
            <View style={[styles.statIcon, { backgroundColor: '#00b89420' }]}>
              <Feather name="check-circle" size={18} color="#00b894" />
            </View>
            <Text style={{ fontSize: 24, fontWeight: '800', color: isDark ? '#e2e8f0' : '#1e293b' }}>
              {completedToday}
            </Text>
            <Text style={{ fontSize: 12, color: isDark ? '#94a3b8' : '#64748b', fontWeight: '500' }}>
              Completed
            </Text>
          </View>

          <View style={[styles.statCard, { backgroundColor: isDark ? '#1a1a2e' : '#ffffff' }]}>
            <View style={[styles.statIcon, { backgroundColor: '#fdcb6e20' }]}>
              <Feather name="dollar-sign" size={18} color="#fdcb6e" />
            </View>
            <Text style={{ fontSize: 24, fontWeight: '800', color: isDark ? '#e2e8f0' : '#1e293b' }}>
              {formatCurrency(weekRevenue)}
            </Text>
            <Text style={{ fontSize: 12, color: isDark ? '#94a3b8' : '#64748b', fontWeight: '500' }}>
              Week Revenue
            </Text>
          </View>

          <View style={[styles.statCard, { backgroundColor: isDark ? '#1a1a2e' : '#ffffff' }]}>
            <View style={[styles.statIcon, { backgroundColor: '#ff6b6b20' }]}>
              <Feather name="clock" size={18} color="#ff6b6b" />
            </View>
            <Text style={{ fontSize: 24, fontWeight: '800', color: isDark ? '#e2e8f0' : '#1e293b' }}>
              {totalWeekJobs}
            </Text>
            <Text style={{ fontSize: 12, color: isDark ? '#94a3b8' : '#64748b', fontWeight: '500' }}>
              Week Total
            </Text>
          </View>
        </Animated.View>

        {/* Today's Progress */}
        <Animated.View entering={FadeInDown.delay(200).duration(500)}>
          <ThemedCard style={styles.sectionCard}>
            <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
              <Text style={{ fontSize: 16, fontWeight: '700', color: isDark ? '#e2e8f0' : '#1e293b' }}>
                Today's Progress
              </Text>
              <Text style={{ fontSize: 13, color: '#6C5CE7', fontWeight: '600' }}>
                {todayJobs.length > 0 ? Math.round((completedToday / todayJobs.length) * 100) : 0}%
              </Text>
            </View>
            <ProgressBar
              progress={todayJobs.length > 0 ? completedToday / todayJobs.length : 0}
              color="#6C5CE7"
              height={10}
            />
            <Text style={{ fontSize: 12, color: isDark ? '#94a3b8' : '#64748b', marginTop: 8 }}>
              {completedToday} of {todayJobs.length} jobs completed today
            </Text>
          </ThemedCard>
        </Animated.View>

        {/* Weekly Chart */}
        <Animated.View entering={FadeInDown.delay(300).duration(500)}>
          <ThemedCard style={styles.sectionCard}>
            <Text style={{ fontSize: 16, fontWeight: '700', color: isDark ? '#e2e8f0' : '#1e293b', marginBottom: 16 }}>
              Weekly Overview
            </Text>
            <BarChart data={weeklyData} height={180} />
          </ThemedCard>
        </Animated.View>

        {/* Job Status Breakdown */}
        <Animated.View entering={FadeInDown.delay(400).duration(500)}>
          <ThemedCard style={styles.sectionCard}>
            <Text style={{ fontSize: 16, fontWeight: '700', color: isDark ? '#e2e8f0' : '#1e293b', marginBottom: 16 }}>
              Job Status Breakdown
            </Text>
            <DonutChart data={statusData} />
          </ThemedCard>
        </Animated.View>

        {/* Today's Jobs */}
        <Animated.View entering={FadeInDown.delay(500).duration(500)}>
          <View style={styles.sectionHeader}>
            <Text style={{ fontSize: 18, fontWeight: '700', color: isDark ? '#e2e8f0' : '#1e293b' }}>
              Today's Jobs
            </Text>
            <TouchableOpacity onPress={() => router.push('/(tabs)/jobs')}>
              <Text style={{ fontSize: 13, color: '#6C5CE7', fontWeight: '600' }}>View All</Text>
            </TouchableOpacity>
          </View>
          {todayJobs.length === 0 ? (
            <ThemedCard style={styles.emptyCard}>
              <Text style={{ fontSize: 40 }}>📋</Text>
              <Text style={{ fontSize: 15, fontWeight: '600', color: isDark ? '#e2e8f0' : '#1e293b', marginTop: 8 }}>
                No jobs scheduled
              </Text>
              <Text style={{ fontSize: 13, color: isDark ? '#94a3b8' : '#64748b', marginTop: 4 }}>
                Tap + to create your first job
              </Text>
            </ThemedCard>
          ) : (
            todayJobs.slice(0, 5).map((job) => (
              <TouchableOpacity
                key={job.id}
                style={[styles.jobCard, { backgroundColor: isDark ? '#1a1a2e' : '#ffffff' }]}
                onPress={() => router.push(`/job/${job.id}`)}
              >
                <View style={styles.jobCardLeft}>
                  <View
                    style={[
                      styles.priorityIndicator,
                      { backgroundColor: job.priority === 'urgent' ? '#ff6b6b' : job.priority === 'high' ? '#fdcb6e' : '#6C5CE7' },
                    ]}
                  />
                  <View style={{ marginLeft: 12, flex: 1 }}>
                    <Text style={{ fontSize: 15, fontWeight: '600', color: isDark ? '#e2e8f0' : '#1e293b' }} numberOfLines={1}>
                      {job.title}
                    </Text>
                    <Text style={{ fontSize: 12, color: isDark ? '#94a3b8' : '#64748b', marginTop: 2 }} numberOfLines={1}>
                      {job.clientName}
                    </Text>
                  </View>
                </View>
                <View style={{ alignItems: 'flex-end' }}>
                  <StatusBadge label={job.status} type="status" />
                  <Text style={{ fontSize: 11, color: isDark ? '#94a3b8' : '#64748b', marginTop: 4 }}>
                    {new Date(job.scheduledTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                  </Text>
                </View>
              </TouchableOpacity>
            ))
          )}
        </Animated.View>

        <View style={{ height: 40 }} />
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  content: {
    paddingHorizontal: 20,
    paddingTop: 16,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 20,
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
  activeJobBanner: {
    borderRadius: 16,
    padding: 16,
    marginBottom: 20,
    borderWidth: 1,
  },
  resumeBtn: {
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 10,
  },
  statsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 12,
    marginBottom: 20,
  },
  statCard: {
    flex: 1,
    minWidth: '46%',
    borderRadius: 16,
    padding: 16,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 8,
    elevation: 3,
  },
  statIcon: {
    width: 40,
    height: 40,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 8,
  },
  sectionCard: {
    marginBottom: 20,
  },
  sectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  emptyCard: {
    alignItems: 'center',
    paddingVertical: 32,
  },
  jobCard: {
    flexDirection: 'row',
    justifyContent: 'space-between',
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
  jobCardLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
  },
  priorityIndicator: {
    width: 4,
    height: 36,
    borderRadius: 2,
  },
});
