package com.example.eclipse.gameplay

import com.example.eclipse.model.Monster
import com.example.eclipse.model.MonsterState
import com.example.eclipse.model.MonsterType
import com.example.eclipse.model.Player
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt

class MonsterAIManager {

    // Player habit learning
    var playerLockerHidesCount: Int = 0
    var playerSprintPreferenceScore: Float = 0f

    fun updateMonsters(
        monsters: List<Monster>,
        player: Player,
        map: Array<IntArray>,
        dt: Float,
        onMonsterScreech: () -> Unit,
        onMonsterHitPlayer: (damage: Float) -> Unit
    ) {
        for (monster in monsters) {
            updateSingleMonster(monster, player, map, dt, onMonsterScreech, onMonsterHitPlayer)
        }
    }

    private fun updateSingleMonster(
        m: Monster,
        player: Player,
        map: Array<IntArray>,
        dt: Float,
        onMonsterScreech: () -> Unit,
        onMonsterHitPlayer: (damage: Float) -> Unit
    ) {
        val distToPlayer = hypot(m.x - player.x, m.y - player.y)

        // 1. Line of sight and hearing checks
        val canSeePlayer = if (player.isHiding) {
            false
        } else {
            hasLineOfSight(m.x, m.y, player.x, player.y, map) && isInFieldOfView(m, player.x, player.y)
        }

        val hearingRadius = when {
            player.isHiding -> 0f
            player.isCrouching -> 0f
            player.isSprinting -> if (m.type == MonsterType.SOUND_HUNTER) 14.0f else 8.5f
            else -> if (m.type == MonsterType.SOUND_HUNTER) 8.0f else 4.0f
        }
        val canHearPlayer = distToPlayer <= hearingRadius

        // If player has flashlight shining near monster, boost detection
        val flashlightBonus = if (player.flashlightOn && distToPlayer < 7.0f && !player.isHiding) 0.35f else 0f

        // 2. Behavioral State Machine based on Monster Type
        when (m.type) {
            MonsterType.THE_OBSERVER -> {
                updateObserverBehavior(m, player, distToPlayer, canSeePlayer, canHearPlayer, dt, map, flashlightBonus, onMonsterScreech, onMonsterHitPlayer)
            }
            MonsterType.SOUND_HUNTER -> {
                updateSoundHunterBehavior(m, player, distToPlayer, canHearPlayer, dt, map, onMonsterScreech, onMonsterHitPlayer)
            }
            MonsterType.THE_LURKER -> {
                updateLurkerBehavior(m, player, distToPlayer, dt, map, onMonsterScreech, onMonsterHitPlayer)
            }
            MonsterType.FACILITY_SENTINEL -> {
                updateSentinelBehavior(m, player, distToPlayer, canSeePlayer, dt, map, onMonsterScreech, onMonsterHitPlayer)
            }
            MonsterType.THE_MIMIC -> {
                updateObserverBehavior(m, player, distToPlayer, canSeePlayer, canHearPlayer, dt, map, flashlightBonus, onMonsterScreech, onMonsterHitPlayer)
            }
        }
    }

    private fun updateObserverBehavior(
        m: Monster,
        player: Player,
        distToPlayer: Float,
        canSeePlayer: Boolean,
        canHearPlayer: Boolean,
        dt: Float,
        map: Array<IntArray>,
        flashlightBonus: Float,
        onMonsterScreech: () -> Unit,
        onMonsterHitPlayer: (damage: Float) -> Unit
    ) {
        when (m.state) {
            MonsterState.PATROL, MonsterState.IDLE, MonsterState.RETURNING_TO_PATROL -> {
                if (canSeePlayer || (canHearPlayer && distToPlayer < 4.0f)) {
                    m.detectionAlert += (dt * (2.0f + flashlightBonus))
                    if (m.detectionAlert >= 1.0f) {
                        m.state = MonsterState.CHASING_PLAYER
                        m.lastSeenPlayerX = player.x
                        m.lastSeenPlayerY = player.y
                        onMonsterScreech()
                    }
                } else if (canHearPlayer) {
                    m.state = MonsterState.INVESTIGATING_SOUND
                    m.targetX = player.x
                    m.targetY = player.y
                    m.timer = 5.0f
                } else {
                    m.detectionAlert = (m.detectionAlert - dt * 0.5f).coerceAtLeast(0f)
                    advancePatrol(m, dt, map)
                }
            }

            MonsterState.INVESTIGATING_SOUND -> {
                if (canSeePlayer) {
                    m.state = MonsterState.CHASING_PLAYER
                    m.lastSeenPlayerX = player.x
                    m.lastSeenPlayerY = player.y
                    onMonsterScreech()
                } else {
                    moveTowards(m, m.targetX, m.targetY, m.speed * 0.85f * dt, map)
                    val distToTarget = hypot(m.x - m.targetX, m.y - m.targetY)
                    m.timer -= dt
                    if (distToTarget < 0.8f || m.timer <= 0f) {
                        m.state = MonsterState.RETURNING_TO_PATROL
                    }
                }
            }

            MonsterState.CHASING_PLAYER -> {
                if (canSeePlayer) {
                    m.lastSeenPlayerX = player.x
                    m.lastSeenPlayerY = player.y
                    m.timer = 6.0f // 6 seconds memory

                    // Chase speed boost
                    val chaseSpeed = m.speed * 1.55f
                    moveTowards(m, player.x, player.y, chaseSpeed * dt, map)

                    // Attack range
                    if (distToPlayer < 1.05f) {
                        onMonsterHitPlayer(35f * dt)
                    }
                } else {
                    // Lost direct sight: move to last seen position
                    m.state = MonsterState.SEARCHING_LAST_SEEN
                    m.targetX = m.lastSeenPlayerX
                    m.targetY = m.lastSeenPlayerY
                }
            }

            MonsterState.SEARCHING_LAST_SEEN -> {
                if (canSeePlayer) {
                    m.state = MonsterState.CHASING_PLAYER
                    onMonsterScreech()
                } else {
                    moveTowards(m, m.targetX, m.targetY, m.speed * dt, map)
                    m.timer -= dt
                    val distToTarget = hypot(m.x - m.targetX, m.y - m.targetY)

                    // Observer learning: if player hid often, linger in the area longer
                    val extraSearchTime = (playerLockerHidesCount * 0.5f).coerceAtMost(4.0f)
                    if (distToTarget < 0.6f || m.timer <= -extraSearchTime) {
                        m.state = MonsterState.RETURNING_TO_PATROL
                        m.detectionAlert = 0f
                    }
                }
            }
        }
    }

    private fun updateSoundHunterBehavior(
        m: Monster,
        player: Player,
        distToPlayer: Float,
        canHearPlayer: Boolean,
        dt: Float,
        map: Array<IntArray>,
        onMonsterScreech: () -> Unit,
        onMonsterHitPlayer: (damage: Float) -> Unit
    ) {
        // Blind hunter dashes furiously only when hearing movement
        if (canHearPlayer && !player.isHiding) {
            m.state = MonsterState.CHASING_PLAYER
            m.targetX = player.x
            m.targetY = player.y
            onMonsterScreech()

            val rushSpeed = m.speed * 1.8f
            moveTowards(m, m.targetX, m.targetY, rushSpeed * dt, map)

            if (distToPlayer < 1.1f) {
                onMonsterHitPlayer(50f * dt)
            }
        } else {
            // Roams or stays still listening
            if (m.state == MonsterState.CHASING_PLAYER) {
                m.timer -= dt
                if (m.timer <= 0f) {
                    m.state = MonsterState.PATROL
                }
            } else {
                advancePatrol(m, dt * 0.6f, map)
            }
        }
    }

    private fun updateLurkerBehavior(
        m: Monster,
        player: Player,
        distToPlayer: Float,
        dt: Float,
        map: Array<IntArray>,
        onMonsterScreech: () -> Unit,
        onMonsterHitPlayer: (damage: Float) -> Unit
    ) {
        // Weeping entity: activates if player is too close or looks directly at her
        val angleToMonster = atan2(m.y - player.y, m.x - player.x)
        var angleDiff = abs(player.angle - angleToMonster)
        while (angleDiff > PI) angleDiff -= (2 * PI).toFloat()
        angleDiff = abs(angleDiff)

        val isPlayerStaringAtLurker = angleDiff < 0.35f && distToPlayer < 6.0f && !player.isHiding

        if (distToPlayer < 2.2f || isPlayerStaringAtLurker) {
            m.state = MonsterState.CHASING_PLAYER
            onMonsterScreech()
            moveTowards(m, player.x, player.y, m.speed * 1.3f * dt, map)

            if (distToPlayer < 1.0f && !player.isHiding) {
                onMonsterHitPlayer(40f * dt)
            }
        } else {
            m.state = MonsterState.IDLE
        }
    }

    private fun updateSentinelBehavior(
        m: Monster,
        player: Player,
        distToPlayer: Float,
        canSeePlayer: Boolean,
        dt: Float,
        map: Array<IntArray>,
        onMonsterScreech: () -> Unit,
        onMonsterHitPlayer: (damage: Float) -> Unit
    ) {
        if (canSeePlayer && !player.isHiding) {
            m.state = MonsterState.CHASING_PLAYER
            onMonsterScreech()
            moveTowards(m, player.x, player.y, m.speed * 1.4f * dt, map)
            if (distToPlayer < 1.0f) {
                onMonsterHitPlayer(45f * dt)
            }
        } else {
            advancePatrol(m, dt, map)
        }
    }

    private fun advancePatrol(m: Monster, dt: Float, map: Array<IntArray>) {
        if (m.patrolPath.isEmpty()) return
        val currentTarget = m.patrolPath[m.patrolIndex]
        val dist = hypot(m.x - currentTarget.first, m.y - currentTarget.second)

        if (dist < 0.4f) {
            m.patrolIndex = (m.patrolIndex + 1) % m.patrolPath.size
        } else {
            moveTowards(m, currentTarget.first, currentTarget.second, m.speed * dt, map)
        }
    }

    private fun moveTowards(m: Monster, targetX: Float, targetY: Float, step: Float, map: Array<IntArray>) {
        val angle = atan2(targetY - m.y, targetX - m.x)
        m.angle = angle
        val nextX = m.x + cos(angle) * step
        val nextY = m.y + sin(angle) * step

        // Collision check with map walls (padding 0.25f)
        val canMoveX = canWalkAt(nextX, m.y, map)
        val canMoveY = canWalkAt(m.x, nextY, map)

        if (canMoveX) m.x = nextX
        if (canMoveY) m.y = nextY
    }

    private fun canWalkAt(x: Float, y: Float, map: Array<IntArray>): Boolean {
        val ix = x.toInt()
        val iy = y.toInt()
        if (ix !in map.indices || iy !in map[0].indices) return false
        return map[ix][iy] == 0
    }

    private fun hasLineOfSight(x1: Float, y1: Float, x2: Float, y2: Float, map: Array<IntArray>): Boolean {
        val dist = hypot(x2 - x1, y2 - y1)
        val steps = (dist * 4).toInt().coerceAtLeast(2)
        val dx = (x2 - x1) / steps
        val dy = (y2 - y1) / steps

        var currX = x1
        var currY = y1

        for (i in 0..steps) {
            val ix = currX.toInt()
            val iy = currY.toInt()
            if (ix in map.indices && iy in map[0].indices) {
                if (map[ix][iy] > 0) return false // Wall blocks view
            }
            currX += dx
            currY += dy
        }
        return true
    }

    private fun isInFieldOfView(m: Monster, targetX: Float, targetY: Float): Boolean {
        if (m.type == MonsterType.SOUND_HUNTER) return false // Sound Hunter is blind

        val angleToTarget = atan2(targetY - m.y, targetX - m.x)
        var diff = abs(m.angle - angleToTarget)
        while (diff > PI) diff -= (2 * PI).toFloat()
        diff = abs(diff)

        // ~110 degrees field of view
        return diff < (110.0 * PI / 180.0)
    }
}
