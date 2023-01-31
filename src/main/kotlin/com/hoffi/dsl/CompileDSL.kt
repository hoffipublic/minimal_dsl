package com.hoffi.dsl

import com.hoffi.dsl.sandwich.Sandwich
import java.io.File
import javax.script.ScriptEngineManager

interface ICompileDSL {
    fun compileDSL() : Sandwich
}
object CompileDSL : ICompileDSL {
    override fun compileDSL() : Sandwich {
        //val engine = ScriptEngineManager().getEngineByExtension("kts") ?: throw Exception("kts script engine not found")
        val engine = ScriptEngineManager().getEngineByName("kotlin")
        println(engine.eval("21 + 21"))
        val dslFileContent = File("./Sandwich.kts").readText()
        val script = """
            import com.hoffi.dsl.sandwich.sandwich
            $dslFileContent
        """.trimIndent()
        val res = engine.eval(script)
        return res as Sandwich
    }
}
