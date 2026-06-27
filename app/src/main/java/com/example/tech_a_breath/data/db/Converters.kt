package com.example.tech_a_breath.data.db

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun responseTypeToString(type: ResponseType): String = type.dbValue

    @TypeConverter
    fun stringToResponseType(value: String): ResponseType = ResponseType.fromDbValue(value)

    @TypeConverter
    fun changeSourceToString(source: ChangeSource): String = source.dbValue

    @TypeConverter
    fun stringToChangeSource(value: String): ChangeSource = ChangeSource.fromDbValue(value)
}
