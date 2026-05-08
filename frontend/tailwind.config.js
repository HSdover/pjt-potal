/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{vue,ts}"],
  theme: {
    extend: {
      colors: {
        brand: {
          ink: "#17202a",
          teal: "#0f766e",
          blue: "#2563eb",
        },
      },
    },
  },
  plugins: [],
};
