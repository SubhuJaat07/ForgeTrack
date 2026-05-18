import { Text, useColorScheme } from 'react-native';
import { colors } from '@/constants/Colors';

type Props = {
  children: React.ReactNode;
  className?: string;
  style?: any;
  variant?: 'default' | 'heading' | 'subheading' | 'caption' | 'muted';
};

export function ThemedText({ children, className, style, variant = 'default' }: Props) {
  const colorScheme = useColorScheme();
  const color = colors[colorScheme ?? 'light'].text;

  const variantStyles: Record<string, any> = {
    default: { fontSize: 16, color },
    heading: { fontSize: 24, fontWeight: '700', color },
    subheading: { fontSize: 18, fontWeight: '600', color },
    caption: { fontSize: 12, color: colors[colorScheme ?? 'light'].muted },
    muted: { fontSize: 14, color: colors[colorScheme ?? 'light'].muted },
  };

  return (
    <Text className={className} style={[variantStyles[variant], style]}>
      {children}
    </Text>
  );
}
