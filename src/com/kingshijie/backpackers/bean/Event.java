package com.kingshijie.backpackers.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class Event extends GPoint {
	protected String destination;
	protected long user_id;
	protected String time;
	protected String spot;
	protected String contact;
	protected String content;
	protected String username;
	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	/**
	 * @return the destination
	 */
	public String getDestination() {
		return destination;
	}
	/**
	 * @param destination the destination to set
	 */
	public void setDestination(String destination) {
		this.destination = destination;
	}
	/**
	 * @return the user_id
	 */
	public long getUser_id() {
		return user_id;
	}
	/**
	 * @param user_id the user_id to set
	 */
	public void setUser_id(long user_id) {
		this.user_id = user_id;
	}
	/**
	 * @return the time
	 */
	public String getTime() {
		return time;
	}
	/**
	 * @param time the time to set
	 */
	public void setTime(String time) {
		this.time = time;
	}
	/**
	 * @return the spot
	 */
	public String getSpot() {
		return spot;
	}
	/**
	 * @param spot the spot to set
	 */
	public void setSpot(String spot) {
		this.spot = spot;
	}
	/**
	 * @return the contact
	 */
	public String getContact() {
		return contact;
	}
	/**
	 * @param contact the contact to set
	 */
	public void setContact(String contact) {
		this.contact = contact;
	}
	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}
	/**
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}
	
	public static final Parcelable.Creator<Event> CREATOR = new Creator<Event>() {
		@Override
		public Event createFromParcel(Parcel source) {
			Event event = new Event();
			event.id = source.readLong();
			event.x = source.readDouble();
			event.y = source.readDouble();
			event.name = source.readString();
			event.distance = source.readDouble();
			event.destination = source.readString();
			event.user_id = source.readLong();
			event.time = source.readString();
			event.spot = source.readString();
			event.contact = source.readString();
			event.content = source.readString();
			event.username = source.readString();
			return event;
		}

		@Override
		public Event[] newArray(int size) {
			return new Event[size];
		}
	};

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeDouble(x);
		dest.writeDouble(y);
		dest.writeString(name);
		dest.writeDouble(distance);
		dest.writeString(destination);
		dest.writeLong(user_id);
		dest.writeString(time);
		dest.writeString(spot);
		dest.writeString(contact);
		dest.writeString(content);
		dest.writeString(username);
	}
}
