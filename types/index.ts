export type JobStatus = 'scheduled' | 'in-progress' | 'completed' | 'cancelled';
export type JobPriority = 'low' | 'medium' | 'high' | 'urgent';
export type PhotoType = 'before' | 'after' | 'progress';

export interface Client {
  id: string;
  name: string;
  email: string;
  phone: string;
  company: string;
  address: string;
  notes: string;
  createdAt: string;
  updatedAt: string;
}

export interface JobPhoto {
  id: string;
  uri: string;
  type: PhotoType;
  annotation: string;
  timestamp: string;
}

export interface VoiceNote {
  id: string;
  uri: string;
  transcription: string;
  duration: number;
  timestamp: string;
}

export interface JobLocation {
  latitude: number;
  longitude: number;
  address: string;
  checkInTime?: string;
  checkOutTime?: string;
}

export interface Job {
  id: string;
  title: string;
  description: string;
  clientId: string;
  clientName: string;
  status: JobStatus;
  priority: JobPriority;
  scheduledDate: string;
  scheduledTime: string;
  location: JobLocation;
  photos: JobPhoto[];
  voiceNotes: VoiceNote[];
  notes: string[];
  signatureUri?: string;
  signatureData?: string;
  timerStart?: string;
  timerEnd?: string;
  totalDuration?: number;
  createdAt: string;
  updatedAt: string;
  completedAt?: string;
  revenue: number;
  cost: number;
  tags: string[];
}

export interface WeeklyStats {
  totalJobs: number;
  completedJobs: number;
  totalRevenue: number;
  totalHours: number;
  avgRating: number;
}

export interface DailyJobSummary {
  date: string;
  jobs: number;
  completed: number;
  revenue: number;
}

export type ThemeMode = 'light' | 'dark' | 'system';
export type NotificationSetting = 'all' | 'important' | 'none';
