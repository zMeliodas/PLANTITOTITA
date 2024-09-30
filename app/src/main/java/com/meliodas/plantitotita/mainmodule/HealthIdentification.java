package com.meliodas.plantitotita.mainmodule;

import java.util.List;

public class HealthIdentification {
    private final Plant plant;
    private final Result result;

    private HealthIdentification(Builder builder) {
        this.result = builder.result;
        this.plant = builder.plant;
    }

    public Result getResult() {
        return result;
    }

    public Plant getPlant() {
        return plant;
    }

    public static class Builder {
        private Plant plant;
        private Result result;

        public Builder result(Result result) {
            this.result = result;
            return this;
        }

        public Builder plant(Plant plant) {
            this.plant = plant;
            return this;
        }

        public HealthIdentification build() {
            return new HealthIdentification(this);
        }
    }

    public static class Result {
        private final IsPlant isPlant;
        private final IsHealthy isHealthy;
        private final Disease disease;

        private Result(Builder builder) {
            this.isPlant = builder.isPlant;
            this.isHealthy = builder.isHealthy;
            this.disease = builder.disease;
        }

        public IsPlant getIsPlant() {
            return isPlant;
        }

        public IsHealthy getIsHealthy() {
            return isHealthy;
        }

        public Disease getDisease() {
            return disease;
        }

        public static class Builder {
            private IsPlant isPlant;
            private IsHealthy isHealthy;
            private Disease disease;

            public Builder isPlant(IsPlant isPlant) {
                this.isPlant = isPlant;
                return this;
            }

            public Builder isHealthy(IsHealthy isHealthy) {
                this.isHealthy = isHealthy;
                return this;
            }

            public Builder disease(Disease disease) {
                this.disease = disease;
                return this;
            }

            public Result build() {
                return new Result(this);
            }
        }

        public static class IsPlant {
            private final double probability;
            private final double threshold;
            private final boolean binary;

            private IsPlant(Builder builder) {
                this.probability = builder.probability;
                this.threshold = builder.threshold;
                this.binary = builder.binary;
            }

            public double getProbability() {
                return probability;
            }

            public double getThreshold() {
                return threshold;
            }

            public boolean isBinary() {
                return binary;
            }

            public static class Builder {
                private double probability;
                private double threshold;
                private boolean binary;

                public Builder probability(double probability) {
                    this.probability = probability;
                    return this;
                }

                public Builder threshold(double threshold) {
                    this.threshold = threshold;
                    return this;
                }

                public Builder binary(boolean binary) {
                    this.binary = binary;
                    return this;
                }

                public IsPlant build() {
                    return new IsPlant(this);
                }
            }
        }

        public static class IsHealthy {
            private final boolean binary;
            private final double threshold;
            private final double probability;

            private IsHealthy(Builder builder) {
                this.binary = builder.binary;
                this.threshold = builder.threshold;
                this.probability = builder.probability;
            }

            public boolean isBinary() {
                return binary;
            }

            public double getThreshold() {
                return threshold;
            }

            public double getProbability() {
                return probability;
            }

            public static class Builder {
                private boolean binary;
                private double threshold;
                private double probability;

                public Builder binary(boolean binary) {
                    this.binary = binary;
                    return this;
                }

                public Builder threshold(double threshold) {
                    this.threshold = threshold;
                    return this;
                }

                public Builder probability(double probability) {
                    this.probability = probability;
                    return this;
                }

                public IsHealthy build() {
                    return new IsHealthy(this);
                }
            }
        }

        public static class Disease {
            private final List<Suggestion> suggestions;

            private Disease(Builder builder) {
                this.suggestions = builder.suggestions;
            }

            public List<Suggestion> getSuggestions() {
                return suggestions;
            }

            public static class Builder {
                private List<Suggestion> suggestions;

                public Builder suggestions(List<Suggestion> suggestions) {
                    this.suggestions = suggestions;
                    return this;
                }

                public Disease build() {
                    return new Disease(this);
                }
            }

            public static class Suggestion {
                private final String id;
                private final String name;
                private final double probability;
                private final List<SimilarImage> similarImages;
                private final Details details;

                private Suggestion(Builder builder) {
                    this.id = builder.id;
                    this.name = builder.name;
                    this.probability = builder.probability;
                    this.similarImages = builder.similarImages;
                    this.details = builder.details;
                }

                public String getId() {
                    return id;
                }

                public String getName() {
                    return name;
                }

                public double getProbability() {
                    return probability;
                }

                public List<SimilarImage> getSimilarImages() {
                    return similarImages;
                }

                public Details getDetails() {
                    return details;
                }

                public static class Builder {
                    private String id;
                    private String name;
                    private double probability;
                    private List<SimilarImage> similarImages;
                    private Details details;

                    public Builder id(String id) {
                        this.id = id;
                        return this;
                    }

                    public Builder name(String name) {
                        this.name = name;
                        return this;
                    }

                    public Builder probability(double probability) {
                        this.probability = probability;
                        return this;
                    }

                    public Builder similarImages(List<SimilarImage> similarImages) {
                        this.similarImages = similarImages;
                        return this;
                    }

                    public Builder details(Details details) {
                        this.details = details;
                        return this;
                    }

                    public Suggestion build() {
                        return new Suggestion(this);
                    }
                }

                public static class SimilarImage {
                    private final String id;
                    private final String url;
                    private final double similarity;
                    private final String urlSmall;

                    private SimilarImage(Builder builder) {
                        this.id = builder.id;
                        this.url = builder.url;
                        this.similarity = builder.similarity;
                        this.urlSmall = builder.urlSmall;
                    }

                    public String getUrlSmall() {
                        return urlSmall;
                    }

                    public double getSimilarity() {
                        return similarity;
                    }

                    public String getUrl() {
                        return url;
                    }

                    public String getId() {
                        return id;
                    }

                    public static class Builder {
                        private String id;
                        private String url;
                        private double similarity;
                        private String urlSmall;

                        public Builder id(String id) {
                            this.id = id;
                            return this;
                        }

                        public Builder url(String url) {
                            this.url = url;
                            return this;
                        }

                        public Builder similarity(double similarity) {
                            this.similarity = similarity;
                            return this;
                        }

                        public Builder urlSmall(String urlSmall) {
                            this.urlSmall = urlSmall;
                            return this;
                        }

                        public SimilarImage build() {
                            return new SimilarImage(this);
                        }
                    }
                }

                public static class Details {
                    private final String localName;
                    private final String description;
                    private final String url;
                    private final Treatment treatment;
                    private final List<String> classification;
                    private final List<String> commonNames;
                    private final String cause;
                    private final String language;
                    private final String entityId;

                    private Details(Builder builder) {
                        this.localName = builder.localName;
                        this.description = builder.description;
                        this.url = builder.url;
                        this.treatment = builder.treatment;
                        this.classification = builder.classification;
                        this.commonNames = builder.commonNames;
                        this.cause = builder.cause;
                        this.language = builder.language;
                        this.entityId = builder.entityId;
                    }

                    public String getLocalName() {
                        return localName;
                    }

                    public String getDescription() {
                        return description;
                    }

                    public String getUrl() {
                        return url;
                    }

                    public Treatment getTreatment() {
                        return treatment;
                    }

                    public List<String> getClassification() {
                        return classification;
                    }

                    public List<String> getCommonNames() {
                        return commonNames;
                    }

                    public String getCause() {
                        return cause;
                    }

                    public String getLanguage() {
                        return language;
                    }

                    public String getEntityId() {
                        return entityId;
                    }

                    public static class Builder {
                        private String localName;
                        private String description;
                        private String url;
                        private Treatment treatment;
                        private List<String> classification;
                        private List<String> commonNames;
                        private String cause;
                        private String language;
                        private String entityId;

                        public Builder localName(String localName) {
                            this.localName = localName;
                            return this;
                        }

                        public Builder description(String description) {
                            this.description = description;
                            return this;
                        }

                        public Builder url(String url) {
                            this.url = url;
                            return this;
                        }

                        public Builder treatment(Treatment treatment) {
                            this.treatment = treatment;
                            return this;
                        }

                        public Builder classification(List<String> classification) {
                            this.classification = classification;
                            return this;
                        }

                        public Builder commonNames(List<String> commonNames) {
                            this.commonNames = commonNames;
                            return this;
                        }

                        public Builder cause(String cause) {
                            this.cause = cause;
                            return this;
                        }

                        public Builder language(String language) {
                            this.language = language;
                            return this;
                        }

                        public Builder entityId(String entityId) {
                            this.entityId = entityId;
                            return this;
                        }

                        public Details build() {
                            return new Details(this);
                        }
                    }

                    public static class Treatment {
                        private final List<String> prevention;
                        private final List<String> biological;
                        private final List<String> chemical;

                        private Treatment(Builder builder) {
                            this.prevention = builder.prevention;
                            this.biological = builder.biological;
                            this.chemical = builder.chemical;
                        }

                        public List<String> getPrevention() {
                            return prevention;
                        }

                        public List<String> getBiological() {
                            return biological;
                        }

                        public List<String> getChemical() {
                            return chemical;
                        }

                        public static class Builder {
                            private List<String> prevention;
                            private List<String> biological;
                            private List<String> chemical;

                            public Builder prevention(List<String> prevention) {
                                this.prevention = prevention;
                                return this;
                            }

                            public Builder biological(List<String> biological) {
                                this.biological = biological;
                                return this;
                            }

                            public Builder chemical(List<String> chemical) {
                                this.chemical = chemical;
                                return this;
                            }

                            public Treatment build() {
                                return new Treatment(this);
                            }
                        }
                    }
                }
            }
        }
    }
}