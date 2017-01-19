package org.gel.cva.storage.core.knownvariant.dto;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.gel.models.cva.avro.CurationScore;

/**
 *
 * This class helps converting between the curation scores in the enum format defined in the model to the
 * score that is actually store which is an integer. It performs some checks on the validity of curation scores too.
 *
 * Created by priesgo on 19/01/17.
 */
public class CurationScoreHelper {

    static private Integer INF_CURATION_SCORE = 0;
    static private Integer SUP_CURATION_SCORE = 5;
    static private BiMap<Integer, CurationScore> curation_score_mapping = HashBiMap.create();
    static {
        curation_score_mapping.put(0, CurationScore.NOT_CURATED);
        curation_score_mapping.put(1, CurationScore.CURATION_CONFIDENCE_1);
        curation_score_mapping.put(2, CurationScore.CURATION_CONFIDENCE_2);
        curation_score_mapping.put(3, CurationScore.CURATION_CONFIDENCE_3);
        curation_score_mapping.put(4, CurationScore.CURATION_CONFIDENCE_4);
        curation_score_mapping.put(5, CurationScore.CURATION_CONFIDENCE_5);
    };

    static public Integer getCurationScoreInt(CurationScore curationScoreEnum) {
        return curation_score_mapping.inverse().get(curationScoreEnum);
    }

    static public CurationScore getCurationScoreEnum (Integer curationScoreInt) {
        if (curationScoreInt < CurationScoreHelper.INF_CURATION_SCORE ||
                curationScoreInt > CurationScoreHelper.SUP_CURATION_SCORE) {
            throw new IllegalArgumentException(String.format(
                    "The curation score must be in the interval [%1$d, %2$d], found %3$d",
                    CurationScoreHelper.INF_CURATION_SCORE, CurationScoreHelper.SUP_CURATION_SCORE, curationScoreInt));
        }
        return curation_score_mapping.get(curationScoreInt);
    }

}
