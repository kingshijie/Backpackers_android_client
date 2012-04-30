package com.kingshijie.backpackers.util;

import android.os.Parcel;
import android.os.Parcelable;

public class Place implements Parcelable ,Comparable<Place>{
	public Place(long id, double x, double y, String name, double distance) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.name = name;
		this.distance = distance;
	}

	public Place() {

	}

	private long id;
	private double x;
	private double y;
	private String name;
	private String description;
	private String notice;
	private double distance;

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return the distance
	 */
	public double getDistance() {
		return distance;
	}

	/**
	 * @param distance
	 *            the distance to set
	 */
	public void setDistance(double distance) {
		this.distance = distance;
	}

	/**
	 * @return the x
	 */
	public double getX() {
		return x;
	}

	/**
	 * @param x
	 *            the x to set
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public double getY() {
		return y;
	}

	/**
	 * @param y
	 *            the y to set
	 */
	public void setY(double y) {
		this.y = y;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
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
			p.description = source.readString();
			p.notice = source.readString();
			p.distance = source.readDouble();
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
		dest.writeString(description);
		dest.writeString(notice);
		dest.writeDouble(distance);
	}


	@Override
	public int compareTo(Place arg0) {
		if(this.getDistance() - arg0.getDistance() < 0)
			return -1;
		else
			return 1;
	}

}
