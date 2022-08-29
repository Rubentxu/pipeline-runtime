package com.pipeline.runtime.extensions.domain

import org.apache.commons.lang.NotImplementedException
import org.json.JSONArray

class JobBuild implements Serializable {

    private final String externalizableId
    private final boolean currentBuild
    private Result result
    private String description
    private String displayName
    private Boolean keepLog
    private String _fullDisplayName

    JobBuild(boolean currentBuild) {
        this.externalizableId = ''
        this.currentBuild = currentBuild
    }

    JobBuild getRawBuild() {
        return this
    }

    private JobBuild build() throws Exception {
        JobBuild r = getRawBuild();
        if (r == null) {
            throw new Exception("No build record " + externalizableId + " could be located.");
        }
        return r;
    }

    void setResult(String result) throws Exception {
        if (!currentBuild) {
            throw new SecurityException("can only set the result property on the current build");
        }
        this.result = Result.getResult(result)
    }

    void setDescription(String description) throws IOException {
        if (!currentBuild) {
            throw new SecurityException("can only set the description property on the current build")
        }
        this.description = description

    }


    void setDisplayName(String displayName) throws IOException {
        if (!currentBuild) {
            throw new SecurityException("can only set the displayName property on the current build");
        }
        this.displayName = displayName
    }

    void setKeepLog(boolean keepLog) throws IOException {
        if (!currentBuild) {
            throw new SecurityException("can only set the keepLog property on the current build");
        }
        this.keepLog = keepLog
    }

    int getNumber() throws Exception {
        return build().getNumber()
    }


    public JSONArray getBuildCauses() throws IOException, ClassNotFoundException {
        JSONArray result = new JSONArray()
        return result
    }

    public JSONArray getBuildCauses(String className) throws IOException {
        JSONArray result = new JSONArray()
        return result
    }

    Result getResult() throws Exception {
      return result
    }

    String getCurrentResult() throws Exception {
        return result != null ? result.toString() : Result.SUCCESS.toString()
    }

    public boolean resultIsBetterOrEqualTo(String other) throws Exception {
        if (result == null) {
            result = Result.SUCCESS;
        }
        Result otherResult = Result.getResult(other)
        return result.isBetterOrEqualTo(otherResult)
    }

    public boolean resultIsWorseOrEqualTo(String other) throws Exception {
        if (result == null) {
            result = Result.SUCCESS;
        }
        Result otherResult = Result.getResult(other)
        return result.isWorseOrEqualTo(otherResult)
    }

    public long getTimeInMillis() throws Exception {
        return 1L
    }

    public long getStartTimeInMillis() throws Exception {
        return 1L
    }

    public long getDuration() throws Exception {
        return System.currentTimeMillis() - getStartTimeInMillis();
    }

    public String getDurationString() throws Exception {
        return getDuration().toString()
    }

    public String getDescription() throws Exception {
        return description
    }

    public String getDisplayName() throws Exception {
        return displayName
    }

    public String getFullDisplayName() throws Exception {
        return this._fullDisplayName
    }

    public boolean isKeepLog() throws Exception {
        return this.keepLog
    }

    public String getProjectName() throws Exception {
        throw new NotImplementedException()
    }

    public String getFullProjectName() throws Exception {
        throw new NotImplementedException()
    }

    Map<String, String> getBuildVariables() throws Exception {
       throw new NotImplementedException()
    }

    String getAbsoluteUrl() throws Exception {
        throw new NotImplementedException()
    }

    JobBuild getParent() {
        return this
    }
}
