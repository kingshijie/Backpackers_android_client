package com.kingshijie.backpackers.util;

public class LevelCalculator {
	public static String getLevelName(int credit) {
		int level = getLevel(credit);
		return getName(level);
	}
	
	private static int getLevel(int credit){
		int i = 1;
		int tot = 0;
		while (credit > i * 10 + tot) {
			tot += i * 10;
			i++;
		}
		return i;
	}
	
	private static String getName(int level){
		if(level==1){
			return "初级背包客";
		}else if(level<3){
			return "入门级背包客";
		}else if(level<5){
			return "正式背包客";
		}else if(level<7){
			return "背包客之星";
		}else if(level<9){
			return "轻车熟路";
		}else if(level>10){
			return "花甲背包客";
		}
		return "背包客";
	}
}
