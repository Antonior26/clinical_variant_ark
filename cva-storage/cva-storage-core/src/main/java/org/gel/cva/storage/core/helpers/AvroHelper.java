package org.gel.cva.storage.core.helpers;

import org.gel.models.cva.avro.HeritablePhenotype;
import org.opencb.biodata.models.variant.avro.Xref;

/**
 * Created by priesgo on 30/01/17.
 */
public class AvroHelper {

    /**
     *
     * @param xref1
     * @param xref2
     * @return
     */
    public static Boolean areXrefEqual(Xref xref1, Xref xref2) {

        Boolean equal = xref1 == null && xref2 == null;
        if (xref1 != null && xref2 != null) {
            equal = xref1.getId().equals(xref2.getId()) && xref1.getSource().equals(xref2.getSource());
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
