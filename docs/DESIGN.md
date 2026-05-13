# FitTrack — Design Document

> Source: Stitch project **"FitTrack Dark Mode App"** (`projects/5608201329247495585`)  
> Device: Mobile · Mode: Dark · Last updated: 2026-05-13

---

## Table of Contents

1. [Brand & Philosophy](#1-brand--philosophy)
2. [Color System](#2-color-system)
3. [Typography](#3-typography)
4. [Layout & Spacing](#4-layout--spacing)
5. [Elevation & Depth](#5-elevation--depth)
6. [Shape Language](#6-shape-language)
7. [Components](#7-components)
8. [Screen Inventory](#8-screen-inventory)

---

## 1. Brand & Philosophy

FitTrack is engineered for **peak performance and high-intensity focus**. The target user is a serious athlete or fitness enthusiast who needs a "heads-down" interface that minimises distraction and maximises data clarity.

**Aesthetic direction:** High-Contrast / Bold + Minimalism

- A pure-dark foundation eliminates visual noise.
- Vibrant lime-green accents (`#a3e635`) serve as high-energy triggers for action and progress.
- The tone is professional and "gym-ready" — evoking premium equipment and high-end wearables.
- Aggressive typography and generous touch targets keep the UI usable **during physical exertion**.

---

## 2. Color System

### Primary Palette

| Role | Token | Hex |
|---|---|---|
| Background / Surface | `surface` | `#11150a` |
| Surface (dim) | `surface-dim` | `#11150a` |
| Surface (bright) | `surface-bright` | `#363b2e` |
| Surface Container | `surface-container` | `#1d2116` |
| Surface Container High | `surface-container-high` | `#272c1f` |
| Surface Container Low | `surface-container-low` | `#191d12` |
| Surface Container Lowest | `surface-container-lowest` | `#0b1006` |
| Surface Container Highest | `surface-container-highest` | `#32362a` |
| **Primary (Lime)** | `primary` | `#ccff80` |
| Primary Container | `primary-container` | `#a3e635` |
| On Primary | `on-primary` | `#213600` |
| On Primary Container | `on-primary-container` | `#416400` |
| On Surface | `on-surface` | `#e0e4d2` |
| On Surface Variant | `on-surface-variant` | `#c2cab0` |
| Outline | `outline` | `#8c947c` |
| Outline Variant | `outline-variant` | `#424936` |
| Error | `error` | `#ffb4ab` |
| Error Container | `error-container` | `#93000a` |

### Usage Rules

- **Pure Dark Background:** Use `#0a0a0a` / `surface` as the base canvas — OLED-optimised.
- **Electric Primary:** `#a3e635` is the sole attention driver. Reserved for primary actions, active states, and critical data points only.
- **Surface Elevation:** Cards sit on `#1e1e1e` — just enough contrast to define structure without breaking dark immersion.
- **Borders:** Use `#2a2a2a` for card/input borders; subtle definition in low-light environments.
- **Active States:** Do not lift an element higher on press. Instead, apply a 1px lime green border or solid lime fill to signal state change.
- **No soft shadows:** Avoid blurs and ambient shadows to maintain the sharp, digital-tool aesthetic.

---

## 3. Typography

Three typefaces are used in a strict hierarchy:

| Role | Typeface | Purpose |
|---|---|---|
| Headlines | **Lexend** | Athletic, wide-aperture — commands attention |
| Body | **Inter** | Maximum legibility in data-dense areas |
| Data / Labels | **JetBrains Mono** | Numerical data, timestamps — emphasises "tracking" |

### Type Scale

| Token | Family | Size | Weight | Line Height | Letter Spacing |
|---|---|---|---|---|---|
| `display-lg` | Lexend | 48px | 800 | 56px | -0.02em |
| `headline-lg` | Lexend | 32px | 700 | 40px | -0.01em |
| `headline-lg-mobile` | Lexend | 28px | 700 | 36px | — |
| `headline-md` | Lexend | 24px | 700 | 32px | — |
| `body-lg` | Inter | 18px | 400 | 28px | — |
| `body-md` | Inter | 16px | 400 | 24px | — |
| `label-caps` | JetBrains Mono | 12px | 600 | 16px | 0.1em |

### Scaling Notes

- On mobile, reduce display sizes by 15–20% but **maintain heavy weights** for arm's-length readability.
- Use bold/extra-bold for all headline hierarchy — never a light weight for emphasis.

---

## 4. Layout & Spacing

The layout follows a **Fluid Grid** model with high internal whitespace to prevent accidental taps during workouts.

### Spacing Tokens

| Token | Value |
|---|---|
| `unit` | 4px |
| `container-margin` | 20px |
| `gutter` | 16px |
| `touch-target-min` | 48px |
| `section-gap` | 40px |

### Rules

- **Rhythm:** All padding and margins must be multiples of **8px** (2× the 4px unit).
- **Outer Margin:** 20px on mobile to prevent content from hitting screen edges.
- **Touch Targets:** Minimum 48px height for all interactive elements; **56px preferred** for primary workout actions.
- **Whitespace:** Generous vertical breathing room is a functional requirement — not just aesthetic — so users can process data quickly between sets.

---

## 5. Elevation & Depth

Elevation is communicated through **Tonal Layering** and **Bold Outlines**, not shadows.

| Layer | Token | Hex |
|---|---|---|
| Base (background) | `surface` | `#11150a` |
| Cards / Surfaces | `surface-container` | `#1d2116` |
| Elevated UI | `surface-container-high` | `#272c1f` |
| Top layer | `surface-container-highest` | `#32362a` |

- All elevated surfaces must carry a **1px solid `#2a2a2a` border**.
- Active/pressed state: gain a 1px lime green border or solid lime fill — no physical lift.
- **Zero soft shadows.** No `box-shadow` blurs anywhere in the UI.

---

## 6. Shape Language

The shape language is structured and "engineered."

| Element | Radius Token | Value |
|---|---|---|
| Cards / Containers | `rounded-lg` | 12px (1rem) |
| Buttons / Inputs | `rounded` (default) | 8px (0.5rem) |
| Progress bars / Dots | `rounded-full` | 9999px |
| Small tags | `rounded-sm` | 4px (0.25rem) |
| Large panels | `rounded-xl` | 24px (1.5rem) |

---

## 7. Components

### Buttons

| Variant | Background | Text | Border |
|---|---|---|---|
| **Primary** | `#a3e635` (solid) | `#0a0a0a` bold | None |
| **Secondary** | Transparent | White | 1px `#2a2a2a` |
| **Ghost** | None | Lime (`#a3e635`) for actions, White for nav | None |

### Inputs

- Background: `#1e1e1e`
- Default border: 1px `#2a2a2a`
- Focus border: 1px `#a3e635`
- Placeholder text: secondary text color (`#a1a1aa`)

### Cards

- Background: `#1e1e1e`
- Always includes 1px border: `#2a2a2a`
- Internal padding: minimum **24px**

### Active / Selection Indicators

- Tab bars & selection states: **3px thick lime line** or **6px lime dot**
- Progress bars: `#2a2a2a` track + `#a3e635` fill

### Data Displays

- Primary metric (e.g. heart rate, weight lifted): large **Lexend Bold**
- Secondary metrics / timestamps: **JetBrains Mono** for a "readout" feel

---

## 8. Screen Inventory

The Stitch project contains the following visible screens (390px wide, mobile):

| Screen ID (short) | Notes |
|---|---|
| `10f93627` | 390×1227 — tall scroll screen |
| `116864c1` | 390×884 |
| `2b35d2ff` | 390×1283 — tall scroll screen |
| `2d4f40c0` | 390×854 |
| `5b36290d` | 390×884 |
| `81fc1bd8` | 390×884 |
| `96ced042` | 390×884 |
| `a04a057b` | 390×884 |
| `acd436b6` | 390×884 |
| `ad0df3b2` | 390×884 |
| `af0a8ac1` | 390×884 |
| `b99285f9` | 390×884 |
| `c6cf20ca` | 390×884 |
| `d30da76a` | 390×884 |
| `e6968db9` | 390×884 |
| `f4d7bca9` | 390×884 |

> Design system instances (component sheets) are also present at 960×540.

---

*Generated from Stitch MCP — project `5608201329247495585`*
