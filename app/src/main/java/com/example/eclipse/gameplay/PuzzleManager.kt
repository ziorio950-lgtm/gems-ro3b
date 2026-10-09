package com.example.eclipse.gameplay

data class KeypadPuzzleState(
    val id: String = "keypad_hospital",
    val titleEn: String = "ICU Elevator Security Keypad",
    val titleAr: String = "لوحة أمان مصعد العناية المركزة",
    val clueEn: String = "Clue found in Morgue log: 'Year the ward closed'",
    val clueAr: String = "الدليل من سجل المشرحة: 'عام إغلاق الجناح'",
    val targetCode: String = "1984",
    var currentInput: String = "",
    var isSolved: Boolean = false,
    var errorMessage: String? = null
)

data class WireConnection(
    val color: String,
    val hexColor: Long,
    var isConnected: Boolean = false
)

data class CircuitPuzzleState(
    val id: String = "circuit_facility",
    val titleEn: String = "Superconducting Breaker Panel",
    val titleAr: String = "لوحة قواطع الطاقة الفائقة",
    val targetOrder: List<Int> = listOf(2, 0, 3, 1),
    var currentToggles: BooleanArray = booleanArrayOf(false, false, false, false),
    var isSolved: Boolean = false
)

data class ValvePuzzleState(
    val id: String = "valve_forest",
    val titleEn: String = "Flood Gate Pressure Valves",
    val titleAr: String = "صمامات ضغط بوابة المياه",
    val targetPsi: Int = 75,
    var valveA: Int = 10,
    var valveB: Int = 15,
    var valveC: Int = 20,
    var isSolved: Boolean = false
) {
    val currentPsi: Int get() = valveA + valveB + valveC
}
