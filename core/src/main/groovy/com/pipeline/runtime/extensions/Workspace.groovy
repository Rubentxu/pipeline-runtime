package com.pipeline.runtime.extensions

import com.pipeline.runtime.dsl.StepsExecutor
import com.pipeline.runtime.validations.ValidationCategory
import groovy.json.JsonSlurper
import groovy.transform.NamedParam
import groovy.transform.NamedParams
import groovy.xml.slurpersupport.GPathResult
import org.apache.commons.io.FileUtils
import org.yaml.snakeyaml.Yaml

import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import groovy.xml.XmlSlurper


class Workspace {
    static Path _baseWorkingDir
    static Path _workingDir
    static final List filesConfig = []

    static void initializeWorkspace(StepsExecutor self, Map configuration) {
        use(ValidationCategory) {
            _baseWorkingDir = toFullPath(self, configuration.validateAndGet('pipeline.workingDir').isString().throwIfInvalid("Error Base WorkingDir path not found"))
            String gitName = configuration.validateAndGet('pipeline.scm.gitscm.userRemoteConfigs').is(List.class).defaultValueIfInvalid([])[0]?.name
            String jobName = configuration.validateAndGet('pipeline.environmentVars.JOB_NAME').isString().defaultValueIfInvalid(gitName ?: 'Job')
            _workingDir = _baseWorkingDir.normalize().resolve(jobName)
            self.getCurrentBuild().setDisplayName(jobName)
        }

        if (_workingDir.toFile().exists()) {
            FileUtils.cleanDirectory(_workingDir.toFile())
        }
        Files.createDirectories(_workingDir)


    }

    static Path toFullPath(StepsExecutor self, String filePath) {
        return FileSystems.getDefault().getPath(filePath).toAbsolutePath()
    }

    static String getWorkingDir(StepsExecutor self) {
        return _workingDir.toString()
    }

    static String getBaseWorkingDir(StepsExecutor self) {
        return _baseWorkingDir.toString()
    }

    static String setWorkingDir(StepsExecutor self, String dir) {
        _workingDir = toFullPath(self, dir)
//        return self.configuration.getValueOrDefault('pipeline.workingDir','build/workspace')
    }

    static Boolean fileExists(StepsExecutor self, @NamedParam(value = "file", type = String, required = true) final Map param) {
        Boolean exist = this._workingDir.resolve(param.file).toFile().exists()
        self.log.info "+ fileExist ${param.file}  is $exist"
        return exist
    }

    static Map readYaml(StepsExecutor self,
                        @NamedParams([
                                @NamedParam(value = "file", type = String, required = true),
                                @NamedParam(value = "text", type = String)
                        ]) final Map param) {
        Map result = [:]
        if (param.file) {
            self.log.info "+ readYaml ${param.file}"
            result =  new Yaml().load(this._workingDir.resolve(param.file).toFile().text)

        } else if (param.text) {
            self.log.info "+ readYaml"
            result = new Yaml().load(param.text as String)

        }
        return result
    }

    static def dir(StepsExecutor self,
                   @NamedParams([
                           @NamedParam(value = "path", type = String, required = true),
                           @NamedParam(value = "body", type = Closure)
                   ]) final Map param) {
        return self.dir(param.path, param.body)
    }

    static def dir(StepsExecutor self, String path, Closure body) {

        Path originalWorkingdir = this._workingDir
        this._workingDir = this._workingDir.resolve(path)
        self.log.info "+ dir in Path ${this._workingDir}"
        if(!self.fileExists(file: this._workingDir)) {
            Files.createDirectories(this._workingDir)
        }
        def result = body()
        this._workingDir = originalWorkingdir
        return result
    }

    static def findFiles(StepsExecutor self, @NamedParams([
            @NamedParam(value = "glob", type = String, required = true),
            @NamedParam(value = "excludes", type = String)
    ]) final Map param) {
        def baseDir = './'
        if (param.excludes) {
            return new FileNameFinder().getFileNames(baseDir, param.glob, param.excludes)
        } else {
            return new FileNameFinder().getFileNames(baseDir, param.glob)
        }
    }


    static def readJSON(StepsExecutor self, @NamedParams([
            @NamedParam(value = "file", type = String, required = true),
            @NamedParam(value = "text", type = String),
            @NamedParam(value = "returnPojo", type = Boolean)
    ]) final Map param) {
        if (param.file) {
            return new JsonSlurper().parseText(this._workingDir.resolve(param.file).toFile().text)
        } else if (param.text) {
            return new JsonSlurper().parseText(param.text as String)
        }
    }

    static Map readMavenPom(StepsExecutor self, @NamedParams([
            @NamedParam(value = "file", type = String, required = true)
    ]) final Map param) {
        self.readMavenPom(param.file)
    }

    static Map readMavenPom(StepsExecutor self, String file) {
        GPathResult nodes = new XmlSlurper().parseText(this._workingDir.resolve(file).toFile().text)
        return self.convertToMap(nodes)
    }

    static String readFile(StepsExecutor self, @NamedParams([
            @NamedParam(value = "file", type = String, required = true),
            @NamedParam(value = "encoding", type = String)
    ]) final Map param) {
        return self.readFile(param.file)
    }

    static String readFile(StepsExecutor self, String file) {
        if (file) {
            return this._workingDir.resolve(file).toFile().text
        }
    }

    static convertToMap(StepsExecutor self, GPathResult nodes) {
        nodes.children().collectEntries {
            [ it.name(), it.childNodes() ? self.convertToMap(it) : it.text() ]
        }
    }

    static def configFile(StepsExecutor self, @NamedParams([
            @NamedParam(value = "fileId", type = String, required = true),
            @NamedParam(value = "variable", type = String, required = true)
    ]) final Map param) {
        def configFile = self.findFileConfig(param.fileId)
        File file = this._workingDir.resolve(configFile.filePath).toFile()
        assert file.exists() : "Config File Not Found in path ${file.toString()}"
        self.env.setProperty(param.variable, file.toString())
    }

    static def storeGlobalConfigFiles(StepsExecutor self, List listConfigFile) {
        synchronized (StepsExecutor.lock) {
            this.filesConfig.addAll(listConfigFile)
        }
    }

    static def findFileConfig(StepsExecutor self, String fileId) {
        synchronized (StepsExecutor.lock) {
            return this.filesConfig.find { it.id == fileId }
        }

    }

}
