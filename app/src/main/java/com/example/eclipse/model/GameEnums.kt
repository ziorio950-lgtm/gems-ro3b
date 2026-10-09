package com.example.eclipse.model

enum class Chapter(
    val id: Int,
    val titleEn: String,
    val titleAr: String,
    val subtitleEn: String,
    val subtitleAr: String
) {
    DEAD_CITY(
        1,
        "Dead City",
        "المدينة المهجورة",
        "Rain, fog, and the first shadow",
        "مطر وضباب وأول ظهور للظل"
    ),
    BLACK_HOSPITAL(
        2,
        "Black Hospital",
        "المستشفى الأسود",
        "Surgical wards and psychological dread",
        "أجنحة الجراحة والرعب النفسي"
    ),
    ENDLESS_FOREST(
        3,
        "The Endless Forest",
        "الغابة اللانهائية",
        "Creatures that hunt by sound",
        "كائنات تقتات على الأصوات"
    ),
    ECLIPSE_FACILITY(
        4,
        "Eclipse Facility",
        "منشأة الكسوف",
        "The secret origin of human fear",
        "الأصل السري لتجارب الخوف البشري"
    ),
    INVERTED_WORLD(
        5,
        "The Inverted World",
        "العالم المعكوس",
        "Impossible geometry and final truth",
        "هندسة مستحيلة والمواجهة النهائية"
    )
}

enum class MonsterType {
    THE_OBSERVER,    // Primary stalker: intelligent, adapts to habits
    SOUND_HUNTER,    // Blind, ultra-fast when hearing loud steps/sprinting
    THE_LURKER,      // Static weeping entity, activates if stared at or approached
    THE_MIMIC,       // Emits deceptive audio/distortions
    FACILITY_SENTINEL // Heavy armored patrol with security beam
}

enum class MonsterState {
    IDLE,
    PATROL,
    INVESTIGATING_SOUND,
    CHASING_PLAYER,
    SEARCHING_LAST_SEEN,
    RETURNING_TO_PATROL
}

enum class ItemType {
    FLASHLIGHT_BATTERY,
    BRASS_KEY,
    SURGICAL_KEYCARD,
    FACILITY_PASSCODE,
    VALVE_WRENCH,
    ELECTRICAL_FUSE,
    SYRINGE_MEDKIT,
    AUDIO_CASSETTE,
    RESEARCH_NOTE
}

enum class Language {
    ARABIC,
    ENGLISH
}

enum class GraphicQuality {
    LOW,
    MEDIUM,
    HIGH
}

enum class GameScreenState {
    MAIN_MENU,
    PLAYING,
    PAUSED,
    INVENTORY,
    READING_NOTE,
    SOLVING_PUZZLE,
    GAME_OVER,
    CHAPTER_COMPLETE,
    SETTINGS,
    CHAPTER_SELECT,
    LORE_ARCHIVE,
    CREDITS
}
