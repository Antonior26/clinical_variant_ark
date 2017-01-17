/*
 * Copyright 2017 Genomics England Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gel.cva.storage.core.knownvariant.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.gel.models.cva.avro.*;
import org.opencb.biodata.models.variant.Variant;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 *
 * This is a wrapper class for the CuratedVariantAvro record in Avro.
 *
 * @author Pablo Riesgo Ferreiro &lt;pablo.ferreiro@genomicsengland.co.uk&gt;
 */
@JsonIgnoreProperties({"impl", "variant"})
public class KnownVariant implements Serializable {

    private KnownVariantAvro impl;
    private Variant variant;
    static private Integer INF_CURATION_SCORE = 0;
    static private Integer SUP_CURATION_SCORE = 5;
    static private Integer DEFAULT_CURATION_SCORE = 0;
    static private String DEFAULT_CURATION_CLASSIFICATION = "VUS";
    static public BiMap<Integer, CurationScore> curation_score_mapping = HashBiMap.create();
    static {
        curation_score_mapping.put(0, CurationScore.NOT_CURATED);
        curation_score_mapping.put(1, CurationScore.CURATION_CONFIDENCE_1);
        curation_score_mapping.put(2, CurationScore.CURATION_CONFIDENCE_2);
        curation_score_mapping.put(3, CurationScore.CURATION_CONFIDENCE_3);
        curation_score_mapping.put(4, CurationScore.CURATION_CONFIDENCE_4);
        curation_score_mapping.put(5, CurationScore.CURATION_CONFIDENCE_5);
    };

    /**
     * Empty constructor, set default values
     */
    public KnownVariant() {
        this.variant = new Variant();
        this.impl = new KnownVariantAvro(
                this.variant.getImpl(),
                this.getDefaultCurationClassification(),
                this.getDefaultCurationScore(),
                this.getDefaultCurationHistory(),
                this.getDefaultEvidences(),
                this.getDefaultComments()
        );
    }

    /**
     * Constructor from the avro object
     * @param avro the avro object
     */
    //TODO: do we need this for reading the DB???
    public KnownVariant(KnownVariantAvro avro) {
        Objects.requireNonNull(avro);
        this.variant = new Variant(avro.getVariant());
        this.impl = avro;
    }

    /**
     * Constructor from the variant wrapper with default values for the KnownVariant specific values
     * @param variant the Variant wrapper
     */
    public KnownVariant(Variant variant) {
        //TODO: perform checks on the Variant, for example we don't want to store information from multiple samples
        // so we may want to delete it
        this.variant = variant;
        this.impl = new KnownVariantAvro(
                this.variant.getImpl(),
                this.getDefaultCurationClassification(),
                this.getDefaultCurationScore(),
                this.getDefaultCurationHistory(),
                this.getDefaultEvidences(),
                this.getDefaultComments()
        );
    }

    /**
     * Constructor for the variant wrapper and curated variant values
     * @param variant the Variant wrapper
     * @param curationClassification the curation classification, only accept values as defined in CurationClassification
     * @param curationScore the curation score, only accepts values between 0 and 5
     * @param curationHistory a list of HistoryEntry
     * @param evidences a list of Evidence
     * @param comments a list of Comment
     */
    public KnownVariant(Variant variant, String curationClassification,
                        Integer curationScore, List curationHistory,
                        List evidences, List comments) {
        this(variant);
        this.setCurationClassification(curationClassification);
        this.setCurationScore(curationScore);
        this.setCurationHistory(curationHistory);
        this.setEvidences(evidences);
        this.setComments(comments);
    }

    /**
     * Default curation classification
     * @return
     */
    private CurationClassification getDefaultCurationClassification() {
        return CurationClassification.valueOf(KnownVariant.DEFAULT_CURATION_CLASSIFICATION);
    }

    /**
     * Default curation score
     * @return
     */
    private CurationScore getDefaultCurationScore() {
        return curation_score_mapping.get(KnownVariant.DEFAULT_CURATION_SCORE);
    }

    /**
     * Default curation history
     * @return
     */
    private List<CurationHistoryEntry> getDefaultCurationHistory() {
        return new LinkedList<CurationHistoryEntry>();
    }

    /**
     * Default evidences
     * @return
     */
    private List<EvidenceEntry> getDefaultEvidences() {
        EvidenceEntry evidenceEntry = new EvidenceEntry();
        Date now = new Date();
        evidenceEntry.setDate(now.getTime());
        evidenceEntry.setAlleleOrigin(AlleleOrigin.unknown);
        evidenceEntry.setSource(EvidenceSource.unknown);
        evidenceEntry.setSubmitter("None");
        List evidences = new LinkedList<EvidenceEntry>();
        evidences.add(evidenceEntry);
        return evidences;
    }

    /**
     * Default comments
     * @return
     */
    private List<Comment> getDefaultComments() {
        return new LinkedList<Comment>();
    }

    /**
     * Setter for curation classification. Throws an IllegalArgumentException if the value is not defined in the enum
     * @param curationClassification
     */
    public void setCurationClassification(String curationClassification) {
        if (curationClassification == null) {
            impl.setClassification(this.getDefaultCurationClassification());
        }
        else {
            impl.setClassification(CurationClassification.valueOf(curationClassification));
        }
    }

    /**
     * Getter for curation classificaion
     * @return
     */
    public String getCurationClassification() {
        return impl.getClassification().toString();
    }

    /**
     * Setter for curation score. Throws an IllegalArgumentException if the score is out of the defined range
     * @param curationScore
     */
    public void setCurationScore(Integer curationScore) {
        if (curationScore == null) {
            impl.setCurationScore(this.getDefaultCurationScore());
        }
        else if (curationScore < KnownVariant.INF_CURATION_SCORE ||
                curationScore > KnownVariant.SUP_CURATION_SCORE) {
            throw new IllegalArgumentException(String.format(
                    "The curation score must be in the interval [%1$d, %2$d], found %3$d",
                    KnownVariant.INF_CURATION_SCORE, KnownVariant.SUP_CURATION_SCORE, curationScore));
        }
        else {
            impl.setCurationScore(curation_score_mapping.get(curationScore));
        }
    }

    /**
     * Getter for curation score
     * @return
     */
    public Integer getCurationScore() {
        return curation_score_mapping.inverse().get(impl.getCurationScore());
    }

    /**
     * Setter for curation history
     * @param curationHistory
     */
    public void setCurationHistory(List curationHistory) {
        if (curationHistory == null) {
            impl.setHistory(this.getDefaultCurationHistory());
        }
        else {
            impl.setHistory(curationHistory);
        }
    }

    /**
     * Getter for curation history
     * @return
     */
    public List getCurationHistory() {
        return impl.getHistory();
    }

    /**
     * Setter for evidences
     * @param evidences
     */
    public void setEvidences(List evidences) {
        if (evidences == null) {
            impl.setEvidences(this.getDefaultEvidences());
        }
        else {
            impl.setEvidences(evidences);
        }
    }

    /**
     * Getter for evidences
     * @return
     */
    public List getEvidences() {
        return impl.getEvidences();
    }

    /**
     * Setter for comments
     * @param comments
     */
    public void setComments(List comments) {
        if (comments == null) {
            impl.setComments(this.getDefaultComments());
        }
        else {
            impl.setComments(comments);
        }
    }

    /**
     * Getter for comments
     * @return
     */
    public List getComments() {
        return impl.getComments();
    }

    /**
     * Getter for Variant, no setter available as it should be passed in the constructor
     * @return
     */
    public Variant getVariant() {
        return variant;
    }

    /**
     * Getter for CuratedVariantAvro, no setter available as it should be passed in the constructor
     * @return
     */
    public KnownVariantAvro getImpl() {
        return impl;
    }

    /**
     * Adds an evidence to the list of evidences
     * @param evidenceEntry
     */
    public void addEvidence(EvidenceEntry evidenceEntry) {
        // checks required fields at evidence
        if (evidenceEntry.getSubmitter() == null || evidenceEntry.getSubmitter().equals("")){
            throw new IllegalArgumentException("Submitter is required to register an evidence");
        }
        if (evidenceEntry.getSource() == null) {
            evidenceEntry.setSource(EvidenceSource.unknown);
        }
        if (evidenceEntry.getAlleleOrigin() == null) {
            evidenceEntry.setAlleleOrigin(AlleleOrigin.unknown);
        }
        // adds the evidence to the current list in KnownVariant
        List<EvidenceEntry> evidences = this.getEvidences();
        evidences.add(evidenceEntry);
        this.setEvidences(evidences);
    }
}

