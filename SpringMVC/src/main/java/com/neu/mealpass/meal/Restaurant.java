package com.neu.mealpass.meal;

import com.neu.mealpass.user.Address;

public class Restaurant {

	private int id;
	private Address address;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Address getAddress() {
		return address;
	}
	public void setAddress(Address address) {
		this.address = address;
	}
	
}