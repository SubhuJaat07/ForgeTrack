import React, { useState, useMemo } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
  useColorScheme,
  Dimensions,
  ActivityIndicator,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { router } from 'expo-router';
import Animated, { FadeIn, FadeInDown } from 'react-native-reanimated';
import { Feather, Ionicons } from '@expo/vector-icons';
import { useJobStore } from '@/stores/useJobStore';
import { Job } from '@/types';
import { formatCurrency, formatDuration, getStatusColor } from '@/utils/theme';

const { width } = Dimensions.get('window');

type ViewMode = 'list' | 'map';

export default function HistoryScreen() {
  const colorScheme = useColorScheme();
  const isDark = colorScheme === 'dark';
  const { jobs } = useJobStore();
  const [viewMode, setViewMode] = useState<ViewMode>('list');
  const [filter, setFilter] = useState<'all' | 'completed' | 'cancelled'>('all');

  const completedJobs = useMemo(() => {
    let result = jobs.filter(
      (j) => j.status === 'completed' || j.status === 'cancelled'
    );

    if (filter !== 'all') {
      result = result.filter((j) => j.status === filter);
    }

    result.sort(
      (a, b) =>
        new Date(b.completedAt || b.updatedAt).getTime() -
        new Date(a.completedAt || a.updatedAt).getTime()
    );
    return result;
  }, [jobs, filter]);

  const totalRevenue = completedJobs
    .filter((j) => j.status === 'completed')
    .reduce((sum, j) => sum + j.revenue, 0);

  const totalHours = completedJobs
    .filter((j) => j.status === 'completed')
    .reduce((sum, j) => sum + (j.totalDuration || 0), 0);

  const textColor = isDark ? '#e2e8f0' : '#1e293b';
  const mutedColor = isDark ? '#94a3b8' : '#64748b';
  const cardBg = isDark ? '#1a1a2e' : '#ffffff';

  const renderJob = ({ item, index }: { item: Job; index: number }) => {
    const statusColor = getStatusColor(item.status);
    const isCompleted = item.status === 'completed';

    return (
      <Animated.View entering={FadeInDown.delay(index * 50).duration(300)}>
        <TouchableOpacity
          style={[styles.jobCard, { backgroundColor: cardBg }]}
          onPress={() => router.push(`/job/${item.id}`)}
          activeOpacity={0.7}
        >
          <View style={styles.jobHeader}>
            <View
              style={[
                styles.statusIndicator,
                { backgroundColor: isCompleted ? '#00b894' : '#ff6b6b' },
              ]}
            />
            <View style={{ flex: 1, marginLeft: 10 }}>
              <Text style={{ fontSize: 15, fontWeight: '600', color: textColor }} numberOfLines={1}>
                {item.title}
              </Text>
              <Text style={{ fontSize: 12, color: mutedColor, marginTop: 2 }}>
                {item.clientName}
              </Text>
            </View>
            <Text
              style={{
                fontSize: 13,
                fontWeight: '700',
                color: isCompleted ? '#00b894' : '#ff6b6b',
              }}
            >
              {isCompleted ? formatCurrency(item.revenue) : 'Cancelled'}
            </Text>
          </View>

          <View style={styles.jobMeta}>
            <View style={{ flexDirection: 'row', alignItems: 'center', gap: 4 }}>
              <Feather name="calendar" size={12} color={mutedColor} />
              <Text style={{ fontSize: 11, color: mutedColor }}>
                {new Date(item.completedAt || item.updatedAt).toLocaleDateString()}
              </Text>
            </View>
            {isCompleted && item.totalDuration ? (
              <View style={{ flexDirection: 'row', alignItems: 'center', gap: 4 }}>
                <Feather name="clock" size={12} color={mutedColor} />
                <Text style={{ fontSize: 11, color: mutedColor }}>
                  {formatDuration(item.totalDuration)}
                </Text>
              </View>
            ) : null}
            <View style={{ flexDirection: 'row', alignItems: 'center', gap: 4 }}>
              <Feather name="image" size={12} color={mutedColor} />
              <Text style={{ fontSize: 11, color: mutedColor }}>{item.photos.length}</Text>
            </View>
          </View>
        </TouchableOpacity>
      </Animated.View>
    );
  };

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: isDark ? '#0f0f1a' : '#f8fafc' }]}>
      {/* Header */}
      <Animated.View entering={FadeIn.duration(400)} style={styles.header}>
        <Text style={{ fontSize: 26, fontWeight: '800', color: textColor }}>History</Text>
        <View style={styles.viewToggle}>
          <TouchableOpacity
            style={[styles.toggleBtn, { backgroundColor: viewMode === 'list' ? '#6C5CE7' : 'transparent' }]}
            onPress={() => setViewMode('list')}
          >
            <Feather name="list" size={16} color={viewMode === 'list' ? '#fff' : mutedColor} />
          </TouchableOpacity>
          <TouchableOpacity
            style={[styles.toggleBtn, { backgroundColor: viewMode === 'map' ? '#6C5CE7' : 'transparent' }]}
            onPress={() => setViewMode('map')}
          >
            <Feather name="map" size={16} color={viewMode === 'map' ? '#fff' : mutedColor} />
          </TouchableOpacity>
        </View>
      </Animated.View>

      {/* Summary Stats */}
      <Animated.View entering={FadeInDown.delay(100).duration(400)} style={styles.summaryRow}>
        <View style={[styles.summaryCard, { backgroundColor: cardBg }]}>
          <Text style={{ fontSize: 20, fontWeight: '800', color: '#00b894' }}>{formatCurrency(totalRevenue)}</Text>
          <Text style={{ fontSize: 11, color: mutedColor, fontWeight: '500' }}>Total Revenue</Text>
        </View>
        <View style={[styles.summaryCard, { backgroundColor: cardBg }]}>
          <Text style={{ fontSize: 20, fontWeight: '800', color: '#6C5CE7' }}>
            {Math.floor(totalHours / 3600)}h {Math.floor((totalHours % 3600) / 60)}m
          </Text>
          <Text style={{ fontSize: 11, color: mutedColor, fontWeight: '500' }}>Total Hours</Text>
        </View>
        <View style={[styles.summaryCard, { backgroundColor: cardBg }]}>
          <Text style={{ fontSize: 20, fontWeight: '800', color: textColor }}>{completedJobs.length}</Text>
          <Text style={{ fontSize: 11, color: mutedColor, fontWeight: '500' }}>Total Jobs</Text>
        </View>
      </Animated.View>

      {/* Filter */}
      <Animated.View entering={FadeInDown.delay(200).duration(300)} style={styles.filterRow}>
        {(['all', 'completed', 'cancelled'] as const).map((f) => (
          <TouchableOpacity
            key={f}
            style={[
              styles.filterChip,
              {
                backgroundColor: filter === f ? '#6C5CE7' : isDark ? '#252542' : '#f1f5f9',
              },
            ]}
            onPress={() => setFilter(f)}
          >
            <Text
              style={{
                fontSize: 13,
                fontWeight: '600',
                color: filter === f ? '#fff' : mutedColor,
                textTransform: 'capitalize',
              }}
            >
              {f}
            </Text>
          </TouchableOpacity>
        ))}
      </Animated.View>

      {viewMode === 'list' ? (
        <FlatList
          data={completedJobs}
          keyExtractor={(item) => item.id}
          renderItem={renderJob}
          contentContainerStyle={styles.list}
          ListEmptyComponent={
            <View style={styles.emptyContainer}>
              <Text style={{ fontSize: 48 }}>📋</Text>
              <Text style={{ fontSize: 16, fontWeight: '600', color: textColor, marginTop: 12 }}>
                No history yet
              </Text>
              <Text style={{ fontSize: 13, color: mutedColor, marginTop: 4 }}>
                Completed jobs will appear here
              </Text>
            </View>
          }
          showsVerticalScrollIndicator={false}
        />
      ) : (
        <View style={styles.mapPlaceholder}>
          <View style={[styles.mapBox, { backgroundColor: isDark ? '#1a1a2e' : '#ffffff' }]}>
            <Text style={{ fontSize: 48 }}>🗺️</Text>
            <Text style={{ fontSize: 16, fontWeight: '600', color: textColor, marginTop: 12 }}>
              Map View
            </Text>
            <Text style={{ fontSize: 13, color: mutedColor, marginTop: 4, textAlign: 'center' }}>
              {completedJobs.length > 0
                ? `${completedJobs.length} job locations on map`
                : 'Complete jobs with locations to see them on the map'}
            </Text>
            {completedJobs.length > 0 && (
              <View style={styles.mapLocations}>
                {completedJobs.slice(0, 5).map((job) => (
                  <View key={job.id} style={styles.mapLocationItem}>
                    <Ionicons name="location" size={14} color="#6C5CE7" />
                    <Text style={{ fontSize: 12, color: textColor }} numberOfLines={1}>
                      {job.title} - {job.location.address}
                    </Text>
                  </View>
                ))}
              </View>
            )}
          </View>
        </View>
      )}
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
  viewToggle: {
    flexDirection: 'row',
    backgroundColor: '#2d2d50',
    borderRadius: 10,
    overflow: 'hidden',
  },
  toggleBtn: { padding: 8, paddingHorizontal: 14 },
  summaryRow: {
    flexDirection: 'row',
    paddingHorizontal: 20,
    gap: 10,
    marginBottom: 16,
  },
  summaryCard: {
    flex: 1,
    borderRadius: 14,
    padding: 12,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 2,
  },
  filterRow: {
    flexDirection: 'row',
    paddingHorizontal: 20,
    gap: 8,
    marginBottom: 12,
  },
  filterChip: {
    paddingHorizontal: 14,
    paddingVertical: 8,
    borderRadius: 20,
  },
  list: { paddingHorizontal: 20, paddingBottom: 20 },
  jobCard: {
    borderRadius: 14,
    padding: 14,
    marginBottom: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 2,
  },
  jobHeader: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  statusIndicator: {
    width: 4,
    height: 32,
    borderRadius: 2,
  },
  jobMeta: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 14,
    marginTop: 10,
    paddingTop: 10,
    borderTopWidth: 1,
    borderTopColor: '#2d2d50',
  },
  emptyContainer: { alignItems: 'center', paddingTop: 80 },
  mapPlaceholder: { flex: 1, padding: 20 },
  mapBox: {
    flex: 1,
    borderRadius: 16,
    padding: 24,
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 8,
    elevation: 3,
  },
  mapLocations: {
    width: '100%',
    marginTop: 20,
    gap: 8,
  },
  mapLocationItem: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    paddingVertical: 4,
  },
});
