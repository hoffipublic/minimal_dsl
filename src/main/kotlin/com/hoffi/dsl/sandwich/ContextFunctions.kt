package com.hoffi.dsl.sandwich

// defining methods that can be called
// on dsl blocks/closures, e.g.:
//   fun onlyContext_A(a_contextBlock: IA_Context.() -> Unit): Sandwich {
//       bothABObj.apply(a_contextBlock)
//       return this
//   }
interface IA_Context {
    val a_contextObject: AObj
    fun aFun(): String
}
interface IB_Context {
    val b_contextObject: BObj
    fun bFun(): String
}

// implementations of val properties of scope interfaces above
class AObj {
}
class BObj {
}

// defining a scope that can call functions of both scope interfaces above
// we need to define an extra interface, as we want to do something like this in the dsl:
//   fun bothContexts(both_contextBlock: IAB_Context.() -> Unit): Sandwich {
//       bothABObj.apply(both_contextBlock)
//       return this
//   }
interface IAB_Context : IA_Context, IB_Context

// implementation of the above combined scope interface
class BothABObj : IAB_Context {
    override val a_contextObject: AObj = AObj()
    override val b_contextObject: BObj = BObj()
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
context(IA_Context)
fun a_contextFun() = "a_contextFun"

context(IB_Context)
fun b_contextFun() = "b_contextFun"

// or with "classic" extension functions:
fun IA_Context.a_extFun() = "a_extFun"
fun IB_Context.b_extFun() = "b_extFun"
fun IAB_Context.ab_extFun() = "ab_extFun"
