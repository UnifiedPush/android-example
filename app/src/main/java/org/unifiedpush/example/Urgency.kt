package org.unifiedpush.example

/**
 * Define in [RFC8030](https://www.rfc-editor.org/rfc/rfc8030#section-5.3)
 *    +----------+-----------------------------+--------------------------+
 *    | Urgency  | Device State                | Example Application      |
 *    |          |                             | Scenario                 |
 *    +----------+-----------------------------+--------------------------+
 *    | very-low | On power and Wi-Fi          | Advertisements           |
 *    | low      | On either power or Wi-Fi    | Topic updates            |
 *    | normal   | On neither power nor Wi-Fi  | Chat or Calendar Message |
 *    | high     | Low battery                 | Incoming phone call or   |
 *    |          |                             | time-sensitive alert     |
 *    +----------+-----------------------------+--------------------------+
 */
enum class Urgency(val value: String) {
    /** On power and Wi-Fi, example: Advertisements */
    VERY_LOW("very-low"),

    /** On either power or Wi-Fi, example: Topic updates */
    LOW("low"),

    /** On neither power nor Wi-Fi, example: Chat or Calendar Message */
    NORMAL("normal"),

    /** Low battery, example: Incoming phone call or time-sensitive alert */
    HIGH("high") ;

    companion object {
        fun fromValue(s: String?): Urgency {
            return Urgency.entries.find { it.value == s } ?: Urgency.NORMAL
        }
    }
}
