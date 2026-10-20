import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

const configDir = process.env.FACTUCORE_WEB_CONFIG || false;

export default defineConfig({
  plugins: [react()],
  publicDir: configDir,
  server: { host: "0.0.0.0", port: 3000, strictPort: true },
});
