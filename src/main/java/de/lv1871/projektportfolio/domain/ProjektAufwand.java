package de.lv1871.projektportfolio.domain;


import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by SchuererR on 16.06.2016.
 */
public class ProjektAufwand {

    private Projekt projekt;
    private Map<Team, BigDecimal> aufwaende = new HashMap<>();

    private ProjektAufwand(Builder builder) {
        setProjekt(builder.projekt);
        setAufwaende(builder.aufwaende);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Projekt getProjekt() {
        return projekt;
    }

    public void setProjekt(Projekt projekt) {
        this.projekt = projekt;
    }

    public Map<Team, BigDecimal> getAufwaende() {
        return aufwaende;
    }

    public void setAufwaende(Map<Team, BigDecimal> aufwaende) {
        this.aufwaende = aufwaende;
    }


    public static final class Builder {
        private Projekt projekt;
        private Map<Team, BigDecimal> aufwaende;

        private Builder() {
        }

        public Builder withProjekt(Projekt val) {
            projekt = val;
            return this;
        }

        public Builder withAufwaende(Map<Team, BigDecimal> val) {
            aufwaende = val;
            return this;
        }

        public ProjektAufwand build() {
            return new ProjektAufwand(this);
        }
    }
}