package com.meliodas.plantitotita.mainmodule;

import java.util.*;

public final class Plant {
    private final String identification;
    private final String name;
    private final String scientificName;
    private final String family;
    private final String genus;
    private final String image;
    private final String description;
    private final String wikiUrl;
    private final List<String> commonNames;
    private final HashMap<String, String> taxonomy;
    private final String url;
    private final int gbifId;
    private final int inaturalistId;
    private final String rank;
    private final List<String> synonyms;
    private final Map<String, String> imageDetails;
    private final ArrayList<String> edibleParts;
    private final ArrayList<String> propagationMethods;
    private final String bestLightCondition;
    private final String bestWatering;
    private final String bestSoilType;
    private final String commonUses;
    private final String toxicity;
    private final String culturalSignificance;

    private Plant(Builder builder) {
        this.identification = builder.identification;
        this.name = builder.name;
        this.scientificName = builder.scientificName;
        this.family = builder.family;
        this.genus = builder.genus;
        this.image = builder.image;
        this.description = builder.description;
        this.wikiUrl = builder.wikiUrl;
        this.commonNames = builder.commonNames;
        this.taxonomy = builder.taxonomy;
        this.url = builder.url;
        this.gbifId = builder.gbifId;
        this.inaturalistId = builder.inaturalistId;
        this.rank = builder.rank;
        this.synonyms = builder.synonyms;
        this.imageDetails = builder.imageDetails;
        this.edibleParts = builder.edibleParts;
        this.propagationMethods = builder.propagationMethods;
        this.bestLightCondition = builder.bestLightCondition;
        this.bestWatering = builder.bestWatering;
        this.bestSoilType = builder.bestSoilType;
        this.commonUses = builder.commonUses;
        this.toxicity = builder.toxicity;
        this.culturalSignificance = builder.culturalSignificance;
    }

    public static class Builder {
        private String identification;
        private String name;
        private String scientificName;
        private String family;
        private String genus;
        private String image;
        private String description;
        private String wikiUrl;
        private List<String> commonNames = List.of();
        private HashMap<String, String> taxonomy = new HashMap<>();
        private String url = "";
        private int gbifId = 0;
        private int inaturalistId = 0;
        private String rank = "";
        private List<String> synonyms = List.of();
        private Map<String, String> imageDetails = Map.of();
        private ArrayList<String> edibleParts = new ArrayList<>();
        private ArrayList<String> propagationMethods = new ArrayList<>();
        private String bestLightCondition;
        private String bestWatering;
        private String bestSoilType;
        private String commonUses;
        private String toxicity;
        private String culturalSignificance;

        public Builder identification(String identification) {
            this.identification = identification;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder scientificName(String scientificName) {
            this.scientificName = scientificName;
            return this;
        }

        public Builder family(String family) {
            this.family = family;
            return this;
        }

        public Builder genus(String genus) {
            this.genus = genus;
            return this;
        }

        public Builder image(String image) {
            this.image = image;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder wikiUrl(String wikiUrl) {
            this.wikiUrl = wikiUrl;
            return this;
        }

        public Builder commonNames(List<String> commonNames) {
            this.commonNames = commonNames;
            return this;
        }

        public Builder taxonomy(HashMap<String, String> taxonomy) {
            this.taxonomy = taxonomy;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder gbifId(int gbifId) {
            this.gbifId = gbifId;
            return this;
        }

        public Builder inaturalistId(int inaturalistId) {
            this.inaturalistId = inaturalistId;
            return this;
        }

        public Builder rank(String rank) {
            this.rank = rank;
            return this;
        }

        public Builder synonyms(List<String> synonyms) {
            this.synonyms = synonyms;
            return this;
        }

        public Builder imageDetails(Map<String, String> imageDetails) {
            this.imageDetails = imageDetails;
            return this;
        }

        public Builder edibleParts(ArrayList<String> edibleParts) {
            this.edibleParts = edibleParts;
            return this;
        }

        public Builder propagationMethods(ArrayList<String> propagationMethods) {
            this.propagationMethods = propagationMethods;
            return this;
        }

        public Builder bestLightCondition(String bestLightCondition) {
            this.bestLightCondition = bestLightCondition;
            return this;
        }

        public Builder bestWatering(String bestWatering) {
            this.bestWatering = bestWatering;
            return this;
        }

        public Builder bestSoilType(String bestSoilType) {
            this.bestSoilType = bestSoilType;
            return this;
        }

        public Builder commonUses(String commonUses) {
            this.commonUses = commonUses;
            return this;
        }

        public Builder toxicity(String toxicity) {
            this.toxicity = toxicity;
            return this;
        }

        public Builder culturalSignificance(String culturalSignificance) {
            this.culturalSignificance = culturalSignificance;
            return this;
        }

        public Plant build() {
            return new Plant(this);
        }
    }

    // Getters for all fields
    public String identification() {
        return identification;
    }

    public String name() {
        return name;
    }

    public String scientificName() {
        return scientificName;
    }

    public String family() {
        return family;
    }

    public String genus() {
        return genus;
    }

    public String image() {
        return image;
    }

    public String description() {
        return description;
    }

    public String wikiUrl() {
        return wikiUrl;
    }

    public List<String> commonNames() {
        return commonNames;
    }

    public HashMap<String, String> taxonomy() {
        return taxonomy;
    }

    public String url() {
        return url;
    }

    public int gbifId() {
        return gbifId;
    }

    public int inaturalistId() {
        return inaturalistId;
    }

    public String rank() {
        return rank;
    }

    public List<String> synonyms() {
        return synonyms;
    }

    public Map<String, String> imageDetails() {
        return imageDetails;
    }

    public ArrayList<String> edibleParts() {
        return edibleParts;
    }
    public ArrayList<String> propagationMethods() {
        return propagationMethods;
    }

    public String bestLightCondition() {
        return bestLightCondition;
    }

    public String bestWatering() {
        return bestWatering;
    }

    public String bestSoilType() {
        return bestSoilType;
    }

    public String commonUses() {
        return commonUses;
    }

    public String toxicity() {
        return toxicity;
    }

    public String culturalSignificance() {
        return culturalSignificance;
    }

    @Override
    public String toString() {
        return "Plant{" +
                "identification='" + identification + '\'' +
                ", name='" + name + '\'' +
                ", scientificName='" + scientificName + '\'' +
                ", family='" + family + '\'' +
                ", genus='" + genus + '\'' +
                ", image='" + image + '\'' +
                ", description='" + description + '\'' +
                ", wikiUrl='" + wikiUrl + '\'' +
                ", commonNames=" + commonNames +
                ", taxonomy=" + taxonomy +
                ", url='" + url + '\'' +
                ", gbifId=" + gbifId +
                ", inaturalistId=" + inaturalistId +
                ", rank='" + rank + '\'' +
                ", synonyms=" + synonyms +
                ", imageDetails=" + imageDetails +
                ", edibleParts=" + edibleParts +
                ", propagationMethods=" + propagationMethods +
                ", bestLightCondition='" + bestLightCondition + '\'' +
                ", bestWatering='" + bestWatering + '\'' +
                ", bestSoilType='" + bestSoilType + '\'' +
                ", commonUses='" + commonUses + '\'' +
                ", toxicity='" + toxicity + '\'' +
                ", culturalSignificance='" + culturalSignificance + '\'' +
                '}';
    }
}