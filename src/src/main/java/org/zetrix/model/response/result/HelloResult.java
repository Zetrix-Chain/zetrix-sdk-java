package org.zetrix.model.response.result;

import com.alibaba.fastjson.annotation.JSONField;

public class HelloResult {
    @JSONField(name = "current_time")
    private String currentTime;
    @JSONField(name = "hash_type")
    private Integer hashType;
    @JSONField(name = "ledger_version")
    private String ledgerVersion;
    @JSONField(name = "license_version")
    private String licenseVersion;
    @JSONField(name = "monitor_version")
    private String monitorVersion;
    @JSONField(name = "network_id")
    private Integer networkId;
    @JSONField(name = "overlay_version")
    private String overlayVersion;
    @JSONField(name = "websocket_address")
    private String websocketAddress;
    @JSONField(name = "ztx_version")
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

    public String getWebsocketAddress() {
        return websocketAddress;
    }

    public void setWebsocketAddress(String websocketAddress) {
        this.websocketAddress = websocketAddress;
    }

    public String getZtxVersion() {
        return ztxVersion;
    }

    public void setZtxVersion(String ztxVersion) {
        this.ztxVersion = ztxVersion;
    }
}
