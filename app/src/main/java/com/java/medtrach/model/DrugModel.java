package com.java.medtrach.model;

public class DrugModel {
    String drugId, drugName, drugDescription;
    String drugPharmacyId, drugPharmacyName, drugPharmacyLocation;
    private Double drugPharmacyLatitude, drugPharmacyLongitude;

    public DrugModel() {
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

    public String getDrugPharmacyId() {
        return drugPharmacyId;
    }

    public void setDrugPharmacyId(String drugPharmacyId) {
        this.drugPharmacyId = drugPharmacyId;
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

    public Double getDrugPharmacyLatitude() {
        return drugPharmacyLatitude;
    }

    public void setDrugPharmacyLatitude(Double drugPharmacyLatitude) {
        this.drugPharmacyLatitude = drugPharmacyLatitude;
    }

    public Double getDrugPharmacyLongitude() {
        return drugPharmacyLongitude;
    }

    public void setDrugPharmacyLongitude(Double drugPharmacyLongitude) {
        this.drugPharmacyLongitude = drugPharmacyLongitude;
    }
}
