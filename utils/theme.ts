import { useColorScheme } from 'react-native';

export function useAppTheme() {
  const colorScheme = useColorScheme();

  return {
    isDark: colorScheme === 'dark',
    colors: {
      bg: colorScheme === 'dark' ? '#0f0f1a' : '#f8fafc',
      card: colorScheme === 'dark' ? '#1a1a2e' : '#ffffff',
      surface: colorScheme === 'dark' ? '#252542' : '#f1f5f9',
      border: colorScheme === 'dark' ? '#2d2d50' : '#e2e8f0',
      text: colorScheme === 'dark' ? '#e2e8f0' : '#1e293b',
      muted: colorScheme === 'dark' ? '#94a3b8' : '#64748b',
      primary: '#6C5CE7',
      primaryLight: '#a29bfe',
      success: '#00b894',
      warning: '#fdcb6e',
      danger: '#ff6b6b',
      info: '#74b9ff',
    },
  };
}

export function formatCurrency(amount: number, currency: string = 'USD'): string {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency,
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
  }).format(amount);
}

export function formatDuration(seconds: number): string {
  const hrs = Math.floor(seconds / 3600);
  const mins = Math.floor((seconds % 3600) / 60);
  const secs = seconds % 60;
  if (hrs > 0) return `${hrs}h ${mins}m`;
  if (mins > 0) return `${mins}m ${secs}s`;
  return `${secs}s`;
}

export function getStatusColor(status: string): string {
  switch (status) {
    case 'completed': return '#00b894';
    case 'in-progress': return '#6C5CE7';
    case 'scheduled': return '#74b9ff';
    case 'cancelled': return '#ff6b6b';
    default: return '#94a3b8';
  }
}

export function getPriorityColor(priority: string): string {
  switch (priority) {
    case 'urgent': return '#ff6b6b';
    case 'high': return '#fdcb6e';
    case 'medium': return '#74b9ff';
    case 'low': return '#94a3b8';
    default: return '#94a3b8';
  }
}
