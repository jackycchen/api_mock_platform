package com.apimock.dto.api;

import java.util.List;

public class ApiVersionCompareResponse {

    private boolean hasDifferences;
    private List<VersionDifference> differences;

    public boolean isHasDifferences() {
        return hasDifferences;
    }

    public void setHasDifferences(boolean hasDifferences) {
        this.hasDifferences = hasDifferences;
    }

    public List<VersionDifference> getDifferences() {
        return differences;
    }

    public void setDifferences(List<VersionDifference> differences) {
        this.differences = differences;
    }

    public static class VersionDifference {
        private String field;
        private String beforeValue;
        private String afterValue;

        public VersionDifference() {
        }

        public VersionDifference(String field, String beforeValue, String afterValue) {
            this.field = field;
            this.beforeValue = beforeValue;
            this.afterValue = afterValue;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getBeforeValue() {
            return beforeValue;
        }

        public void setBeforeValue(String beforeValue) {
            this.beforeValue = beforeValue;
        }

        public String getAfterValue() {
            return afterValue;
        }

        public void setAfterValue(String afterValue) {
            this.afterValue = afterValue;
        }
    }
}
