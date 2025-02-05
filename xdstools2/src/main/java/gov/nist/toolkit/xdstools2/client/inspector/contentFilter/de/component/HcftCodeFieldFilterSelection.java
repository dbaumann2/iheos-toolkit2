package gov.nist.toolkit.xdstools2.client.inspector.contentFilter.de.component;

import gov.nist.toolkit.registrymetadata.client.DocumentEntry;
import gov.nist.toolkit.xdstools2.client.inspector.contentFilter.de.DocumentEntryIndexField;
import gov.nist.toolkit.xdstools2.client.util.SimpleCallbackT;

import java.util.List;

public class HcftCodeFieldFilterSelection extends CodeFieldFilterSelector {

    public HcftCodeFieldFilterSelection(String label, String codeName, SimpleCallbackT valueChangeNotification) {
        super(label, codeName, valueChangeNotification);
    }

    @Override
    public List<String> getFieldValue(DocumentEntry de) {
        return de.hcftc;
    }

    @Override
    public DocumentEntryIndexField getFieldType() {
        return DocumentEntryIndexField.HEALTHCARE_FACILITY_TYPE_CODE;
    }
}
