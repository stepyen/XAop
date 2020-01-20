package com.stepyen.test


import org.gradle.api.Plugin
import org.gradle.api.Project


class MyPlugin implements Plugin<Project> {
    @Override
    void apply(Project target) {
        println('buildSrc中MyPlugin执行了')
        target.task("mytask"){
            doLast {
                println('buildSrc中MyPlugin中的task执行了')
            }
        }
    }
}
