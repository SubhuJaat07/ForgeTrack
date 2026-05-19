import React from 'react';
import { View, useColorScheme, StyleSheet } from 'react-native';
import Animated, { useSharedValue, useAnimatedStyle, withRepeat, withTiming } from 'react-native-reanimated';

interface SkeletonProps {
  width?: number | string;
  height?: number;
  borderRadius?: number;
  style?: any;
}

export function Skeleton({ width = '100%', height = 20, borderRadius = 8, style }: SkeletonProps) {
  const colorScheme = useColorScheme();
  const offset = useSharedValue(0);
  const baseColor = colorScheme === 'dark' ? '#252542' : '#e2e8f0';
  const highlightColor = colorScheme === 'dark' ? '#353560' : '#f1f5f9';

  React.useEffect(() => {
    offset.value = withRepeat(
      withTiming(1, { duration: 1000 }),
      -1,
      true
    );
  }, [offset]);

  const animatedStyle = useAnimatedStyle(() => {
    return {
      opacity: 0.3 + offset.value * 0.3,
      backgroundColor: offset.value > 0.5 ? highlightColor : baseColor,
    };
  });

  return (
    <Animated.View
      style={[
        {
          width,
          height,
          borderRadius,
          overflow: 'hidden',
        },
        animatedStyle,
        style,
      ]}
    />
  );
}

export function CardSkeleton() {
  const colorScheme = useColorScheme();
  return (
    <View
      style={[
        styles.cardSkeleton,
        {
          backgroundColor: colorScheme === 'dark' ? '#1a1a2e' : '#ffffff',
          borderRadius: 16,
          padding: 16,
        },
      ]}
    >
      <Skeleton width="60%" height={16} borderRadius={4} />
      <View style={{ height: 8 }} />
      <Skeleton width="100%" height={12} borderRadius={4} />
      <View style={{ height: 8 }} />
      <Skeleton width="80%" height={12} borderRadius={4} />
    </View>
  );
}

const styles = StyleSheet.create({
  cardSkeleton: {
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 8,
    elevation: 3,
  },
});
