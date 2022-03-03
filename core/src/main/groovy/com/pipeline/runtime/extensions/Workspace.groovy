package com.pipeline.runtime.extensions

import com.pipeline.runtime.dsl.StepsExecutor
import com.pipeline.runtime.interfaces.IConfiguration
import org.apache.commons.io.FileUtils

import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class Workspace {
    static String _baseWorkingDir = 'build/workspace'
    static Path _workingDir
    static final ConcurrentMap<String, Object> params = [:] as ConcurrentHashMap

    static void initializeWorkspace(StepsExecutor self, IConfiguration configuration) {
        _baseWorkingDir = configuration.getValueOrDefault('pipeline.baseWorkingDir','build/workspace')
        def gitName = configuration.getValue('pipeline.scm.gitscm.userRemoteConfigs')[0]?.name
        def jobName = configuration.getValueOrDefault('pipeline.environmentVars.JOB_NAME',gitName?:'Job')
        def workingDir = "$_baseWorkingDir/$jobName"

        _workingDir = toFullPath(self,workingDir)

        if (_workingDir.toFile().exists()){
            FileUtils.cleanDirectory(_workingDir.toFile())
        }
        Files.createDirectories(_workingDir)


    }

    static Path toFullPath(StepsExecutor self, String filePath) {
        return FileSystems.getDefault().getPath(filePath).toAbsolutePath()
    }


    static void dir(StepsExecutor self, String dir) {
        self.workingDir = dir
    }

    static String getWorkingDir(StepsExecutor self) {
        return _workingDir.toString()
    }

    static String setWorkingDir(StepsExecutor self, String dir) {
        _workingDir = toFullPath(self,dir)
//        return self.configuration.getValueOrDefault('pipeline.workingDir','build/workspace')
    }

    static ConcurrentMap<String, Object> getParams(StepsExecutor self) {
        return params
    }
}
