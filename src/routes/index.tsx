import { createFileRoute } from "@tanstack/react-router";
import type { MouseEvent as ReactMouseEvent } from "react";
import { Download, Github, Menu, Watch, EyeOff, Power, Cog, ScrollText, CircleDot } from "lucide-react";
import mockup from "@/assets/silentsos-mockup.png";
import apk from "@/assets/silent-sos.apk.asset.json";
import { Button } from "@/components/ui/button";
import { Reveal } from "@/components/Reveal";
import { SmoothScroll } from "@/components/SmoothScroll";


const GITHUB = "https://github.com/murilobaracho/FETEC-SilentSOS.git";

async function downloadApk(e: ReactMouseEvent<HTMLAnchorElement>) {
  e.preventDefault();
  try {
    const res = await fetch(apk.url);
    const blob = await res.blob();
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "silent-sos.apk";
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
  } catch {
    window.location.href = apk.url;
  }
}

export const Route = createFileRoute("/")({
  head: () => ({
    meta: [
      { title: "SilentSOS — Proteção discreta e imediata" },
      {
        name: "description",
        content:
          "SilentSOS é o app Android de alerta silencioso: acionamento discreto, integração Wear OS e tela de falso desligamento. Baixe o APK gratuito.",
      },
      { property: "og:title", content: "SilentSOS — Proteção discreta e imediata" },
      {
        property: "og:description",
        content:
          "Sistema inteligente de alerta silencioso e assistência de emergência na palma da sua mão.",
      },
      { property: "og:type", content: "website" },
      { name: "twitter:card", content: "summary_large_image" },
    ],
  }),
  component: Landing,
});

const features = [
  {
    icon: CircleDot,
    title: "Acionamento Discreto",
    text: "Alertas invisíveis enviados sem acender ou acionar a tela do aparelho.",
  },
  {
    icon: Watch,
    title: "Integração Wear OS",
    text: "Gatilho e sincronização remota via relógio inteligente.",
  },
  {
    icon: EyeOff,
    title: "Atalho por Botões Físicos",
    text: "Detecção rápida através dos botões de volume e energia.",
  },
  {
    icon: Power,
    title: "Tela de Falso Desligamento",
    text: "Simula o desligamento enquanto mantém a proteção ativa.",
  },
  {
    icon: Cog,
    title: "Foreground Service",
    text: "Monitoramento contínuo, estável e ininterrupto em segundo plano.",
  },
  {
    icon: ScrollText,
    title: "Histórico Local",
    text: "Registros salvos apenas no dispositivo com privacidade total.",
  },
];

const stack = ["Kotlin", "Jetpack Compose", "AccessibilityService", "ForegroundService", "Wear OS API"];

function Landing() {
  return (
    <div className="min-h-screen bg-background font-sans text-foreground antialiased">
      <SmoothScroll />
      {/* Header */}
      <header className="fixed inset-x-0 top-0 z-50 border-b border-[var(--glass-border)] bg-background/60 backdrop-blur-2xl">
        <nav className="mx-auto flex h-16 max-w-6xl items-center justify-between px-6">
          <a href="#top" className="flex items-center gap-2 text-[15px] font-semibold tracking-tight">
            SilentSOS
            <span className="relative flex h-2 w-2">
              <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-primary opacity-75" />
              <span className="relative inline-flex h-2 w-2 rounded-full bg-primary" />
            </span>
          </a>
          <div className="hidden items-center gap-8 text-[13px] text-muted-foreground md:flex">
            <a className="transition-colors hover:text-foreground" href="#recursos">Recursos</a>
            <a className="transition-colors hover:text-foreground" href="#tecnologias">Tecnologias</a>
            <a className="transition-colors hover:text-foreground" href="#download">Download</a>
          </div>
          <div className="flex items-center gap-3">
            <a href={apk.url} download="silent-sos.apk" onClick={downloadApk}>
              <Button variant="pill" size="sm" className="cta-pulse">Baixar APK</Button>
            </a>
            <Menu className="h-5 w-5 text-muted-foreground md:hidden" />
          </div>
        </nav>
      </header>

      {/* Hero */}
      <section id="top" className="relative overflow-hidden px-6 pt-36 pb-16 text-center">
        <div
          aria-hidden
          className="pointer-events-none absolute left-1/2 top-10 h-[520px] w-[520px] -translate-x-1/2 rounded-full opacity-40 blur-[120px]"
          style={{ background: "var(--gradient-accent)" }}
        />
        <div className="relative mx-auto max-w-3xl">
          <Reveal as="p" className="mb-6 inline-flex rounded-full border border-[var(--glass-border)] bg-[var(--glass)] px-4 py-1.5 text-xs text-muted-foreground backdrop-blur-xl">
            Aplicativo Android nativo · v1.0.0
          </Reveal>
          <Reveal as="h1" delay={100} className="text-balance text-5xl font-semibold leading-[1.05] tracking-tight sm:text-7xl">
            <span className="bg-clip-text text-transparent" style={{ backgroundImage: "var(--gradient-text)" }}>
              SilentSOS.
              <br />
              Proteção discreta e imediata.
            </span>
          </Reveal>
          <Reveal as="p" delay={200} className="mx-auto mt-6 max-w-xl text-lg text-muted-foreground">
            O sistema inteligente de alerta silencioso e assistência de emergência na palma da sua mão.
          </Reveal>
          <Reveal delay={300} className="mt-9 flex flex-wrap items-center justify-center gap-3">
            <a href={apk.url} download="silent-sos.apk" onClick={downloadApk}>
              <Button variant="hero" size="xl" className="cta-pulse">Baixar agora</Button>
            </a>
            <a href={GITHUB} target="_blank" rel="noreferrer">
              <Button variant="glass" size="xl">
                <Github className="h-4 w-4" /> Ver no GitHub
              </Button>
            </a>
          </Reveal>
        </div>

        <Reveal delay={400} className="relative mx-auto mt-16 max-w-sm">
          <img
            src={mockup}
            alt="Interface do aplicativo SilentSOS com botão de emergência"
            width={1024}
            height={1024}
            className="w-full drop-shadow-[0_40px_80px_rgba(0,0,0,0.9)]"
          />
        </Reveal>
      </section>


      {/* Bento features */}
      <section id="recursos" className="mx-auto max-w-6xl px-6 py-24">
        <Reveal as="h2" className="max-w-2xl text-4xl font-semibold tracking-tight sm:text-5xl">
          Feito para agir <span className="text-muted-foreground">quando ninguém pode perceber.</span>
        </Reveal>
        <div className="mt-12 grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
          {features.map(({ icon: Icon, title, text }, i) => (
            <Reveal
              key={title}
              as="article"
              delay={i * 100}
              className="group flex h-full flex-col justify-end rounded-2xl border border-white/[0.08] bg-white/[0.04] p-6 backdrop-blur-md transition-all duration-500 hover:border-[#FF3B30]/30 hover:bg-white/[0.06]"
            >
              <Icon className="mb-5 h-6 w-6 text-[#FF3B30] transition-transform duration-500 group-hover:scale-110" />
              <h3 className="text-lg font-bold tracking-tight text-white">{title}</h3>
              <p className="mt-2 text-sm leading-relaxed text-[#86868B]">{text}</p>
            </Reveal>
          ))}
        </div>
      </section>

      {/* Stack */}
      <section id="tecnologias" className="mx-auto max-w-6xl px-6 pb-24">
        <Reveal as="h2" className="text-2xl font-semibold tracking-tight">Arquitetura & stack tecnológica</Reveal>
        <Reveal as="p" delay={100} className="mt-2 text-sm text-muted-foreground">Construído 100% nativo para Android.</Reveal>
        <div className="mt-8 flex flex-wrap gap-3">
          {stack.map((t, i) => (
            <Reveal
              key={t}
              as="span"
              delay={150 + i * 100}
              className="inline-block rounded-2xl border border-[var(--glass-border)] bg-[var(--glass)] px-5 py-3 text-sm text-foreground/90 backdrop-blur-xl transition-colors hover:border-primary/30"
            >
              {t}
            </Reveal>
          ))}
        </div>
      </section>

      {/* Download */}
      <section id="download" className="px-6 pb-28">
        <Reveal className="relative mx-auto max-w-3xl overflow-hidden rounded-[36px] border border-[var(--glass-border)] bg-[var(--glass)] px-5 py-10 text-center backdrop-blur-2xl sm:px-8 sm:py-16">
          <div
            aria-hidden
            className="pointer-events-none absolute left-1/2 top-full h-72 w-72 -translate-x-1/2 -translate-y-1/2 rounded-full opacity-40 blur-[100px]"
            style={{ background: "var(--gradient-accent)" }}
          />
          <h2 className="relative text-4xl font-semibold tracking-tight sm:text-5xl">Segurança ao seu alcance.</h2>
          <p className="relative mt-4 text-muted-foreground">Compatível com Android 8.0 ou superior.</p>
          <div className="relative mt-9">
            <a href={apk.url} download="silent-sos.apk" onClick={downloadApk} className="block sm:inline-block">
              <Button variant="hero" size="xl" className="cta-pulse h-auto w-full whitespace-normal px-6 py-4 text-sm leading-snug sm:w-auto sm:text-base">
                <Download className="h-4 w-4 shrink-0" /> Baixar APK Gratuito (v1.0.0)
              </Button>
            </a>
          </div>
          <p className="relative mx-auto mt-6 max-w-md text-xs leading-relaxed text-muted-foreground">
            Após a instalação, ative as permissões de acessibilidade em Ajustes → Acessibilidade → SilentSOS para
            habilitar o acionamento discreto.
          </p>
        </Reveal>
      </section>


      {/* Footer */}
      <footer className="border-t border-[var(--glass-border)] px-6 py-10">
        <div className="mx-auto flex max-w-6xl flex-col items-center justify-between gap-4 text-xs text-muted-foreground sm:flex-row">
          <p>Desenvolvido para a FETEC.</p>
          <a
            href={GITHUB}
            target="_blank"
            rel="noreferrer"
            className="inline-flex items-center gap-2 transition-colors hover:text-foreground"
          >
            <Github className="h-4 w-4" /> Repositório no GitHub
          </a>
          <p>© 2026 SilentSOS · Licença MIT</p>
        </div>
      </footer>
    </div>
  );
}
