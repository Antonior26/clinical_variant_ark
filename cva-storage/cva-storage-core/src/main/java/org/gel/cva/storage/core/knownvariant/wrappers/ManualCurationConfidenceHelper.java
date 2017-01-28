package org.gel.cva.storage.core.knownvariant.wrappers;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.gel.models.cva.avro.ManualCurationConfidence;

/**
 *
 * This class helps converting between the manual curation confidence in the enum format defined in the model to the
 * score that is actually store which is an integer. It performs some checks on the validity of curation scores too.
 *
 * Created by priesgo on 19/01/17.
 */
public class ManualCurationConfidenceHelper {

    static private Integer INF_CURATION_SCORE = 0;
    static private Integer SUP_CURATION_SCORE = 5;
    static private BiMap<Integer, ManualCurationConfidence> curation_score_mapping = HashBiMap.create();
    static {
        curation_score_mapping.put(0, ManualCurationConfidence.not_curated);
        curation_score_mapping.put(1, ManualCurationConfidence.very_low_confidence);
        curation_score_mapping.put(2, ManualCurationConfidence.low_confidence);
        curation_score_mapping.put(3, ManualCurationConfidence.moderate_confidence);
        curation_score_mapping.put(4, ManualCurationConfidence.high_confidence);
        curation_score_mapping.put(5, ManualCurationConfidence.very_high_confidence);
    };

    static public Integer getManualCurationConfidenceInt(ManualCurationConfidence manualCurationConfidence) {
        return curation_score_mapping.inverse().get(manualCurationConfidence);
    }

    static public ManualCurationConfidence getManualCurationConfidenceEnum(Integer manualCurationConfidenceInt) {
        if (manualCurationConfidenceInt < ManualCurationConfidenceHelper.INF_CURATION_SCORE ||
                manualCurationConfidenceInt > ManualCurationConfidenceHelper.SUP_CURATION_SCORE) {
            throw new IllegalArgumentException(String.format(
                    "The curation score must be in the interval [%1$d, %2$d], found %3$d",
                    ManualCurationConfidenceHelper.INF_CURATION_SCORE,
                    ManualCurationConfidenceHelper.SUP_CURATION_SCORE,
                    manualCurationConfidenceInt));
        }
        return curation_score_mapping.get(manualCurationConfidenceInt);
    }

}
