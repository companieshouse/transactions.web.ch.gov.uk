package uk.gov.companieshouse.transactions.web.model.presenter;

public class IndividualUserDto {

    private String uvid;
    private String name;
    private String dob;

    public IndividualUserDto() {}

    public IndividualUserDto(String uvid, String name, String dob) {
        this.uvid = uvid;
        this.name = name;
        this.dob = dob;
    }

    public String getUvid() {
        return uvid;
    }

    public void setUvid(String uvid) {
        this.uvid = uvid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }
}
