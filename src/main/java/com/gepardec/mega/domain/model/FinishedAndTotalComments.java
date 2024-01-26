package com.gepardec.mega.domain.model;

public class FinishedAndTotalComments {
    private final Long finishedComments;

    private final Long totalComments;

    public FinishedAndTotalComments(builder builder) {
        this.finishedComments = builder.finishedComments;
        this.totalComments = builder.totalComments;
    }

    public static builder builder() {
        return builder.aFinishedAndTotalComments();
    }

    public Long getFinishedComments() {
        return finishedComments;
    }

    public Long getTotalComments() {
        return totalComments;
    }

    public static final class builder {
        private Long finishedComments;
        private Long totalComments;

        private builder() {
        }

        public static builder aFinishedAndTotalComments() {
            return new builder();
        }

        public builder finishedComments(Long finishedComments) {
            this.finishedComments = finishedComments;
            return this;
        }

        public builder totalComments(Long totalComments) {
            this.totalComments = totalComments;
            return this;
        }

        public FinishedAndTotalComments build() {
           return new FinishedAndTotalComments(this);
        }
    }
}
