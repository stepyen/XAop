package com.stepyen.xaop_plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.aspectj.bridge.IMessage
import org.aspectj.bridge.MessageHandler
import org.aspectj.tools.ajc.Main
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.compile.JavaCompile


class XAopPlugin implements Plugin<Project>{

    @Override
    void apply(Project project) {

        // 判断是否有 Application 或者 Library Plugin
        def hasApp = project.plugins.withType(AppPlugin)
        def hasLib = project.plugins.withType(LibraryPlugin)
        if (!hasApp && !hasLib) {
            throw new IllegalStateException("'android' or 'android-library' plugin required.")
        }

        // 添加依赖
        project.dependencies {
            implemention 'org.aspectj:aspectjrt:1.8.9'
        }

        // 日志
        final def log = project.logger
        log.error "========================"
        log.error "XAop::Aspectj切片开始编织Class!"
        log.error "========================"

        //
        final def variants
        if (hasApp) {
            variants = project.android.applicationVariants
        }else{
            variants = project.android.libraryVariants
        }
        variants.all{variant->
            JavaCompile javaCompile = null
            if (variant.hasProperty('javaCompileProvider')) {
                // gradle 4.10.1 +
                TaskProvider<JavaCompile> provider = variant.javaCompileProvider
                javaCompile = provider.get()
            }else{
                javaCompile = variant.hasProperty('javaCompiler') ? variant.javaCompiler : variant.javaCompile
            }

            javaCompile.doLast {
                String[] args = ["-showWeaveInfo",
                                 "-1.8",
                                 "-inpath", javaCompile.destinationDir.toString(),
                                 "-aspectpath", javaCompile.classpath.asPath,
                                 "-d", javaCompile.destinationDir.toString(),
                                 "-classpath", javaCompile.classpath.asPath,
                                 "-bootclasspath", project.android.bootClasspath.join(File.pathSeparator)]
                log.debug "ajc args: " + Arrays.toString(args)

                MessageHandler handler = new MessageHandler(true);
                new Main().run(args, handler);
                for (IMessage message : handler.getMessages(null, true)) {
                    switch (message.getKind()) {
                        case IMessage.ABORT:
                        case IMessage.ERROR:
                        case IMessage.FAIL:
                            log.error message.message, message.thrown
                            break;
                        case IMessage.WARNING:
                            log.warn message.message, message.thrown
                            break;
                        case IMessage.INFO:
                            log.info message.message, message.thrown
                            break;
                        case IMessage.DEBUG:
                            log.debug message.message, message.thrown
                            break;
                    }
                }
            }
        }

    }
}