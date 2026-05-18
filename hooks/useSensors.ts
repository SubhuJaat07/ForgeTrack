import { useState, useEffect } from 'react';
import { Accelerometer } from 'expo-sensors';

interface SensorData {
  x: number;
  y: number;
  z: number;
}

export function useSensors() {
  const [accelerometerData, setAccelerometerData] = useState<SensorData>({
    x: 0,
    y: 0,
    z: 0,
  });
  const [isShaking, setIsShaking] = useState(false);

  useEffect(() => {
    const subscription = Accelerometer.addListener((data) => {
      setAccelerometerData(data);
      const totalForce = Math.sqrt(data.x ** 2 + data.y ** 2 + data.z ** 2);
      if (totalForce > 2.5) {
        setIsShaking(true);
        setTimeout(() => setIsShaking(false), 500);
      }
    });

    Accelerometer.setUpdateInterval(500);
    return () => subscription.remove();
  }, []);

  return { accelerometerData, isShaking };
}
