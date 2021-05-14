package com.hoffi.dsl

const val DEFAULT_TYPE = "<default>"

fun sandwich(order: Sandwich.() -> Unit): Sandwich =
    Sandwich().apply(order)

@DslMarker annotation class DslSandwich

@DslSandwich
class Sandwich {
    val with = this
    private var type = DEFAULT_TYPE
    var bread: String = "white"
    private val fillings = Fillings()
    private val dressings = Dressings()
    private val sides = mutableMapOf<String, SideOrders>()

    infix fun type(sandwichType: String) {
        type = sandwichType
    }
    infix fun bread(breadType: String): Sandwich {
        bread = breadType
        return this
    }
    infix fun filling(fillingToAdd: String): Sandwich {
        fillings.apply { +fillingToAdd }
        return this
    }
    infix fun fillings(toAdd: Fillings.() -> Unit): Sandwich {
        fillings.toAdd()
        return this
    }
    infix fun dressing(dressingToAdd: String): Sandwich {
        dressings.apply { +dressingToAdd }
        return this
    }
    fun dressings(toAdd: Dressings.() -> Unit): Sandwich {
        dressings.toAdd()
        return this
    }
    fun sideOrders(name: String = SideOrders.DEFAULT, sideOrderToAdd: SideOrders.() -> Unit): Sandwich {
        val so = sides.getOrPut(name) { val so = SideOrders() ; sides[name] = so; so }
        so.sideOrderToAdd()
        return this
    }
    fun construct(): Sandwich {
        println("Making your sandwich")
        return this
    }
    fun receipt(): String {
        var receipt = "$type sandwich on $bread bread"
        receipt += "\n\t" + fillings.receipt()
        receipt += "\n\t" + dressings.receipt()
        sides.forEach { (name, so) ->
            when (name) {
                SideOrders.DEFAULT -> receipt += "\n\twith sides: ${so.receipt()}"
                else -> {
                    receipt += "\n\twith \"$name\" sides:"
                    receipt += "\n\t\t" + so.receipt()
                }
            }
        }
        return "Sandwich Receipt\n$receipt"
    }


}

@DslSandwich
class Fillings {
    private val fillings = mutableListOf<String>()

    // operator "unary +" overload as extension function on String if method body (this) inside Fillings class
    operator fun String.unaryPlus() = fillings.add(this)

    fun receipt(): String {
        return if (fillings.isEmpty()) " with no fillings "
        else "with fillings: " + fillings.joinToString(", ")
    }

    fun set(listOfFillings: List<String>) {
        fillings.clear()
        fillings.addAll(listOfFillings)
    }

    fun count(): Int {
        return fillings.size
    }

}

@DslSandwich
class Dressings {
    private val dressings = mutableListOf<String>()

    // operator "unary +" overload as extension function on String if method body (this) inside Dressings class
    operator fun String.unaryPlus() = dressings.add(this)

    fun receipt(): String {
        return if (dressings.isEmpty()) " with no dressings "
        else "with dressings: " + dressings.joinToString(", ")
    }

    fun set(listOfDressings: List<String>) {
        dressings.clear()
        dressings.addAll(listOfDressings)
    }
}

@DslSandwich
class SideOrders {
    private val sides = mutableListOf<String>()

    companion object {
        const val DEFAULT = "default"
    }

    fun side(sideOrderToAdd: String) {
        sides.add(sideOrderToAdd)
    }

    fun receipt(): String {
        return if (sides.isEmpty()) " with no sides "
        else sides.joinToString(", ")
    }

    fun set(listOfSides: List<String>) {
        sides.clear()
        sides.addAll(listOfSides)
    }
}
