package com.pipeline.runtime.traits

import com.pipeline.runtime.PipelineDsl
import com.pipeline.runtime.StageDsl

import static groovy.lang.Closure.DELEGATE_FIRST
import static groovy.lang.Closure.DELEGATE_ONLY

trait CredentialsManagement {
    def urlServer = 'https://cloudbees.gcp.pipeline.com/desarrollo-cna-master/scriptText'
    def user = 'e_rcabre@pipeline.com'
    def token = '119fa458a19840fa17fd13d79492013ded'

//    method(name: 'withCredentials', type: 'Object', params: [bindings:java.util.List, body:'Closure'], doc: 'Bind credentials to variables')
    def withCredentials(List bindings, Closure body) {
        def credentials = this.sh script: """
                curl -d "script=message='Hello foo....'; println message" -v --user $user:$token $urlServer
             """, returnStdout: true

    }

//    def findCredentialsId(String id) {
//
//        def indent = { String text, int indentationCount ->
//            def replacement = "\t" * indentationCount
//            text.replaceAll("(?m)^", replacement)
//        }
//
//        def stores = Jenkins.get().allItems().collectMany{CredentialsProvider.lookupStores(it).toList()}.unique().sort()
//        def result= [:]
//        stores.forEach { store ->
//            Map<Domain, List<Credentials>> domainCreds = [:]
//            store.domains.each { domainCreds.put(it, store.getCredentials(it))}
//            if (domainCreds.collectMany{it.value}.empty) {
//                return
//            }
//
//            // Sort domainCreds by name
//            domainCreds = domainCreds.sort { it.key.name }
//
//            def shortenedClassName = store.getClass().name.substring(store.getClass().name.lastIndexOf(".") + 1)
//            domainCreds.forEach { domain , creds ->
//                // Sort creds by id
//                def myCreds = new ArrayList(creds)
//                myCreds = myCreds.sort { it.id }
//
//                myCreds.each { cred ->
//                    def temp = [
//                        store_context:  store.contextDisplayName,
//                        class_name: shortenedClassName,
//                        domain: domain.name,
//                        credentials_id: cred.id
//                    ]
//                    // Sort properties by id
//                    def properties = cred.properties.sort{it.key}
//
//                    def unimportantProps = ['descriptor', 'secretBytes', 'module', 'serviceAccountConfig', 'serviceAccountConfigDescriptors']
//
//                    properties.each { prop, val ->
//                        if(!unimportantProps.contains(prop)){
//                           temp.put(prop,val)
//                        }
//                    }
//                }
//            }
//        }
//
//    }
}
