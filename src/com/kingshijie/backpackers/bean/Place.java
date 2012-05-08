package com.kingshijie.backpackers.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class Place extends GPoint implements Comparable<Place>{
	public Place(long id, double x, double y, String name, double distance) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.name = name;
		this.distance = distance;
	}

	public Place() {

	}

	private String description;
	private String notice;
	private int score;
	private int voted;
	private String city;
	private long user_id;
	private String username;

	/**
	 * @return the score
	 */
	public int getScore() {
		return score;
	}

	/**
	 * @param score the score to set
	 */
	public void setScore(int score) {
		this.score = score;
	}

	/**
	 * @return the voted
	 */
	public int getVoted() {
		return voted;
	}

	/**
	 * @param voted the voted to set
	 */
	public void setVoted(int voted) {
		this.voted = voted;
	}

	/**
	 * @return the city
	 */
	public String getCity() {
		return city;
	}

	/**
	 * @param city the city to set
	 */
	public void setCity(String city) {
		this.city = city;
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
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the notice
	 */
	public String getNotice() {
		return notice;
	}

	/**
	 * @param notice
	 *            the notice to set
	 */
	public void setNotice(String notice) {
		this.notice = notice;
	}

	public static final Parcelable.Creator<Place> CREATOR = new Creator<Place>() {
		@Override
		public Place createFromParcel(Parcel source) {
			Place p = new Place();
			p.id = source.readLong();
			p.x = source.readDouble();
			p.y = source.readDouble();
			p.name = source.readString();
			p.distance = source.readDouble();
			p.description = source.readString();
			p.notice = source.readString();
			p.score = source.readInt();
			p.voted = source.readInt();
			p.city = source.readString();
			p.user_id = source.readLong();
			p.username = source.readString();
			return p;
		}

		@Override
		public Place[] newArray(int size) {
			return new Place[size];
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
		dest.writeString(description);
		dest.writeString(notice);
		dest.writeInt(score);
		dest.writeInt(voted);
		dest.writeString(city);
		dest.writeLong(user_id);
		dest.writeString(username);
	}


	@Override
	public int compareTo(Place arg0) {
		if(this.getDistance() - arg0.getDistance() < 0)
			return -1;
		else
			return 1;
	}

}
