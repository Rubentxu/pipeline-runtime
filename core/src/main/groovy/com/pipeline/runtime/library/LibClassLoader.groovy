package com.pipeline.runtime.library

import com.pipeline.runtime.PipelineRuntime
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.MethodUtils;


class LibClassLoader {
    private String className
    private PipelineRuntime pruntime
    Class loadedClass

    LibClassLoader(pruntime, String className){
        this.pruntime = pruntime
        this.className = className
        this.loadedClass = null
    }

    LibClassLoader(pruntime, String className, loadedClass){
        this.pruntime = pruntime
        this.className = className
        this.loadedClass = loadedClass
    }


    Object getProperty(String property) {

        if(loadedClass) {
            return loadedClass.getProperties()[property]
        }

        if(!this.className) {
            return new LibClassLoader(this.pruntime, property)
        }

        if(property =~ /^[A-Z].*/) {

            def gcl = getLibLoader().getGroovyClassLoader()
            loadedClass = gcl.loadClass( (String) "${this.className}.${property}")
            return new LibClassLoader(this.pruntime, "${this.className}.${property}", loadedClass)
        } else {
            return new LibClassLoader(this.pruntime, "${this.className}.${property}")
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