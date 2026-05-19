import React, { useState, useEffect, useMemo } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
  TextInput,
  useColorScheme,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { router } from 'expo-router';
import Animated, { FadeIn, FadeInDown } from 'react-native-reanimated';
import { Feather, Ionicons } from '@expo/vector-icons';
import { StatusBadge } from '@/components/ui/StatusBadge';
import { useJobStore } from '@/stores/useJobStore';
import { Job, JobStatus } from '@/types';
import { formatCurrency } from '@/utils/theme';

const statusFilters: { label: string; value: JobStatus | 'all' }[] = [
  { label: 'All', value: 'all' },
  { label: 'Scheduled', value: 'scheduled' },
  { label: 'In Progress', value: 'in-progress' },
  { label: 'Completed', value: 'completed' },
  { label: 'Cancelled', value: 'cancelled' },
];

export default function JobsScreen() {
  const colorScheme = useColorScheme();
  const isDark = colorScheme === 'dark';
  const { jobs } = useJobStore();
  const [search, setSearch] = useState('');
  const [activeFilter, setActiveFilter] = useState<JobStatus | 'all'>('all');
  const [sortBy, setSortBy] = useState<'date' | 'priority' | 'revenue'>('date');

  const filteredJobs = useMemo(() => {
    let result = [...jobs];

    if (activeFilter !== 'all') {
      result = result.filter((j) => j.status === activeFilter);
    }

    if (search) {
      const q = search.toLowerCase();
      result = result.filter(
        (j) =>
          j.title.toLowerCase().includes(q) ||
          j.clientName.toLowerCase().includes(q) ||
          j.description.toLowerCase().includes(q)
      );
    }

    const priorityOrder = { urgent: 0, high: 1, medium: 2, low: 3 };
    switch (sortBy) {
      case 'priority':
        result.sort((a, b) => priorityOrder[a.priority] - priorityOrder[b.priority]);
        break;
      case 'revenue':
        result.sort((a, b) => b.revenue - a.revenue);
        break;
      default:
        result.sort((a, b) => new Date(b.scheduledDate).getTime() - new Date(a.scheduledDate).getTime());
    }

    return result;
  }, [jobs, search, activeFilter, sortBy]);

  const renderJob = ({ item, index }: { item: Job; index: number }) => (
    <Animated.View entering={FadeInDown.delay(index * 50).duration(300)}>
      <TouchableOpacity
        style={[styles.jobCard, { backgroundColor: isDark ? '#1a1a2e' : '#ffffff' }]}
        onPress={() => router.push(`/job/${item.id}`)}
        activeOpacity={0.7}
      >
        <View style={styles.jobHeader}>
          <View style={styles.jobHeaderLeft}>
            <View
              style={[
                styles.priorityDot,
                {
                  backgroundColor:
                    item.priority === 'urgent'
                      ? '#ff6b6b'
                      : item.priority === 'high'
                      ? '#fdcb6e'
                      : item.priority === 'medium'
                      ? '#74b9ff'
                      : '#94a3b8',
                },
              ]}
            />
            <Text style={{ fontSize: 15, fontWeight: '600', color: isDark ? '#e2e8f0' : '#1e293b', flex: 1 }} numberOfLines={1}>
              {item.title}
            </Text>
          </View>
          <StatusBadge label={item.status} type="status" />
        </View>
        <Text style={{ fontSize: 13, color: isDark ? '#94a3b8' : '#64748b', marginTop: 4 }} numberOfLines={1}>
          {item.clientName}
        </Text>
        <View style={styles.jobFooter}>
          <View style={{ flexDirection: 'row', alignItems: 'center', gap: 4 }}>
            <Feather name="calendar" size={13} color={isDark ? '#94a3b8' : '#64748b'} />
            <Text style={{ fontSize: 12, color: isDark ? '#94a3b8' : '#64748b' }}>
              {new Date(item.scheduledDate).toLocaleDateString()}
            </Text>
          </View>
          <View style={{ flexDirection: 'row', alignItems: 'center', gap: 4 }}>
            <Feather name="dollar-sign" size={13} color="#00b894" />
            <Text style={{ fontSize: 12, color: '#00b894', fontWeight: '600' }}>
              {formatCurrency(item.revenue)}
            </Text>
          </View>
          <View style={{ flexDirection: 'row', alignItems: 'center', gap: 4 }}>
            <Feather name="image" size={13} color={isDark ? '#94a3b8' : '#64748b'} />
            <Text style={{ fontSize: 12, color: isDark ? '#94a3b8' : '#64748b' }}>
              {item.photos.length}
            </Text>
          </View>
        </View>
      </TouchableOpacity>
    </Animated.View>
  );

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: isDark ? '#0f0f1a' : '#f8fafc' }]}>
      <Animated.View entering={FadeIn.duration(400)} style={styles.header}>
        <Text style={{ fontSize: 26, fontWeight: '800', color: isDark ? '#e2e8f0' : '#1e293b' }}>
          Jobs
        </Text>
        <TouchableOpacity
          style={styles.createBtn}
          onPress={() => router.push('/job/create')}
        >
          <Ionicons name="add" size={22} color="#fff" />
        </TouchableOpacity>
      </Animated.View>

      {/* Search */}
      <Animated.View entering={FadeInDown.delay(100).duration(300)}>
        <View style={[styles.searchBox, { backgroundColor: isDark ? '#1a1a2e' : '#ffffff' }]}>
          <Feather name="search" size={18} color={isDark ? '#94a3b8' : '#64748b'} />
          <TextInput
            style={[styles.searchInput, { color: isDark ? '#e2e8f0' : '#1e293b' }]}
            placeholder="Search jobs..."
            placeholderTextColor={isDark ? '#64748b' : '#94a3b8'}
            value={search}
            onChangeText={setSearch}
          />
        </View>
      </Animated.View>

      {/* Filters */}
      <Animated.View entering={FadeInDown.delay(200).duration(300)} style={styles.filterRow}>
        <FlatList
          data={statusFilters}
          keyExtractor={(item) => item.value}
          horizontal
          showsHorizontalScrollIndicator={false}
          renderItem={({ item }) => (
            <TouchableOpacity
              style={[
                styles.filterChip,
                {
                  backgroundColor: activeFilter === item.value ? '#6C5CE7' : isDark ? '#252542' : '#f1f5f9',
                },
              ]}
              onPress={() => setActiveFilter(item.value)}
            >
              <Text
                style={{
                  fontSize: 13,
                  fontWeight: '600',
                  color: activeFilter === item.value ? '#fff' : isDark ? '#94a3b8' : '#64748b',
                }}
              >
                {item.label}
              </Text>
            </TouchableOpacity>
          )}
          contentContainerStyle={{ gap: 8 }}
        />
      </Animated.View>

      {/* Sort */}
      <View style={styles.sortRow}>
        <Text style={{ fontSize: 13, color: isDark ? '#94a3b8' : '#64748b' }}>
          {filteredJobs.length} jobs
        </Text>
        <TouchableOpacity
          style={styles.sortBtn}
          onPress={() => {
            const options: ('date' | 'priority' | 'revenue')[] = ['date', 'priority', 'revenue'];
            setSortBy(options[(options.indexOf(sortBy) + 1) % options.length]);
          }}
        >
          <Feather name="arrow-down" size={14} color="#6C5CE7" />
          <Text style={{ fontSize: 12, color: '#6C5CE7', fontWeight: '600', marginLeft: 4 }}>
            Sort: {sortBy}
          </Text>
        </TouchableOpacity>
      </View>

      {/* Jobs List */}
      <FlatList
        data={filteredJobs}
        keyExtractor={(item) => item.id}
        renderItem={renderJob}
        contentContainerStyle={styles.jobsList}
        ListEmptyComponent={
          <View style={styles.emptyContainer}>
            <Text style={{ fontSize: 48 }}>💼</Text>
            <Text style={{ fontSize: 16, fontWeight: '600', color: isDark ? '#e2e8f0' : '#1e293b', marginTop: 12 }}>
              No jobs found
            </Text>
            <Text style={{ fontSize: 13, color: isDark ? '#94a3b8' : '#64748b', marginTop: 4 }}>
              {search ? 'Try a different search' : 'Create your first job to get started'}
            </Text>
          </View>
        }
        showsVerticalScrollIndicator={false}
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
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
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 2,
  },
  searchInput: {
    flex: 1,
    fontSize: 15,
    fontWeight: '500',
  },
  filterRow: {
    paddingHorizontal: 20,
    marginTop: 12,
  },
  filterChip: {
    paddingHorizontal: 14,
    paddingVertical: 8,
    borderRadius: 20,
  },
  sortRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    marginTop: 12,
    marginBottom: 8,
  },
  sortBtn: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  jobsList: {
    paddingHorizontal: 20,
    paddingBottom: 20,
  },
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
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  jobHeaderLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
  },
  priorityDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    marginRight: 10,
  },
  jobFooter: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 16,
    marginTop: 10,
    paddingTop: 10,
    borderTopWidth: 1,
    borderTopColor: '#2d2d50',
  },
  emptyContainer: {
    alignItems: 'center',
    paddingTop: 80,
  },
});
