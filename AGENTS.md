# AGENTS.md

Android food-ordering and admin-management app (Vietnamese university course project). Single module `:app`, package `com.example.chieuthu3_ketthucmonhoc`. Persistence is **SQLite via Room** (`restaurant.db`, constraints FK/UNIQUE enforced by the database; seeded from `InitialDataFactory` on first run). No network. The cart is session-scoped in-memory by design — it resets on every app restart; menu/orders/users/admins persist.

## Build & Test (Gradle 9.5.0 / AGP 9.3.3, Windows host — use `./gradlew` in Git Bash or `gradlew.bat`)

```bash
./gradlew assembleDebug                 # build debug APK
./gradlew testDebugUnitTest             # run unit tests (JUnit 4)
./gradlew testDebugUnitTest --tests "com.example.chieuthu3_ketthucmonhoc.services.CartServiceTest"  # one class
./gradlew connectedAndroidTest          # instrumented tests (needs emulator/device)
./gradlew lint                          # Android lint
```

Dependencies go through the version catalog `gradle/libs.versions.toml` — add versions/libraries there, not as inline coordinates. compileSdk/targetSdk 35, minSdk 24, Java 11.

## Architecture (layered, interface-driven)

```
app/src/main/java/com/example/chieuthu3_ketthucmonhoc/
├── App.java                              # Application: AppServiceLocator.init(this) before any Activity
├── IntroActivity.java                    # Launch / Splash screen intro
├── MainActivity.java / AdminActivity.java # Core Activities (Main & Admin management)
├── adapters/                            # RecyclerView adapters
├── di/AppServiceLocator.java            # manual DI: lazy singletons wiring everything
├── domain/models/                       # data classes & enums (MenuItem, Order, CartItem, ...)
├── domain/services/                     # I*Service interfaces + *ServiceImpl
├── data/repository/                     # I*Repository interfaces + in-memory impls (unit-test doubles)
├── data/database/                       # Room: AppDatabase, entities/, dao/, DatabaseSeeder, Room*Repository (production)
└── data/datasource/InitialDataFactory.java # all seed data (menu, orders, users, admins)
```

Layer rules:
- Activities and adapters get dependencies **only** from `AppServiceLocator`, typed as the `I*Service` interface — never instantiate a `*ServiceImpl`, repository, or DAO directly.
- Production repositories are `Room*Repository` (SQLite, in `data/database/`); the in-memory `*RepositoryImpl` classes in `data/repository/` exist for unit tests — tests construct them directly with `InitialDataFactory` data. Only `AppServiceLocator` wires the Room ones.
- Services depend on `I*Repository` interfaces; they never touch `AppDatabase`/DAOs.
- New seed data goes in `InitialDataFactory`; `DatabaseSeeder.seedIfEmpty` inserts it on first launch inside one transaction — seed data must satisfy the schema constraints (FK `order_items→orders/ menu_items`, `UNIQUE` phone/email/username, one menu item per order line).

## Conventions

- UI is View-based with **ViewBinding** (`buildFeatures.viewBinding = true`), not Compose. Follow the existing pattern: `binding = ActivityMainBinding.inflate(layoutInflater)` in `onCreate`; dialogs/bottom sheets inflate their own `Dialog*Binding`.
- Naming: interfaces prefixed `I` (`ICartService`), implementations suffixed `Impl` (`CartServiceImpl`).
- User-facing strings are Vietnamese; keep `values/` and `values-night/` in sync when touching themes.
- Edge-to-edge is enabled via `enableEdgeToEdge()`; inset handling uses `ViewCompat`/`WindowInsetsCompat`.
- Unit tests: one test class per service in `app/src/test/.../services/` (services are pure Java, no Android deps — keep them that way so they stay unit-testable).

## Gotchas

- `App.onCreate` must call `AppServiceLocator.init(this)` before any Activity touches a service (property initializers on Activities run during construction). Room uses `allowMainThreadQueries()` because the `I*Repository` contract is synchronous.
- `deleteMenuItem` on a dish already referenced by an order hits FK `RESTRICT` and falls back to a soft delete (`isAvailable = false`) — order history is never silently broken.
- DAO queries must use the **DB column names** (`order_id`, `customer_id`, `created_at`), not Java field names; Room validates SQL at compile time via annotation processor.
- The README's "Cấu trúc mã nguồn" section predates the OOP refactor (commit a2cb0db) — trust the tree above, not the README.
- `*.docx` and `*.drawio` (course report, use-case diagram in repo root) and `local.properties` are gitignored; they exist locally but must not be committed.
- Release build has `isMinifyEnabled = false`; if enabling, review `proguard-rules.pro` first (keep Room generated `*Impl` classes).
