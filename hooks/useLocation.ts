import { useState, useEffect, useCallback } from 'react';
import * as Location from 'expo-location';
import { reverseGeocode } from '@/utils/geofencing';

interface LocationState {
  location: Location.LocationObject | null;
  address: string;
  loading: boolean;
  error: string | null;
}

export function useLocation() {
  const [state, setState] = useState<LocationState>({
    location: null,
    address: '',
    loading: false,
    error: null,
  });

  const requestPermission = useCallback(async () => {
    const { status } = await Location.requestForegroundPermissionsAsync();
    return status === 'granted';
  }, []);

  const getCurrentPosition = useCallback(async () => {
    setState((s) => ({ ...s, loading: true, error: null }));
    try {
      const hasPermission = await requestPermission();
      if (!hasPermission) {
        setState((s) => ({ ...s, loading: false, error: 'Location permission denied' }));
        return null;
      }

      const location = await Location.getCurrentPositionAsync({
        accuracy: Location.Accuracy.High,
      });

      const address = await reverseGeocode(
        location.coords.latitude,
        location.coords.longitude
      );

      setState({ location, address, loading: false, error: null });
      return location;
    } catch (error) {
      setState((s) => ({
        ...s,
        loading: false,
        error: error instanceof Error ? error.message : 'Failed to get location',
      }));
      return null;
    }
  }, [requestPermission]);

  useEffect(() => {
    requestPermission();
  }, [requestPermission]);

  return {
    ...state,
    getCurrentPosition,
    requestPermission,
  };
}
