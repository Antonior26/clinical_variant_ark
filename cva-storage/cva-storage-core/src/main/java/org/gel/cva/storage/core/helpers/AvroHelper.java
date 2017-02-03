package org.gel.cva.storage.core.helpers;

import org.gel.models.cva.avro.HeritablePhenotype;
import org.opencb.biodata.models.variant.avro.Xref;

/**
 * Created by priesgo on 30/01/17.
 */
public class AvroHelper {

    /**
     *
     * @param transcript1
     * @param transcript2
     * @return
     */
    public static Boolean areTranscriptsEqual(String transcript1, String transcript2) {

        Boolean equal = transcript1 == null && transcript2 == null;
        if (transcript1 != null && transcript2 != null) {
            equal = transcript1.equals(transcript2);
        }
        return equal;
    }

    /**
     *
     * @param heritablePhenotype1
     * @param heritablePhenotype2
     * @return
     */
    public static Boolean areHeritablePhenotypeEqual(
            HeritablePhenotype heritablePhenotype1,
            HeritablePhenotype heritablePhenotype2) {

        return heritablePhenotype1.getPhenotype().equals(heritablePhenotype2.getPhenotype()) &&
                heritablePhenotype1.getInheritanceMode() == heritablePhenotype2.getInheritanceMode();
    }
}
