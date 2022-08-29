package com.pipeline.runtime

import com.pipeline.runtime.interfaces.IRunnerLogger


class StepLogger {
    static IRunnerLogger getLogger(Script self) {
        return PipelineRuntime.logger
    }
}
