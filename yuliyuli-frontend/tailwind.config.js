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
          50: '#fdf2f8',
          100: '#fce7f3',
          200: '#fbcfe8',
          300: '#f9a8d4',
          400: '#f472b6',
          500: '#ec4899',
          600: '#db2777',
          700: '#be185d',
          800: '#9d174d',
          900: '#831843',
          950: '#500724',
        },
        deeppink: {
          DEFAULT: '#ff1493',
          50: '#fff0f7',
          100: '#ffd6eb',
          200: '#ffadde',
          300: '#ff85d1',
          400: '#ff5cb3',
          500: '#ff1493',
          600: '#e6007a',
          700: '#c20064',
          800: '#9e0050',
          900: '#800041',
          950: '#480021',
        },
      },
    },
  },
  plugins: [],
}