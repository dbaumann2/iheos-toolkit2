package gov.nist.toolkit.xdstools2.server.gazelle.sysconfig

import groovy.transform.ToString
import groovy.transform.TypeChecked

/**
 *
 */
@TypeChecked
@ToString
class V2ResponderDef {
    String configType
    String company
    String system
    String host
    String actor
    boolean secured
    boolean approved
    String comment
    String assigningAuthority
    String rcvApplication
    String rcvFacility
    String port
    String proxyPort
    String portSecured

    boolean isPIF() {
        actor == 'DOC_REGISTRY' && comment.contains('ITI-8 Patient Identity Feed')
    }
}
