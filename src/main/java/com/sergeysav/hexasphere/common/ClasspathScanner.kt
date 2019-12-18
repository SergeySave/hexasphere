package com.sergeysav.hexasphere.common

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarFile
import java.util.zip.ZipEntry
import kotlin.streams.asSequence

/**
 * @author sergeys
 */
object ClasspathScanner {
    
    private val jreFiles = arrayOf("charsets.jar", "deploy.jar", "cldrdata.jar", "dnsns.jar", "jaccess.jar",
                                   "jfxrt.jar", "localedata.jar", "nashorn.jar", "sunec.jar", "sunjce_provider.jar",
                                   "sunpkcs11.jar", "zipfs.jar", "javaws.jar", "jce.jar", "jfr.jar", "jfxswt.jar",
                                   "jsse.jar", "management-agent.jar", "plugin.jar", "resources.jar", "rt.jar",
                                   "javafx-mx.jar", "jconsole.jar", "packager.jar", "sa-jdi.jar", "tools.jar",
                                   "idea_rt.jar", "ant-javafx.jar", "dt.jar")
    
    private val ignored = arrayOf(".*/lwjgl-.*\\.jar".toRegex(),
                                  ".*/joml-.*\\.jar".toRegex(),
                                  ".*/kotlin-.*\\.jar".toRegex(),
                                  ".*/logback-.*\\.jar".toRegex(),
                                  ".*/slf4j-api.*\\.jar".toRegex())
    
    fun scan(ignoreJRE: Boolean = true): Sequence<String> {
        var classpath = System.getProperty("java.class.path").split(":").asSequence()
        if (ignoreJRE) {
            classpath = classpath.filter { cp -> jreFiles.none { cp.endsWith(it) } }
        }
        classpath = classpath.filterNot { cp -> ignored.any { cp.matches(it) } }
        return classpath.flatMap { cp ->
            try {
                return@flatMap if (cp.endsWith(".jar")) {
                    JarFile(File(cp)).stream().asSequence()
                            .map(ZipEntry::getName)
                } else {
                    val origin = File(cp).toPath()
                    Files.walk(origin).asSequence()
                            .filterNot { Files.isDirectory(it) }
                            .map { origin.relativize(it) }
                            .map(Path::toString)
                }.filter { it.endsWith(".class") }
                        .filterNot { it.contains("META-INF") }
                        .filterNot { it.endsWith("module-info.class") }
                        .map { it.replace('/', '.').substring(0, it.length - 6) }
            } catch (e: IOException) {
                e.printStackTrace() // Should be impossible
            }
            emptySequence<String>()
        }
    }
    
    fun scanForAnnotation(annotation: Class<out Annotation>): Sequence<Class<*>> = scan(true)
//            .asStream().peek(System.out::println).asSequence()
            .map { Class.forName(it) }
            .filter { it.getAnnotation(annotation) != null }
}