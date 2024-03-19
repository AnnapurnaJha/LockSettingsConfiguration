package com.codingchallenge.locksettingsconfig

data class LockConfigData(
    val lockVoltage: LockVoltage,
    val lockType: LockType,
    val lockKick: LockKick,
    val lockRelease: LockRelease,
    val lockReleaseTime: LockReleaseTime,
    val lockAngle: LockAngle
)

data class LockVoltage(
    val values: List<String>,
    var default: String
)

data class LockType(
    val values: List<String>,
    var default: String
)

data class LockKick(
    val values: List<String>,
    var default: String
)

data class LockRelease(
    val values: List<String>,
    var default: String,
    val common: Boolean = false
)

data class LockReleaseTime(
    val range: Range,
    val unit: String,
    var default: Double
)

data class LockAngle(
    val range: Range,
    val unit: String,
    var default: Int,
    val common: Boolean = false
)

data class Range(
    val min: Double,
    val max: Double
)
