package com.kingshijie.backpackers.util;

public class ServerHelper {
	//模块的中文名
	public static String hostel = "旅店";
	public static String camping = "露营点";
	public static String scenery = "旅游点";
	public static String shop = "购物点";
	//模块对应的Controller名称
	public static String hostelController = "module_hostel";
	public static String campingController = "module_camping";
	public static String sceneryController = "module_scenery";
	public static String shopController = "module_shop";
	public static String userController = "main";
	public static String eventController = "event";
	//远程提供的action名称
	public static String addModuleAction = "android_add";
	public static String editModuleAction = "android_edit";
	public static String searchNearAction = "android_search_near";
	public static String loginAction = "android_login";
	public static String fetchItemAction = "android_fetch_item";
	public static String reportAction = "android_report";
	public static String scoringAction = "android_scoring";
	public static String setParamsAction = "android_set_user_params";
	public static String setMyLocationAction = "android_set_my_location";
	public static String fetchNearUserAction = "android_fetch_near_user";
	public static String fetchNearEventAction = "android_fetch_near_event";
	public static String fetchEventAction = "android_fetch_event";
	public static String publishEventAction = "android_publish_event";
	public static String fetchUserInfoAction = "android_fetch_user_info";
	
	public static String getControllerByName(String moduleName){
		if(moduleName.equals(hostel)){
			return hostelController;
		}else if(moduleName.equals(camping)){
			return campingController;
		}else if(moduleName.equals(scenery)){
			return sceneryController;
		}else if(moduleName.endsWith(shop)){
			return shopController;
		}
		return null;
	}
	
	public static String getNameByController(String ctrl){
		if(ctrl.equals(hostelController)){
			return hostel;
		}else if(ctrl.equals(campingController)){
			return camping;
		}else if(ctrl.equals(sceneryController)){
			return scenery;
		}else if(ctrl.equals(shopController)){
			return shop;
		}
		return null;
	}
}
