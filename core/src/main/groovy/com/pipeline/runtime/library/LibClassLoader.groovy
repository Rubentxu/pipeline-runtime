package com.pipeline.runtime.library

import com.pipeline.runtime.PipelineRuntime
import com.pipeline.runtime.dsl.StepsExecutor
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.MethodUtils;


class LibClassLoader {
    private String className
    private StepsExecutor steps
    Class loadedClass

    LibClassLoader(steps, String className){
        this.steps = steps
        this.className = className
        this.loadedClass = null
    }

    LibClassLoader(steps, String className, loadedClass){
        this.steps = steps
        this.className = className
        this.loadedClass = loadedClass
    }


    Object getProperty(String property) {

        if(loadedClass) {
            return loadedClass.getProperties()[property]
        }

        if(!this.className) {
            return new LibClassLoader(this.steps, property)
        }

        if(property =~ /^[A-Z].*/) {

            def gcl = getLibLoader().getGroovyClassLoader()
            loadedClass = gcl.loadClass( (String) "${this.className}.${property}")
            return new LibClassLoader(this.steps, "${this.className}.${property}", loadedClass)
        } else {
            return new LibClassLoader(this.steps, "${this.className}.${property}")
        }
    }

 
    Object invokeMethod(String name, Object _args) {
        Object[] args = _args as Object[]
        if(loadedClass) {
            if (name.equals("new")) {
                return ConstructorUtils.invokeConstructor(loadedClass, args);
            } else {
                return MethodUtils.invokeStaticMethod(loadedClass, name, args);
            }
        }

    }

}