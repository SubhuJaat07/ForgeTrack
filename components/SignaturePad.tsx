import React, { useRef, useState } from 'react';
import { View, StyleSheet, Modal, Text, Pressable, useColorScheme, PanResponder, Dimensions } from 'react-native';
import Svg, { Path } from 'react-native-svg';

interface SignaturePadProps {
  visible: boolean;
  onConfirm: (signature: string) => void;
  onCancel: () => void;
}

interface Point {
  x: number;
  y: number;
}

export function SignaturePad({ visible, onConfirm, onCancel }: SignaturePadProps) {
  const colorScheme = useColorScheme();
  const [paths, setPaths] = useState<string[]>([]);
  const [currentPath, setCurrentPath] = useState<Point[]>([]);
  const [isEmpty, setIsEmpty] = useState(true);
  const panResponder = useRef(
    PanResponder.create({
      onStartShouldSetPanResponder: () => true,
      onMoveShouldSetPanResponder: () => true,
      onPanResponderGrant: (evt) => {
        const point = { x: evt.nativeEvent.locationX, y: evt.nativeEvent.locationY };
        setCurrentPath([point]);
        setIsEmpty(false);
      },
      onPanResponderMove: (evt) => {
        setCurrentPath((prev) => [...prev, { x: evt.nativeEvent.locationX, y: evt.nativeEvent.locationY }]);
      },
      onPanResponderRelease: () => {
        if (currentPath.length > 1) {
          const pathD = currentPath.map((p, i) => `${i === 0 ? 'M' : 'L'} ${p.x} ${p.y}`).join(' ');
          setPaths((prev) => [...prev, pathD]);
        }
        setCurrentPath([]);
      },
    })
  ).current;

  const handleClear = () => {
    setPaths([]);
    setCurrentPath([]);
    setIsEmpty(true);
  };

  const handleConfirm = () => {
    if (!isEmpty) {
      onConfirm('signature_captured_' + Date.now());
    }
  };

  const buildPathD = (points: Point[]) =>
    points.map((p, i) => `${i === 0 ? 'M' : 'L'} ${p.x} ${p.y}`).join(' ');

  if (!visible) return null;

  const bgColor = colorScheme === 'dark' ? '#1a1a2e' : '#ffffff';
  const textColor = colorScheme === 'dark' ? '#e2e8f0' : '#1e293b';
  const strokeColor = colorScheme === 'dark' ? '#e2e8f0' : '#1e293b';

  return (
    <Modal visible={visible} animationType="slide" transparent>
      <View style={styles.overlay}>
        <View style={[styles.container, { backgroundColor: bgColor }]}>
          <View style={styles.header}>
            <Pressable onPress={onCancel}>
              <Text style={{ color: '#ff6b6b', fontWeight: '600', fontSize: 16 }}>Cancel</Text>
            </Pressable>
            <Text style={{ fontWeight: '700', fontSize: 16, color: textColor }}>Client Signature</Text>
            <Pressable onPress={handleConfirm} disabled={isEmpty}>
              <Text style={{ color: isEmpty ? '#94a3b8' : '#6C5CE7', fontWeight: '600', fontSize: 16 }}>Done</Text>
            </Pressable>
          </View>
          <View style={[styles.canvasContainer, { backgroundColor: colorScheme === 'dark' ? '#252542' : '#f8fafc' }]}>
            <Svg style={StyleSheet.absoluteFill}>
              {paths.map((d, i) => (
                <Path key={i} d={d} stroke={strokeColor} strokeWidth={3} fill="none" strokeLinecap="round" strokeLinejoin="round" />
              ))}
              {currentPath.length > 1 && (
                <Path d={buildPathD(currentPath)} stroke={strokeColor} strokeWidth={3} fill="none" strokeLinecap="round" strokeLinejoin="round" />
              )}
            </Svg>
            <View {...panResponder.panHandlers} style={StyleSheet.absoluteFill} />
            {isEmpty && (
              <Text style={{ color: '#94a3b8', fontSize: 14, position: 'absolute', alignSelf: 'center', top: '45%' }}>
                Sign here
              </Text>
            )}
          </View>
          <Pressable style={styles.clearBtn} onPress={handleClear}>
            <Text style={{ color: '#6C5CE7', fontWeight: '600', fontSize: 14 }}>Clear Signature</Text>
          </Pressable>
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.5)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  container: {
    width: '90%',
    height: '70%',
    borderRadius: 20,
    overflow: 'hidden',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 16,
  },
  canvasContainer: {
    flex: 1,
    margin: 16,
    borderRadius: 12,
    overflow: 'hidden',
  },
  clearBtn: {
    alignItems: 'center',
    paddingVertical: 12,
  },
});
