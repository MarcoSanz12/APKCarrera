package com.gf.common.entity.activity

data class ActivityModelSimple(
    var points: List<List<RegistryPoint>>,
    var time: List<Int> ,
    var distance: Int,
    var activityType: ActivityType
)