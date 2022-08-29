package com.pipeline.runtime.extensions.domain

enum Result {
    SUCCESS("SUCCESS",true),
    UNSTABLE("UNSTABLE",true),
    FAILURE("FAILURE",true),
    NOT_BUILT("NOT_BUILT",false),
    ABORTED("ABORTED",false)

    final String id;
    final Boolean interrupted
    static final Map map

    static {
        map = [:] as TreeMap
        values().each{ result ->
            println "id: " + result.id + ", interrupted:" + result.interrupted
            map.put(result.id, result)
        }

    }

    private Result(String id, Boolean interrupted) {
        this.id = id;
        this.interrupted = interrupted;
    }

    static Result getResult( id ) {
        map[id]
    }

    boolean isBetterOrEqualTo(Result that) {
        return this.ordinal <= that.ordinal;
    }

    boolean isWorseOrEqualTo(Result that) {
        return this.ordinal >= that.ordinal;
    }
}
