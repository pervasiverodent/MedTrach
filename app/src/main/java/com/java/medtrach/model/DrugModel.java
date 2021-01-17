package com.java.medtrach.model;

public class DrugModel {
    String drugId, drugName, drugDescription;
    String drugPharmacyName, drugPharmacyLocation;

    public DrugModel() {
    }

    public DrugModel(String drugId, String drugName, String drugDescription, String drugPharmacyName, String drugPharmacyLocation) {
        this.drugId = drugId;
        this.drugName = drugName;
        this.drugDescription = drugDescription;
        this.drugPharmacyName = drugPharmacyName;
        this.drugPharmacyLocation = drugPharmacyLocation;
    }

    public DrugModel(String drugId, String drugName, String drugDescription) {
        this.drugId = drugId;
        this.drugName = drugName;
        this.drugDescription = drugDescription;
    }

    public String getDrugId() {
        return drugId;
    }

    public void setDrugId(String drugId) {
        this.drugId = drugId;
    }

    public String getDrugName() {
        return drugName;
    }

    public void setDrugName(String drugName) {
        this.drugName = drugName;
    }

    public String getDrugDescription() {
        return drugDescription;
    }

    public void setDrugDescription(String drugDescription) {
        this.drugDescription = drugDescription;
    }

    public String getDrugPharmacyName() {
        return drugPharmacyName;
    }

    public void setDrugPharmacyName(String drugPharmacyName) {
        this.drugPharmacyName = drugPharmacyName;
    }

    public String getDrugPharmacyLocation() {
        return drugPharmacyLocation;
    }

    public void setDrugPharmacyLocation(String drugPharmacyLocation) {
        this.drugPharmacyLocation = drugPharmacyLocation;
    }
}
