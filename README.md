## Ejecutar pipeline de ejemplo con:
```bash
$ gradle examples:runScript
$ gradle clean pipeline-cli:nativeCompile
$ pipeline-cli/build/native/nativeCompile/pipeline-cli -p pipeline-cli/src/test/resources/pipelines/JenkinsfileTest.groovy \
 -c pipeline-cli/src/test/resources/pipelines/config.yaml 
 
$  pipeline-runtime/pipeline-cli/build/native/nativeCompile/pipeline-cli  -p=JenkinsfileTest.groovy -c=config.yaml   
```
