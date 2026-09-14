/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        ledger: {
          paper: '#F6F1E4',
          card: '#FFFDF7',
          ink: '#2A2621',
          faint: '#8A8172',
          rule: '#D9CFB8',
        },
        recall: {
          DEFAULT: '#2F5D50',   // deep archive-green -- "known"
          light: '#DCE9E2',
        },
        forget: {
          DEFAULT: '#9C3B2E',   // library-stamp red -- "forgotten"
          light: '#F3DFDA',
        },
        signal: '#C97B3D',      // due-today amber, like a librarian's date stamp
      },
      fontFamily: {
        display: ['"Source Serif 4"', 'Georgia', 'serif'],
        body: ['"Inter"', 'system-ui', 'sans-serif'],
        mono: ['"IBM Plex Mono"', 'monospace'],
      },
      boxShadow: {
        card: '0 1px 2px rgba(42,38,33,0.06), 0 6px 16px rgba(42,38,33,0.08)',
      },
      borderRadius: {
        card: '10px',
      },
    },
  },
  plugins: [],
};
