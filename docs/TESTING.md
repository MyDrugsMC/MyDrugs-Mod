# Testing and Validation

Use the narrowest meaningful check first. Do not claim success unless the command actually ran.

## Command ladder

```bash
./gradlew compileJava
./gradlew test
./gradlew validateCodeContracts
./gradlew validateResources
./gradlew check
./gradlew runData
./gradlew build
```

Recommended order:

1. `compileJava` after Java edits.
2. `test` after pure logic or JVM-testable domain changes.
3. `validateCodeContracts` after architecture-sensitive changes.
4. `validateResources` after JSON/lang/model/guide/resource changes.
5. `runData` after changes to datagen providers.
6. `build` before calling a broad change done.

## What each command proves

| Command | Proves | Does not prove |
|---|---|---|
| `compileJava` | main sources compile | tests, resources, gameplay correctness |
| `test` | JUnit 5 source tests pass | Minecraft runtime/in-game behavior |
| `validateCodeContracts` | custom architecture contracts pass | visual correctness or balance |
| `validateResources` | resource/lang/guide/model contracts pass | all assets are artistically final |
| `runData` | generated resources can be regenerated | generated output was reviewed |
| `build` | broad Gradle build + resource validation | manual gameplay quality |

## Dedicated-server safety check

Run after touching client/common boundaries:

```bash
rg -n "import net\.minecraft\.client|import net\.neoforged\.neoforge\.client|import org\.mydrugs\.mydrugs\.client" src/main/java/org/mydrugs/mydrugs --glob '!client/**'
```

Expected result: no matches in common/server packages.

## Useful source checks

```bash
rg -n "Component\.literal\(" src/main/java/org/mydrugs/mydrugs
rg -n "\.ordinal\(|ByteBufCodecs\.STRING_UTF8|playToServer|registerPayload" src/main/java/org/mydrugs/mydrugs
rg -n "@EventBusSubscriber\(modid = MyDrugs\.MODID\)" src/main/java/org/mydrugs/mydrugs
rg -n "AddictionManager\.consume\(|DrugKnowledge\.markConsumed\(" src/main/java/org/mydrugs/mydrugs --glob '!core/drug/use/DrugUseService.java'
```

## Validator notes

`validateCodeContracts` checks include:

- LF line endings for `gradlew`;
- duplicate and missing lang keys;
- MachineStatus translation coverage;
- client-only imports outside client packages;
- client event subscribers using `Dist.CLIENT`;
- progression gate contracts;
- canonical `DrugUseService` consumption;
- pipe dedup/loading/performance contracts;
- runtime drug effect lifecycle API;
- external-tool/disclaimer safety.

`validateResources` checks include:

- JSON UTF-8 without BOM;
- accidental backup/checkpoint files;
- generated cache gitignore rule;
- model/texture references;
- lang coverage;
- guide `@item` references;
- asset TODO output.

## Gradle wrapper rule

If `./gradlew` is missing, not executable, or has CRLF line endings, fix/report that before claiming any Gradle command can run reliably.
