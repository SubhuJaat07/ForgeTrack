import { create } from 'zustand';
import { ThemeMode, NotificationSetting } from '@/types';

interface SettingsStore {
  theme: ThemeMode;
  notifications: NotificationSetting;
  isDarkMode: boolean;
  isOnboarded: boolean;
  userName: string;
  userCompany: string;
  userRole: string;
  currency: string;
  autoGeneratePdf: boolean;
  setTheme: (theme: ThemeMode) => void;
  setNotifications: (n: NotificationSetting) => void;
  setDarkMode: (isDark: boolean) => void;
  setOnboarded: (v: boolean) => void;
  setUserName: (n: string) => void;
  setUserCompany: (c: string) => void;
  setUserRole: (r: string) => void;
  setCurrency: (c: string) => void;
  setAutoGeneratePdf: (v: boolean) => void;
}

export const useSettingsStore = create<SettingsStore>((set) => ({
  theme: 'system',
  notifications: 'all',
  isDarkMode: false,
  isOnboarded: false,
  userName: '',
  userCompany: '',
  userRole: 'Contractor',
  currency: 'USD',
  autoGeneratePdf: true,

  setTheme: (theme) => set({ theme }),
  setNotifications: (notifications) => set({ notifications }),
  setDarkMode: (isDarkMode) => set({ isDarkMode }),
  setOnboarded: (isOnboarded) => set({ isOnboarded }),
  setUserName: (userName) => set({ userName }),
  setUserCompany: (userCompany) => set({ userCompany }),
  setUserRole: (userRole) => set({ userRole }),
  setCurrency: (currency) => set({ currency }),
  setAutoGeneratePdf: (autoGeneratePdf) => set({ autoGeneratePdf }),
}));
