package com.pipeline.runtime.extensions


import spock.lang.Specification

//@CompileStatic
class GitSCMSpec extends Specification {
    def "length of Spock's and his friends' names"() {
        expect:
        name.size() == length

        where:
        name     | length
        "Spock"  | 5
        "Kirk"   | 4
        "Scotty" | 6
    }
}