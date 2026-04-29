package org.zetrix.model.response.result;

public class HelloResult {
    private String currentTime;
    private Integer hashType;
    private String ledgerVersion;
    private String licenseVersion;
    private String monitorVersion;
    private Integer networkId;
    private String overlayVersion;
    private Integer websocketAddress;
    private String ztxVersion;

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public Integer getHashType() {
        return hashType;
    }

    public void setHashType(Integer hashType) {
        this.hashType = hashType;
    }

    public String getLedgerVersion() {
        return ledgerVersion;
    }

    public void setLedgerVersion(String ledgerVersion) {
        this.ledgerVersion = ledgerVersion;
    }

    public String getLicenseVersion() {
        return licenseVersion;
    }

    public void setLicenseVersion(String licenseVersion) {
        this.licenseVersion = licenseVersion;
    }

    public String getMonitorVersion() {
        return monitorVersion;
    }

    public void setMonitorVersion(String monitorVersion) {
        this.monitorVersion = monitorVersion;
    }

    public Integer getNetworkId() {
        return networkId;
    }

    public void setNetworkId(Integer networkId) {
        this.networkId = networkId;
    }

    public String getOverlayVersion() {
        return overlayVersion;
    }

    public void setOverlayVersion(String overlayVersion) {
        this.overlayVersion = overlayVersion;
    }

    public Integer getWebsocketAddress() {
        return websocketAddress;
    }

    public void setWebsocketAddress(Integer websocketAddress) {
        this.websocketAddress = websocketAddress;
    }

    public String getZtxVersion() {
        return ztxVersion;
    }

    public void setZtxVersion(String ztxVersion) {
        this.ztxVersion = ztxVersion;
    }
}
