import React from 'react';
import { View, StyleSheet } from 'react-native';
import Animated, { useAnimatedStyle, withTiming, withDelay } from 'react-native-reanimated';

interface ProgressBarProps {
  progress: number; // 0 to 1
  color?: string;
  height?: number;
  animated?: boolean;
}

export function ProgressBar({ progress, color = '#6C5CE7', height = 8, animated = true }: ProgressBarProps) {
  const clampedProgress = Math.max(0, Math.min(1, progress));
  const animatedWidth = useAnimatedStyle(() => ({
    width: animated
      ? withTiming(`${clampedProgress * 100}%`, { duration: 800 })
      : `${clampedProgress * 100}%`,
  }));

  return (
    <View style={[styles.container, { height, borderRadius: height / 2, backgroundColor: color + '20' }]}>
      <Animated.View
        style={[
          styles.bar,
          animatedWidth,
          {
            height,
            borderRadius: height / 2,
            backgroundColor: color,
          },
        ]}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    width: '100%',
    overflow: 'hidden',
  },
  bar: {
    maxWidth: '100%',
  },
});
