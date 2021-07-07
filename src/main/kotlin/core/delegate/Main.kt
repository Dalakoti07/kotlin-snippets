package core.delegate

import kotlin.properties.Delegates

class DelegatedLevel(val id: Int, var boss: String) {
    companion object {
        var highestLevel = 1
    }
    var unlocked: Boolean by Delegates.observable(false) {
            _, old, new ->
        if (new && id > highestLevel) {
            highestLevel = id
        }
        println("$old -> $new")
    }
}

fun main(){
    val delegatedlevel1 = DelegatedLevel(id = 1, boss = "Chameleon")
    val delegatedlevel2 = DelegatedLevel(id = 2, boss = "Squid")

    println(DelegatedLevel.highestLevel) // 1

    delegatedlevel2.unlocked = true

    println(DelegatedLevel.highestLevel) // 2
}