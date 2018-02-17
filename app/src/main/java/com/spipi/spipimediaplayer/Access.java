package com.spipi.spipimediaplayer;

import android.util.Base64;

import com.spipi.spipimediaplayer.hubic.Connection;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class Access implements Serializable {
	protected Long id;
	protected String client_id;
	protected String client_secret;
	protected String api_url;
	protected String refresh_token;
	protected String access_token;
	protected String openstack_access_token;
	protected String openstack_access_token_expiration;
	protected String openstack_url;
	protected String expiration;
	protected String type = "hubic";
	protected String name;
	protected String localStorage;

	public Access(String name){
		this.name=name;

	}

	public String getOpenStackAccessToken(){
		
		return openstack_access_token;
	}
	public String getOpenStackUrl(){
		return openstack_url;
	}

	
	public String getClient_id() {
		return client_id;
	}
	public void setClient_id(String client_id) {
		this.client_id = client_id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getClient_secret() {
		return client_secret;
	}
	public void setClient_secret(String client_secret) {
		this.client_secret = client_secret;
	}
	public String getApi_url() {
		return api_url;
	}
	public void setApi_url(String api_url) {
		this.api_url = api_url;
	}
	public String getRefresh_token() {
		return refresh_token;
	}
	public void setRefresh_token(String refresh_token) {
		this.refresh_token = refresh_token;
	}
	public String getAccess_token() {
		return access_token;
	}
	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}
	public String getOpenstack_access_token() {
		return openstack_access_token;
	}
	public void setOpenstack_access_token(String openstack_access_token) {
		this.openstack_access_token = openstack_access_token;
	}
	public String getOpenstack_access_token_expiration() {
		return openstack_access_token_expiration;
	}
	public void setOpenstack_access_token_expiration(String openstack_access_token_expiration) {
		this.openstack_access_token_expiration = openstack_access_token_expiration;
	}
	public String getOpenstack_url() {
		return openstack_url;
	}
	public void setOpenstack_url(String openstack_url) {
		this.openstack_url = openstack_url;
	}
	public String getExpiration() {
		return expiration;
	}
	public void setExpiration(String expiration) {
		this.expiration = expiration;
	}
	public void setAccessToken(String access_token){
		this.access_token = access_token;
		
	}


	public String getLocalStorage() {
		// TODO Auto-generated method stub
		return localStorage;
	}
	public String getLocalTmp() {
		// TODO Auto-generated method stub
		return "/sdcard/hubic/tmp/";
	}
	public void setLocalStorage(String string) {
		// TODO Auto-generated method stub
		localStorage = string;
	}
	
	
}
