import { useEffect, useRef, useCallback } from 'react';
import { useTimerStore } from '@/stores/useTimerStore';

export function useTimer() {
  const { isRunning, elapsedSeconds, activeJobId, startTimer, stopTimer, resetTimer, tick } =
    useTimerStore();
  const intervalRef = useRef<NodeJS.Timeout | null>(null);

  useEffect(() => {
    if (isRunning) {
      intervalRef.current = setInterval(tick, 1000);
    } else if (intervalRef.current) {
      clearInterval(intervalRef.current);
      intervalRef.current = null;
    }
    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current);
    };
  }, [isRunning, tick]);

  const start = useCallback(
    (jobId: string) => startTimer(jobId),
    [startTimer]
  );

  const stop = useCallback(() => stopTimer(), [stopTimer]);

  const reset = useCallback(() => resetTimer(), [resetTimer]);

  return {
    isRunning,
    elapsedSeconds,
    activeJobId,
    start,
    stop,
    reset,
  };
}
