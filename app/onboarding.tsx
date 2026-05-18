import React, { useState, useRef } from 'react';
import {
  View,
  Text,
  StyleSheet,
  Dimensions,
  SafeAreaView,
  TouchableOpacity,
  useColorScheme,
} from 'react-native';
import { router } from 'expo-router';
import Animated, {
  useSharedValue,
  useAnimatedStyle,
  withSpring,
  withTiming,
  interpolate,
  Extrapolation,
} from 'react-native-reanimated';
import { useSettingsStore } from '@/stores/useSettingsStore';

const { width } = Dimensions.get('window');

const slides = [
  {
    id: 1,
    title: 'Manage Jobs On The Go',
    description:
      'Track all your field service jobs from scheduling to completion. Never miss a deadline with smart reminders and real-time GPS tracking.',
    icon: '🔧',
    gradient: ['#6C5CE7', '#a29bfe'],
  },
  {
    id: 2,
    title: 'Capture Everything',
    description:
      'Take before/after photos with your camera, add voice notes, capture client signatures, and generate professional PDF reports instantly.',
    icon: '📸',
    gradient: ['#00b894', '#55efc4'],
  },
  {
    id: 3,
    title: 'Grow Your Business',
    description:
      'View detailed analytics, track revenue, manage clients, and optimize your workflow with powerful insights and reporting tools.',
    icon: '📈',
    gradient: ['#fdcb6e', '#ffeaa7'],
  },
];

export default function OnboardingScreen() {
  const [currentIndex, setCurrentIndex] = useState(0);
  const colorScheme = useColorScheme();
  const { setOnboarded, setUserName, setUserCompany, setUserRole, userName, userCompany } =
    useSettingsStore();

  const translateX = useSharedValue(0);
  const dotScale = useSharedValue(1);

  const slideAnimatedStyle = useAnimatedStyle(() => ({
    transform: [{ translateX: translateX.value }],
  }));

  const goToNext = () => {
    if (currentIndex < slides.length - 1) {
      const newIndex = currentIndex + 1;
      setCurrentIndex(newIndex);
      translateX.value = withSpring(-newIndex * width, { damping: 20, stiffness: 90 });
      dotScale.value = withSpring(1.3, {}, () => {
        dotScale.value = withSpring(1);
      });
    }
  };

  const goToPrevious = () => {
    if (currentIndex > 0) {
      const newIndex = currentIndex - 1;
      setCurrentIndex(newIndex);
      translateX.value = withSpring(-newIndex * width, { damping: 20, stiffness: 90 });
    }
  };

  const skip = () => {
    setOnboarded(true);
    router.replace('/(tabs)');
  };

  const getStarted = () => {
    setOnboarded(true);
    router.replace('/(tabs)');
  };

  const isDark = colorScheme === 'dark';
  const currentSlide = slides[currentIndex];

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: isDark ? '#0f0f1a' : '#f8fafc' }]}>
      {/* Skip Button */}
      <View style={styles.skipContainer}>
        <TouchableOpacity onPress={skip}>
          <Text style={{ color: isDark ? '#94a3b8' : '#64748b', fontSize: 14, fontWeight: '600' }}>
            Skip
          </Text>
        </TouchableOpacity>
      </View>

      {/* Slides */}
      <View style={styles.slidesContainer}>
        <Animated.View style={[styles.slidesWrapper, slideAnimatedStyle]}>
          {slides.map((slide, index) => (
            <View key={slide.id} style={[styles.slide, { width }]}>
              <View
                style={[
                  styles.iconContainer,
                  {
                    backgroundColor: `${slide.gradient[0]}20`,
                    borderColor: `${slide.gradient[0]}40`,
                  },
                ]}
              >
                <Text style={{ fontSize: 60 }}>{slide.icon}</Text>
              </View>
              <Text
                style={[
                  styles.slideTitle,
                  { color: isDark ? '#e2e8f0' : '#1e293b' },
                ]}
              >
                {slide.title}
              </Text>
              <Text
                style={[
                  styles.slideDescription,
                  { color: isDark ? '#94a3b8' : '#64748b' },
                ]}
              >
                {slide.description}
              </Text>
            </View>
          ))}
        </Animated.View>
      </View>

      {/* Dots */}
      <View style={styles.dotsContainer}>
        {slides.map((_, index) => {
          const isActive = index === currentIndex;
          return (
            <Animated.View
              key={index}
              style={[
                styles.dot,
                {
                  backgroundColor: isActive ? currentSlide.gradient[0] : isDark ? '#2d2d50' : '#cbd5e1',
                  width: isActive ? 24 : 8,
                },
              ]}
            />
          );
        })}
      </View>

      {/* Navigation */}
      <View style={styles.navigationContainer}>
        {currentIndex > 0 ? (
          <TouchableOpacity
            style={[styles.navButton, styles.backButton]}
            onPress={goToPrevious}
          >
            <Text style={{ color: isDark ? '#e2e8f0' : '#1e293b', fontWeight: '600', fontSize: 16 }}>
              Back
            </Text>
          </TouchableOpacity>
        ) : (
          <View style={{ width: 80 }} />
        )}

        {currentIndex === slides.length - 1 ? (
          <TouchableOpacity
            style={[
              styles.navButton,
              styles.nextButton,
              { backgroundColor: currentSlide.gradient[0] },
            ]}
            onPress={getStarted}
          >
            <Text style={{ color: '#fff', fontWeight: '700', fontSize: 16 }}>Get Started</Text>
          </TouchableOpacity>
        ) : (
          <TouchableOpacity
            style={[
              styles.navButton,
              styles.nextButton,
              { backgroundColor: currentSlide.gradient[0] },
            ]}
            onPress={goToNext}
          >
            <Text style={{ color: '#fff', fontWeight: '700', fontSize: 16 }}>Next</Text>
          </TouchableOpacity>
        )}
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  skipContainer: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    paddingHorizontal: 24,
    paddingTop: 16,
  },
  slidesContainer: {
    flex: 1,
    overflow: 'hidden',
  },
  slidesWrapper: {
    flexDirection: 'row',
  },
  slide: {
    alignItems: 'center',
    paddingHorizontal: 32,
    justifyContent: 'center',
  },
  iconContainer: {
    width: 160,
    height: 160,
    borderRadius: 80,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 2,
    marginBottom: 40,
  },
  slideTitle: {
    fontSize: 28,
    fontWeight: '800',
    textAlign: 'center',
    marginBottom: 16,
  },
  slideDescription: {
    fontSize: 16,
    textAlign: 'center',
    lineHeight: 24,
    maxWidth: 320,
  },
  dotsContainer: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    gap: 8,
    marginBottom: 32,
  },
  dot: {
    height: 8,
    borderRadius: 4,
  },
  navigationContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 24,
    marginBottom: 40,
  },
  navButton: {
    paddingVertical: 14,
    paddingHorizontal: 32,
    borderRadius: 14,
  },
  backButton: {
    backgroundColor: 'transparent',
  },
  nextButton: {
    minWidth: 120,
    alignItems: 'center',
  },
});
