package com.example.ai_bsrs.homepage_module;

public class InferenceDataset {

    public String xMin, xMax, yMin, yMax, confidenceLevel, nameOfBread, numOfClass;

    public InferenceDataset(String xMin, String xMax, String yMin, String yMax, String confidenceLevel, String nameOfBread, String numOfClass){
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        this.confidenceLevel = confidenceLevel;
        this.nameOfBread = nameOfBread;
        this.numOfClass = numOfClass;
    }

    public String getNumOfClass() {
        return numOfClass;
    }

    public void setNumOfClass(String numOfClass) {
        this.numOfClass = numOfClass;
    }

    public String getxMin() {
        return xMin;
    }

    public void setxMin(String xMin) {
        this.xMin = xMin;
    }

    public String getxMax() {
        return xMax;
    }

    public void setxMax(String xMax) {
        this.xMax = xMax;
    }

    public String getyMin() {
        return yMin;
    }

    public void setyMin(String yMin) {
        this.yMin = yMin;
    }

    public String getyMax() {
        return yMax;
    }

    public void setyMax(String yMax) {
        this.yMax = yMax;
    }

    public String getConfidenceLevel() {
        return confidenceLevel;
    }

    public void setConfidenceLevel(String confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
    }

    public String getNameOfBread() {
        return nameOfBread;
    }

    public void setNameOfBread(String nameOfBread) {
        this.nameOfBread = nameOfBread;
    }

    @Override
    public String toString(){
        String data = "Bread Name: "+ nameOfBread +
                    "\nConfidence: "+ confidenceLevel +
                    "\nXmin: "+ xMin +
                    "\nXmax: "+ xMax +
                    "\nYmin: "+ yMin +
                    "\nYmax: "+ yMax + "\n";
        return data;
    }
}
