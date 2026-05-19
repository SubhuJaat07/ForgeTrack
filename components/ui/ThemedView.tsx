import { View, useColorScheme } from 'react-native';
import { colors } from '@/constants/Colors';

type Props = {
  children: React.ReactNode;
  className?: string;
  style?: any;
};

export function ThemedView({ children, className, style }: Props) {
  const colorScheme = useColorScheme();
  return (
    <View
      className={className}
      style={[
        {
          backgroundColor: colors[colorScheme ?? 'light'].background,
        },
        style,
      ]}
    >
      {children}
    </View>
  );
}

export function ThemedCard({ children, className, style }: Props) {
  const colorScheme = useColorScheme();
  return (
    <View
      className={className}
      style={[
        {
          backgroundColor: colors[colorScheme ?? 'light'].card,
          borderRadius: 16,
          padding: 16,
          shadowColor: '#000',
          shadowOffset: { width: 0, height: 2 },
          shadowOpacity: 0.1,
          shadowRadius: 8,
          elevation: 3,
        },
        style,
      ]}
    >
      {children}
    </View>
  );
}
