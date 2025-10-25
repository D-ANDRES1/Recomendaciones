package com.example.recomendaciones_app.data.model.foursquare;

import java.util.List;

public class FoursquareResponse {
    private List<FoursquarePlace> results;

    public List<FoursquarePlace> getResults() {
        return results;
    }

    public void setResults(List<FoursquarePlace> results) {
        this.results = results;
    }
}
