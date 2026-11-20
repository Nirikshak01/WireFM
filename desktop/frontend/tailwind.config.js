/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          bg: '#0D0D11',
          surface: '#16171D',
          sidebar: '#121318',
          card: '#1A1B22',
          folder: '#CCD8DA',
          folderHover: '#DCE6E8',
          folderDark: '#1C2226',
          blue: '#2F8BF0',
          blueHover: '#4099FB',
          pill: '#FFFFFF',
          pillContent: '#000000',
          border: '#23252E',
          textPrimary: '#FFFFFF',
          textSecondary: '#8F909D',
          textTertiary: '#5C5D6B',
          accentGreen: '#10B981',
          accentRed: '#EF4444',
        }
      },
      borderRadius: {
        'ios': '16px',
        'ios-lg': '22px',
        'ios-pill': '9999px',
      }
    },
  },
  plugins: [],
}
