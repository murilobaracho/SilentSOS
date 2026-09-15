import { useEffect } from "react";

/**
 * Rolagem suave com inércia (Lenis). Só roda no cliente.
 * Ativa também dentro do iframe de prévia para validação visual.
 */
export function SmoothScroll() {
  useEffect(() => {
    let raf = 0;
    let lenis: { raf: (t: number) => void; resize: () => void; destroy: () => void } | null = null;
    let cancelled = false;
    let observer: ResizeObserver | null = null;

    import("lenis").then(({ default: Lenis }) => {
      if (cancelled) return;
      const instance = new Lenis({
        duration: 1.6,
        easing: (t: number) => Math.min(1, 1.001 - Math.pow(2, -10 * t)),
        smoothWheel: true,
        wheelMultiplier: 0.75,
        syncTouch: true,
        anchors: true,
      });
      lenis = instance;

      const loop = (time: number) => {
        instance.raf(time);
        raf = requestAnimationFrame(loop);
      };
      raf = requestAnimationFrame(loop);

      if (typeof ResizeObserver !== "undefined") {
        observer = new ResizeObserver(() => instance.resize());
        observer.observe(document.body);
      }
    });

    return () => {
      cancelled = true;
      cancelAnimationFrame(raf);
      observer?.disconnect();
      lenis?.destroy();
    };
  }, []);

  return null;
}
