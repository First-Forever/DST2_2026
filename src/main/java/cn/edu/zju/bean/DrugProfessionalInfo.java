package cn.edu.zju.bean;

public class DrugProfessionalInfo {

    private Long id;
    private String drugName;
    private String relatedGenes;
    private String sourceType;
    private String evidenceLevel;
    private String guidelineOrLabelTags;
    private String literatureSummary;
    private String pmidList;
    private String sourceUrl;

    public DrugProfessionalInfo() {
    }

    public DrugProfessionalInfo(String drugName, String relatedGenes, String sourceType, String evidenceLevel,
                                String guidelineOrLabelTags, String literatureSummary, String pmidList, String sourceUrl) {
        this(null, drugName, relatedGenes, sourceType, evidenceLevel, guidelineOrLabelTags, literatureSummary, pmidList, sourceUrl);
    }

    public DrugProfessionalInfo(Long id, String drugName, String relatedGenes, String sourceType, String evidenceLevel,
                                String guidelineOrLabelTags, String literatureSummary, String pmidList, String sourceUrl) {
        this.id = id;
        this.drugName = drugName;
        this.relatedGenes = relatedGenes;
        this.sourceType = sourceType;
        this.evidenceLevel = evidenceLevel;
        this.guidelineOrLabelTags = guidelineOrLabelTags;
        this.literatureSummary = literatureSummary;
        this.pmidList = pmidList;
        this.sourceUrl = sourceUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDrugName() {
        return drugName;
    }

    public void setDrugName(String drugName) {
        this.drugName = drugName;
    }

    public String getRelatedGenes() {
        return relatedGenes;
    }

    public void setRelatedGenes(String relatedGenes) {
        this.relatedGenes = relatedGenes;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getEvidenceLevel() {
        return evidenceLevel;
    }

    public void setEvidenceLevel(String evidenceLevel) {
        this.evidenceLevel = evidenceLevel;
    }

    public String getGuidelineOrLabelTags() {
        return guidelineOrLabelTags;
    }

    public void setGuidelineOrLabelTags(String guidelineOrLabelTags) {
        this.guidelineOrLabelTags = guidelineOrLabelTags;
    }

    public String getLiteratureSummary() {
        return literatureSummary;
    }

    public void setLiteratureSummary(String literatureSummary) {
        this.literatureSummary = literatureSummary;
    }

    public String getPmidList() {
        return pmidList;
    }

    public void setPmidList(String pmidList) {
        this.pmidList = pmidList;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }
}
