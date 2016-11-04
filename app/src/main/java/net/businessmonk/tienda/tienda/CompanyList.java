package net.businessmonk.tienda.tienda;

import java.util.ArrayList;

/**
 * Created by ahmed on 24/07/16.
 */
public class CompanyList {
	private String id;
	private String points ;
	private int gifts_num;
	private String logo;
	private String promotions;
	private ArrayList<String> gifts  = new ArrayList<>();

	public void setGifts(String g ) {
		this.gifts.add(g);
	}

	public String getPromotions() {
		return promotions;
	}

	public void setPromotions(String promotions) {
		this.promotions = promotions;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPoints() {
		return points;
	}

	public void setPoints(String points) {
		this.points = points;
	}

	public String getGifts(int i) {
		return gifts.get(i);
	}

	public int getGifts_num() {
		return gifts_num;
	}

	public void setGifts_num(int gifts_num) {
		this.gifts_num += gifts_num;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}
}
