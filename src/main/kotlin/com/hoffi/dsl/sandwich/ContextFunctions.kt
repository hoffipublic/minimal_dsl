package com.hoffi.dsl.sandwich

// defining methods that can be called
// on dsl blocks/closures, e.g.:
//   fun onlyContext_A(a_contextBlock: IA_Ifc.() -> Unit): Sandwich {
//       bothABObj.apply(a_contextBlock)
//       return this
//   }
interface IA_Ifc {
    val a_IfcObject: AImpl
    fun aFun(): String
}
interface IB_Ifc {
    val b_IfcObject: BImpl
    fun bFun(): String
}

// implementations of val properties of scope interfaces above
class AImpl {
}
class BImpl {
}

// defining a scope that can call functions of both scope interfaces above
// we need to define an extra interface, as we want to do something like this in the dsl:
//   fun bothContexts(both_contextBlock: IAB_Ifc.() -> Unit): Sandwich {
//       bothABObj.apply(both_contextBlock)
//       return this
//   }
interface IAB_Ifc : IA_Ifc, IB_Ifc

// implementation of the above combined scope interface
class BothABImpl : IAB_Ifc {
    override val a_IfcObject: AImpl = AImpl()
    override val b_IfcObject: BImpl = BImpl()
    override fun aFun() = "aFun of both"
    override fun bFun() = "bFun of both"

}

// context receiver functions as well as extension functions also (kind of) do the trick,
// but:
// context reciever functions: if having multiple context's, ALL have to be in scope
//                             can only(??notsure??) be defined on a single function
// extension functions: "fool" the reader of the code to believe that these functions "do something" on the Context
//                      which is not true in the DSL case, ase it is just a "vehicle" to have the functions available "in scope"

// with kotlin "context receivers" see https://www.youtube.com/watch?v=GISPalIVdQY
// you can define functions that are only "available" if a certain class is "in scope":
context(IA_Ifc)
fun a_contextFun() = "a_contextFun"

context(IB_Ifc)
fun b_contextFun() = "b_contextFun"

// or with "classic" extension functions:
fun IA_Ifc.a_extFun() = "a_extFun"
fun IB_Ifc.b_extFun() = "b_extFun"
fun IAB_Ifc.ab_extFun() = "ab_extFun"
