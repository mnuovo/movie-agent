# 🗺️ Product Roadmap & Backlog

> Turning **Movie-Agent** into a personal AI entertainment concierge.

---

## 📌 Quick navigation

- [🌟 Vision](#vision)
- [🎯 Product goals](#product-goals)
- [🧭 Roadmap at a glance](#roadmap-at-a-glance)
- [🚀 Release roadmap](#release-roadmap)
- [🧰 Recommended integrations](#recommended-integrations)
- [📊 Prioritized epics](#prioritized-epics)
- [📝 Backlog of epics and user stories](#backlog-of-epics-and-user-stories)
- [✅ MVP recommendation](#mvp-recommendation)
- [📅 Suggested implementation order](#suggested-implementation-order)
- [🛡️ Non-functional considerations](#non-functional-considerations)
- [❓ Open product questions](#open-product-questions)
- [🏁 Short-term recommendation](#short-term-recommendation)
- [🎬 Product positioning statement](#product-positioning-statement)

---

<a id="vision"></a>

## 🌟 Vision

**Movie-Agent** should evolve from a TMDB-powered chat demo into a **personal AI entertainment concierge** that helps users discover, compare, decide, and remember what to watch.

The product direction is to combine:

- conversational discovery
- trustworthy TMDB-grounded answers
- decision-support features
- watch availability awareness
- persistent user memory and preferences

---

<a id="product-goals"></a>

## 🎯 Product goals

### Primary goals

1. **Help users decide faster** what to watch
2. **Increase relevance** through personalization and context
3. **Improve retention** with watchlists, memory, and continuity
4. **Differentiate** from simple movie search by offering reasoning, comparison, and tailored recommendations

### Success signals

- users ask follow-up questions in the same session
- users save content to a watchlist
- users return for repeat discovery sessions
- recommendations become increasingly preference-aware
- users can act on recommendations immediately thanks to provider availability

---

<a id="roadmap-at-a-glance"></a>

## 🧭 Roadmap at a glance

### Status legend

| Symbol | Meaning                                |
|--------|----------------------------------------|
| 🟢     | Best next investment                   |
| 🟡     | Important after the core roadmap items |
| 🔵     | Strategic differentiator               |
| 🟣     | Longer-term premium capability         |

| Horizon | Theme                      | Outcome                                               | Focus |
|---------|----------------------------|-------------------------------------------------------|-------|
| Now     | Smarter discovery          | Better recommendations from TMDB data                 | 🟢    |
| Next    | Practical decision support | Users can compare titles and see where to watch       | 🟢    |
| Later   | Personal AI assistant      | Memory, preferences, and persistent watchlists        | 🔵    |
| Future  | Premium experience         | Proactive recommendations, notifications, and rich UX | 🟣    |

---

<a id="release-roadmap"></a>

## 🚀 Release roadmap

## Phase 1 — Smarter Discovery 🟢
**Goal:** Improve recommendation quality using capabilities that fit the current architecture.

### Epics
- EPIC-1: Discovery enrichment
- EPIC-2: Recommendation quality improvements
- EPIC-3: Richer title intelligence

### Candidate features
- trending / popular / top-rated / upcoming tools
- similar titles and TMDB recommendations
- richer title details including cast, genres, and overview grounding
- trailer and review enrichment

### Suggested deliverables
- `trendingMediaTool`
- `topRatedMediaTool`
- `upcomingReleasesTool`
- `similarTitlesTool`
- `recommendationsTool`
- `creditsLookupTool`
- `trailerLookupTool`
- `reviewsSummaryTool`

---

## Phase 2 — Practical Decision Support 🟢
**Goal:** Help users choose, not just search.

### Epics
- EPIC-4: Comparison and selection support
- EPIC-5: Watch availability intelligence
- EPIC-6: Guided discovery UX

### Candidate features
- title comparison flows
- region-aware streaming provider lookup
- runtime, genre, mood, and family-safe filtering
- richer response cards in the UI

### Suggested deliverables
- `compareTitlesTool`
- `watchProviderTool`
- `familySafeRecommendationTool`
- `runtimeFilteredRecommendationTool`
- quick actions in chat UI: “Compare”, “Where to watch”, “Show similar”

---

## Phase 3 — Personalization & Memory 🔵
**Goal:** Make the assistant feel personal and sticky.

### Epics
- EPIC-7: User profile and preferences
- EPIC-8: Watchlists and saved lists
- EPIC-9: Conversation memory

### Candidate features
- favorite genres / directors / actors
- liked and disliked titles
- watchlists and custom lists
- remembered region and platform preferences
- cross-session recommendation continuity

### Suggested deliverables
- user profile persistence
- `savePreferenceTool`
- `saveWatchlistTool`
- `loadUserTasteProfileTool`
- `conversationMemoryTool`

---

## Phase 4 — Intelligent Assistant Experience 🟣
**Goal:** Create meaningful differentiation through semantic reasoning and proactive support.

### Epics
- EPIC-10: Semantic recommendation engine
- EPIC-11: Context and mood-based discovery
- EPIC-12: Proactive notifications and retention

### Candidate features
- semantic similarity search
- mood-based recommendations
- “why this recommendation?” explanations
- alerts for streaming availability and upcoming releases
- weekly digests

### Suggested deliverables
- vector-based recommendation retrieval
- `moodRecommendationTool`
- digest/notification scheduler
- account-level recommendation memory

---

<a id="recommended-integrations"></a>

## 🧰 Recommended integrations

## Tier 1 — Highest value, best fit 🟢

### 1. Expanded TMDB usage
Use additional TMDB endpoints for:
- watch providers
- similar titles
- recommendations
- reviews
- videos/trailers
- credits
- trending / top-rated / upcoming

**Why now:** fastest path to better answers with minimal architectural change.

### 2. Persistence layer
Recommended options:
- **PostgreSQL** for structured persistence
- **MongoDB** for flexible profile/session documents

Use for:
- user preferences
- watchlists
- saved lists
- chat history
- regional/provider settings

### 3. Redis
Use for:
- chat session state
- hot caches for TMDB results
- rate limiting
- temporary recommendation context

### 4. Vector store
Recommended options:
- **pgvector** if PostgreSQL is used
- **Qdrant** for a dedicated vector database
- **Weaviate** for a richer AI-native platform

Use for:
- semantic similarity
- taste memory
- mood retrieval
- explanation support

---

## Tier 2 — Strong differentiators 🔵

### 5. Trakt.tv
Use for:
- watch history sync
- watchlist sync
- richer user retention features

### 6. JustWatch or equivalent availability provider
Use for:
- country-aware streaming availability
- provider-level filtering

### 7. YouTube / trailer source
Use for:
- trailer playback
- richer UI cards and previews

### 8. Neo4j
Use for:
- people/title relationship graphs
- actor/director connection-based recommendations

---

<a id="prioritized-epics"></a>

## 📊 Prioritized epics

| Epic ID | Epic                             | User value | Complexity | Priority | Signal |
|---------|----------------------------------|-----------:|-----------:|----------|--------|
| EPIC-1  | Discovery enrichment             |       High |        Low | P0       | 🟢     |
| EPIC-4  | Comparison and selection support |       High |     Medium | P0       | 🟢     |
| EPIC-5  | Watch availability intelligence  |  Very high |     Medium | P0       | 🟢     |
| EPIC-7  | User profile and preferences     |  Very high |     Medium | P1       | 🟡     |
| EPIC-8  | Watchlists and saved lists       |       High |     Medium | P1       | 🟡     |
| EPIC-10 | Semantic recommendation engine   |  Very high |       High | P2       | 🔵     |
| EPIC-11 | Mood-based discovery             |       High |       High | P2       | 🔵     |
| EPIC-12 | Notifications and retention      |     Medium |     Medium | P3       | 🟣     |

---

<a id="backlog-of-epics-and-user-stories"></a>

## 📝 Backlog of epics and user stories

## EPIC-1: Discovery enrichment 🎞️
**Problem:** Search alone is too limited for natural discovery.

### User stories
- As a user, I want to see **trending movies and TV shows** so that I can quickly discover what is popular now.
- As a user, I want to see **top-rated titles** so that I can find high-quality options quickly.
- As a user, I want to see **upcoming releases** so that I can discover what is coming soon.
- As a user, I want the agent to suggest **similar titles** so that I can explore adjacent options when I like a movie or series.
- As a user, I want the agent to show **TMDB recommendations** for a title so that I can continue browsing efficiently.

### Acceptance ideas
- results should be grounded in TMDB data
- results should be filterable by media type where relevant
- the agent should explain why a result set is relevant in plain language

---

## EPIC-2: Recommendation quality improvements ✨
**Problem:** Generic recommendations lack confidence and relevance.

### User stories
- As a user, I want the agent to combine **genres, popularity, and similarity** so that recommendations feel more relevant.
- As a user, I want **short explanation text** for each recommendation so that I understand why it was suggested.
- As a user, I want recommendations to use **more than title matching** so that hidden gems surface more often.

---

## EPIC-3: Richer title intelligence 🎥
**Problem:** Discovery is stronger when each answer contains more useful metadata.

### User stories
- As a user, I want to see **cast and crew highlights** so that I can decide based on creators and actors.
- As a user, I want access to **trailers** so that I can preview a recommendation immediately.
- As a user, I want the agent to summarize **spoiler-free reviews** so that I can assess quality before watching.
- As a user, I want the assistant to surface **runtime and genre cues** so that I can pick something matching my context.

---

## EPIC-4: Comparison and selection support ⚖️
**Problem:** Users often choose between a shortlist, not from a blank slate.

### User stories
- As a user, I want to **compare two or more titles** so that I can choose the best fit for my mood or time.
- As a user, I want to compare titles by **runtime, genre, tone, popularity, and ratings** so that I can make a faster decision.
- As a user, I want the agent to answer **“which one should I watch tonight?”** so that I get a direct recommendation rather than just data.
- As a user, I want a **family-safe comparison** so that I can avoid inappropriate content.

### Acceptance ideas
- comparisons should be grounded in tool-fetched metadata
- the final answer should include a recommendation, not only a raw comparison
- if the user asks for a shortlist, the agent should summarize trade-offs clearly

---

## EPIC-5: Watch availability intelligence 📺
**Problem:** A recommendation is much more valuable when users can act on it immediately.

### User stories
- As a user, I want to know **where a movie or show is streaming in my country** so that I can watch it right away.
- As a user, I want to filter recommendations by **Netflix, Prime Video, Disney+, Apple TV, or other providers** so that results fit my subscriptions.
- As a user, I want to know whether a title is available to **stream, rent, or buy** so that I can decide quickly.
- As a user, I want the assistant to remember my **default country/region** so that availability answers are relevant by default.

### Acceptance ideas
- provider data should be regionalized
- the agent should state when availability is unknown or unsupported
- follow-up actions should suggest alternative titles when a title is unavailable

---

## EPIC-6: Guided discovery UX 🖼️
**Problem:** Chat alone is helpful, but assisted interactions improve usability.

### User stories
- As a user, I want clickable **quick actions** like “Show similar” or “Compare” so that I can refine without retyping.
- As a user, I want **poster cards** in responses so that discovery feels more visual.
- As a user, I want **watch badges** and metadata chips so that I can scan options faster.
- As a user, I want a **recent searches/history panel** so that I can revisit prior exploration.

---

## EPIC-7: User profile and preferences 🧠
**Problem:** Recommendations are generic unless the assistant learns user taste.

### User stories
- As a user, I want to save my **favorite genres, actors, and directors** so that recommendations become more personal.
- As a user, I want to tell the agent what I **liked and disliked** so that future suggestions improve.
- As a user, I want to specify **constraints** like “no horror” or “under 2 hours” so that results fit my needs.
- As a user, I want the assistant to remember my **language and region preferences** so that answers are tailored automatically.

### Acceptance ideas
- preferences should persist between sessions
- users should be able to update or remove preferences
- the assistant should mention when a recommendation is influenced by remembered taste

---

## EPIC-8: Watchlists and saved lists 📚
**Problem:** Discovery has more value when users can keep what they find.

### User stories
- As a user, I want to **save a movie or show to a watchlist** so that I can return later.
- As a user, I want to create **custom themed lists** so that I can organize discoveries.
- As a user, I want to mark items as **watched, favorite, or skipped** so that the assistant learns over time.
- As a user, I want to **share a list** so that I can send recommendations to friends or family.

---

## EPIC-9: Conversation memory 💬
**Problem:** The assistant feels stateless without continuity.

### User stories
- As a user, I want the assistant to remember **the last few recommendations** so that follow-up questions make sense.
- As a user, I want to continue a discovery flow across sessions so that I do not need to start over.
- As a user, I want the assistant to remember what I said I was **in the mood for** so that the conversation feels natural.

---

## EPIC-10: Semantic recommendation engine 🔎
**Problem:** Keyword search misses contextual similarity.

### User stories
- As a user, I want recommendations based on **themes, mood, and story similarity** so that I discover titles beyond literal title matches.
- As a user, I want the assistant to find **less obvious alternatives** so that I can go beyond mainstream suggestions.
- As a user, I want recommendations based on **what I liked before** so that the assistant improves over time.

### Technical enablers
- embeddings for title descriptions and metadata
- vector similarity search
- hybrid ranking with TMDB popularity and preference signals

---

## EPIC-11: Mood-based discovery 🌈
**Problem:** Users often ask in emotional or situational language rather than genre language.

### User stories
- As a user, I want to ask for **uplifting, mind-bending, cozy, dark, or slow-burn** titles so that discovery matches my mood.
- As a user, I want to ask for something like **“Friday night comfort TV”** so that results fit a real-life context.
- As a user, I want the assistant to translate mood words into **useful recommendations and filters** so that I do not need to know exact genres.

---

## EPIC-12: Notifications and retention 🔔
**Problem:** The assistant is reactive today and could become proactively useful.

### User stories
- As a user, I want to be notified when a title on my watchlist becomes **available on a provider** I use.
- As a user, I want alerts for **upcoming releases** I may care about.
- As a user, I want a **weekly digest** of recommendations so that I keep discovering new titles without starting from scratch.

---

<a id="mvp-recommendation"></a>

## ✅ MVP recommendation

If the team wants the **highest product impact with manageable complexity**, the recommended MVP expansion is:

1. **Watch provider integration**
2. **Trending / similar / recommendations tools**
3. **Comparison tool**
4. **Watchlist persistence**
5. **Basic user preferences**

This combination improves:
- practicality
- recommendation quality
- repeat engagement
- product differentiation

---

<a id="suggested-implementation-order"></a>

## 📅 Suggested implementation order

### Sprint-oriented sequence

#### Sprint 1 🟢
- TMDB similar titles
- TMDB recommendations
- trending / top-rated / upcoming endpoints
- basic UI quick actions

#### Sprint 2 🟢
- watch provider lookup
- provider-aware recommendation prompts
- region preference handling

#### Sprint 3 🟡
- compare titles capability
- richer card-style responses
- trailers / reviews enrichment

#### Sprint 4 🔵
- persistence layer for watchlists and preferences
- save/load preference tools
- recent search and session memory

#### Sprint 5+ 🟣
- vector-based semantic retrieval
- mood recommendations
- proactive notifications

---

<a id="non-functional-considerations"></a>

## 🛡️ Non-functional considerations

To support the roadmap, the following engineering concerns should stay visible:

- response latency and streaming smoothness
- caching strategy for TMDB-heavy interactions
- prompt/tool orchestration quality
- observability and debugging for tool usage
- rate limiting and quota handling for external APIs
- graceful fallback when provider/review/trailer data is unavailable
- privacy and retention policies for user memory data

---

<a id="open-product-questions"></a>

## ❓ Open product questions

These questions should be answered before implementing deeper personalization features:

1. Will the product support **anonymous users only** or **authenticated accounts**?
2. Is **watch-provider availability** a must-have for the first public release?
3. Should user memory be **session-only** first or **persistent** from day one?
4. Is the priority **developer showcase** or **end-user utility**?
5. Should the agent optimize for **movies only first** or **movies + TV equally**?

---

<a id="short-term-recommendation"></a>

## 🏁 Short-term recommendation

If only three enhancements are selected next, prioritize:

1. **Watch provider integration**
2. **Similar/recommended/trending tools**
3. **User preference memory**

These three together offer the strongest balance of:
- user value
- implementation feasibility
- product differentiation

---

<a id="product-positioning-statement"></a>

## 🎬 Product positioning statement

> Movie-Agent is a personal AI entertainment concierge that helps users discover, compare, and choose what to watch using real-time movie data, watch availability, and evolving taste memory.

