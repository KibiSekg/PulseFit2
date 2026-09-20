package com.pulsefit.app.data.model

/** Firestore: users/{userId}/meals/{mealId}. Also cached locally in Room (see MealEntity). */
data class MealLog(
    var mealId: String = "",
    var userId: String = "",
    var mealType: String = "", // BREAKFAST, LUNCH, DINNER, SNACK
    var mealName: String = "",
    var calories: Double = 0.0,
    var proteinGrams: Double = 0.0,
    var carbsGrams: Double = 0.0,
    var fatGrams: Double = 0.0,
    var mealDate: Long = System.currentTimeMillis(),
    var synced: Boolean = true
) {
    fun getTotalCalories(): Int = calories.toInt()

    fun getMacroRatio(): String {
        val total = proteinGrams + carbsGrams + fatGrams
        if (total <= 0.0) return "0/0/0"
        val p = (proteinGrams / total * 100).toInt()
        val c = (carbsGrams / total * 100).toInt()
        val f = (fatGrams / total * 100).toInt()
        return "$p/$c/$f"
    }
}
