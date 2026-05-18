import { Tabs } from 'expo-router';
import React from 'react';
import { useColorScheme } from 'react-native';
import { colors } from '@/constants/Colors';
import Animated, { useSharedValue, useAnimatedStyle, withSpring } from 'react-native-reanimated';
import { Ionicons } from '@expo/vector-icons';

const iconMap: Record<string, keyof typeof Ionicons.glyphMap> = {
  index: 'home',
  jobs: 'briefcase',
  clients: 'people',
  history: 'time',
  analytics: 'bar-chart',
  settings: 'settings',
};

function TabIcon({ name, color, size, focused }: { name: string; color: string; size: number; focused: boolean }) {
  const scale = useSharedValue(focused ? 1.15 : 1);

  React.useEffect(() => {
    scale.value = withSpring(focused ? 1.15 : 1, { stiffness: 300, damping: 20 });
  }, [focused, scale]);

  const animatedStyle = useAnimatedStyle(() => ({
    transform: [{ scale: scale.value }],
  }));

  const iconName = iconMap[name] || 'circle';

  return (
    <Animated.View style={animatedStyle}>
      <Ionicons name={iconName} size={size} color={color} />
    </Animated.View>
  );
}

export default function TabLayout() {
  const colorScheme = useColorScheme();
  const currentColors = colors[colorScheme ?? 'light'];

  return (
    <Tabs
      screenOptions={{
        tabBarActiveTintColor: currentColors.tabIconSelected,
        tabBarInactiveTintColor: currentColors.tabIconDefault,
        tabBarStyle: {
          backgroundColor: colorScheme === 'dark' ? '#1a1a2e' : '#ffffff',
          borderTopColor: currentColors.border,
          borderTopWidth: 1,
          height: 60,
          paddingTop: 4,
          paddingBottom: 8,
        },
        tabBarLabelStyle: {
          fontSize: 11,
          fontWeight: '600',
        },
        headerStyle: {
          backgroundColor: colorScheme === 'dark' ? '#1a1a2e' : '#ffffff',
          borderBottomColor: currentColors.border,
          borderBottomWidth: 1,
        },
        headerTitleStyle: {
          fontWeight: '700',
          fontSize: 20,
          color: currentColors.text,
        },
        headerShadowVisible: false,
      }}
    >
      <Tabs.Screen
        name="index"
        options={{
          title: 'Dashboard',
          tabBarIcon: ({ color, size, focused }) => (
            <TabIcon name="index" color={color} size={size} focused={focused} />
          ),
        }}
      />
      <Tabs.Screen
        name="jobs"
        options={{
          title: 'Jobs',
          tabBarIcon: ({ color, size, focused }) => (
            <TabIcon name="jobs" color={color} size={size} focused={focused} />
          ),
        }}
      />
      <Tabs.Screen
        name="clients"
        options={{
          title: 'Clients',
          tabBarIcon: ({ color, size, focused }) => (
            <TabIcon name="clients" color={color} size={size} focused={focused} />
          ),
        }}
      />
      <Tabs.Screen
        name="history"
        options={{
          title: 'History',
          tabBarIcon: ({ color, size, focused }) => (
            <TabIcon name="history" color={color} size={size} focused={focused} />
          ),
        }}
      />
      <Tabs.Screen
        name="analytics"
        options={{
          title: 'Analytics',
          tabBarIcon: ({ color, size, focused }) => (
            <TabIcon name="analytics" color={color} size={size} focused={focused} />
          ),
        }}
      />
      <Tabs.Screen
        name="settings"
        options={{
          title: 'Settings',
          tabBarIcon: ({ color, size, focused }) => (
            <TabIcon name="settings" color={color} size={size} focused={focused} />
          ),
        }}
      />
    </Tabs>
  );
}
