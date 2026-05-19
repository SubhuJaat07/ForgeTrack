import * as Location from 'expo-location';

export interface GeofenceRegion {
  identifier: string;
  latitude: number;
  longitude: number;
  radius: number;
}

export async function checkGeofenceEntry(
  currentLocation: { latitude: number; longitude: number },
  targetLocation: { latitude: number; longitude: number },
  radiusMeters: number = 100
): Promise<boolean> {
  const distance = getDistanceBetweenPoints(
    currentLocation.latitude,
    currentLocation.longitude,
    targetLocation.latitude,
    targetLocation.longitude
  );
  return distance <= radiusMeters;
}

export function getDistanceBetweenPoints(
  lat1: number,
  lon1: number,
  lat2: number,
  lon2: number
): number {
  const R = 6371e3;
  const phi1 = (lat1 * Math.PI) / 180;
  const phi2 = (lat2 * Math.PI) / 180;
  const deltaPhi = ((lat2 - lat1) * Math.PI) / 180;
  const deltaLambda = ((lon2 - lon1) * Math.PI) / 180;

  const a =
    Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
    Math.cos(phi1) * Math.cos(phi2) * Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

  return R * c;
}

export async function startBackgroundLocationTracking(): Promise<void> {
  const { status } = await Location.requestBackgroundPermissionsAsync();
  if (status !== 'granted') {
    throw new Error('Background location permission denied');
  }

  await Location.startLocationUpdatesAsync('forge-track-bg', {
    accuracy: Location.Accuracy.Balanced,
    distanceInterval: 50,
    deferredUpdatesInterval: 60000,
    showsBackgroundLocationIndicator: true,
    foregroundService: {
      notificationTitle: 'ForgeTrack',
      notificationBody: 'Tracking your location for job check-in',
      notificationColor: '#6C5CE7',
    },
  });
}

export async function stopBackgroundLocationTracking(): Promise<void> {
  await Location.stopLocationUpdatesAsync('forge-track-bg');
}

export async function getCurrentLocation(): Promise<Location.LocationObject | null> {
  try {
    const { status } = await Location.requestForegroundPermissionsAsync();
    if (status !== 'granted') return null;

    const location = await Location.getCurrentPositionAsync({
      accuracy: Location.Accuracy.High,
    });
    return location;
  } catch {
    return null;
  }
}

export async function reverseGeocode(latitude: number, longitude: number): Promise<string> {
  try {
    const addresses = await Location.reverseGeocodeAsync({ latitude, longitude });
    if (addresses.length > 0) {
      const addr = addresses[0];
      return [addr.street, addr.city, addr.region, addr.postalCode, addr.country]
        .filter(Boolean)
        .join(', ');
    }
  } catch {
    // ignore
  }
  return `${latitude.toFixed(6)}, ${longitude.toFixed(6)}`;
}
