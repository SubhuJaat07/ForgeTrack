import React from 'react';
import { View, StyleSheet, useColorScheme } from 'react-native';
import Svg, { Circle, Rect, Line, Text as SvgText, G } from 'react-native-svg';

interface BarChartProps {
  data: { label: string; value: number; color?: string }[];
  height?: number;
}

export function BarChart({ data, height = 200 }: BarChartProps) {
  const colorScheme = useColorScheme();
  const maxValue = Math.max(...data.map((d) => d.value), 1);
  const barWidth = 100 / data.length;
  const barColor = colorScheme === 'dark' ? '#6C5CE7' : '#5a42db';

  return (
    <View style={[styles.chartContainer, { height }]}>
      <Svg width="100%" height="100%">
        {data.map((item, index) => {
          const barHeight = (item.value / maxValue) * (height - 40);
          return (
            <G key={index}>
              <Rect
                x={`${index * barWidth + barWidth * 0.15}%`}
                y={height - 20 - barHeight}
                width={`${barWidth * 0.7}%`}
                height={barHeight}
                rx={4}
                fill={item.color || barColor}
                opacity={0.85}
              />
              <SvgText
                x={`${index * barWidth + barWidth / 2}%`}
                y={height - 4}
                textAnchor="middle"
                fontSize={10}
                fill={colorScheme === 'dark' ? '#94a3b8' : '#64748b'}
              >
                {item.label}
              </SvgText>
              <SvgText
                x={`${index * barWidth + barWidth / 2}%`}
                y={height - 25 - barHeight}
                textAnchor="middle"
                fontSize={10}
                fontWeight="bold"
                fill={item.color || barColor}
              >
                {item.value}
              </SvgText>
            </G>
          );
        })}
      </Svg>
    </View>
  );
}

interface DonutChartProps {
  data: { value: number; color: string; label: string }[];
  size?: number;
}

export function DonutChart({ data, size = 120 }: DonutChartProps) {
  const total = data.reduce((sum, d) => sum + d.value, 0) || 1;
  const strokeWidth = 20;
  const radius = (size - strokeWidth) / 2;
  const circumference = 2 * Math.PI * radius;
  let startAngle = -90;

  return (
    <View style={styles.donutContainer}>
      <Svg width={size} height={size}>
        {data.map((item, index) => {
          const strokeDash = (item.value / total) * circumference;
          const gap = index * 4;
          const rotation = startAngle + (index > 0 ? data.slice(0, index).reduce((s, d) => s + (d.value / total) * 360, 0) : 0);
          return (
            <Circle
              key={index}
              cx={size / 2}
              cy={size / 2}
              r={radius}
              stroke={item.color}
              strokeWidth={strokeWidth}
              strokeDasharray={`${strokeDash - gap} ${circumference - strokeDash + gap}`}
              strokeLinecap="round"
              transform={`rotate(${rotation}, ${size / 2}, ${size / 2})`}
              fill="none"
            />
          );
        })}
      </Svg>
      <View style={styles.donutLegend}>
        {data.map((item, index) => (
          <View key={index} style={styles.legendItem}>
            <View style={[styles.legendDot, { backgroundColor: item.color }]} />
            <View>
              <View style={{ flexDirection: 'row', alignItems: 'center', gap: 4 }}>
                <SvgText fontSize={16} fontWeight="bold" fill={item.color}>
                  {item.value}
                </SvgText>
                <SvgText fontSize={11} fill="#94a3b8">
                  ({Math.round((item.value / total) * 100)}%)
                </SvgText>
              </View>
            </View>
          </View>
        ))}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  chartContainer: {
    width: '100%',
    paddingVertical: 8,
  },
  donutContainer: {
    alignItems: 'center',
  },
  donutLegend: {
    flexDirection: 'column',
    gap: 8,
    marginTop: 16,
  },
  legendItem: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  legendDot: {
    width: 10,
    height: 10,
    borderRadius: 5,
  },
});
