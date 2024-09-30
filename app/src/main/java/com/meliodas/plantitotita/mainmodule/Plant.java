package com.meliodas.plantitotita.mainmodule;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    private final Map<String, String> taxonomy;
    private final String url;
    private final int gbifId;
    private final int inaturalistId;
    private final String rank;
    private final List<String> synonyms;
    private final Map<String, String> imageDetails;
    private final String edibleParts;

    public Plant(String identification, String name, String scientificName, String family, String genus,
                 String image, String description, String wikiUrl, List<String> commonNames,
                 Map<String, String> taxonomy, String url, int gbifId, int inaturalistId, String rank,
                 List<String> synonyms, Map<String, String> imageDetails, String edibleParts) {
        this.identification = identification;
        this.name = name;
        this.scientificName = scientificName;
        this.family = family;
        this.genus = genus;
        this.image = image;
        this.description = description;
        this.wikiUrl = wikiUrl;
        this.commonNames = commonNames;
        this.taxonomy = taxonomy;
        this.url = url;
        this.gbifId = gbifId;
        this.inaturalistId = inaturalistId;
        this.rank = rank;
        this.synonyms = synonyms;
        this.imageDetails = imageDetails;
        this.edibleParts = edibleParts;
    }

    public Plant(String identification, String name, String scientificName, String family, String genus, String image, String description, String wikiUrl, List<String> commonNames, String edibleParts) {
        this.identification = identification;
        this.name = name;
        this.scientificName = scientificName;
        this.family = family;
        this.genus = genus;
        this.image = image;
        this.description = description;
        this.wikiUrl = wikiUrl;
        this.commonNames = commonNames;
        this.edibleParts = edibleParts;
        this.taxonomy = Map.of();
        this.url = "";
        this.gbifId = 0;
        this.inaturalistId = 0;
        this.rank = "";
        this.synonyms = List.of();
        this.imageDetails = Map.of();
    }

    public Plant(String identification, String plantName, String plantScientificName, String family, String genus, String plantImage, String description, String wikiUrl, String edibleParts) {
        this.identification = identification;
        this.name = plantName;
        this.scientificName = plantScientificName;
        this.family = family;
        this.genus = genus;
        this.image = plantImage;
        this.description = description;
        this.wikiUrl = wikiUrl;
        this.edibleParts = edibleParts;
        this.commonNames = List.of();
        this.taxonomy = Map.of();
        this.url = "";
        this.gbifId = 0;
        this.inaturalistId = 0;
        this.rank = "";
        this.synonyms = List.of();
        this.imageDetails = Map.of();
    }

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

    public Map<String, String> taxonomy() {
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

    public String edibleParts() {
        return edibleParts;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Plant) obj;
        return Objects.equals(this.identification, that.identification) &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.scientificName, that.scientificName) &&
                Objects.equals(this.family, that.family) &&
                Objects.equals(this.genus, that.genus) &&
                Objects.equals(this.image, that.image) &&
                Objects.equals(this.description, that.description) &&
                Objects.equals(this.wikiUrl, that.wikiUrl) &&
                Objects.equals(this.commonNames, that.commonNames) &&
                Objects.equals(this.taxonomy, that.taxonomy) &&
                Objects.equals(this.url, that.url) &&
                this.gbifId == that.gbifId &&
                this.inaturalistId == that.inaturalistId &&
                Objects.equals(this.rank, that.rank) &&
                Objects.equals(this.synonyms, that.synonyms) &&
                Objects.equals(this.imageDetails, that.imageDetails) &&
                Objects.equals(this.edibleParts, that.edibleParts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identification, name, scientificName, family, genus, image, description, wikiUrl,
                commonNames, taxonomy, url, gbifId, inaturalistId, rank, synonyms, imageDetails, edibleParts);
    }

    @Override
    public @NotNull String toString() {
        return "Plant[" +
                "identification=" + identification + ", " +
                "name=" + name + ", " +
                "scientificName=" + scientificName + ", " +
                "family=" + family + ", " +
                "genus=" + genus + ", " +
                "image=" + image + ", " +
                "description=" + description + ", " +
                "wikiUrl=" + wikiUrl + ", " +
                "commonNames=" + commonNames + ", " +
                "taxonomy=" + taxonomy + ", " +
                "url=" + url + ", " +
                "gbifId=" + gbifId + ", " +
                "inaturalistId=" + inaturalistId + ", " +
                "rank=" + rank + ", " +
                "synonyms=" + synonyms + ", " +
                "imageDetails=" + imageDetails + "," +
                "edibleParts=" + edibleParts + "]";
    }
}