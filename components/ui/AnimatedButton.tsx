import React from 'react';
import { Pressable, StyleSheet, Text, useColorScheme } from 'react-native';
import Animated, {
  useSharedValue,
  useAnimatedStyle,
  withSpring,
  withTiming,
  interpolateColor,
} from 'react-native-reanimated';

interface AnimatedButtonProps {
  title: string;
  onPress: () => void;
  variant?: 'primary' | 'secondary' | 'danger' | 'success';
  size?: 'sm' | 'md' | 'lg';
  disabled?: boolean;
  style?: any;
}

const AnimatedPressable = Animated.createAnimatedComponent(Pressable);

export function AnimatedButton({
  title,
  onPress,
  variant = 'primary',
  size = 'md',
  disabled = false,
  style,
}: AnimatedButtonProps) {
  const scale = useSharedValue(1);
  const opacity = useSharedValue(1);
  const colorScheme = useColorScheme();

  const variantColors: Record<string, { bg: string; text: string }> = {
    primary: { bg: '#6C5CE7', text: '#ffffff' },
    secondary: { bg: colorScheme === 'dark' ? '#252542' : '#e2e8f0', text: colorScheme === 'dark' ? '#e2e8f0' : '#1e293b' },
    danger: { bg: '#ff6b6b', text: '#ffffff' },
    success: { bg: '#00b894', text: '#ffffff' },
  };

  const sizeStyles: Record<string, any> = {
    sm: { paddingVertical: 8, paddingHorizontal: 16 },
    md: { paddingVertical: 12, paddingHorizontal: 24 },
    lg: { paddingVertical: 16, paddingHorizontal: 32 },
  };

  const animatedStyle = useAnimatedStyle(() => ({
    transform: [{ scale: scale.value }],
    opacity: opacity.value,
  }));

  const handlePressIn = () => {
    scale.value = withSpring(0.95, { stiffness: 400, damping: 17 });
  };

  const handlePressOut = () => {
    scale.value = withSpring(1, { stiffness: 400, damping: 17 });
  };

  return (
    <AnimatedPressable
      style={[
        {
          backgroundColor: variantColors[variant].bg,
          borderRadius: 12,
          alignItems: 'center',
          justifyContent: 'center',
          flexDirection: 'row',
          ...sizeStyles[size],
        },
        animatedStyle,
        style,
      ]}
      onPressIn={handlePressIn}
      onPressOut={handlePressOut}
      onPress={onPress}
      disabled={disabled}
    >
      <Text
        style={{
          color: variantColors[variant].text,
          fontWeight: '600',
          fontSize: size === 'sm' ? 14 : size === 'lg' ? 18 : 16,
        }}
      >
        {title}
      </Text>
    </AnimatedPressable>
  );
}
