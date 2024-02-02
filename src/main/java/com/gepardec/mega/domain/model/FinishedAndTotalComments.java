package com.gepardec.mega.domain.model;

public class FinishedAndTotalComments {
    private final Long finishedComments;

    private final Long totalComments;

    private FinishedAndTotalComments(Builder builder) {
        this.finishedComments = builder.finishedComments;
        this.totalComments = builder.totalComments;
    }

    public static Builder builder() {
        return Builder.aFinishedAndTotalComments();
    }

    public Long getFinishedComments() {
        return finishedComments;
    }

    public Long getTotalComments() {
        return totalComments;
    }

    public static final class Builder {
        private Long finishedComments;
        private Long totalComments;

        private Builder() {
        }

        public static Builder aFinishedAndTotalComments() {
            return new Builder();
        }

        public Builder finishedComments(Long finishedComments) {
            this.finishedComments = finishedComments;
            return this;
        }

        public Builder totalComments(Long totalComments) {
            this.totalComments = totalComments;
            return this;
        }

        public FinishedAndTotalComments build() {
            return new FinishedAndTotalComments(this);
        }
    }
}
