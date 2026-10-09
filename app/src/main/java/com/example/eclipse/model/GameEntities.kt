package com.example.eclipse.model

data class Player(
    var x: Float = 2.5f,
    var y: Float = 2.5f,
    var angle: Float = 0f, // radians
    var pitch: Float = 0f, // vertical camera look (-0.5f to 0.5f)
    var health: Float = 100f,
    val maxHealth: Float = 100f,
    var stamina: Float = 100f,
    val maxStamina: Float = 100f,
    var isSprinting: Boolean = false,
    var isCrouching: Boolean = false,
    var isHiding: Boolean = false,
    var hidingSpotId: String? = null,
    var flashlightOn: Boolean = true,
    var batteryPercent: Float = 95f,
    var noiseLevel: Float = 0f, // 0 = silent, 1 = normal walk, 2.5 = sprint
    var sanity: Float = 100f // 100 = calm, 0 = severe hallucination
)

data class Monster(
    val id: String,
    val type: MonsterType,
    var x: Float,
    var y: Float,
    var angle: Float = 0f,
    var state: MonsterState = MonsterState.PATROL,
    var speed: Float = 1.2f,
    var targetX: Float = x,
    var targetY: Float = y,
    var lastSeenPlayerX: Float = x,
    var lastSeenPlayerY: Float = y,
    var timer: Float = 0f,
    var patrolIndex: Int = 0,
    var patrolPath: List<Pair<Float, Float>> = emptyList(),
    var detectionAlert: Float = 0f // 0f to 1f (1f triggers chase)
)

data class WorldDoor(
    val id: String,
    val x: Int,
    val y: Int,
    var isOpen: Boolean = false,
    var isLocked: Boolean = false,
    val requiredItem: ItemType? = null,
    var openProgress: Float = 0f // 0f = closed, 1f = open
)

data class WorldPickup(
    val id: String,
    val type: ItemType,
    val x: Float,
    val y: Float,
    val titleEn: String,
    val titleAr: String,
    val descriptionEn: String,
    val descriptionAr: String,
    var isCollected: Boolean = false
)

data class HidingSpot(
    val id: String,
    val x: Float,
    val y: Float,
    val nameEn: String,
    val nameAr: String
)

data class LoreNote(
    val id: String,
    val chapter: Chapter,
    val titleEn: String,
    val titleAr: String,
    val dateOrSource: String,
    val contentEn: String,
    val contentAr: String,
    val isAudioTape: Boolean = false,
    var isRead: Boolean = false
)

data class InventorySlot(
    val id: String,
    val type: ItemType,
    val count: Int = 1,
    val titleEn: String,
    val titleAr: String,
    val descriptionEn: String,
    val descriptionAr: String
)

data class GameObjective(
    val id: String,
    val textEn: String,
    val textAr: String,
    var isCompleted: Boolean = false
)

data class RayHit(
    val distance: Float,
    val mapX: Int,
    val mapY: Int,
    val side: Int, // 0 for vertical, 1 for horizontal
    val wallType: Int,
    val wallX: Float // exact hit coordinate on wall surface (0..1)
)

data class BillboardSprite(
    val x: Float,
    val y: Float,
    val distance: Float,
    val type: String, // "monster_observer", "door", "pickup_key", "pickup_battery", "barrel", etc.
    val data: Any? = null
)
