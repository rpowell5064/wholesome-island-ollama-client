# Wholesome Island 🌴

Wholesome Island is a private, local-first Android client for Ollama LLMs, featuring advanced web search capabilities and a customizable API engine.

## ✨ Key Features

- **Extensible Web Search**: Connect to any search engine via `GET` or `POST` APIs. Use placeholders like `{query}` for seamless integration with providers like Serper.dev or your private SearXNG instance.
- **Privacy-First**: All AI processing happens on your local server. Search queries go directly to the provider you choose—no middle-man tracking.
- **Prompt Cache Optimization**: Engineered system prompts ensure Ollama's KV Caching works perfectly for near-instant starts.
- **Vision & Multi-modal**: Full support for attaching images, with smart history management to keep context windows efficient.
- **Robust Material 3 UI**: Modern dark-mode interface with a focus on ease of use.

## 🛠 Advanced Connectivity

Wholesome Island is built for flexible infrastructures:
- **Auth Support**: Securely provide API keys for both Ollama and your Search Engine.
- **Custom Headers**: Configure specific authentication headers (e.g., `X-API-KEY`) per search engine.
- **History Management**: Dynamic turn-based history trimming saves tokens and reduces latency.

## 📋 Setup

1. **Ollama**: Ensure [Ollama](https://ollama.com/) is running and accessible on your network.
2. **Server**: In the app settings, enter your server URL and optional API Key.
3. **Search**: Use the default DuckDuckGo scraper or add your own custom API engine in settings.
4. **Chat**: Select your local model and start exploring.

---
*Built for the future of local AI.*
