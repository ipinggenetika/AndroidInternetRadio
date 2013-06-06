package com.dirt.radio;

public class Stations {

	String stationName;
	String url;
	int stationID;

	public Stations(String urlParam, String stationNameParam, int stationIDParam) {

		this.stationName = stationNameParam;
		this.url = urlParam;
		this.stationID = stationIDParam;

	}

	public int getStationId() {
		return this.stationID;
	}

	public String getUrl() {
		return this.url;
	}

	public String getsationName() {
		return this.stationName;
	}

}
