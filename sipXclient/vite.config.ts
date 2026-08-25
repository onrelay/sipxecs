import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

declare const process: { env: Record<string, string | undefined> };

export default defineConfig({
  plugins: [react()],
  base: './',
  build: {
    outDir: process.env.SIPX_MAKE_BUILD
      ? '../build/sipXclient/dist'
      : 'build',
    emptyOutDir: true,
  },
});
