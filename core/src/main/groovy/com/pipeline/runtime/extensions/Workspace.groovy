package com.pipeline.runtime.extensions

import com.pipeline.runtime.dsl.StepsExecutor
import org.apache.commons.io.FileUtils

import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class Workspace {

    static final ConcurrentMap<String, Object> params = [:] as ConcurrentHashMap

    static void initializeWorkspace(StepsExecutor self, String jobName) {

        def workingDirPath = toFullPath(self,"${self.getWorkingDir()}")
        def workingDirJobPath = toFullPath(self,"${self.getWorkingDir()}/${jobName}")
        if (workingDirPath.toFile().exists()){
            FileUtils.cleanDirectory(workingDirPath.toFile());
        }
        Files.createDirectories(workingDirPath)
        Files.createDirectories(workingDirJobPath)

    }

    static Path toFullPath(StepsExecutor self, String filePath) {
        return FileSystems.getDefault().getPath(filePath).toAbsolutePath()
    }


    static void dir(StepsExecutor self, String dir) {
        this.workingDir = dir
    }

    static String getWorkingDir(StepsExecutor self) {
        return self.configuration.getValueOrDefault('pipeline.workingDir','build/workspace')
    }

    static ConcurrentMap<String, Object> getParams(StepsExecutor self) {
        return params
    }
}
