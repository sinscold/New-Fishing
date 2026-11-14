# CLAUDE.md — мод на рыбалку (NeoForge 1.21.1)

Этот файл читает Claude Code в начале каждой сессии. Держи его актуальным: поменялось решение — поправь здесь.

## О проекте

Мод для Minecraft переделывает рыбалку:
- большая удочка (размером со спиннинг) и пиксельная леска;
- модификации удочки: обычный крючок, незеритовый крючок, незеритовая леска;
- лут в воде зависит от биома поплавка и биома игрока;
- рыбалка в лаве, только в Незере;
- новые рыбы: Лавакарп и Обсидиановый сом;
- зелье из Лавакарпа: огнестойкость + поджог цели при любом ударе;
- водяной и лавовый кейсы с лутом.

Полный дизайн, таблица биомов и roadmap лежат в документе «Мод «Рыбалка» — NeoForge 1.21.1: идеи и Roadmap» (Claude Docs). Если код расходится с документом, сначала спроси, что правильно.

## Стек

- Minecraft **1.21.1**, NeoForge **21.1.x**, ModDevGradle (MDK `NeoForgeMDKs/MDK-1.21.1-ModDevGradle`)
- Java **21**
- IntelliJ IDEA + плагин Minecraft Development
- `mod_id`: **newfishing**
- Базовый пакет: **com.sinscold.newfishing**

## Команды

```
gradlew runClient     # запустить игру с модом
gradlew runServer     # выделенный сервер (проверка, что клиентский код не грузится на сервере)
gradlew runData       # data generation, если подключим datagen
gradlew build         # собрать .jar в build/libs/
```

На Windows: `gradlew.bat ...` или `.\gradlew ...`.

## Структура

```
src/main/java/com/sinscold/newfishing/
  NewFishing.java          # главный класс с @Mod
  registry/               # ModItems, ModEntities, ModEffects, ModPotions, ModDataComponents, ModLootConditions, ModLootModifiers
  item/                   # классы предметов: удочка, кейсы, сом
  entity/                 # лавовый поплавок
  effect/                 # эффект «огненный удар»
  loot/                   # свои условия лута и Global Loot Modifiers
  client/                 # всё клиентское: рендерер поплавка/лески, item properties
  event/                  # подписчики на события
src/main/resources/
  assets/newfishing/
    lang/ru_ru.json, en_us.json
    models/item/
    textures/item/, textures/entity/
  data/newfishing/
    loot_table/           # ВАЖНО: в 1.21 папки в единственном числе
    recipe/
  data/neoforge/loot_modifiers/global_loot_modifiers.json
```

## Правила кода

- Всё регистрируется через `DeferredRegister` (`DeferredRegister.Items`, `DeferredRegister.create(Registries.X, MODID)`), на мод-шине в конструкторе главного класса.
- Идентификаторы: `ResourceLocation.fromNamespaceAndPath(MODID, "name")`. Конструктор `new ResourceLocation(...)` в 1.21 убран.
- Данные на предметах хранятся в **Data Components** (`DataComponentType`), не в NBT. Модификации удочки (крючок, леска) — это свой компонент.
- Клиентский код (рендеры, `ItemProperties`, `EntityRenderersEvent`) держи только в `client/` и регистрируй через `@EventBusSubscriber(value = Dist.CLIENT)`. Иначе выделенный сервер упадёт.
- Логику лута по возможности выносим в JSON: loot tables, `minecraft:location_check` для биома поплавка, своё условие для биома игрока, GLM для вмешательства в ванильную `minecraft:gameplay/fishing`.
- Пути в данных пишутся в единственном числе: `loot_table`, `recipe`, `tags/item`. Старые гайды с `loot_tables`/`recipes` для 1.21 не подходят.
- Любой новый предмет требует: регистрацию, модель, текстуру, строку в `ru_ru.json` и `en_us.json`, добавление во вкладку креатива.
- Имена в коде на английском (`lavacarp`, `obsidian_catfish`, `netherite_hook`, `netherite_line`, `water_crate`, `lava_crate`), в `ru_ru.json` — русские названия.

## Ключевые решения

- Лавовая рыбалка работает только в измерении `Level.NETHER` и только с незеритовым крючком **и** незеритовой леской.
- Лавовый поплавок — своя сущность по образцу ванильного `FishingHook`: проверка `FluidTags.LAVA`, `fireImmune()`.
- Биом поплавка = точка `ORIGIN` в контексте лута рыбалки. Биом игрока берём через владельца поплавка (`THIS_ENTITY` → `getPlayerOwner()`).
- Пиксельная леска — последний этап (только визуал, сложный рендер).

## Открытые вопросы (не реализовывать наугад, спросить)

- Модификации: это детали на одной удочке или отдельная «незеритовая удочка»?
- Зачем обычный крючок, если ванильная удочка и так ловит в воде?
- Рецепты крючков и лески.
- Что лежит в «сокровище» водяного кейса и как часто кейсы ловятся.
- Длительность зелья, его усиленные версии, время поджога.
- Филе сома: еда, жарится или ингредиент?

## Работа вдвоём

- Перед работой всегда `git pull`.
- Маленькие коммиты с понятным сообщением на русском: `Этап 1: добавлены рыбы и кейсы`.
- Не коммитить `build/`, `run/`, `.gradle/`, `.idea/` (они уже в `.gitignore`).
- Перед push проверить, что `gradlew runClient` запускается без ошибок.
- Баги заводим в GitHub Issues: что делал, что ожидал, что вышло, кусок `run/logs/latest.log`.

## Как проверять работу

Claude не может запустить Minecraft. После каждого изменения:
1. Человек запускает `gradlew runClient`.
2. Проверяет по чеклисту этапа из roadmap.
3. Если упало, присылает ошибку из консоли или `run/logs/latest.log`.

## Текущий этап

Этап 0 готов: MDK залит, репозиторий синхронизирован.
Следующий шаг — **этап 1: предметы** (рыбы, крючки, леска, филе, кейсы; текстуры и перевод).
