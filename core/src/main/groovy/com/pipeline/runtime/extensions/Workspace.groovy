package com.pipeline.runtime.extensions

import com.pipeline.runtime.dsl.StepsExecutor
import org.apache.commons.io.FileUtils

import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class Workspace {
    static String workingDir = "build/workspace"

    static final ConcurrentMap<String, Object> params = [:] as ConcurrentHashMap

    static void initializeWorkspace(StepsExecutor self) {
        def workingDirPath = toFullPath(self,"${workingDir}/${self.env.JOB_NAME}")
        if (workingDirPath.toFile().exists()){
            FileUtils.cleanDirectory(workingDirPath.toFile());
        }
        Files.createDirectories(workingDirPath)
        workingDir = workingDirPath.toString()
    }

    static Path toFullPath(StepsExecutor self, String filePath) {
        return FileSystems.getDefault().getPath(filePath).toAbsolutePath()
    }


    static void dir(StepsExecutor self, String dir) {
        this.workingDir = dir
    }

    static String getWorkingDir(StepsExecutor self) {
        return workingDir
    }

    static ConcurrentMap<String, Object> getParams(StepsExecutor self) {
        return params
    }
}
