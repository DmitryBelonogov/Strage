package com.github.nougust3.strage.compiler

import com.github.nougust3.strage.annotation.Adapter
import com.github.nougust3.strage.annotation.BindId
import com.github.nougust3.strage.annotation.BindName
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.ExecutableType

data class Binder(
    val methodName: String,
    val typeName: String,
    val layout: Int,
    val viewTypes: List<ViewType>,
    val layoutName: String = ""
) {
    constructor(method: String, type: String, layout: String, viewTypes: List<ViewType>):
            this(method, type, -1, viewTypes, layout)
}

data class ViewType(val name: String, val type: String)

class Processor: AbstractProcessor() {

    companion object {
        const val NAME = "kapt.kotlin.generated"
        val annotation = Adapter::class.java
        val bindIdAnnotation = BindId::class.java
        val bindNameAnnotation = BindName::class.java
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        roundEnv.getElementsAnnotatedWith(annotation).forEach {
            val modulePackage = it.getAnnotation(annotation).modulePackage
            val qualifiedName = processingEnv.elementUtils.getPackageOf(it).qualifiedName.toString()
            val packageName = processingEnv.elementUtils.getPackageOf(it).toString()
            val elementName = it.simpleName.toString()
            val binders = getBinders(roundEnv, it)
            generateClass(generate(modulePackage, packageName, elementName, binders), elementName)
        }
        return false
    }

    private fun generateClass(clazz: String, className: String){
        val fileName = "${className}Adapter.kt"
        val file = File(processingEnv.options[NAME], "$fileName.kt")

        file.writeText(clazz)
    }

    private fun getBinders(roundEnv: RoundEnvironment, element: Element): List<Binder> {
        val result = ArrayList<Binder>()

        result.addAll(roundEnv
            .getElementsAnnotatedWith(bindIdAnnotation)
            .filter {
                it.enclosingElement == element
            }
            .map {
                val params = (it.asType() as ExecutableType).parameterTypes
                val dataType = if (params.first().toString() == "java.lang.String") "kotlin.String" else params.first().toString()
                val viewTypes = (it as ExecutableElement).parameters.takeLast(params.size - 1).map {
                    ViewType(it.simpleName.toString(), it.asType().toString())
                }
                Binder(it.simpleName.toString(), dataType, it.getAnnotation(bindIdAnnotation).id, viewTypes)
            })

        result.addAll(roundEnv
            .getElementsAnnotatedWith(bindNameAnnotation)
            .filter {
                it.enclosingElement == element
            }
            .map {
                val params = (it.asType() as ExecutableType).parameterTypes
                val dataType = if (params.first().toString() == "java.lang.String") "kotlin.String" else params.first().toString()
                val viewTypes = (it as ExecutableElement).parameters.takeLast(params.size - 1).map {
                    ViewType(it.simpleName.toString(), it.asType().toString())
                }
                var layout = it.simpleName.toString()
                repeat(layout.count { it.isUpperCase() }) {
                    val index = layout.indexOfFirst { it.isUpperCase() }
                    layout = layout.substring(0, index) + "_" + layout[index].toLowerCase() + layout.substring(index + 1, layout.length)
                }
                Binder(it.simpleName.toString(), dataType, layout, viewTypes)
            })

        return result
    }

    private fun getBindersStr(binders: List<Binder>) =
        binders.joinToString("\n") {
            """
                .bind<${it.typeName}>(R.layout.${it.layoutName}) {
                    adapter.${it.methodName}(
                        it${getViewTypes(it)}
                    )
                }
            """
        }

    private fun getViewTypes(binder: Binder) =
        binder.viewTypes.firstOrNull()?.let {
            ", " + binder.viewTypes.joinToString(", ") {
                "this.view<${it.type}>(R.id.${it.name})"
            }
        }

    private fun generate(
        modulePackage: String,
        packageName: String,
        adapterName: String,
        binders: List<Binder>
    ) = """
    package $packageName
    
    import android.content.Context
    import com.githab.nougust3.strage.Strage
    import com.githab.nougust3.strage.bind
    import com.githab.nougust3.strage.StrageAdapter
    import $modulePackage.R
    
    class ${adapterName}Adapter(val context: Context) {
    
        val adapter = ${adapterName}()
    
        fun get() = Strage(context)
            ${getBindersStr(binders)}
    }
    """.trimIndent()

    override fun getSupportedAnnotationTypes(): Set<String> = setOf(annotation.canonicalName)

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

}