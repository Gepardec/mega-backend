package com.gepardec.mega.domain.model;

public class FinishedAndTotalComments {
    private Long finishedComments;

    private Long totalComments;

    public FinishedAndTotalComments() {
    }

    public FinishedAndTotalComments(Long finishedComments, Long totalComments) {
        this.finishedComments = finishedComments;
        this.totalComments = totalComments;
    }

    public static FinishedAndTotalCommentsBuilder builder() {
        return FinishedAndTotalCommentsBuilder.aFinishedAndTotalComments();
    }

    public Long getFinishedComments() {
        return finishedComments;
    }

    public void setFinishedComments(Long finishedComments) {
        this.finishedComments = finishedComments;
    }

    public Long getTotalComments() {
        return totalComments;
    }

    public void setTotalComments(Long totalComments) {
        this.totalComments = totalComments;
    }

    public static final class FinishedAndTotalCommentsBuilder {
        private Long finishedComments;
        private Long totalComments;

        private FinishedAndTotalCommentsBuilder() {
        }

        public static FinishedAndTotalCommentsBuilder aFinishedAndTotalComments() {
            return new FinishedAndTotalCommentsBuilder();
        }

        public FinishedAndTotalCommentsBuilder finishedComments(Long finishedComments) {
            this.finishedComments = finishedComments;
            return this;
        }

        public FinishedAndTotalCommentsBuilder totalComments(Long totalComments) {
            this.totalComments = totalComments;
            return this;
        }

        public FinishedAndTotalComments build() {
            FinishedAndTotalComments finishedAndTotalComments = new FinishedAndTotalComments();
            finishedAndTotalComments.setFinishedComments(finishedComments);
            finishedAndTotalComments.setTotalComments(totalComments);
            return finishedAndTotalComments;
        }
    }
}
