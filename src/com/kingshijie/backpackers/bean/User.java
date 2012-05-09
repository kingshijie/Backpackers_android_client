package com.kingshijie.backpackers.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class User extends GPoint{
	
	protected String birthCity;
	protected String liveCity;
	protected int credit;
	protected String interests;
	protected int sex;
	protected int additionNum;
	protected int reportNum;

	

	/**
	 * @return the additionNum
	 */
	public int getAdditionNum() {
		return additionNum;
	}

	/**
	 * @param additionNum the additionNum to set
	 */
	public void setAdditionNum(int additionNum) {
		this.additionNum = additionNum;
	}

	/**
	 * @return the reportNum
	 */
	public int getReportNum() {
		return reportNum;
	}

	/**
	 * @param reportNum the reportNum to set
	 */
	public void setReportNum(int reportNum) {
		this.reportNum = reportNum;
	}


	/**
	 * @return the sex
	 */
	public int getSex() {
		return sex;
	}

	/**
	 * @param sex the sex to set
	 */
	public void setSex(int sex) {
		this.sex = sex;
	}

	/**
	 * @return the birthCity
	 */
	public String getBirthCity() {
		return birthCity;
	}

	/**
	 * @param birthCity the birthCity to set
	 */
	public void setBirthCity(String birthCity) {
		this.birthCity = birthCity;
	}

	/**
	 * @return the liveCity
	 */
	public String getLiveCity() {
		return liveCity;
	}

	/**
	 * @param liveCity the liveCity to set
	 */
	public void setLiveCity(String liveCity) {
		this.liveCity = liveCity;
	}

	/**
	 * @return the credit
	 */
	public int getCredit() {
		return credit;
	}

	/**
	 * @param credit the credit to set
	 */
	public void setCredit(int credit) {
		this.credit = credit;
	}

	/**
	 * @return the interests
	 */
	public String getInterests() {
		return interests;
	}

	/**
	 * @param interests the interests to set
	 */
	public void setInterests(String interests) {
		this.interests = interests;
	}


	public static final Parcelable.Creator<User> CREATOR = new Creator<User>() {

		@Override
		public User createFromParcel(Parcel source) {
			User user = new User();
			user.id = source.readLong();
			user.x = source.readDouble();
			user.y = source.readDouble();
			user.name = source.readString();
			user.distance = source.readDouble();
			user.birthCity = source.readString();
			user.liveCity = source.readString();
			user.credit = source.readInt();
			user.interests = source.readString();
			user.sex = source.readInt();
			user.additionNum = source.readInt();
			user.reportNum = source.readInt();
			return user;
		}

		@Override
		public User[] newArray(int size) {
			return new User[size];
		}
		
	};
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel user, int flags) {
		user.writeLong(id);
		user.writeDouble(x);
		user.writeDouble(y);
		user.writeString(name);
		user.writeDouble(distance);
		user.writeString(birthCity);
		user.writeString(liveCity);
		user.writeInt(credit);
		user.writeString(interests);
		user.writeInt(sex);
		user.writeInt(additionNum);
		user.writeInt(reportNum);
	}

}
