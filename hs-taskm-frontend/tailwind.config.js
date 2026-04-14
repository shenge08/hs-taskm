/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#2563EB',
          foreground: '#FFFFFF',
        },
        secondary: {
          DEFAULT: '#3B82F6',
          foreground: '#FFFFFF',
        },
        accent: {
          DEFAULT: '#EA580C',
          foreground: '#FFFFFF',
        },
        success: '#22C55E',
        warning: '#F59E0B',
        danger: '#DC2626',
        background: '#F8FAFC',
        foreground: '#1E293B',
        card: '#FFFFFF',
        'card-foreground': '#1E293B',
        muted: '#64748B',
        border: '#E2E8F0',
        input: '#E2E8F0',
        ring: '#2563EB',
      },
      fontFamily: {
        heading: ['Fira Code', 'monospace'],
        body: ['Fira Sans', 'sans-serif'],
      },
    },
  },
  plugins: [],
}
