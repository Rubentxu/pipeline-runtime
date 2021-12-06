package com.pipeline.runtime.extensions

import com.pipeline.runtime.dsl.NodeDsl

abstract class StepsExtensions extends Script implements Shell, Credentials, Environment, GitSCMSteps,
        Workspace, NodeDsl {



}