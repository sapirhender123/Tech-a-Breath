# Friendly Dashboard Plan

## Top-Level Overview

Build a user-facing "friendly" dashboard flow — two screens in sequence, accessible from the
home screen above the existing "Open Dashboard" button.

**Screen 1 — Weekly Check-in**
Warm, calm summary of the week. Shows how many trigger events were masked. User picks one of
4 emoji faces (😞 😐 🙂 ✨) to rate how they felt. "Continue" is disabled until a pick is made.
On Continue → navigate to Screen 2.

**Screen 2 — Adjustment Suggestion**
Based on the emoji rating, the app suggests a masking volume adjustment:
- Rating 3–4 (😊/✨ feeling good) → suggest a small reduction (~10%) — "Automatic Volume Update"
  card with current vs recommended progress bars + "Apply" / "Keep as is" buttons.
- Rating 1–2 (😞/😐 struggling) → supportive message, no reduction suggested.
Back arrow returns to Screen 1.

**Storage:** The weekly rating (1–4 int) is stored in `SharedPreferences`
(`key = "weekly_rating_<ISO_week>"`) — NOT in `event_feedback`, because that table requires
a valid `event_id` FK and is designed for per-event monitoring feedback, not weekly mood.

**Scope:** The existing 3-tab analytics dashboard is completely untouched.

---

## Sub-Tasks

---

### Sub-Task 1 — Data layer: WeeklyRatingStore

**Intent**
A tiny `SharedPreferences`-backed store that saves and reads the weekly rating.
No Room changes needed.

**Expected Outcomes**
- `WeeklyRatingStore.kt` in `data/prefs/` package
- `fun saveRating(rating: Int)` — writes `"weekly_rating_<ISO_week>" = rating`
- `fun getRating(): Int?` — reads this week's rating, returns null if not yet set
- Used directly by `FriendlyDashboardViewModel` (no repository layer needed — it's
  UI-local state)

**Todo List**
1. Create `app/src/main/java/com/example/tech_a_breath/data/prefs/WeeklyRatingStore.kt`
2. Constructor takes `Context`, uses `getSharedPreferences("friendly_dashboard", MODE_PRIVATE)`
3. ISO week key: `"weekly_rating_${Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)}"`
4. `saveRating(Int)` and `getRating(): Int?`

**Relevant Context**
- No existing prefs file — create from scratch
- No Room / DAO involvement

**Status** — `[ ] pending`

---

### Sub-Task 2 — FriendlyDashboardViewModel

**Intent**
ViewModel for Screen 1. Combines:
- Weekly event count from `repository.getWeeklyTotalEvents()` (existing Flow)
- Dominant masking % from `repository.getWeeklyCountByTrigger()` (existing — pick top trigger,
  look up its masking % from `repository.getCurrentMasking()`)
- Local selected emoji rating (`MutableStateFlow<Int?>`)

On `submitFeedback(rating)`: saves via `WeeklyRatingStore`, flips `feedbackSubmitted = true`.

**Expected Outcomes**
```
data class FriendlyUiState(
    val isLoading: Boolean = true,
    val weeklyCount: Int = 0,
    val dominantMaskingPct: Int = 80,
    val selectedRating: Int? = null,       // 1–4, null = not picked
    val feedbackSubmitted: Boolean = false
)
```
- `submitFeedback(rating: Int)` saves to prefs, sets `feedbackSubmitted = true`
- Standard `ViewModelProvider.Factory` inner class

**Todo List**
1. Create `ui/dashboard/friendly/FriendlyDashboardViewModel.kt`
2. Inject `DashboardRepository` + `WeeklyRatingStore`
3. Combine `getWeeklyTotalEvents()` + `getCurrentMasking()` + `getWeeklyCountByTrigger()`
   into `FriendlyUiState` via `combine { }` + `stateIn`
4. `fun selectRating(r: Int)` updates local MutableStateFlow
5. `fun submitFeedback()` — saves selectedRating to store, flips submitted flag

**Relevant Context**
- Pattern: `WeeklySummaryViewModel.kt`
- Flows: `getWeeklyTotalEvents()`, `getWeeklyCountByTrigger()`, `getCurrentMasking()`
  all exist in `DashboardRepository`

**Status** — `[ ] pending`

---

### Sub-Task 3 — Screen 1: FriendlyDashboardScreen (Check-in)

**Intent**
Compose screen matching the screenshot aesthetic:
- Off-white / Teal50 background
- Rounded botanical illustration box at top (🌿 emoji in sage-green rounded box — no asset)
- "Weekly Summary" heading (Teal900, large)
- Rounded card: natural-language summary sentence
- Rounded card: "How did masking feel this week?" label + 4 emoji buttons in a Row
  (😞 😐 🙂 ✨) — selected one gets a filled Teal200 circle background
- Rounded "Continue" button (Teal500 filled) — disabled + greyed out until emoji selected
- On Continue: calls `viewModel.submitFeedback()` then `onContinue(selectedRating)`

**Summary sentence logic:**
```
"This week we detected $count moments of noise.
In most of them, ${dominantPct}% masking was active.
<encouragement>"
```
Encouragement:
- pct >= 80 → "Great work protecting your calm."
- pct in 50..79 → "Good progress this week."
- else → "Every step forward counts."

**Todo List**
1. Create `ui/dashboard/friendly/FriendlyDashboardScreen.kt`
2. Composable signature: `FriendlyDashboardScreen(viewModel, onContinue: (rating: Int) -> Unit)`
3. Full-screen `Column` with `Teal50` background, verticalArrangement spacedBy, center-aligned
4. Botanical box: `Box` 160×120dp, `Teal100` background, `RoundedCornerShape(20.dp)`, 🌿 centered
5. Summary card: `Card(Teal50/white surface)` with generated sentence
6. Emoji row: 4 `IconButton`-style boxes, selected = `Teal200` circle background
7. Continue `Button`: enabled only when `state.selectedRating != null`

**Relevant Context**
- Color tokens: `Teal50`, `Teal100`, `Teal200`, `Teal500`, `Teal900` in `Color.kt`
- `SharedComponents.kt` — reuse nothing specific, but follow same card/padding conventions

**Status** — `[ ] pending`

---

### Sub-Task 4 — Screen 2: AdjustmentSuggestionScreen

**Intent**
Compose screen shown after Continue. Matches the screenshot:
- Back arrow top-left (`←`) → returns to Screen 1
- "Adjustment Suggestion" heading
- Subtitle: "Based on your feedback from last week, it seems we can try a slight reduction."
- Dashed-border rounded card containing:
  - Sliders icon (use `Icons.Filled.Tune`)
  - **"Automatic Volume Update"** bold title
  - Body copy: "Would you like us to slightly lower the overall masking volume for next week?
    The process is gradual, and you can always change it back in settings."
  - Two labelled progress bars: "Current Volume" (full width) + "Recommended (New)" (~90% width)
    both in Teal500 — showing the ~10% reduction visually
  - Two buttons: filled **"Apply"** (Teal500) + outlined **"Keep as is"** (Teal700 border)
- If rating was 1–2: replace card with a warm supportive message card only, no suggestion shown.

**Logic:** screen receives `rating: Int` as a parameter.
- rating >= 3 → show the adjustment suggestion card
- rating <= 2 → show supportive message: "Thank you for sharing. Keep going at your own pace."

**Todo List**
1. Create `ui/dashboard/friendly/AdjustmentSuggestionScreen.kt`
2. Composable signature: `AdjustmentSuggestionScreen(rating: Int, onBack: () -> Unit, onApply: () -> Unit, onKeep: () -> Unit)`
3. Back arrow: `IconButton` with `Icons.Filled.ArrowBack` top-left
4. Dashed border card: use `Modifier.border(width=1.dp, color=Teal300, shape=RoundedCornerShape(16.dp))`
5. Progress bars: reuse `LinearProgressBar` from `SharedComponents.kt`
6. "Apply" + "Keep as is" buttons both call their respective callbacks then navigate home

**Relevant Context**
- `SharedComponents.kt` — `LinearProgressBar` reusable
- `Icons.Filled.Tune` — available in material-icons-extended (already in deps)
- `Icons.Filled.ArrowBack` — in material-icons-core

**Status** — `[ ] pending`

---

### Sub-Task 5 — Wire into MainActivity

**Intent**
Connect both screens to `MainActivity` with minimal state management.

**Expected Outcomes**
- Home screen has two buttons stacked:
  1. **"How was your week?"** (new, above)
  2. **"Open Dashboard"** (existing, below)
- Tapping "How was your week?" → `FriendlyDashboardScreen`
- Continue on Screen 1 → `AdjustmentSuggestionScreen`
- Apply / Keep as is / Back all navigate back to home
- The analytics dashboard flow is unchanged

**Todo List**
1. Add `var screen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }` replacing the
   two separate boolean flags (`showDashboard`, could add `showFriendly`)
2. Define a simple sealed class `AppScreen` with: `Home`, `Analytics`, `FriendlyCheckin`,
   `FriendlyAdjustment(rating: Int)`
3. Pass `WeeklyRatingStore` and `repository` into `FriendlyDashboardViewModel.Factory`
4. Add the "How was your week?" button above "Open Dashboard" in `MainScreen`

**Relevant Context**
- `MainActivity.kt` — `MainScreen` composable, current boolean nav state

**Status** — `[ ] pending`

---

## Design Decisions

| Decision | Choice |
|---|---|
| Where is rating stored? | `SharedPreferences` — NOT `event_feedback` (that table requires a valid `event_id` FK) |
| Rating scale | 1–4 mapped to emojis: 😞=1 😐=2 🙂=3 ✨=4 |
| Continue enabled? | Only after emoji selected |
| Rating 3–4 after Continue | Show adjustment suggestion card (10% reduction) |
| Rating 1–2 after Continue | Show supportive message only, no volume suggestion |
| Botanical illustration | 🌿 emoji in a Teal100 rounded box — no drawable asset |
| Analytics dashboard | Completely untouched |
| Button order on home | "How was your week?" above "Open Dashboard" |
