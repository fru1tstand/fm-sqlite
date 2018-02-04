package me.fru1t.sqlite.annotation.processor

import me.fru1t.sqlite.annotation.Column
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

class SqliteAnnotationProcessor : AbstractProcessor() {
  companion object {
    const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
  }

  override fun process(
      annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
    for (element in roundEnv!!.getElementsAnnotatedWith(Column::class.java)) {
      processingEnv.messager.printMessage(
          Diagnostic.Kind.MANDATORY_WARNING, "test", element)
    }

    return true
  }

  override fun getSupportedAnnotationTypes() = linkedSetOf(Column::class.java.name)
  override fun getSupportedSourceVersion() = SourceVersion.latest()!!
  override fun getSupportedOptions() = linkedSetOf("generate.kotlin.code", "generate.error")
}
