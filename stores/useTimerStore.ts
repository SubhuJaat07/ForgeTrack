import { create } from 'zustand';

interface TimerStore {
  activeJobId: string | null;
  isRunning: boolean;
  elapsedSeconds: number;
  startTime: string | null;
  startTimer: (jobId: string) => void;
  stopTimer: () => void;
  resetTimer: () => void;
  tick: () => void;
}

export const useTimerStore = create<TimerStore>((set, get) => ({
  activeJobId: null,
  isRunning: false,
  elapsedSeconds: 0,
  startTime: null,

  startTimer: (jobId) =>
    set({
      activeJobId: jobId,
      isRunning: true,
      elapsedSeconds: 0,
      startTime: new Date().toISOString(),
    }),

  stopTimer: () =>
    set((state) => ({
      isRunning: false,
      activeJobId: state.activeJobId,
    })),

  resetTimer: () =>
    set({
      activeJobId: null,
      isRunning: false,
      elapsedSeconds: 0,
      startTime: null,
    }),

  tick: () =>
    set((state) => ({
      elapsedSeconds: state.elapsedSeconds + 1,
    })),
}));
