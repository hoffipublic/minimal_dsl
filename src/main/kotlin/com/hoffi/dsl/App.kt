package com.hoffi.dsl

import com.hoffi.dsl.sandwich.*

// call as `https://github.com/holgerbrandl/kscript` with current Dir = dir of this file and
// kscript App.kt

//INCLUDE Sandwich.kt
// or @file:Include("Sandwich.kt")

////ENTRY AppKt
//// annotation-driven script configuration
//@file:DependsOn("com.github.holgerbrandl:kutils:0.12")
//// comment directive
////DEPS com.github.holgerbrandl:kutils:0.12

fun main(args: Array<String>) {
    App().doIt(args)
}

class App {
    @Suppress("unused")
    fun doIt(args: Array<String>) {

        val compiledSandwich = CompileDSL.compileDSL()
        println(compiledSandwich.receipt())
        println("\n=======================================================\n")


        val dslSandwich = sandwich {
            with type "toasted"
            bread = "baguette" // with bread "ciabatta"


            onlyContext_A {// this: IA_Ifc ->
                a_IfcObject
             // b_IfcObject
                aFun()
             // bFun()         // unresolved reference, as it doesn't exist, because we are in IA_Ifc here
                a_contextFun()
             // b_contextFun() // no required context receiver found: Cxt { context(IB_Ifc) ... }
                a_extFun()
             // b_extFun()     // Unresolved reference. None of the following candidates is applicable because of receiver type mismatch
            }
            onlyContext_B {// this: IB_Ifc ->
             // a_IfcObject
                b_IfcObject
             // aFun()         // unresolved reference, as it doesn't exist, because we are in IA_Ifc here
                bFun()
             // a_contextFun() // no required context receiver found: Cxt { context(IA_Ifc) ... }
                b_contextFun()
             // a_extFun()     // Unresolved reference. None of the following candidates is applicable because of receiver type mismatch
                b_extFun()
            }
            bothContexts {// this: IAB_Ifc ->
                a_IfcObject
                b_IfcObject
                aFun()
                bFun()
                a_contextFun()
                b_contextFun()
                a_extFun()
                b_extFun()
            }


            fillings {
                +"StandardFilling"
            }

            filling("cheese")
            filling("ham")
            filling("tomato")

            dressings {
                +"Basil"
                +"Pepper"
                // // 'fun sideOrders(sideOrderToAdd: SideOrders.() -> Unit): Sandwich' can't be called in this context by implicit receiver. Use the explicit one if necessary
                // sideOrders {
                //     side("Green Salad")
                // }
            }
            dressing("Extra Hot")

            sideOrders {
                side("French Fries")
                side("Coca Cola 0.5")
            }
            sideOrders("second") {
                side("Rice")
                side("Water (sparkling)")
            }
            sideOrders("dessert") {
                side("ice cream (vanilla)")
                side("chocolate cake")
            }
        }

        println(dslSandwich.receipt())
    }
}
