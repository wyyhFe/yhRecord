/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './index.html',
    './src/**/*.{vue,js,ts,jsx,tsx}'
  ],
  theme: {
    extend: {
      colors: {
        ink: '#1e293b',
        paper: '#f6efe5',
        cream: '#fffaf4',
        sand: '#e8d9c5',
        clay: '#c47c52',
        moss: '#4b6b57',
        blush: '#d87c86',
        gold: '#d7a648'
      },
      boxShadow: {
        card: '0 18rpx 48rpx rgba(67, 41, 26, 0.08)'
      },
      borderRadius: {
        panel: '28rpx'
      }
    }
  },
  corePlugins: {
    preflight: false
  }
}
