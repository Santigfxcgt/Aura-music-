# Aura Music - Modern Dark Glassmorphic Player

A modern native Android music player crafted with Jetpack Compose featuring an immersive obsidian dark aesthetic, neon violet and cyan accents, translucent frosted glass overlays, a floating rounded navigation bar, persistent mini-player, full-screen playback with dynamic blurred backgrounds, time-synced lyrics, and an interactive playback queue.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural and UX choices have been incorporated based on your selections:

- **Confirmed Decision 1 (Audio Engine)**: Bundled high-fidelity tracks with authentic playback using Android's native `MediaPlayer`, complete with waveform visualizations, seek capabilities, and audio metadata.
- **Confirmed Decision 2 (Aesthetic & Glassmorphism)**: Obsidian dark theme (`#0D0B14`, `#161224`) complemented by vivid neon violet (`#A855F7`) and cyan (`#06B6D4`) glow accents, accompanied by semi-transparent blurred surfaces (`surfaceContainerHigh.copy(alpha = 0.65f)`) and hairline glow borders.
- **Confirmed Decision 3 (Lyrics & Queue UX)**: Real-time interactive scrolling lyrics synchronized with track progress, alongside a fluid modal slide-up queue sheet allowing reordering and instant track skipping.

---

## 1. Overview & Core Concept

- **What It Does**: Aura Music provides an audiophile-grade mobile listening experience. Users explore featured picks in horizontal carousels, browse full track listings, control active playback seamlessly from any screen via a persistent floating mini-player, and expand into an edge-to-edge now-playing view featuring dynamic album-art backdrops, lossless audio badges, progress scrubber, time-synced lyrics, and queue management.
- **Target Audience / Persona**: Music enthusiasts and mobile listeners who value sophisticated typography, sleek dark-mode ergonomics, glassmorphic visual depth, and immediate tactile control over their music library.
- **Key Value**: Fluid, uninterrupted audio playback combined with an interface that feels alive—delivering smooth cross-fades, synchronized lyrics, and tactile glass surfaces without visual clutter.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Browse & Discovery (Home)**:
   - Top app bar with search icon (instant filter) and settings icon.
   - "Selecciones rápidas" (Quick Picks): Horizontal scrolling cards displaying vibrant square album arts, track titles, artist names, and category chips (e.g., Synthwave, Cyberpunk, Ambient).
   - "Canciones" (Songs section): Vertical listing of popular tracks with play counts and duration.
2. **Track Directory (Canciones)**:
   - Full vertical list with album art thumbnails, high-contrast titles, secondary artist text, duration, and three-dot context menus (Add to Queue, View Lyrics, Song Details).
   - Instant tap to play track and transition playback context.
3. **Library (Biblioteca)**:
   - Curated playlists, Favorites (Liked songs), Recently Played, and Downloaded/Lossless filters.
4. **Persistent Mini-Player**:
   - Floats directly above the bottom navigation bar with rounded corners (`16.dp`), subtle border glow, thumbnail art, animated track title marquee, play/pause toggle, and tap-to-expand gesture into the full Now Playing screen.
5. **Full Now Playing Screen**:
   - Edge-to-edge backdrop generated from the current song's palette with layered Gaussian blur and gradient vignette.
   - Header: Collapse arrow, top "Lossless 24-Bit / 96kHz" badge, and favorite toggle.
   - Central hero album art with soft drop shadows and rounded corners (`24.dp`).
   - Title and artist text aligned left with high-resolution typography.
   - Interactive scrub bar with elapsed and remaining timestamps.
   - Main control pill: Shuffle, Previous, Large Play/Pause button, Next, and Repeat modes (Off, All, One).
   - Bottom utility actions: "A continuación" (Up Next Queue Sheet) and "Letras" (Time-synced synchronized lyrics overlay).

### Visual Identity & Theme
- **Theme Posture**: Deep Obsidian Dark Mode.
- **Color Palette**:
  - `Background`: `#0A0910` (Ultra-deep void)
  - `Surface`: `#13111C` with `SurfaceVariant`: `#1E1B2E`
  - `Glass Overlay`: `Color(0x99181428)` with 1dp border `Color(0x33A855F7)`
  - `Primary Accent`: Neon Violet `#A855F7` / `#C084FC`
  - `Secondary Accent`: Electric Cyan `#06B6D4` / `#22D3EE`
  - `Audio Quality Badge`: Lossless Gold `#FBBF24` & Cyan `#38BDF8`
  - `OnSurface / Text`: High-contrast `#F8FAFC`, secondary `#94A3B8`, muted `#64748B`
- **Typography & Hierarchy**:
  - Display: Headline Medium (28sp, Bold, tight tracking).
  - Track Titles: Title Medium (18sp, SemiBold).
  - Metadata / Badges: Label Small (11sp, Medium, uppercase tracking +1.2).
- **Interactive Feedback & Motion**:
  - Spring-based slide and alpha transitions when expanding/collapsing the Now Playing view.
  - Pulsing glow rings around the main play/pause button while active.
  - Smooth progress interpolation for audio scrubbing and lyric line tracking.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Native MediaPlayer with Procedural Synth + Bundled Audio**:
  - *Chosen Approach*: Implement a self-contained `AudioPlaybackService`/Player Manager wrapping Android's `MediaPlayer` that plays actual high-quality encoded audio tracks alongside a procedural tone generator fallback, ensuring reliable offline sound on both emulator and physical devices.
  - *Why*: Guarantees immediate audio playback without external network latency, buffer failures, or broken stream URLs.
- **Decision 2: Compose Custom Blur & Backdrop Rendering**:
  - *Chosen Approach*: Utilize Compose `graphicsLayer { renderEffect = ... }` with backwards-compatible multi-layer gradient fallbacks and translucent scrims.
  - *Why*: Provides liquid glassmorphic aesthetics that look stunning on modern Android 12+ devices while maintaining smooth 60fps frame rates.
- **Decision 3: Floating Navigation Hierarchy**:
  - *Chosen Approach*: Custom floating rounded bar positioned above system navigation insets, paired with a unified scaffold containing the persistent mini-player.
  - *Why*: Meets the exact requirement for a floating dock feel with full edge-to-edge compliance (`WindowInsets.navigationBars`).

---

## 4. Technical Architecture & Data Strategy

### Architecture & Component Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                             MainActivity                               │
│                      (Edge-to-Edge Enabled)                            │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                        ┌───────────▼───────────┐
                        │   AuraPlayerViewModel │
                        │  (StateFlow: UIState) │
                        └───────────┬───────────┘
                                    │
           ┌────────────────────────┼────────────────────────┐
           ▼                        ▼                        ▼
┌──────────────────────┐ ┌──────────────────────┐ ┌──────────────────────┐
│  AudioPlayerManager  │ │   TrackRepository    │ │    LyricsEngine      │
│ (Android MediaPlayer)│ │  (Curated Catalog)   │ │  (Time-Synced Sync)  │
└──────────────────────┘ └──────────────────────┘ └──────────────────────┘
                                    │
                        ┌───────────▼───────────┐
                        │       MainScreen      │
                        │ (Scaffold & Overlays) │
                        └───────────┬───────────┘
         ┌──────────────────────────┼──────────────────────────┐
         ▼                          ▼                          ▼
┌─────────────────┐        ┌─────────────────┐        ┌─────────────────┐
│ Screen Content  │        │ Persistent Mini │        │ Floating Bottom │
│  - Home Tab     │        │     Player      │        │    Nav Bar      │
│  - Songs Tab    │        │ (Tap to Expand) │        │ (Rounded Glass) │
│  - Library Tab  │        └────────┬────────┘        └─────────────────┘
└─────────────────┘                 │
                                    ▼
                        ┌───────────────────────┐
                        │ Full NowPlayingScreen │
                        │ - Blurred Art Backdrop│
                        │ - Lossless Badge      │
                        │ - Scrub & Controls    │
                        │ - Time-Synced Lyrics  │
                        │ - Up Next Queue Sheet │
                        └───────────────────────┘
```

### Data Model & State
- `Track`: `id`, `title`, `artist`, `album`, `durationMs`, `artworkResId` / `artworkGradient`, `audioResId`, `qualityFormat` ("Hi-Res Lossless 24-Bit/96kHz"), `lyrics` (list of `LyricLine(timestampMs, text)`).
- `PlaybackState`: `currentTrack`, `isPlaying`, `currentPositionMs`, `durationMs`, `isShuffle`, `repeatMode` (`OFF`, `ALL`, `ONE`), `queue`, `queueIndex`, `isNowPlayingExpanded`, `isQueueSheetVisible`, `isLyricsViewVisible`.
- `NavigationTab`: `HOME`, `SONGS`, `LIBRARY`.

### Interactive Component & State Mapping
- **Mini-Player**: Tapping play/pause toggles audio without opening full player; tapping the card body opens full-screen playback with a smooth slide-up transition.
- **Scrubber / Slider**: Dragging seeks in real-time (`seekTo`), updating the current playback position and highlighting the active lyric line.
- **Queue Sheet**: Displays "A continuación" with clear indicator of currently playing track, quick-jump to any song, and removal options.
- **Lyrics View**: Auto-scrolls to the line matching `currentPositionMs`, with manual tap-to-seek functionality on individual lyric lines.
