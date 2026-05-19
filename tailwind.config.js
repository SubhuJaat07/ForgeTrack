/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './app/**/*.{js,jsx,ts,tsx}',
    './components/**/*.{js,jsx,ts,tsx}',
    './hooks/**/*.{js,jsx,ts,tsx}',
    './stores/**/*.{js,jsx,ts,tsx}',
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#f0eeff',
          100: '#dcd8ff',
          200: '#b9b0ff',
          300: '#9b8aff',
          400: '#7c63ff',
          500: '#6C5CE7',
          600: '#5a42db',
          700: '#4d32c7',
          800: '#3f28a3',
          900: '#352384',
        },
        dark: {
          bg: '#0f0f1a',
          card: '#1a1a2e',
          surface: '#252542',
          border: '#2d2d50',
          text: '#e2e8f0',
          muted: '#94a3b8',
        },
        light: {
          bg: '#f8fafc',
          card: '#ffffff',
          surface: '#f1f5f9',
          border: '#e2e8f0',
          text: '#1e293b',
          muted: '#64748b',
        },
        success: '#00b894',
        warning: '#fdcb6e',
        danger: '#ff6b6b',
        info: '#74b9ff',
      },
      fontFamily: {
        sans: ['Inter', 'System', 'sans-serif'],
        mono: ['JetBrains Mono', 'monospace'],
      },
    },
  },
  plugins: [],
};
