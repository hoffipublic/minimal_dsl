package com.hoffi.dsl

const val DEFAULT_TYPE = "<default>"

fun sandwich(order: Sandwich.() -> Unit): Sandwich =
    Sandwich().apply(order)

@DslMarker annotation class DslSandwich

@DslSandwich
class Sandwich {
    public val with = this
    private var type = DEFAULT_TYPE
    public var bread: String = "white"
    internal val fillings = Fillings()
    private val dressings = Dressings()
    private val sides = SideOrders()

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
    fun sideOrders(sideOrderToAdd: SideOrders.() -> Unit): Sandwich {
        sides.sideOrderToAdd()
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
        receipt += "\n\t" + sides.receipt()
        return "Sandwich Receipt\n$receipt"
    }


}

@DslSandwich
class Fillings {
    private val fillings = mutableListOf<String>()

    operator fun String.unaryPlus() = fillings.add(this)

    fun receipt(): String {
        return if (fillings.isEmpty()) " with no fillings "
        else " with fillings: " + fillings.joinToString(", ")
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

    operator fun String.unaryPlus() = dressings.add(this)

    fun receipt(): String {
        return if (dressings.isEmpty()) " with no dressings "
        else " with dressings: " + dressings.joinToString(", ")
    }

    fun set(listOfDressings: List<String>) {
        dressings.clear()
        dressings.addAll(listOfDressings)
    }
}

@DslSandwich
class SideOrders {
    private val sides = mutableListOf<String>()

    fun side(sideOrderToAdd: String) {
        sides.add(sideOrderToAdd)
    }

    fun receipt(): String {
        return if (sides.isEmpty()) " with no sides "
        else " with sides: " + sides.joinToString(", ")
    }

    fun set(listOfSides: List<String>) {
        sides.clear()
        sides.addAll(listOfSides)
    }
}
