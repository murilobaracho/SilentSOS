# 🚨 FETEC - SilentSOS

> **Sistema Inteligente de Alerta Silencioso e Assistência de Emergência**

[![Status do Projeto](https://img.shields.io/badge/Status-Em%20Desenvolvimento-yellow.svg)](#)
[![Plataforma](https://img.shields.io/badge/Plataforma-Android%20%7C%20Wear%20OS-brightgreen.svg)](#)
[![Linguagem](https://img.shields.io/badge/Linguagem-Kotlin-purple.svg)](#)
[![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg)](#)

---

## 📌 Sobre o Projeto

O **SilentSOS** é uma aplicação nativa para Android desenvolvida para a **FETEC**. Seu objetivo é permitir o acionamento discreto e imediato de pedidos de socorro em situações de risco em que a vítima não pode utilizar o smartphone de forma visível.

O sistema opera em segundo plano e suporta acionamentos por botões físicos via serviços de acessibilidade, integração com relógios inteligentes (Wear OS) e mecanismos de camuflagem de tela.

---

## ✨ Principais Funcionalidades

- 🔴 **Acionamento Discreto:** Envio de alertas de emergência sem chamar a atenção no dispositivo.
- ⌚ **Integração com Wear OS:** Gatilhos e sincronização remota via relógio inteligente.
- 🔘 **Atalho por Botões Físicos:** Serviço de acessibilidade para detecção de acionamento rápido.
- ⚙️ **Foreground Service:** Execução contínua em segundo plano garantindo estabilidade do monitoramento.
- 🎭 **Tela de Falso Desligamento (Fake Shutdown):** Interface de disfarce para simular o desligamento do aparelho enquanto mantém os alertas ativos.
- 📜 **Histórico Local:** Armazenamento seguro do registro de emergências e acionamentos.

---

## 🛠️ Stack Tecnológica

- **Linguagem:** [Kotlin](https://kotlinlang.org/)
- **Interface de Usuário:** [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Recursos Android Native:**
  - `AccessibilityService` (Detecção de eventos do sistema)
  - `ForegroundService` (Serviços em primeiro plano)
  - `Wear OS API` (Comunicação com relógios inteligentes)
  - `BroadcastReceiver` (Recepção de eventos e gatilhos)
- **IDE:** Android Studio (Gradle Kotlin DSL)

---

## ⏱️ Ordem e Histórico de Desenvolvimento

Com base na estrutura de código do repositório, o desenvolvimento seguiu esta sequência:

1. **Configuração Inicial do Projeto (Estrutura Base):**
   - Criação do projeto Kotlin com Jetpack Compose no Android Studio.
   - Configuração do Gradle Kotlin DSL (`build.gradle.kts`, `settings.gradle.kts`, `libs.versions.toml`).
   - Definição do tema visual e paleta de cores (`ui/theme/`).

2. **Interface Principal e Gestão de Dados:**
   - Implementação da `MainActivity.kt` como tela inicial.
   - Criação do `HistoryManager.kt` para salvar localmente o histórico de acionamentos.
   - Declaração das permissões e componentes no `AndroidManifest.xml`.

3. **Core dos Serviços de Emergência em Segundo Plano:**
   - Desenvolvimento do `EmergencyForegroundService.kt` para manter o app ativo no sistema.
   - Implementação do `EmergencyAccessibilityService.kt` para ler os botões físicos de volume/liga-desliga.

4. **Integração Vestível e Recursos de Segurança:**
   - Integração com smartwatch via `SilentSOSWearService.kt` e `WearTriggerReceiver.kt`.
   - Criação da `FakeShutdownActivity.kt` para simular o desligamento do aparelho.

---

## 🏗️ Arquitetura do Projeto

```text
app/src/main/java/com/example/projetofetec/
├── MainActivity.kt               # Interface principal do aplicativo
├── FakeShutdownActivity.kt       # Interface de camuflagem de desligamento
├── data/
│   └── HistoryManager.kt         # Gerenciamento do histórico de alertas
├── services/
│   ├── EmergencyAccessibilityService.kt  # Captura de eventos físicos de acessibilidade
│   ├── EmergencyForegroundService.kt     # Serviço de execução em segundo plano
│   ├── SilentSOSWearService.kt           # Serviço de integração com Wear OS
│   └── WearTriggerReceiver.kt            # Receptor de gatilhos do smartwatch
└── ui/theme/                     # Estilização e componentes de UI (Compose)
```

---

## 🚀 Como Executar o Projeto

1. **Clone o repositório:**
   ```bash
   git clone https://github.com/murilobaracho/FETEC-SilentSOS.git
   ```
2. Abra o **Android Studio**.
3. Selecione **Open** e navegue até a pasta do projeto clonado.
4. Aguarde a sincronização do **Gradle** (`build.gradle.kts`).
5. Execute a aplicação em um emulador Android ou dispositivo físico (Android 8.0+ recomendado).
   > *Nota: Para testar as funções de acessibilidade e Wear OS, ative as permissões correspondentes nas configurações do dispositivo.*

---

## 📝 Licença

Este projeto foi desenvolvido para fins acadêmicos e de demonstração na **FETEC**.
