import React, { useMemo, useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  useColorScheme,
  Dimensions,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import Animated, { FadeIn, FadeInDown } from 'react-native-reanimated';
import { Feather, FontAwesome5, MaterialIcons } from '@expo/vector-icons';
import { BarChart, DonutChart } from '@/components/Charts';
import { useJobStore } from '@/stores/useJobStore';
import { formatCurrency, formatDuration } from '@/utils/theme';

type Period = 'week' | 'month' | 'year';

export default function AnalyticsScreen() {
  const colorScheme = useColorScheme();
  const isDark = colorScheme === 'dark';
  const { jobs } = useJobStore();
  const [period, setPeriod] = useState<Period>('week');

  const textColor = isDark ? '#e2e8f0' : '#1e293b';
  const mutedColor = isDark ? '#94a3b8' : '#64748b';
  const cardBg = isDark ? '#1a1a2e' : '#ffffff';

  const stats = useMemo(() => {
    const now = new Date();
    let periodStart: Date;

    switch (period) {
      case 'month':
        periodStart = new Date(now.getFullYear(), now.getMonth(), 1);
        break;
      case 'year':
        periodStart = new Date(now.getFullYear(), 0, 1);
        break;
      default:
        periodStart = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
    }

    const periodJobs = jobs.filter(
      (j) => new Date(j.scheduledDate) >= periodStart && j.status !== 'cancelled'
    );
    const completedJobs = periodJobs.filter((j) => j.status === 'completed');

    const totalRevenue = completedJobs.reduce((sum, j) => sum + j.revenue, 0);
    const totalCost = completedJobs.reduce((sum, j) => sum + j.cost, 0);
    const totalHours = completedJobs.reduce((sum, j) => sum + (j.totalDuration || 0), 0);
    const avgRevenue = completedJobs.length > 0 ? totalRevenue / completedJobs.length : 0;
    const profitMargin = totalRevenue > 0 ? ((totalRevenue - totalCost) / totalRevenue) * 100 : 0;
    const completionRate = periodJobs.length > 0 ? (completedJobs.length / periodJobs.length) * 100 : 0;

    return {
      totalJobs: periodJobs.length,
      completedJobs: completedJobs.length,
      totalRevenue,
      totalCost,
      totalHours,
      avgRevenue,
      profitMargin,
      completionRate,
    };
  }, [jobs, period]);

  const revenueByDay = useMemo(() => {
    const days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
    const now = new Date();
    return days.map((day, i) => {
      const date = new Date(now);
      date.setDate(date.getDate() - (6 - i));
      const dateStr = date.toISOString().split('T')[0];
      const dayRevenue = jobs
        .filter((j) => j.scheduledDate.startsWith(dateStr) && j.status === 'completed')
        .reduce((sum, j) => sum + j.revenue, 0);
      return { label: day, value: dayRevenue, color: '#6C5CE7' };
    });
  }, [jobs]);

  const jobsByStatus = useMemo(() => {
    const now = new Date();
    const periodStart = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
    const weekJobs = jobs.filter((j) => new Date(j.scheduledDate) >= periodStart);

    return [
      {
        value: weekJobs.filter((j) => j.status === 'completed').length || 0,
        color: '#00b894',
        label: 'Completed',
      },
      {
        value: weekJobs.filter((j) => j.status === 'in-progress').length || 0,
        color: '#6C5CE7',
        label: 'In Progress',
      },
      {
        value: weekJobs.filter((j) => j.status === 'scheduled').length || 0,
        color: '#74b9ff',
        label: 'Scheduled',
      },
      {
        value: weekJobs.filter((j) => j.status === 'cancelled').length || 0,
        color: '#ff6b6b',
        label: 'Cancelled',
      },
    ];
  }, [jobs]);

  const revenueByPriority = useMemo(() => {
    const completed = jobs.filter((j) => j.status === 'completed');
    return [
      { label: 'Urgent', value: completed.filter((j) => j.priority === 'urgent').reduce((s, j) => s + j.revenue, 0), color: '#ff6b6b' },
      { label: 'High', value: completed.filter((j) => j.priority === 'high').reduce((s, j) => s + j.revenue, 0), color: '#fdcb6e' },
      { label: 'Medium', value: completed.filter((j) => j.priority === 'medium').reduce((s, j) => s + j.revenue, 0), color: '#74b9ff' },
      { label: 'Low', value: completed.filter((j) => j.priority === 'low').reduce((s, j) => s + j.revenue, 0), color: '#94a3b8' },
    ];
  }, [jobs]);

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: isDark ? '#0f0f1a' : '#f8fafc' }]}>
      <ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>
        {/* Header */}
        <Animated.View entering={FadeIn.duration(400)} style={styles.header}>
          <Text style={{ fontSize: 26, fontWeight: '800', color: textColor }}>Analytics</Text>
          <View style={styles.periodToggle}>
            {(['week', 'month', 'year'] as const).map((p) => (
              <TouchableOpacity
                key={p}
                style={[
                  styles.periodChip,
                  {
                    backgroundColor: period === p ? '#6C5CE7' : isDark ? '#252542' : '#f1f5f9',
                  },
                ]}
                onPress={() => setPeriod(p)}
              >
                <Text
                  style={{
                    fontSize: 12,
                    fontWeight: '600',
                    color: period === p ? '#fff' : mutedColor,
                    textTransform: 'capitalize',
                  }}
                >
                  {p}
                </Text>
              </TouchableOpacity>
            ))}
          </View>
        </Animated.View>

        {/* KPI Cards */}
        <Animated.View entering={FadeInDown.delay(100).duration(500)} style={styles.kpiGrid}>
          <View style={[styles.kpiCard, { backgroundColor: cardBg }]}>
            <FontAwesome5 name="dollar-sign" size={20} color="#00b894" />
            <Text style={{ fontSize: 22, fontWeight: '800', color: textColor, marginTop: 8 }}>
              {formatCurrency(stats.totalRevenue)}
            </Text>
            <Text style={{ fontSize: 11, color: mutedColor, fontWeight: '500' }}>Revenue</Text>
          </View>
          <View style={[styles.kpiCard, { backgroundColor: cardBg }]}>
            <FontAwesome5 name="briefcase" size={20} color="#6C5CE7" />
            <Text style={{ fontSize: 22, fontWeight: '800', color: textColor, marginTop: 8 }}>
              {stats.completedJobs}/{stats.totalJobs}
            </Text>
            <Text style={{ fontSize: 11, color: mutedColor, fontWeight: '500' }}>Jobs</Text>
          </View>
          <View style={[styles.kpiCard, { backgroundColor: cardBg }]}>
            <FontAwesome5 name="clock" size={20} color="#fdcb6e" />
            <Text style={{ fontSize: 22, fontWeight: '800', color: textColor, marginTop: 8 }}>
              {Math.floor(stats.totalHours / 3600)}h
            </Text>
            <Text style={{ fontSize: 11, color: mutedColor, fontWeight: '500' }}>Hours</Text>
          </View>
          <View style={[styles.kpiCard, { backgroundColor: cardBg }]}>
            <FontAwesome5 name="chart-line" size={20} color="#ff6b6b" />
            <Text style={{ fontSize: 22, fontWeight: '800', color: textColor, marginTop: 8 }}>
              {stats.profitMargin.toFixed(0)}%
            </Text>
            <Text style={{ fontSize: 11, color: mutedColor, fontWeight: '500' }}>Profit Margin</Text>
          </View>
        </Animated.View>

        {/* Revenue Chart */}
        <Animated.View entering={FadeInDown.delay(200).duration(500)}>
          <View style={[styles.sectionCard, { backgroundColor: cardBg }]}>
            <Text style={{ fontSize: 16, fontWeight: '700', color: textColor, marginBottom: 16 }}>
              Revenue This Week
            </Text>
            <BarChart data={revenueByDay} height={200} />
          </View>
        </Animated.View>

        {/* Job Status Donut */}
        <Animated.View entering={FadeInDown.delay(300).duration(500)}>
          <View style={[styles.sectionCard, { backgroundColor: cardBg }]}>
            <Text style={{ fontSize: 16, fontWeight: '700', color: textColor, marginBottom: 16 }}>
              Job Status Distribution
            </Text>
            <DonutChart data={jobsByStatus} size={140} />
          </View>
        </Animated.View>

        {/* Completion Rate */}
        <Animated.View entering={FadeInDown.delay(400).duration(500)}>
          <View style={[styles.sectionCard, { backgroundColor: cardBg }]}>
            <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
              <Text style={{ fontSize: 16, fontWeight: '700', color: textColor }}>Completion Rate</Text>
              <Text style={{ fontSize: 20, fontWeight: '800', color: '#6C5CE7' }}>
                {stats.completionRate.toFixed(0)}%
              </Text>
            </View>
            <View
              style={{
                width: '100%',
                height: 12,
                borderRadius: 6,
                backgroundColor: isDark ? '#252542' : '#f1f5f9',
                overflow: 'hidden',
              }}
            >
              <View
                style={{
                  width: `${stats.completionRate}%`,
                  height: '100%',
                  borderRadius: 6,
                  backgroundColor: '#6C5CE7',
                }}
              />
            </View>
            <Text style={{ fontSize: 12, color: mutedColor, marginTop: 6 }}>
              {stats.completedJobs} of {stats.totalJobs} jobs completed
            </Text>
          </View>
        </Animated.View>

        {/* Revenue by Priority */}
        <Animated.View entering={FadeInDown.delay(500).duration(500)}>
          <View style={[styles.sectionCard, { backgroundColor: cardBg }]}>
            <Text style={{ fontSize: 16, fontWeight: '700', color: textColor, marginBottom: 16 }}>
              Revenue by Priority
            </Text>
            {revenueByPriority.map((item) => (
              <View key={item.label} style={styles.priorityRow}>
                <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8, flex: 1 }}>
                  <View
                    style={{
                      width: 10,
                      height: 10,
                      borderRadius: 5,
                      backgroundColor: item.color,
                    }}
                  />
                  <Text style={{ fontSize: 14, fontWeight: '500', color: textColor }}>{item.label}</Text>
                </View>
                <Text style={{ fontSize: 14, fontWeight: '700', color: textColor }}>
                  {formatCurrency(item.value)}
                </Text>
              </View>
            ))}
          </View>
        </Animated.View>

        {/* Performance Metrics */}
        <Animated.View entering={FadeInDown.delay(600).duration(500)}>
          <View style={[styles.sectionCard, { backgroundColor: cardBg }]}>
            <Text style={{ fontSize: 16, fontWeight: '700', color: textColor, marginBottom: 16 }}>
              Performance Metrics
            </Text>
            <View style={styles.metricItem}>
              <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8 }}>
                <MaterialIcons name="trending-up" size={20} color="#00b894" />
                <Text style={{ fontSize: 14, color: textColor }}>Average Revenue per Job</Text>
              </View>
              <Text style={{ fontSize: 16, fontWeight: '700', color: '#00b894' }}>
                {formatCurrency(stats.avgRevenue)}
              </Text>
            </View>
            <View style={styles.metricItem}>
              <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8 }}>
                <MaterialIcons name="attach-money" size={20} color="#6C5CE7" />
                <Text style={{ fontSize: 14, color: textColor }}>Total Cost</Text>
              </View>
              <Text style={{ fontSize: 16, fontWeight: '700', color: '#6C5CE7' }}>
                {formatCurrency(stats.totalCost)}
              </Text>
            </View>
            <View style={styles.metricItem}>
              <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8 }}>
                <MaterialIcons name="show-chart" size={20} color="#fdcb6e" />
                <Text style={{ fontSize: 14, color: textColor }}>Net Profit</Text>
              </View>
              <Text
                style={{
                  fontSize: 16,
                  fontWeight: '700',
                  color: stats.totalRevenue - stats.totalCost >= 0 ? '#00b894' : '#ff6b6b',
                }}
              >
                {formatCurrency(stats.totalRevenue - stats.totalCost)}
              </Text>
            </View>
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
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 20,
  },
  periodToggle: { flexDirection: 'row', gap: 6 },
  periodChip: { paddingHorizontal: 14, paddingVertical: 8, borderRadius: 20 },
  kpiGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 12,
    marginBottom: 20,
  },
  kpiCard: {
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
  sectionCard: {
    borderRadius: 16,
    padding: 16,
    marginBottom: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 8,
    elevation: 3,
  },
  priorityRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 10,
    borderBottomWidth: 1,
    borderBottomColor: '#2d2d50',
  },
  metricItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 10,
  },
});
