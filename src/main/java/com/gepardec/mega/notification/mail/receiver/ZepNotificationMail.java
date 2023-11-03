package com.gepardec.mega.notification.mail.receiver;

import com.google.common.base.MoreObjects;

import java.time.LocalDate;

public class ZepNotificationMail {

    private final LocalDate tag;
    private final String mitarbeiterVorname;
    private final String mitarbeiterNachname;
    private final String buchungInfo;
    private final String projekt;
    private final String vorgang;
    private final String bemerkung;

    private ZepNotificationMail(Builder source) {
        this.tag = source.tag;
        this.mitarbeiterVorname = source.mitarbeiterVorname;
        this.mitarbeiterNachname = source.mitarbeiterNachname;
        this.buchungInfo = source.buchungInfo;
        this.projekt = source.projekt;
        this.vorgang = source.vorgang;
        this.bemerkung = source.bemerkung;
    }

    public static Builder builder() {
        return new Builder();
    }

    public LocalDate getTag() {
        return tag;
    }

    public String getMitarbeiterVorname() {
        return mitarbeiterVorname;
    }

    public String getMitarbeiterNachname() {
        return mitarbeiterNachname;
    }

    public String getBuchungInfo() {
        return buchungInfo;
    }

    public String getProjekt() {
        return projekt;
    }

    public String getVorgang() {
        return vorgang;
    }

    public String getBemerkung() {
        return bemerkung;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("tag", tag)
                .add("mitarbeiterVorname", mitarbeiterVorname)
                .add("mitarbeiterNachname", mitarbeiterNachname)
                .add("buchungInfo", buchungInfo)
                .add("projekt", projekt)
                .add("vorgang", vorgang)
                .add("bemerkung", bemerkung)
                .toString();
    }

    public static final class Builder {

        private LocalDate tag;
        private String mitarbeiterVorname;
        private String mitarbeiterNachname;
        private String buchungInfo;
        private String projekt;
        private String vorgang;
        private String bemerkung;

        public Builder withTag(LocalDate tag) {
            this.tag = tag;
            return this;
        }

        public Builder withMitarbeiterVorname(String mitarbeiterVorname) {
            this.mitarbeiterVorname = mitarbeiterVorname;
            return this;
        }

        public Builder withMitarbeiterNachname(String mitarbeiterNachname) {
            this.mitarbeiterNachname = mitarbeiterNachname;
            return this;
        }

        public Builder withBuchungInfo(String buchungInfo) {
            this.buchungInfo = buchungInfo;
            return this;
        }

        public Builder withProjekt(String projekt) {
            this.projekt = projekt;
            return this;
        }

        public Builder withVorgang(String vorgang) {
            this.vorgang = vorgang;
            return this;
        }

        public Builder withBemerkung(String bemerkung) {
            this.bemerkung = bemerkung;
            return this;
        }

        public ZepNotificationMail build() {
            return new ZepNotificationMail(this);
        }
    }
}
