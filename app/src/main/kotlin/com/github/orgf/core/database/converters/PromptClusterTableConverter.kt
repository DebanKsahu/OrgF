package com.github.orgf.core.database.converters

import androidx.room.TypeConverter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class PromptClusterTableConverter {
    @TypeConverter
    fun fromFloatArray(floatArray: FloatArray?): ByteArray? {
        if (floatArray==null) return null
        val byteBuffer = ByteBuffer.allocate(floatArray.size * 4).order(ByteOrder.LITTLE_ENDIAN)
        for (index in floatArray.indices) {
            byteBuffer.putFloat(floatArray[index])
        }

        return byteBuffer.array()
    }

    @TypeConverter
    fun toFloatArray(byteArray: ByteArray?): FloatArray? {
        if (byteArray == null) return null
        val byteBuffer = ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN)
        val floatArray = FloatArray(byteArray.size / 4)
        for (index in floatArray.indices) {
            floatArray[index] = byteBuffer.getFloat()
        }
        return floatArray
    }
}