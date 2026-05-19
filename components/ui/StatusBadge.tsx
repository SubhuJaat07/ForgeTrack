import { View, Text, StyleSheet } from 'react-native';
import { getStatusColor, getPriorityColor } from '@/utils/theme';

interface StatusBadgeProps {
  label: string;
  type: 'status' | 'priority';
}

export function StatusBadge({ label, type }: StatusBadgeProps) {
  const color = type === 'status' ? getStatusColor(label) : getPriorityColor(label);

  return (
    <View style={[styles.badge, { backgroundColor: `${color}20` }]}>
      <View style={[styles.dot, { backgroundColor: color }]} />
      <Text style={[styles.text, { color }]}>{label.toUpperCase()}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  badge: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 20,
    gap: 6,
  },
  dot: {
    width: 6,
    height: 6,
    borderRadius: 3,
  },
  text: {
    fontSize: 11,
    fontWeight: '700',
    letterSpacing: 0.5,
  },
});
