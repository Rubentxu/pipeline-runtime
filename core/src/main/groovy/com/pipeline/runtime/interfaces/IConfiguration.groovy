package com.pipeline.runtime.interfaces

interface IConfiguration {
    def loadConfig(File configFile )
    def getValue(key)
    def containsKey(key)
    def getValueOrDefault(key, defaultValue)
    def printConfiguration()
}