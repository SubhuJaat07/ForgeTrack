# ForgeTrack - Professional Field Service & Job Tracker

![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20iOS-blue)
![Expo](https://img.shields.io/badge/Expo-SDK%2054-000)
![TypeScript](https://img.shields.io/badge/TypeScript-5.9-blue)

A production-ready, premium quality field service management app built for contractors and technicians.

## Features

- **Dashboard** - Today's jobs, weekly summary, progress charts, active timer
- **Job Management** - Create, track, and manage field service jobs
- **Live Job Tracking** - Start/stop timer, GPS check-in/check-out
- **Photo Capture** - Before/after photos with camera integration
- **Voice Notes** - Record and auto-transcribe voice memos
- **Client Signatures** - Capture client signatures on device
- **PDF Reports** - Auto-generate professional PDF job reports
- **Client Management** - Full CRM for your clients
- **Analytics** - Revenue, hours, completion rates, charts
- **History** - Past jobs with map view
- **Dark/Light Mode** - Automatic theme switching
- **Offline-First** - Works without internet connection
- **Smart Notifications** - Job reminders and updates

## Tech Stack

- **Framework**: React Native + Expo SDK 54
- **Language**: TypeScript
- **Navigation**: Expo Router
- **State Management**: Zustand
- **Styling**: NativeWind (Tailwind CSS)
- **Animations**: React Native Reanimated
- **Maps**: React Native Maps
- **Storage**: Expo FileSystem
- **Camera**: Expo Camera + Image Picker
- **Location**: Expo Location + Geofencing
- **PDF**: Expo Print
- **Speech**: Expo Speech
- **Notifications**: Expo Notifications
- **Sensors**: Expo Sensors (Accelerometer)

## Getting Started

```bash
# Clone the repository
git clone https://github.com/SubhuJaat07/ForgeTrack.git
cd ForgeTrack

# Install dependencies
npm install

# Start development server
npx expo start

# Run on Android
npx expo start --android

# Run on iOS
npx expo start --ios
```

## Build

```bash
# Build for Android (APK)
eas build --platform android --profile preview

# Build for Android (AAB - Play Store)
eas build --platform android --profile production
```

## Project Structure

```
ForgeTrack/
├── app/                    # Expo Router screens
│   ├── (tabs)/            # Tab navigation screens
│   ├── job/               # Job detail & create screens
│   ├── _layout.tsx        # Root layout
│   └── onboarding.tsx     # Onboarding flow
├── components/            # Reusable components
│   ├── ui/               # UI components (buttons, cards, etc.)
│   ├── Charts.tsx        # SVG charts
│   └── SignaturePad.tsx  # Signature capture
├── stores/               # Zustand state stores
├── hooks/                # Custom hooks
├── utils/                # Utility functions
├── types/                # TypeScript type definitions
└── constants/            # App constants
```

## License

MIT
