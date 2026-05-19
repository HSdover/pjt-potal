/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{vue,ts}"],
  theme: {
    extend: {
      colors: {
        brand: {
          ink: "#17202a",
          teal: "#0f766e",
          blue: "#0057b8",
          navy: "#061b4f",
          cyan: "#00a6d6",
        },
      },
    },
  },
  plugins: [],
};
