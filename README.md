# Wholesome Island 🌴

Wholesome Island is a high-performance, private, and efficient Android client for local Large Language Models (LLMs) via **Ollama**. It is designed with a focus on real-time information retrieval and model efficiency.

## 🚀 The PRINCIPAL Methodology

This application is built using the **PRINCIPAL** design pattern for LLM integration:

- **P**ersistent Context: Maintains conversation history with state management.
- **R**eal-time Integration: Live web search via DuckDuckGo and Jsoup scraping.
- **I**ncremental Streaming: Real-time character-by-character response delivery.
- **N**ative Performance: Optimized for local execution with reduced protocol overhead.
- **C**ontext Windowing: Automatic history and vision token management to prevent slowdowns.
- **I**ntent Detection: Seamless switching between conversational mode and tool-use (web search).
- **P**rompt Engineering: Strict system-level instructions to minimize hallucinations.
- **A**synchronous Lifecycle: Fully non-blocking UI using Kotlin Coroutines and Flow.
- **L**ocal First: Prioritizes privacy by communicating with your local Ollama instance.

## ✨ Key Features

- **Real-Time Web Search**: Automatically searches the web for current events (e.g., "Who won the Super Bowl in 2026?") and synthesizes the results.
- **Prompt Cache Optimization**: Stabilized system prompts to maximize Ollama's KV Caching, resulting in near-instant response starts.
- **Vision Support**: Attach images to your chat. The app intelligently trims old images from history to keep processing fast.
- **Jsoup Scraping**: High-quality search snippets extracted directly from HTML for accurate data injection.
- **Wholesome UI**: A clean, Material 3 interface with dark mode and tropical aesthetics.

## 🛠 Efficiency & Speed

Wholesome Island is tuned for "Zero-Latency" communication:
1. **Connection Pooling**: Reuses TCP connections to avoid handshake overhead.
2. **GZIP Support**: Minimizes data transfer for large chat histories.
3. **No Body Buffering**: Headers-only logging allows data to stream to the UI character-by-character without internal waiting.
4. **History Trimming**: Drops high-token vision data after 2 turns to keep the model responsive.

## 📋 Setup

1. Ensure [Ollama](https://ollama.com/) is running on your network.
2. Set your server URL in the app settings (e.g., `http://192.168.1.xxx:11434`).
3. Select your model (Llama 3, Qwen, Gemma, etc.).
4. Start chatting!

---
*Built for the future of local AI.*
