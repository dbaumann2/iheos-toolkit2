package war.toolkitx.testkit.plugins.FhirAssertion

import gov.nist.toolkit.testengine.engine.FhirSimulatorTransaction
import gov.nist.toolkit.testengine.engine.validations.fhir.AbstractFhirValidater
import gov.nist.toolkit.testengine.engine.validations.ValidaterResult
import gov.nist.toolkit.testengine.transactions.BasicTransaction
import org.hl7.fhir.dstu3.model.Bundle
import org.hl7.fhir.dstu3.model.DocumentManifest
import org.hl7.fhir.dstu3.model.DocumentReference
import org.hl7.fhir.dstu3.model.Resource

class SingleDocSubmissionValidater extends AbstractFhirValidater {

    SingleDocSubmissionValidater() {
        filterDescription = 'Submission of a Single DocumentReference linked to DocumentManifest'
    }

    @Override
    ValidaterResult validate(FhirSimulatorTransaction transactionInstance) {
        boolean match = transactionInstance.request instanceof Bundle && isSingleDocSubmission(transactionInstance.request) && !isErrors()
        new ValidaterResult(transactionInstance, this, match)
    }

    private boolean isSingleDocSubmission(Bundle bundle) {
        DocumentManifest documentManifest = null
        List<DocumentReference> documentReferences = []

        bundle.entry.each { Bundle.BundleEntryComponent component ->
            Resource resource = component.getResource()
            if (resource instanceof DocumentManifest) {
                if (documentManifest)
                    error("Extra DocumentManifest found in submission")
                documentManifest = resource
            }
            if (resource instanceof DocumentReference) {
                documentReferences.add(resource)
            }
        }
        documentManifest && documentReferences.size() == 1
    }

    def errors = []
    def error(String x) {
        errors << x
    }

    String getLog() {
        errors.join('\n')
    }

    boolean isErrors() { !errors.empty }

}
