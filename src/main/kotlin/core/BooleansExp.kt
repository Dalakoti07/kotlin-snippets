package core

var one = false
var two = false
var three = false

var isCheckWhole = one || two || three

fun main(){
    println(isCheckWhole)
    three = true
    println(isCheckWhole)
}
