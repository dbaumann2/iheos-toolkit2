package gov.nist.toolkit.valsupport.client;


import com.google.gwt.user.client.rpc.IsSerializable;
import gov.nist.toolkit.commondatatypes.client.MetadataTypes;
import gov.nist.toolkit.commondatatypes.client.SchematronMetadataTypes;
import gov.nist.toolkit.errorrecording.client.XdsErrorCode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;



/**
 * Collection of flags and logic to record needed validation steps. If validation
 * steps are discovered along the way then this keeps track of what was run.
 * @author bill
 *
 */
public class ValidationContext  implements Serializable, IsSerializable {

	String codesFilename = null;

	// These flags control the validation


	public boolean xds_b     = false;

	//
	// primary transaction selection
    //
   public boolean isR       = false;
	public boolean isRODDE 	 = false;
	public boolean isStableOrODDE = false;
	public boolean isPnR     = false;
	/**
	 * is this a Retrieve Document Set Transaction (ITI-43)?
	 */
	public boolean isRet	 = false;
	public boolean isXDR	 = false;
	public boolean isXCDR    = false;	// ITI-80, Cross-Gateway Document Provide
	public boolean isXDRLimited = false;   // IHE version
	public boolean isXDRMinimal = false;   // Direct version
	public boolean isXDM     = false;
	public boolean isSQ      = false;
	public boolean isRM       = false;
	public boolean isDIRECT  = false;
	public boolean isCCDA	 = false;
	public boolean isRD      = false;

	// only one
	public boolean isMU      = false;
	public boolean isRMU      = false;
	public boolean isRMD	= false;

	/**
	 * Is this a Retrieve Imaging Document Set (RAD-69) transaction? Also set for
	 * Cross Gateway Retrieve Imaging Document Set (RAD-75) transaction. In that
	 * case, {@link #isXC} is also set.
	 */
	public boolean isRad69	 = false;
	public boolean isRad55 = false;
   //NHIN xcpd
    public boolean isXcpd = false;
    public boolean isNwHINxcpd = false;
    public boolean isC32 = false;
    //E-Priscription ncpdp/
    public boolean isNcpdp = false;

    public boolean forceMtom = false;

    //
    // Modifiers
    //

	/**
	 * is this a Cross Community Transaction?
	 * A modifier on other settings; enforces the home community id requirement.
	 */
	public boolean isXC      = false;

	public boolean isValidateCodes = true;
	public boolean isPartOfRecipient = false;
	public boolean validateAgainstPatientIdentityFeed = false;

	/**
	 * Is this a Request? A setting of false does not mean that this is a
	 * Response; that is set using {@link #isResponse}. If neither flag is set
	 * then context is not known.
	 */
	public boolean isRequest = false;/**
    * Is this a Response? A setting of false does not mean that this is a
    * Request; that is set using {@link #isRequest}. If neither flag is set
    * then context is not known.
    */
	public boolean isResponse= false;

	public boolean isAsync = false;
	public boolean isMultiPatient = false;
	//		public boolean minMeta = false;

	public boolean skipInternalStructure = false;
	public boolean updateable = true;

	public boolean isEpsos = false;

	//
	// State maintained by various validators
	//

	// this is set by MtomMessageValidator and SimpleSoapMessageValidator
	// to record what was actually found in message
	public boolean isSimpleSoap = false;

	// this is not a 'type' but instead and indicator that
	// a SOAP wrapper either is present or must be present
	// in validation
	public boolean hasSoap = false;
	public boolean hasSaml = false ;
	public boolean requiresStsSaml = false;
	public boolean hasHttp = false;

	// a bad place to keep this status
	public boolean updateEnabled = false;

	public boolean leafClassWithDocumentOk = false;

	public String ccdaType = null;
	public byte[] privKey = null;
	public String privKeyPassword = "";

	public enum MetadataPattern { UpdateDocumentEntry, UpdateDocumentEntryStatus, UpdateFolder };

	public List<MetadataPattern> metadataPatterns = new ArrayList<MetadataPattern>();
    public String wsAction;


	// Since content can be nested (CCDA inside XDM inside ...) the context(s) for validation
	// must also be nested.
	// In the current code, a CCDA nested inside a Direct message is coded as isCCDA = true and ccdaType = ???
	// In time this should be converted to:
	//  VC(Direct)
	//    VC(CCDA, ccdaType = ???)
	// This will allow:
	//   VC(Direct)
	//     VC(XDM)
	//       VC(CCDA, ccdaType = ???)
	// which parsed as a CCDA of type ??? nested in an XDM included as a part of a Direct message.
	// This is a list instead of a single element since we may have the need to validate a Direct
	// message (or XDM) a plain text attachment AND a CCDA attachment.
	// For now the only containers (formats that contain other formats) are Direct messages
	// and XDM.  Provide & Register (XDS or XDR) could be considered a container as well. So
	// far there is no requirement to deal with content validation in those areas.
	List<ValidationContext> innerContexts = new ArrayList<ValidationContext>();

    // Never never call this directly
    // Should only be used by GWT compiler
    public ValidationContext() {}

    public ValidationContext(String codesFilename) {
        this.codesFilename = codesFilename;
    }

    public void setDirection(MessageDirection dir) {
        if (MessageDirection.REQUEST.equals(dir)) {
            isRequest = true;
            isResponse = false;
        } else {
            isRequest = false;
            isResponse = true;
        }
    }

	public void addInnerContext(ValidationContext ivc) {
		innerContexts.add(ivc);
	}

	public int getInnerContextCount() {
		return innerContexts.size();
	}

	public ValidationContext getInnerContext(int i) {
		if (i < innerContexts.size())
			return innerContexts.get(i);
		return null;
	}

	//
	// End state maintained by various validators
	//

	public void addMetadataPattern(MetadataPattern pattern) {
		metadataPatterns.add(pattern);
	}

	public boolean hasMetadataPattern(String patternString) {
		for (MetadataPattern pat : metadataPatterns) {
			if (patternString.equalsIgnoreCase(pat.toString()))
				return true;
		}
		return false;
	}

	public boolean requiresSimpleSoap() {
		return isR || isMU || isRMU || isRM || (isSQ && !isEpsos);
	}

	public XdsErrorCode.Code getBasicErrorCode() {
		if (requiresMtom())
			return XdsErrorCode.Code.XDSRepositoryError;
		return XdsErrorCode.Code.XDSRegistryError;
	}

	public boolean requiresMtom() {
		return isPnR || isRet || isXDR || isXCDR || (isSQ && isEpsos) || forceMtom || isRad69;
	}

	public boolean containsDocuments() {
		if (!hasHttp) return false;
		if (isPnR && isRequest) return true;
		if (isXDR && isRequest && !isR) return true;
		if (isXCDR && isRequest) return true;
		if (isRet && isResponse) return true;
		if (isRad69 && isResponse) return true;
		return false;
	}

	public boolean equals(ValidationContext v) {
		return
				updateEnabled == v.updateEnabled &&
				isMU == v.isMU &&
						isRMU == v.isRMU &&
						isRM == v.isRM &&
				isR == v.isR &&
 				isRODDE == v.isRODDE &&
				isPnR == v.isPnR &&
				isRet == v.isRet &&
						isRD == v.isRD &&
				isRad69 == v.isRad69 &&
				//			isXDR == v.isXDR &&     // not sure how this needs to work
				isXCDR == v.isXCDR &&
				isDIRECT == v.isDIRECT &&
				isCCDA == v.isCCDA &&
				isSQ == v.isSQ &&
				isXC == v.isXC &&
				isRequest == v.isRequest &&
				isResponse == v.isResponse &&
				isAsync == v.isAsync &&
				isMultiPatient == v.isMultiPatient &&
				isEpsos == v.isEpsos &&
				//NHIN xcpd and C32
				isXcpd == v.isXcpd &&
				isNwHINxcpd == v.isNwHINxcpd &&
				isC32 == v.isC32 &&
				//			minMeta == v.minMeta &&
				leafClassWithDocumentOk == v.leafClassWithDocumentOk &&
				isNcpdp == v.isNcpdp &&
                        forceMtom == v.forceMtom &&
				((ccdaType == null) ?  v.ccdaType == null   :  ccdaType.equals(v.ccdaType))
				;
	}

	public void clone(ValidationContext v) {
		hasSoap = v.hasSoap;
		hasSaml = v.hasSaml;
		requiresStsSaml = v.requiresStsSaml;
		hasHttp = v.hasHttp;

		xds_b = v.xds_b;

		isMU = v.isMU;
		isRMU = v.isRMU;
		isRM = v.isRM;
		updateEnabled = v.updateEnabled;
		//			minMeta = v.minMeta;

		isR = v.isR;
		isRODDE = v.isRODDE;
		isPnR = v.isPnR;
		isRet = v.isRet;
		isRD = v.isRD;
		isRad69 = v.isRad69;
		isXDR = v.isXDR;
		isXCDR = v.isXCDR;
		isXDRLimited = v.isXDRLimited;
		isXDRMinimal =  v.isXDRMinimal;
		isXDM = v.isXDM;
		isSQ  = v.isSQ;
		isDIRECT = v.isDIRECT;
		isCCDA = v.isCCDA;

		isXC  = v.isXC;

		isRequest = v.isRequest;
		isResponse = v.isResponse;

		isAsync = v.isAsync;
		isMultiPatient = v.isMultiPatient;

		skipInternalStructure = v.skipInternalStructure;
		updateable = v.updateable;
		isEpsos = v.isEpsos;
		//NHIN xcpd and C32
		isXcpd = v.isXcpd;
		isNwHINxcpd = v.isNwHINxcpd;
		isC32 = v.isC32;
		leafClassWithDocumentOk = v.leafClassWithDocumentOk;
		isNcpdp = v.isNcpdp;
		ccdaType = v.ccdaType;
		codesFilename = v.codesFilename;
        forceMtom = v.forceMtom;
	}

	public boolean hasMetadata() {
		if ((isR || isRODDE || isMU || isRMU || isPnR || isXDR || isXDM) && isRequest) return true;
		if (isSQ && isResponse) return true;
		return false;
	}

	// TODO The XCDR transaction is also labelled as isPNR. For now, this will
	// just process as if the XCDR transaction is the same as the PnR transaction.
	// Need to review if this needs an adjustment.

	public String getTransactionName() {
		if (isPnR) {
			if (isRequest)
				return "ProvideAndRegister.b";
			if (isResponse)
				return "RegistryResponse";
		}
		if (isR) {
			if (isRequest)
				return "Register.b";
			if (isResponse)
				return "RegistryResponse";
		}
		if (isRODDE) {
			if (isRequest)
				return "Register On-Demand Document Entry";
			if (isResponse)
				return "RegistryResponse";
		}
		if (isRM) {
			if (isRequest)
				return "Remove Metadata";
			if (isResponse)
				return "RegistryResponst";
		}
		if (isMU) {
			if (isRequest)
				return "RMU";
			if (isResponse)
				return "RegistryResponse";
		}
		if (isRMU) {
			if (isRequest)
				return "Metadata Update";
			if (isResponse)
				return "RegistryResponse";
		}
		if (isRD) {
			if (isRequest)
				return "Remove Documents";
			if (isResponse)
				return "RegistryResponse";
		}
		if (isRet) {
			if (isRequest) {
				if (isXC) {
					return "Cross Gateway Retrieve Request";
				} else {
					return "Retrieve Request";
				}
			}
			if (isResponse) {
				if (isXC)
					return "Cross Gateway Retrieve Response";
				else
					return "Retrieve Response";
			}
		}
		if (isXDR) {
			if (isRequest)
				return "ProvideAndRegister.b";
			if (isResponse)
				;
		}
		if (isXDM) return "XDM";
		if (isSQ) {
			if (isXC) {
				if (isEpsos) {
					if (isRequest)
						return "Epsos Cross Gateway Query Request";
					if (isResponse)
						return "Epsos Cross Gateway Query Response";
				} else {
					if (isRequest)
						return "Cross Gateway Query Request";
					if (isResponse)
						return "Cross Gateway Query Response";
				}
			} else {
				if (isRequest)
					return "Stored Query Request";
				if (isResponse)
					return "Stored Query Response";
			}
		}
		if (isRad69) {
			if (isRequest)
				return "Rad-69 Request";
			if (isResponse)
				return "Rad-69 Response";
		}
		return "";
	}

	// NwNIN transactions
	public int getSchematronValidationType() {
		if (isXcpd) {
			if (isRequest)
				return SchematronMetadataTypes.IHE_XCPD_305;
			if (isResponse)
				return SchematronMetadataTypes.IHE_XCPD_306;
		}
		if(isNcpdp) {
			return SchematronMetadataTypes.NCPDP;
		}
		if (isNwHINxcpd) {
			if (isRequest)
				return SchematronMetadataTypes.NwHINPD_305;
			if (isResponse)
				return SchematronMetadataTypes.NwHINPD_306;
		}
		if (isC32) {
			return SchematronMetadataTypes.C32;
		}
		return SchematronMetadataTypes.METADATA_TYPE_UNKNOWN;
	}

	public String[] getSchematronValidationTypeName(int type) {
		return SchematronMetadataTypes.getSchematronMetadataTypeName(type);
	}
	public int getSchemaValidationType() {
		if (isPnR || isXDR) {
			if (isRequest)
				return MetadataTypes.METADATA_TYPE_PRb;
			if (isResponse)
				return MetadataTypes.METADATA_TYPE_REGISTRY_RESPONSE3;
		}
		if (isR || isMU || isRMU || isRM) {
			if (isRequest)
				return MetadataTypes.METADATA_TYPE_Rb;
			if (isResponse)
				return MetadataTypes.METADATA_TYPE_REGISTRY_RESPONSE3;
		}
		if (isRODDE) {
			if (isRequest)
				return MetadataTypes.METADATA_TYPE_RODDE;
			if (isResponse)
				return MetadataTypes.METADATA_TYPE_REGISTRY_RESPONSE3;
		}
		if (isXDM)
			return MetadataTypes.METADATA_TYPE_Rb;
		if (isSQ && isRequest)
			return MetadataTypes.METADATA_TYPE_SQ;
		if (isSQ && isResponse)
			return MetadataTypes.METADATA_TYPE_SQ;
		if (isRet)
			return MetadataTypes.METADATA_TYPE_RET;
		if (isRD)
			return MetadataTypes.METADATA_TYPE_RD;
		if (isRad69)
			return MetadataTypes.METADATA_TYPE_RAD69;
		return MetadataTypes.METADATA_TYPE_UNKNOWN;
	}

	public String getSchemaValidationTypeName(int type) {
		return MetadataTypes.getMetadataTypeName(type);
	}


	public String toString() {
		StringBuffer buf = new StringBuffer();

        buf.append("[");

		if (isRequest) buf.append("Request");
		else if (isResponse) buf.append("Response");
		else buf.append("???");

		if (isR) buf.append(";Register");
		if (isRODDE) buf.append(";RegisterODDE");
		if (isMU) buf.append(";MU");
		if (isRMU) buf.append(";RMU");
		if (isRM) buf.append(";RM");
		if (isPnR) buf.append(";PnR");
		if (isRet) buf.append(";Retrieve");
		if (isRD) buf.append(";RD");
		if (isRad69) buf.append(";RAD69");
		if (isXDR) buf.append(";XDR");
		if (isXCDR) buf.append(";XCDR");
		if (isXDM) buf.append(";XDM");
		if (isSQ) buf.append(";SQ");


		if (hasHttp) buf.append(";HTTP");
		if (hasSoap) buf.append(";SOAP");
		if (hasSaml) buf.append(";SAML");
		if (requiresStsSaml) buf.append(";STSSAML");
		if (xds_b) buf.append(";xds.b");
		if (isDIRECT) buf.append(";DIRECT");
		if (isCCDA) buf.append(";CCDA");
		if (isXC) buf.append(";XC");
		if (isEpsos) buf.append(";Epsos");
		//NHIN xcpd and C32
		if (isXcpd) buf.append(";Xcpd");
		if (isNwHINxcpd) buf.append(";NwHINxcpd");
		if (isC32) buf.append(";C32");
		if (isNcpdp) buf.append(";Ncpdp");
		if (isAsync) buf.append(";Async");
		if (isMultiPatient) buf.append(";MultiPatient");
		if (skipInternalStructure) buf.append(";SkipInternalStructure");
		//			if (minMeta) buf.append(";MinMetadata");
		if (isXDRLimited) buf.append(";XDRLimited");
		if (isXDRMinimal) buf.append(";XDRMinimal");
        if (isPartOfRecipient) buf.append(";partOfRecipient");
//		if (updateable)
//			buf.append(";Updateable");
//		else
//			buf.append(";NotUpdateable");
		if (!metadataPatterns.isEmpty())
			buf.append(";MetadataPatterns:").append(metadataPatterns);
		if (ccdaType != null)
			buf.append(";CCDA type is " + ccdaType);

		if (innerContexts != null) {
			for (ValidationContext v : innerContexts) {
				buf.append("[").append(v.toString()).append("]");
			}
		}
//		if (codesFilename != null)
//			buf.append(";HasCodes");

        if (forceMtom) buf.append(";forceMtom");

        buf.append("]");
		return buf.toString();
	}

	public boolean isTransactionKnown() {
		return isR || isRODDE || isMU || isRMU || isRM || isPnR || isRet || isRD || isXDR  || isXCDR || isXDM || isSQ || isRad69 || isRad55;
	}

	public boolean isMessageTypeKnown() {
		return
				(isTransactionKnown() && (isRequest || isResponse))
				|| isXDM || isXcpd || isNwHINxcpd || isC32 || isNcpdp || isDIRECT
				|| isCCDA
				;
	}

	public boolean isValid() {
		return isMessageTypeKnown() || hasSoap;
	}

	public boolean isSubmit() {
		return isR || isRODDE || isMU || isRMU || isRM || isPnR || isXDR || isXCDR || isXDM;
	}

	public boolean availabilityStatusRequired() {
		return isSQ && isResponse;
	}
	public boolean hashRequired() {
		if (isXDRMinimal) return false;
		if (isXDRLimited) return false;
		if (isXDR) return false;
		if (isXDM) return true;
		if (isPnR) return false;
		if (isXCDR) return false;
		if (isR && isRequest) return true;
		if (isRODDE && isRequest) return false;
		if (isMU && isRequest) return true;
		if (isRMU && isRequest) return true;
		if (isSQ && isResponse) return false;
		return true;
	}
	public boolean sizeRequired() {
		return hashRequired();
	}
	public boolean homeRequired() {
		return isXC && (isSQ || isRet) && isResponse
				//			&& !minMeta
				;
	}
	public boolean repositoryUniqueIdRequired() {
		if (isXDM) return false;
        if (isPartOfRecipient) return false;
		if (isR && isRequest) return true;
		if (isMU && isRequest) return true;
		if (isRMU && isRequest) return true;
		if (isSQ && isResponse) return true;
		return false;
	}
	public boolean uriRequired() {
		return isXDM;
	}

	public void setCodesFilename(String filename) {
		this.codesFilename = filename;
	}

	public String getCodesFilename() {
		return codesFilename;
	}

}




