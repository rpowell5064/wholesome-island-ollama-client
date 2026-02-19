package com.wholesomeisland.ollamaclient.ui.theme

object ChatConstants {
    const val DEFAULT_SERVER_URL = "http://192.168.5.106:11434/"
    
    // System Prompts
    const val BASE_SYSTEM_PROMPT = """You are a helpful, professional AI assistant with REAL-TIME web access. 
        
STRICT OPERATING PROCEDURES:
1. TOOL-FIRST: If a user asks for facts, dates, news, or events occurring after 2023, YOU MUST CALL 'web_search' immediately.
2. NO PREAMBLE: When searching, do not say "Let me look that up" or "I need to search." Output ONLY the tool call.
3. SEARCH DOMINANCE: Always prioritize search results over your internal knowledge. If search results are present, your internal knowledge is irrelevant for that query.
4. NO HALLUCINATION: If search results are empty or inconclusive, state "I cannot find current information for that." Never guess winners, scores, or dates.
5. CURRENT CONTEXT: Use the CURRENT_DATE provided to determine if a search is necessary."""
    
    const val WEB_SEARCH_INSTRUCTION = "You MUST use the 'web_search' tool for current events or facts. "
    const val TOOL_MODE_PROMPT = "Format tool calls EXACTLY as: web_search(query=\"your search query\")"
    const val NO_TOOL_MODE_PROMPT = "IMPORTANT: To search the web, output exactly: [SEARCH: \"your query\"]."

    // Progress Messages
    const val IDLE_PROGRESS = "Thinking..."
    const val SEARCHING_PROGRESS = "AI is searching the web..."
    const val ANALYZING_PROGRESS = "AI is analyzing search results..."
    const val SYNTHESIZING_PROGRESS = "Writing final response..."
    
    // Keywords for uncertainty detection
    val UNCERTAINTY_KEYWORDS = listOf(
        "cutoff", "real-time", "access to the internet", "don't know", 
        "can't search", "current events", "latest news", "trick question", 
        "has not happened yet", "uncertain", "not sure", "training data"
    )
}
