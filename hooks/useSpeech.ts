import { useState, useCallback } from 'react';
import * as Speech from 'expo-speech';

interface SpeechState {
  isListening: boolean;
  transcription: string;
  error: string | null;
}

export function useSpeech() {
  const [state, setState] = useState<SpeechState>({
    isListening: false,
    transcription: '',
    error: null,
  });

  const speak = useCallback((text: string) => {
    Speech.speak(text, {
      language: 'en-US',
      pitch: 1,
      rate: 1,
    });
  }, []);

  const stop = useCallback(() => {
    Speech.stop();
    setState((s) => ({ ...s, isListening: false }));
  }, []);

  return {
    ...state,
    speak,
    stop,
    setState,
  };
}
