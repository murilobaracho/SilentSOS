// @lovable.dev/vite-tanstack-config already includes the following — do NOT add them manually
// or the app will break with duplicate plugins:
//   - TanStack devtools (dev-only, first), tanstackStart, viteReact, tailwindcss, tsConfigPaths,
//     nitro (build-only using cloudflare as a default target), VITE_* env injection, @ path alias,
//     React/TanStack dedupe, error logger plugins, and sandbox detection (port/host/strictPort).
// You can pass additional config via defineConfig({ vite: { ... }, etc... }) if needed.
import { defineConfig } from "@lovable.dev/vite-tanstack-config";

// GitHub Pages build (run with PAGES_BUILD=1 — see .github/workflows/deploy-pages.yml).
// GitHub Pages has no server runtime and serves the site under /<repo-name>/, so a
// Pages build uses the static preset, a base path and prerenders "/" to index.html.
const pages = process.env["PAGES_BUILD"] === "1";
const repoName = process.env["GITHUB_REPOSITORY"]?.split("/")[1] ?? "FETEC-SilentSOS";
const basePath = `/${repoName}`;

export default defineConfig({
  tanstackStart: {
    // Redirect TanStack Start's bundled server entry to src/server.ts (our SSR error wrapper).
    // nitro/vite builds from this
    server: { entry: "server" },
    ...(pages
      ? {
          router: { basepath: basePath },
          prerender: { enabled: true },
          pages: [{ path: "/" }],
          sitemap: { enabled: false },
        }
      : {}),
  },
  ...(pages
    ? {
        vite: { base: `${basePath}/` },
        nitro: false,
      }
    : {}),
});
