# Fix SerializationException for MainDashboardRoute

The application is failing with a `SerializationException` because the `kotlinx.serialization` compiler plugin is not applied to the `:app:shared` module, where `MainDashboardRoute` is defined. Additionally, `MainDashboardRoute` has a redundant double `@Serializable` annotation.

## User Review Required

> [!NOTE]
> I am adding the `kotlinx.serialization` plugin and its JSON library dependency to the `:app:shared` module. This is required for type-safe Compose Navigation routes defined in this module.

## Proposed Changes

### app/shared

#### [MODIFY] [App.kt](file:///Users/aalsamman/AndroidProjects/FamilyChore/app/shared/src/commonMain/kotlin/org/aals/family/chore/App.kt)
- Remove redundant `@Serializable` annotation from `MainDashboardRoute`.

#### [MODIFY] [build.gradle.kts](file:///Users/aalsamman/AndroidProjects/FamilyChore/app/shared/build.gradle.kts)
- Apply the `kotlinSerialization` plugin.
- Add `kotlinx-serialization-json` dependency to `commonMain`.

## Verification Plan

### Automated Tests
- Run `:app:shared:assemble` to verify compilation.
- I will attempt to build the shared module to ensure the plugin is correctly applied and the route is now serializable.

### Manual Verification
- The user should run the application and verify that the `SerializationException` is resolved when navigating to or starting with `MainDashboardRoute`.
