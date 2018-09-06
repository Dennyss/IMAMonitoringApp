package com.bean;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Denys Kovalenko on 10/14/2016.
 */
@Component
public class DataStorage {
    private Map<String, Integer> dataForEndpoints = new HashMap<>();
    private int previousValue;
    private boolean alert;

    @Value("${alert.threshold}")
    private int alertThreshold;

    public void putRecord(String endpointName, int activeConnections) {
        dataForEndpoints.put(endpointName, activeConnections);
    }

    public Map<String, Integer> getDataForEndpoints() {
        return dataForEndpoints;
    }

    public int calculateTotalNumberOfConnections() {
        int totalNumberOfActiveConnections = 0;

        for (String endpointName : dataForEndpoints.keySet()) {
            Integer activeConnectionsPerEndpoint = dataForEndpoints.get(endpointName);
            totalNumberOfActiveConnections += activeConnectionsPerEndpoint;
        }

        alert = updateForAlert(totalNumberOfActiveConnections, previousValue);

        previousValue = totalNumberOfActiveConnections;
        return totalNumberOfActiveConnections;
    }

    public boolean checkForAlert(){
        return alert;
    }

    public int getCurrentConnectionsNumber(){
        return previousValue;
    }

    private boolean updateForAlert(int currentNumber, int previousValue) {
        if(previousValue != 0 && currentNumber < previousValue * (100 - alertThreshold) / 100){
            return true;
        }
        return false;
    }
}
