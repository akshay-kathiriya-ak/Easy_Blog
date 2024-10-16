package com.example.easyblog.model

data class UserData(
    var name : String?= "",
    var email : String?= "",

    // default con
){
    constructor(): this("","")
}
