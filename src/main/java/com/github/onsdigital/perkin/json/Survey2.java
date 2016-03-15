package com.github.onsdigital.perkin.json;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Simple object to contain Survey data.
 */
@Data
@Builder
public class Survey2 {

    private String type;
    private String version;
    private String origin;
    @SerializedName("survey_id")
    private String id;

    private Collection collection;

    @SerializedName("submitted_at")
    private Date date;

    private Metadata metadata;

    @Singular("answer")
    @SerializedName("data")
    private Map<String, String> answers;

    public String getAnswer(String key) {
        if (answers == null) {
            return null;
        }

        return answers.get(key);
    }

    public Set<String> getKeys() {
        if (answers == null) {
            return Collections.emptySet();
        }

        return answers.keySet();
    }

    //TODO: move to Metadata class
    public String getRespondentId() {
        return getMetadata().getRuRef().substring(0, 11);
    }

    //TODO: move to Metadata class
    public char getRespondentCheckLetter() {
        return getMetadata().getRuRef().substring(11, 12).charAt(0);
    }
}