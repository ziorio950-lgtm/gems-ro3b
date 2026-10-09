package com.example.eclipse.gameplay

import com.example.eclipse.model.Chapter
import com.example.eclipse.model.GameObjective
import com.example.eclipse.model.HidingSpot
import com.example.eclipse.model.ItemType
import com.example.eclipse.model.LoreNote
import com.example.eclipse.model.Monster
import com.example.eclipse.model.MonsterState
import com.example.eclipse.model.MonsterType
import com.example.eclipse.model.WorldDoor
import com.example.eclipse.model.WorldPickup

data class WorldProp(
    val id: String,
    val type: String, // "hospital_bed", "curtain", "supply_cart", "exit_sign", "blood_stain"
    val x: Float,
    val y: Float
)

data class ChapterLevelData(
    val chapter: Chapter,
    val map: Array<IntArray>,
    val playerStartX: Float,
    val playerStartY: Float,
    val playerStartAngle: Float,
    val doors: List<WorldDoor>,
    val pickups: List<WorldPickup>,
    val hidingSpots: List<HidingSpot>,
    val monsters: List<Monster>,
    val loreNotes: List<LoreNote>,
    val objectives: List<GameObjective>,
    val exitTriggerX: Float,
    val exitTriggerY: Float,
    val puzzleTerminalX: Float? = null,
    val puzzleTerminalY: Float? = null,
    val props: List<WorldProp> = emptyList()
)

object MapDefinitions {

    fun getLevelData(chapter: Chapter): ChapterLevelData {
        return when (chapter) {
            Chapter.DEAD_CITY -> createDeadCity()
            Chapter.BLACK_HOSPITAL -> createBlackHospital()
            Chapter.ENDLESS_FOREST -> createEndlessForest()
            Chapter.ECLIPSE_FACILITY -> createEclipseFacility()
            Chapter.INVERTED_WORLD -> createInvertedWorld()
        }
    }

    // -------------------------------------------------------------
    // CHAPTER 1: DEAD CITY (المدينة المهجورة)
    // -------------------------------------------------------------
    private fun createDeadCity(): ChapterLevelData {
        // 16x16 grid. 1 = Concrete/Brick wall, 0 = Empty street/alley, 6 = Gate
        val map = arrayOf(
            intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            intArrayOf(1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1),
            intArrayOf(1, 0, 1, 0, 1, 0, 1, 1, 1, 0, 1, 0, 1, 1, 0, 1),
            intArrayOf(1, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1),
            intArrayOf(1, 0, 1, 1, 1, 0, 1, 0, 1, 1, 1, 1, 0, 1, 0, 1),
            intArrayOf(1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 1),
            intArrayOf(1, 1, 1, 0, 1, 1, 1, 0, 1, 0, 1, 1, 1, 1, 0, 1),
            intArrayOf(1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1),
            intArrayOf(1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 0, 1, 1, 0, 1),
            intArrayOf(1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1, 0, 0, 1),
            intArrayOf(1, 0, 1, 0, 1, 1, 1, 0, 1, 0, 0, 0, 1, 0, 1, 1),
            intArrayOf(1, 0, 0, 0, 1, 0, 0, 0, 1, 1, 1, 0, 1, 0, 0, 1),
            intArrayOf(1, 1, 1, 0, 1, 0, 1, 0, 0, 0, 1, 0, 1, 1, 0, 1),
            intArrayOf(1, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 1),
            intArrayOf(1, 0, 1, 1, 1, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 1),
            intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
        )

        val doors = listOf(
            WorldDoor(id = "gate_subway", x = 14, y = 14, isLocked = true, requiredItem = ItemType.BRASS_KEY)
        )

        val pickups = listOf(
            WorldPickup(
                id = "p_key1",
                type = ItemType.BRASS_KEY,
                x = 1.5f,
                y = 13.5f,
                titleEn = "Subway Iron Key",
                titleAr = "مفتاح بوابة المترو",
                descriptionEn = "Rusted heavy iron key found near the overturned ambulance.",
                descriptionAr = "مفتاح حديدي صدئ وجد بجوار سيارة الإسعاف المنقلبة."
            ),
            WorldPickup(
                id = "p_bat1",
                type = ItemType.FLASHLIGHT_BATTERY,
                x = 9.5f,
                y = 3.5f,
                titleEn = "Lithium Battery",
                titleAr = "بطارية ليثيوم",
                descriptionEn = "Charges flashlight to 100%.",
                descriptionAr = "تعيد شحن المصباح اليدوي بالكامل."
            ),
            WorldPickup(
                id = "p_med1",
                type = ItemType.SYRINGE_MEDKIT,
                x = 5.5f,
                y = 1.5f,
                titleEn = "Adrenaline Syringe",
                titleAr = "حقنة أدرينالين",
                descriptionEn = "Restores health and steadies breathing.",
                descriptionAr = "تستعيد الصحة وتهدئ سرعة نبضات القلب."
            )
        )

        val hidingSpots = listOf(
            HidingSpot("h_car1", 1.5f, 3.5f, "Abandoned Patrol Car", "سيارة دورية مهجورة"),
            HidingSpot("h_locker1", 7.5f, 9.5f, "Rusty Storage Locker", "خزانة معدنية صدئة"),
            HidingSpot("h_dumpster1", 11.5f, 5.5f, "Dark Alley Waste Bin", "حاوية نفايات مظلمة")
        )

        val monsters = listOf(
            Monster(
                id = "m_observer_ch1",
                type = MonsterType.THE_OBSERVER,
                x = 13.5f,
                y = 1.5f,
                speed = 1.1f,
                patrolPath = listOf(
                    Pair(13.5f, 1.5f),
                    Pair(13.5f, 9.5f),
                    Pair(9.5f, 9.5f),
                    Pair(9.5f, 3.5f),
                    Pair(5.5f, 3.5f),
                    Pair(5.5f, 11.5f)
                )
            )
        )

        val notes = listOf(
            LoreNote(
                id = "note_ch1_1",
                chapter = Chapter.DEAD_CITY,
                titleEn = "Officer's Wet Notepad",
                titleAr = "دفتر الشرطي المبلل",
                dateOrSource = "23:45 - Dead City Police",
                contentEn = "The fog rolled in with the storm. People started vanishing from their cars. When I looked up at the water tower, something impossibly tall was standing there, staring down without blinking...",
                contentAr = "اجتاح الضباب المدينة مع العاصفة. بدأ السكان يختفون من سياراتهم فجأة. عندما نظرت نحو برج المياه، رأيت كائناً فارع الطول يقف متجمداً يراقبنا دون أن يرمش..."
            ),
            LoreNote(
                id = "note_ch1_2",
                chapter = Chapter.DEAD_CITY,
                titleEn = "Emergency Broadcast Transcript",
                titleAr = "تسجيل بث الطوارئ",
                dateOrSource = "Radio Tower Log",
                contentEn = "ALL CITIZENS: Stay indoors. Do NOT shine lights into the dark. If you hear rhythmic breathing behind you, do not run. Find shelter and keep silent.",
                contentAr = "إلى جميع المواطنين: الزموا منازلكم. لا توجهوا مصابيحكم نحو الظلام. إذا سمعتم تنفساً إيقاعياً خلفكم، لا تركضوا. اختبئوا والتزموا الصمت التام.",
                isAudioTape = true
            )
        )

        val objectives = listOf(
            GameObjective("obj_ch1_1", "Explore the street and find a flashlight battery", "استكشف الشارع واعثر على بطارية للمصباح"),
            GameObjective("obj_ch1_2", "Retrieve the Subway Iron Key from the west crash", "اعثر على مفتاح بوابة المترو من موقع الحادث"),
            GameObjective("obj_ch1_3", "Avoid The Observer and unlock the Subway Entrance Gate", "تجنب الوحش المراقب وافتح بوابة المترو للهروب")
        )

        return ChapterLevelData(
            chapter = Chapter.DEAD_CITY,
            map = map,
            playerStartX = 1.5f,
            playerStartY = 1.5f,
            playerStartAngle = 0f,
            doors = doors,
            pickups = pickups,
            hidingSpots = hidingSpots,
            monsters = monsters,
            loreNotes = notes,
            objectives = objectives,
            exitTriggerX = 14f,
            exitTriggerY = 14f
        )
    }

    // -------------------------------------------------------------
    // CHAPTER 2: BLACK HOSPITAL (المستشفى الأسود)
    // -------------------------------------------------------------
    private fun createBlackHospital(): ChapterLevelData {
        val map = arrayOf(
            intArrayOf(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2),
            intArrayOf(2, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 2),
            intArrayOf(2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 2, 2, 2, 0, 2),
            intArrayOf(2, 0, 2, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 2, 0, 2),
            intArrayOf(2, 0, 2, 2, 2, 0, 2, 2, 2, 0, 2, 0, 0, 2, 0, 2),
            intArrayOf(2, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 2, 0, 2),
            intArrayOf(2, 2, 2, 0, 2, 2, 2, 0, 2, 2, 2, 2, 0, 2, 0, 2),
            intArrayOf(2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 2),
            intArrayOf(2, 0, 2, 2, 2, 0, 2, 2, 2, 2, 0, 2, 2, 2, 0, 2),
            intArrayOf(2, 0, 2, 0, 0, 0, 2, 0, 0, 2, 0, 0, 0, 2, 0, 2),
            intArrayOf(2, 0, 2, 0, 2, 2, 2, 0, 0, 2, 2, 2, 0, 2, 0, 2),
            intArrayOf(2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 2, 0, 2, 0, 2),
            intArrayOf(2, 2, 2, 0, 2, 0, 2, 2, 2, 2, 0, 2, 0, 0, 0, 2),
            intArrayOf(2, 0, 0, 0, 0, 0, 2, 0, 0, 2, 0, 2, 2, 2, 0, 2),
            intArrayOf(2, 0, 2, 2, 2, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 2),
            intArrayOf(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2)
        )

        val doors = listOf(
            WorldDoor("door_surgery", 14, 14, isLocked = true, requiredItem = ItemType.SURGICAL_KEYCARD)
        )

        val pickups = listOf(
            WorldPickup("p_card1", ItemType.SURGICAL_KEYCARD, 13.5f, 1.5f, "Chief Surgeon Keycard", "بطاقة رئيس الجراحين", "Access card for the ICU and Morgue elevator.", "بطاقة عبور للعناية المركزة ومصعد المشرحة."),
            WorldPickup("p_bat2", ItemType.FLASHLIGHT_BATTERY, 3.5f, 7.5f, "Battery Pack", "حزمة بطاريات", "Restores light power.", "تغذي المصباح بالطاقة."),
            WorldPickup("p_med2", ItemType.SYRINGE_MEDKIT, 9.5f, 9.5f, "Morphine Pack", "حزمة مورفين", "Emergency trauma aid.", "إسعاف فوري للإصابات.")
        )

        val hidingSpots = listOf(
            HidingSpot("h_morgue_locker1", 5.5f, 1.5f, "Cold Morgue Cabinet", "خزانة حفظ الجثث"),
            HidingSpot("h_hospital_screen", 7.5f, 11.5f, "Behind Medical Screen", "خلف الستار الطبي"),
            HidingSpot("h_doctor_wardrobe", 1.5f, 13.5f, "Doctor's Steel Wardrobe", "خزانة الطبيب الفولاذية")
        )

        val monsters = listOf(
            Monster(
                id = "m_lurker_ch2",
                type = MonsterType.THE_LURKER,
                x = 7.5f,
                y = 5.5f,
                speed = 1.4f,
                patrolPath = listOf(Pair(7.5f, 5.5f), Pair(1.5f, 5.5f))
            ),
            Monster(
                id = "m_observer_ch2",
                type = MonsterType.THE_OBSERVER,
                x = 11.5f,
                y = 13.5f,
                speed = 1.25f,
                patrolPath = listOf(Pair(11.5f, 13.5f), Pair(11.5f, 7.5f), Pair(5.5f, 7.5f))
            )
        )

        val notes = listOf(
            LoreNote(
                id = "note_ch2_1",
                chapter = Chapter.BLACK_HOSPITAL,
                titleEn = "Dr. Mansoor's Autopsy Log",
                titleAr = "سجل تشريح د. منصور",
                dateOrSource = "Morgue Lab B - 03:12",
                contentEn = "Subject #402 has no heartbeat, yet brainwave activity exceeds anything measured in living humans. When we attempted to cut the sternum, the lights burst, and a shadow detached from the ceiling...",
                contentAr = "الحالة #402 بلا نبض، ومع ذلك نشاط الموجات الدماغية يفوق أي مقياس بشري معروف. عندما حاولنا فتح عظم القص، انفجرت الأضواء وانفصل ظل أسود عن السقف..."
            ),
            LoreNote(
                id = "note_ch2_2",
                chapter = Chapter.BLACK_HOSPITAL,
                titleEn = "Cassette: Patient 7 Whispers",
                titleAr = "شريط: همسات المريض السابع",
                dateOrSource = "Ward Audio Recorder",
                contentEn = "Don't look at the woman crying in the corner. If you look into her face, she knows you see her. Look away... walk slowly... don't make a sound...",
                contentAr = "إياك أن تنظر إلى المرأة الباكية في الزاوية. إذا نظرت في وجهها ستعلم أنك تراها. انظر بعيداً... سر ببطء... ولا تصدر أي صوت...",
                isAudioTape = true
            )
        )

        val objectives = listOf(
            GameObjective("obj_ch2_1", "Navigate the dark corridors and avoid the weeping entity", "اعبر الممرات المظلمة وتجنب الكائن الباكي"),
            GameObjective("obj_ch2_2", "Locate the Surgical Keycard in the Operating Theatre", "اعثر على بطاقة الجراحة في غرفة العمليات"),
            GameObjective("obj_ch2_3", "Input the ICU elevator override code: 1984", "أدخل رمز تجاوز مصعد العناية المركزة: 1984")
        )

        val hospitalProps = listOf(
            WorldProp("bed_left_1", "hospital_bed", 2.2f, 1.6f),
            WorldProp("curtain_left", "curtain", 2.2f, 2.2f),
            WorldProp("cart_right_1", "supply_cart", 2.5f, 3.4f),
            WorldProp("bed_right_1", "hospital_bed", 4.2f, 3.5f),
            WorldProp("bed_corridor_1", "hospital_bed", 7.5f, 1.8f),
            WorldProp("exit_sign_deep", "exit_sign", 13.5f, 1.5f),
            WorldProp("blood_floor_1", "blood_stain", 3.0f, 1.5f)
        )

        return ChapterLevelData(
            chapter = Chapter.BLACK_HOSPITAL,
            map = map,
            playerStartX = 1.5f,
            playerStartY = 1.5f,
            playerStartAngle = 0f,
            doors = doors,
            pickups = pickups,
            hidingSpots = hidingSpots,
            monsters = monsters,
            loreNotes = notes,
            objectives = objectives,
            exitTriggerX = 14f,
            exitTriggerY = 14f,
            puzzleTerminalX = 14f,
            puzzleTerminalY = 13f,
            props = hospitalProps
        )
    }

    // -------------------------------------------------------------
    // CHAPTER 3: THE ENDLESS FOREST (الغابة اللانهائية)
    // -------------------------------------------------------------
    private fun createEndlessForest(): ChapterLevelData {
        val map = arrayOf(
            intArrayOf(3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3),
            intArrayOf(3, 0, 0, 0, 3, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 3),
            intArrayOf(3, 0, 3, 0, 0, 0, 3, 3, 0, 0, 3, 0, 3, 3, 0, 3),
            intArrayOf(3, 0, 3, 3, 0, 0, 0, 3, 0, 0, 0, 0, 0, 3, 0, 3),
            intArrayOf(3, 0, 0, 3, 0, 3, 0, 3, 3, 3, 0, 3, 0, 3, 0, 3),
            intArrayOf(3, 3, 0, 0, 0, 3, 0, 0, 0, 3, 0, 3, 0, 0, 0, 3),
            intArrayOf(3, 0, 0, 3, 3, 3, 0, 3, 0, 3, 0, 3, 3, 3, 0, 3),
            intArrayOf(3, 0, 3, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 3, 0, 3),
            intArrayOf(3, 0, 3, 0, 3, 3, 0, 3, 3, 3, 3, 3, 0, 3, 0, 3),
            intArrayOf(3, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 3),
            intArrayOf(3, 3, 3, 0, 3, 0, 3, 3, 3, 3, 0, 3, 3, 3, 0, 3),
            intArrayOf(3, 0, 0, 0, 0, 0, 3, 0, 0, 3, 0, 0, 0, 3, 0, 3),
            intArrayOf(3, 0, 3, 3, 3, 0, 3, 0, 0, 3, 3, 3, 0, 3, 0, 3),
            intArrayOf(3, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 3),
            intArrayOf(3, 3, 0, 0, 0, 0, 3, 3, 0, 0, 0, 0, 0, 3, 0, 3),
            intArrayOf(3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3)
        )

        val doors = listOf(
            WorldDoor("gate_bunker", 14, 14, isLocked = true, requiredItem = ItemType.VALVE_WRENCH)
        )

        val pickups = listOf(
            WorldPickup("p_valve1", ItemType.VALVE_WRENCH, 1.5f, 13.5f, "Heavy Valve Wrench", "مفتاح الصمامات الثقيل", "Opens the underground bunker water intake seal.", "يفتح صمام مأخذ المياه لملجأ المنشأة السري."),
            WorldPickup("p_bat3", ItemType.FLASHLIGHT_BATTERY, 7.5f, 1.5f, "Hunter's Spare Cell", "بطارية الصياد الاحتياطية", "Fresh battery found in tree hollow.", "بطارية وجِدت داخل تجويف شجرة."),
            WorldPickup("p_med3", ItemType.SYRINGE_MEDKIT, 9.5f, 7.5f, "Forest First Aid", "إسعافات الغابة", "Herbal coagulant and bandage.", "ضمادات ومسكنات قوية.")
        )

        val hidingSpots = listOf(
            HidingSpot("h_cabin1", 3.5f, 1.5f, "Rotted Hunter's Cabin", "كوخ الصياد المتهالك"),
            HidingSpot("h_hollow_trunk", 7.5f, 9.5f, "Giant Hollow Oak Trunk", "جذع شجرة بلوط مجوف"),
            HidingSpot("h_trench", 13.5f, 5.5f, "Overgrown Drainage Trench", "خندق تصريف مغطى بالنباتات")
        )

        val monsters = listOf(
            Monster(
                id = "m_hunter_ch3",
                type = MonsterType.SOUND_HUNTER,
                x = 13.5f,
                y = 1.5f,
                speed = 1.6f,
                patrolPath = listOf(Pair(13.5f, 1.5f), Pair(9.5f, 3.5f), Pair(9.5f, 9.5f), Pair(1.5f, 9.5f))
            )
        )

        val notes = listOf(
            LoreNote(
                id = "note_ch3_1",
                chapter = Chapter.ENDLESS_FOREST,
                titleEn = "Lost Camper's Bloodstained Journal",
                titleAr = "يوميات المخيم المفقود الملطخة بالدماء",
                dateOrSource = "Found pinned to a pine",
                contentEn = "The creatures in the pines have no eyes. When I accidentally snapped a twig, it screamed and cleared fifty yards in two seconds. Walk! Crouch! Never run through the dead brush!",
                contentAr = "المخلوقات بين أشجار الصنوبر بلا أعين. عندما كسرت غصناً بقدمي دون قصد، أطلقت صرخة وقطعت خمسين ياردة في ثانيتين. امشِ ببطء! انحنِ! إياك أن تركض وسط الأغصان اليابسة!"
            )
        )

        val objectives = listOf(
            GameObjective("obj_ch3_1", "Move quietly: The Sound Hunter dashes when you sprint", "تحرك بهدوء: صياد الأصوات يندفع نحوك إذا ركضت"),
            GameObjective("obj_ch3_2", "Find the Heavy Valve Wrench in the Old Hunter's Hideout", "اعثر على مفتاح الصمامات في مخبأ الصياد القديم"),
            GameObjective("obj_ch3_3", "Drain the flood gate to enter the Subterranean Bunker", "قم بتفريغ صمام بوابة المياه لدخول الملجأ السري")
        )

        return ChapterLevelData(
            chapter = Chapter.ENDLESS_FOREST,
            map = map,
            playerStartX = 1.5f,
            playerStartY = 1.5f,
            playerStartAngle = 0f,
            doors = doors,
            pickups = pickups,
            hidingSpots = hidingSpots,
            monsters = monsters,
            loreNotes = notes,
            objectives = objectives,
            exitTriggerX = 14f,
            exitTriggerY = 14f
        )
    }

    // -------------------------------------------------------------
    // CHAPTER 4: ECLIPSE FACILITY (منشأة الكسوف)
    // -------------------------------------------------------------
    private fun createEclipseFacility(): ChapterLevelData {
        val map = arrayOf(
            intArrayOf(4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4),
            intArrayOf(4, 0, 0, 0, 4, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 4),
            intArrayOf(4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 4, 4, 4, 0, 4),
            intArrayOf(4, 0, 4, 0, 0, 0, 4, 0, 0, 0, 4, 0, 0, 4, 0, 4),
            intArrayOf(4, 0, 4, 4, 4, 0, 4, 4, 4, 0, 4, 0, 0, 4, 0, 4),
            intArrayOf(4, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 4, 0, 4),
            intArrayOf(4, 4, 4, 0, 4, 4, 4, 0, 4, 0, 4, 4, 0, 4, 0, 4),
            intArrayOf(4, 0, 0, 0, 4, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 4),
            intArrayOf(4, 0, 4, 4, 4, 0, 4, 4, 4, 0, 4, 0, 4, 4, 0, 4),
            intArrayOf(4, 0, 4, 0, 0, 0, 4, 0, 0, 0, 4, 0, 4, 0, 0, 4),
            intArrayOf(4, 0, 4, 0, 4, 4, 4, 0, 4, 4, 4, 0, 4, 0, 4, 4),
            intArrayOf(4, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 4),
            intArrayOf(4, 4, 4, 0, 4, 0, 4, 4, 4, 4, 4, 0, 4, 4, 0, 4),
            intArrayOf(4, 0, 0, 0, 0, 0, 4, 0, 0, 0, 4, 0, 0, 0, 0, 4),
            intArrayOf(4, 0, 4, 4, 4, 0, 0, 0, 4, 0, 0, 0, 4, 4, 0, 4),
            intArrayOf(4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4)
        )

        val doors = listOf(
            WorldDoor("door_anomaly_chamber", 14, 14, isLocked = true, requiredItem = ItemType.ELECTRICAL_FUSE)
        )

        val pickups = listOf(
            WorldPickup("p_fuse1", ItemType.ELECTRICAL_FUSE, 13.5f, 1.5f, "Quantum Fuse Breaker", "فيوز طاقة كمي", "High-capacity superconducting fuse to restore containment door.", "فيوز طاقة فائق التوصيل لإعادة تشغيل بوابة العزل."),
            WorldPickup("p_bat4", ItemType.FLASHLIGHT_BATTERY, 1.5f, 11.5f, "Military Grade Battery", "بطارية بمواصفات عسكرية", "High endurance power cell.", "خلية طاقة فائقة التحمل.")
        )

        val hidingSpots = listOf(
            HidingSpot("h_server_rack", 5.5f, 1.5f, "Behind Server Cooling Tower", "خلف برج تبريد الخوادم"),
            HidingSpot("h_chem_cabinet", 1.5f, 7.5f, "Hazardous Material Cabinet", "خزانة المواد الخطرة"),
            HidingSpot("h_vent1", 9.5f, 11.5f, "Floor Maintenance Conduit", "قناة صيانة أرضية")
        )

        val monsters = listOf(
            Monster(
                id = "m_sentinel_ch4",
                type = MonsterType.FACILITY_SENTINEL,
                x = 7.5f,
                y = 7.5f,
                speed = 1.3f,
                patrolPath = listOf(Pair(7.5f, 7.5f), Pair(7.5f, 1.5f), Pair(1.5f, 1.5f))
            ),
            Monster(
                id = "m_observer_ch4",
                type = MonsterType.THE_OBSERVER,
                x = 11.5f,
                y = 11.5f,
                speed = 1.35f,
                patrolPath = listOf(Pair(11.5f, 11.5f), Pair(11.5f, 5.5f), Pair(5.5f, 5.5f))
            )
        )

        val notes = listOf(
            LoreNote(
                id = "note_ch4_1",
                chapter = Chapter.ECLIPSE_FACILITY,
                titleEn = "Project Eclipse: Classified Dossier",
                titleAr = "مشروع الكسوف: ملف سري للغاية",
                dateOrSource = "Directorate Level 5",
                contentEn = "We thought we were studying human collective fear. But fear is not an emotion — it is a resonance. When that resonance matched the cosmic frequency, the barrier between our world and the Void dissolved...",
                contentAr = "اعتقدنا أننا ندرس الخوف الجماعي البشري. لكن الخوف ليس مجرد شعور، بل تردد كوني. وحين تطابق ذلك التردد، انهار الحاجز الفاصل بين واقعنا وعالم الفراغ...",
                isAudioTape = true
            )
        )

        val objectives = listOf(
            GameObjective("obj_ch4_1", "Infiltrate Central Research Core", "تسلل إلى قلب مركز الأبحاث المركزي"),
            GameObjective("obj_ch4_2", "Retrieve the Superconducting Fuse from Lab Gamma", "استعد فيوز الطاقة من مختبر جاما"),
            GameObjective("obj_ch4_3", "Restore breaker circuits to unlock the Anomaly Gateway", "أصلح دوائر القواطع لفتح بوابة الشذوذ الكوني")
        )

        return ChapterLevelData(
            chapter = Chapter.ECLIPSE_FACILITY,
            map = map,
            playerStartX = 1.5f,
            playerStartY = 1.5f,
            playerStartAngle = 0f,
            doors = doors,
            pickups = pickups,
            hidingSpots = hidingSpots,
            monsters = monsters,
            loreNotes = notes,
            objectives = objectives,
            exitTriggerX = 14f,
            exitTriggerY = 14f,
            puzzleTerminalX = 13f,
            puzzleTerminalY = 14f
        )
    }

    // -------------------------------------------------------------
    // CHAPTER 5: THE INVERTED WORLD (العالم المعكوس)
    // -------------------------------------------------------------
    private fun createInvertedWorld(): ChapterLevelData {
        val map = arrayOf(
            intArrayOf(5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5),
            intArrayOf(5, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 5),
            intArrayOf(5, 0, 5, 5, 5, 0, 5, 0, 5, 5, 5, 5, 5, 5, 0, 5),
            intArrayOf(5, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 5),
            intArrayOf(5, 0, 5, 0, 5, 5, 5, 5, 5, 5, 5, 5, 0, 5, 0, 5),
            intArrayOf(5, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 5),
            intArrayOf(5, 5, 5, 0, 5, 0, 5, 5, 5, 5, 0, 5, 5, 5, 0, 5),
            intArrayOf(5, 0, 0, 0, 0, 0, 5, 0, 0, 5, 0, 0, 0, 0, 0, 5),
            intArrayOf(5, 0, 5, 5, 5, 0, 5, 0, 0, 5, 0, 5, 5, 5, 0, 5),
            intArrayOf(5, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 5),
            intArrayOf(5, 0, 5, 0, 5, 5, 5, 5, 5, 5, 5, 5, 0, 5, 0, 5),
            intArrayOf(5, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 5),
            intArrayOf(5, 5, 5, 0, 5, 0, 5, 5, 5, 5, 0, 5, 5, 5, 0, 5),
            intArrayOf(5, 0, 0, 0, 0, 0, 5, 0, 0, 5, 0, 0, 0, 0, 0, 5),
            intArrayOf(5, 0, 5, 5, 5, 5, 5, 0, 0, 5, 5, 5, 5, 5, 0, 5),
            intArrayOf(5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5)
        )

        val doors = listOf(
            WorldDoor("door_void_core", 14, 14, isLocked = true, requiredItem = ItemType.BRASS_KEY)
        )

        val pickups = listOf(
            WorldPickup("p_key_void", ItemType.BRASS_KEY, 7.5f, 7.5f, "Tear of Reality (Key)", "دمعة الواقع (المفتاح الأخير)", "Crystallized fragment of the broken world.", "شظية بلورية من نسيج الواقع المنهار."),
            WorldPickup("p_bat5", ItemType.FLASHLIGHT_BATTERY, 1.5f, 9.5f, "Eldritch Luminescence", "طاقة ضوء خارقة", "Radiates constant blinding white aura.", "تشع بطاقة ضوء بيضاء لا تنطفئ.")
        )

        val hidingSpots = listOf(
            HidingSpot("h_rift1", 3.5f, 5.5f, "Gravitational Shadow Fold", "طيّة ظل الجاذبية"),
            HidingSpot("h_rift2", 11.5f, 11.5f, "Void Distortion Alcove", "كوة تشوه الفراغ")
        )

        val monsters = listOf(
            Monster(
                id = "m_observer_prime",
                type = MonsterType.THE_OBSERVER,
                x = 13.5f,
                y = 1.5f,
                speed = 1.45f,
                patrolPath = listOf(
                    Pair(13.5f, 1.5f),
                    Pair(13.5f, 7.5f),
                    Pair(7.5f, 7.5f),
                    Pair(7.5f, 13.5f),
                    Pair(1.5f, 13.5f),
                    Pair(1.5f, 1.5f)
                )
            )
        )

        val notes = listOf(
            LoreNote(
                id = "note_ch5_1",
                chapter = Chapter.INVERTED_WORLD,
                titleEn = "The Final Awakening",
                titleAr = "الصحوة الأخيرة",
                dateOrSource = "Beyond Time and Space",
                contentEn = "The car accident at the start was not the beginning. It was the moment your mind shattered under the weight of what you saw. You didn't stumble into the Eclipse... you were the one who opened it.",
                contentAr = "حادث السيارة في البداية لم يكن البداية الحقيقية... بل كان اللحظة التي انكسر فيها وعيك تحت وطأة ما شهدته. أنت لم تعثر على الكسوف صدفة... بل كنت أنت من فتح بوابته.",
                isAudioTape = true
            )
        )

        val objectives = listOf(
            GameObjective("obj_ch5_1", "Survive the impossible inverted labyrinth", "انجُ داخل المتاهة المعكوسة المستحيلة"),
            GameObjective("obj_ch5_2", "Claim the Tear of Reality at the nexus", "استحوذ على دمعة الواقع من نقطة المركز"),
            GameObjective("obj_ch5_3", "Confront The Observer and seal the Cosmic Eclipse", "واجه المراقب واغلق كسوف الأبدية للأبد")
        )

        return ChapterLevelData(
            chapter = Chapter.INVERTED_WORLD,
            map = map,
            playerStartX = 1.5f,
            playerStartY = 1.5f,
            playerStartAngle = 0f,
            doors = doors,
            pickups = pickups,
            hidingSpots = hidingSpots,
            monsters = monsters,
            loreNotes = notes,
            objectives = objectives,
            exitTriggerX = 14f,
            exitTriggerY = 14f
        )
    }
}
