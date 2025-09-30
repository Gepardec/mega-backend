package com.gepardec.mega.notification.mail.receiver;

import com.google.common.base.MoreObjects;

import java.time.LocalDate;

public class ZepProjektzeitDetailsMail {

    private final LocalDate tag;
    private final String uhrzeitVon;
    private final String uhrzeitBis;
    private final String nachricht;
    private final String zepIdErsteller;
    private final String mitarbeiterVorname;
    private final String mitarbeiterNachname;
    private final String projekt;
    private final String vorgang;
    private final String bemerkung;
    private final String rawContent;

    private ZepProjektzeitDetailsMail(Builder source) {
        this.tag = source.tag;
        this.uhrzeitVon = source.uhrzeitVon;
        this.uhrzeitBis = source.uhrzeitBis;
        this.nachricht = source.nachricht;
        this.zepIdErsteller = source.zepIdErsteller;
        this.mitarbeiterVorname = source.mitarbeiterVorname;
        this.mitarbeiterNachname = source.mitarbeiterNachname;
        this.projekt = source.projekt;
        this.vorgang = source.vorgang;
        this.bemerkung = source.bemerkung;
        this.rawContent = source.rawContent;
    }

    public static Builder builder() {
        return new Builder();
    }

    public LocalDate getTag() {
        return tag;
    }

    public String getUhrzeitVon() {
        return uhrzeitVon;
    }

    public String getUhrzeitBis() {
        return uhrzeitBis;
    }

    public String getNachricht() {
        return nachricht;
    }

    public String getZepIdErsteller() {
        return zepIdErsteller;
    }

    public String getMitarbeiterVorname() {
        return mitarbeiterVorname;
    }

    public String getMitarbeiterNachname() {
        return mitarbeiterNachname;
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

    public String getRawContent() {
        return rawContent;
    }

    public String getMitarbeiterName() {
        return "%s, %s".formatted(mitarbeiterNachname, mitarbeiterVorname);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("tag", tag)
                .add("uhrzeitVon", uhrzeitVon)
                .add("uhrzeitBis", uhrzeitBis)
                .add("nachricht", nachricht)
                .add("zepIdErsteller", zepIdErsteller)
                .add("mitarbeiterVorname", mitarbeiterVorname)
                .add("mitarbeiterNachname", mitarbeiterNachname)
                .add("projekt", projekt)
                .add("vorgang", vorgang)
                .add("bemerkung", bemerkung)
                .add("rawContent", rawContent)
                .toString();
    }

    public static final class Builder {

        private LocalDate tag;
        private String uhrzeitVon;
        private String uhrzeitBis;
        private String nachricht;
        private String zepIdErsteller;
        private String mitarbeiterVorname;
        private String mitarbeiterNachname;
        private String projekt;
        private String vorgang;
        private String bemerkung;
        private String rawContent;

        public Builder withTag(LocalDate tag) {
            this.tag = tag;
            return this;
        }

        public Builder withUhrzeitVon(String uhrzeitVon) {
            this.uhrzeitVon = uhrzeitVon;
            return this;
        }

        public Builder withUhrzeitBis(String uhrzeitBis) {
            this.uhrzeitBis = uhrzeitBis;
            return this;
        }

        public Builder withNachricht(String nachricht) {
            this.nachricht = nachricht;
            return this;
        }

        public Builder withZepIdErsteller(String zepIdErsteller) {
            this.zepIdErsteller = zepIdErsteller;
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

        public Builder withRawContent(String rawContent) {
            this.rawContent = rawContent;
            return this;
        }

        public ZepProjektzeitDetailsMail build() {
            return new ZepProjektzeitDetailsMail(this);
        }
    }
}
