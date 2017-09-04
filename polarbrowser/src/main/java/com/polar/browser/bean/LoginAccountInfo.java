package com.polar.browser.bean;

public class LoginAccountInfo {
	private int id;
	private String url;
	private String username;
	private String password;
	private String timestamp;

	public LoginAccountInfo() {
	}

	public LoginAccountInfo(int id, String url, String username, String password, String timestamp) {
		this.id = id;
		this.url = url;
		this.username = username;
		this.password = password;
		this.timestamp = timestamp;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
}
