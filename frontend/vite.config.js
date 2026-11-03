import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'node:path';
// https://vitejs.dev/config/
export default defineConfig({
    plugins: [react()],
    resolve: {
        alias: {
            '@': path.resolve(__dirname, './src'),
        },
    },
    server: {
        port: 5173,
        proxy: {
            // Dev-time proxy so the SPA can call the Spring Boot backend without CORS friction.
            '/api': {
                target: process.env.VITE_API_PROXY_TARGET || 'http://localhost:8080',
                changeOrigin: true,
            },
        },
    },
});
