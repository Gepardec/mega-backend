package com.gepardec.mega.notification.mail;

public enum Mail {
    EMPLOYEE_CHECK_PROJECTTIME(-1, MailType.WORKING_DAY_BASED, Constants.EMAILS_REMINDER_TEMPLATE_HTML),
    OM_CONTROL_EMPLOYEES_CONTENT(3, MailType.WORKING_DAY_BASED, Constants.EMAILS_REMINDER_TEMPLATE_HTML),
    PL_PROJECT_CONTROLLING(5, MailType.WORKING_DAY_BASED, Constants.EMAILS_REMINDER_TEMPLATE_HTML),
    OM_RELEASE(-5, MailType.WORKING_DAY_BASED, Constants.EMAILS_REMINDER_TEMPLATE_HTML),
    OM_ADMINISTRATIVE(15, MailType.DAY_OF_MONTH_BASED, Constants.EMAILS_REMINDER_TEMPLATE_HTML),
    OM_SALARY(-3, MailType.WORKING_DAY_BASED, Constants.EMAILS_REMINDER_TEMPLATE_HTML),
    COMMENT_CLOSED(MailType.MANUAL),
    COMMENT_CREATED(MailType.MANUAL),
    COMMENT_DELETED(MailType.MANUAL),
    COMMENT_MODIFIED(MailType.MANUAL),
    ZEP_COMMENT_PROCESSING_ERROR(MailType.MANUAL);

    private final Integer day;

    private final MailType type;

    private final String template;

    Mail(MailType type) {
        this(null, type, null);
    }

    Mail(Integer day, MailType type, String template) {
        this.day = day;
        this.type = type;
        this.template = template;
    }

    public Integer getDay() {
        return day;
    }

    public MailType getType() {
        return type;
    }

    public String getTemplate() {
        return template;
    }

    private static class Constants {
        public static final String EMAILS_REMINDER_TEMPLATE_HTML = "emails/reminder-template.html";
    }
}
