import { useEffect, useRef, useState, type ElementType, type ReactNode } from "react";

import { cn } from "@/lib/utils";

interface RevealProps {
  children: ReactNode;
  className?: string;
  /** Atraso em ms (efeito cascata) */
  delay?: number;
  as?: ElementType;
}

/**
 * Entrada suave (translateY + fade) quando o elemento entra na viewport.
 * Regras de segurança:
 *  - dispara com qualquer parte visível (threshold 0 + rootMargin positivo);
 *  - o que já está na primeira dobra aparece imediatamente no carregamento;
 *  - sem IntersectionObserver, o conteúdo fica visível (nunca opacidade 0 permanente).
 */
export function Reveal({ children, className, delay = 0, as: Tag = "div" }: RevealProps) {
  const ref = useRef<HTMLElement | null>(null);
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    const node = ref.current;
    if (!node) {
      setVisible(true);
      return;
    }

    // Fallback: sem suporte ao observer, mostra tudo.
    if (typeof IntersectionObserver === "undefined") {
      setVisible(true);
      return;
    }

    // Primeira dobra: visível imediatamente, sem depender de scroll.
    const rect = node.getBoundingClientRect();
    if (rect.top < window.innerHeight && rect.bottom > 0) {
      setVisible(true);
      return;
    }

    const observer = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) {
          if (entry.isIntersecting || entry.intersectionRatio > 0) {
            setVisible(true);
            observer.disconnect();
          }
        }
      },
      { threshold: 0, rootMargin: "0px 0px 10% 0px" },
    );
    observer.observe(node);

    // Rede de segurança: se nada disparar em 3s, revela de qualquer forma.
    const safety = window.setTimeout(() => setVisible(true), 3000);

    return () => {
      window.clearTimeout(safety);
      observer.disconnect();
    };
  }, []);

  return (
    <Tag
      ref={ref}
      className={cn("reveal", visible && "reveal-visible", className)}
      style={{ transitionDelay: `${delay}ms` }}
    >
      {children}
    </Tag>
  );
}
