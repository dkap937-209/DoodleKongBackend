package data.models

import util.Constants.TYPE_DRAW_DATA

data class DrawData(
    val roomName: String,
    val colour: Int,
    val thickness: Float,
    val fromX: Float,
    val fromY: Float,
    val toX: Float,
    val toY: Float,
    val motionEvent: Int
): BaseModel(TYPE_DRAW_DATA)
