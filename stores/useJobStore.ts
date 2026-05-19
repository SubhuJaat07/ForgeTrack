import { create } from 'zustand';
import { Job, JobStatus } from '@/types';

interface JobStore {
  jobs: Job[];
  addJob: (job: Job) => void;
  updateJob: (id: string, updates: Partial<Job>) => void;
  deleteJob: (id: string) => void;
  getJobById: (id: string) => Job | undefined;
  getJobsByStatus: (status: JobStatus) => Job[];
  getTodayJobs: () => Job[];
  getWeekJobs: () => Job[];
}

export const useJobStore = create<JobStore>((set, get) => ({
  jobs: [],

  addJob: (job) =>
    set((state) => ({ jobs: [job, ...state.jobs] })),

  updateJob: (id, updates) =>
    set((state) => ({
      jobs: state.jobs.map((job) =>
        job.id === id
          ? { ...job, ...updates, updatedAt: new Date().toISOString() }
          : job
      ),
    })),

  deleteJob: (id) =>
    set((state) => ({
      jobs: state.jobs.filter((job) => job.id !== id),
    })),

  getJobById: (id) => get().jobs.find((job) => job.id === id),

  getJobsByStatus: (status) => get().jobs.filter((job) => job.status === status),

  getTodayJobs: () => {
    const today = new Date().toISOString().split('T')[0];
    return get().jobs.filter(
      (job) => job.scheduledDate.startsWith(today) && job.status !== 'cancelled'
    );
  },

  getWeekJobs: () => {
    const now = new Date();
    const weekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
    return get().jobs.filter(
      (job) => new Date(job.scheduledDate) >= weekAgo && job.status !== 'cancelled'
    );
  },
}));
