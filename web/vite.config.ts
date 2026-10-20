import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  const configDir = env.FACTUCORE_WEB_CONFIG || false;

  return {
    plugins: [react()],
    publicDir: configDir,
    server: { host: "0.0.0.0", port: 3000, strictPort: true },
  };
});
