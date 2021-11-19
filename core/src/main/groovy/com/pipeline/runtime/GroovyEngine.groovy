package com.pipeline.runtime

class GroovyEngine {
//    public static void main(String[] args) throws Exception {
//        def basePath = ''
//        String[] paths = ["$basePath/pipeline-runtime/src/groovy", "$basePath/src" , "$basePath/vars" ];
//
//        GroovyScriptEngine gse = new GroovyScriptEngine(paths);
//        Binding binding = new Binding();
//
////        def config = gse.loadScriptByName("config.groovy")
////        def instance = config.newInstance()
////        binding.setVariable("config", instance);
//        def tools = new GroovyScriptEngine( '.' ).with {
//            loadScriptByName( 'config.groovy' )
//        }
//        this.metaClass.mixin
//        String result = (String) gse.run("$basePath/pipeline-runtime/src/test/groovy/pipelines/Jenkinsfile_example.groovy", binding);
//        System.out.println("Groovy Result: "  + result);
//
//    }
//
//    public void executeScriptByPath(String scriptPath, Map<Object, Object> context) {
//        try {
//            String scriptName = PathUtil.fileName(scriptPath);
//            String scriptDir = PathUtil.parentFolder(scriptPath);
//
//            GroovyScriptEngine gse = new GroovyScriptEngine(scriptDir);
//            Binding binding = new Binding(context);
//            gse.run(scriptName, binding);
//        } catch (Exception e) {
//            throw new Exception(e);
//        }
//    }
}
