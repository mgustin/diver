package com.fer.labust.lawnmower.model;

import java.util.ArrayList;
import java.util.List;

public class Poligon {
	private List<Point> vrhovi;
	private List<Line> pravci;

	private Point najzapadnijaTocka;
	private Point najistocnijaTocka;
	private Point najsjevernijaTocka;
	private Point najjuznijaTocka;

	public Poligon(List<Point> uVrhovi) {
		pravci = new ArrayList<Line>();
		this.vrhovi = uVrhovi;
		odrediPravce();
		odrediNajzapadnijuTocku();
		odrediNajistocnijuTocku();
		odrediNajsjevernijuTocku();
		odrediNajjuznijuTocku();
	}

	private void odrediPravce() {
		if (pravci.size() > 0) {
			pravci.clear();
		}
		int i = 1;
		while (vrhovi.size() > i) {
			Line pravac = new Line(vrhovi.get(i - 1), vrhovi.get(i));
			pravci.add(pravac);
			i++;
		}
		Line zPravac = new Line(vrhovi.get(0), vrhovi.get(i - 1));
		pravci.add(zPravac);
	}

	void odrediNajzapadnijuTocku() {
		this.najzapadnijaTocka = vrhovi.get(0);
		for (Point tocka : vrhovi) {
			if (tocka.getLongitude() < najzapadnijaTocka.getLongitude()) {
				this.najzapadnijaTocka = tocka;
			}
		}
	}

	private void odrediNajistocnijuTocku() {
		this.najistocnijaTocka = vrhovi.get(0);
		for (Point tocka : vrhovi) {
			if (tocka.getLongitude() > najistocnijaTocka.getLongitude()) {
				this.najistocnijaTocka = tocka;
			}
		}
	}

	private void odrediNajsjevernijuTocku() {
		this.najsjevernijaTocka = vrhovi.get(0);
		for (Point tocka : vrhovi) {
			if (tocka.getLatitude() > najsjevernijaTocka.getLatitude()) {
				this.najsjevernijaTocka = tocka;
			}
		}
	}

	private void odrediNajjuznijuTocku() {
		this.najjuznijaTocka = vrhovi.get(0);
		for (Point tocka : vrhovi) {
			if (tocka.getLatitude() < najjuznijaTocka.getLatitude()) {
				this.najjuznijaTocka = tocka;
			}
		}
	}

	public List<Point> getVrhovi() {
		return vrhovi;
	}

	public void setVrhovi(List<Point> vrhovi) {
		this.vrhovi = vrhovi;
	}

	public List<Line> getPravci() {
		return pravci;
	}

	public void setPravci(List<Line> pravci) {
		this.pravci = pravci;
	}

	public Point getNajzapadnijaTocka() {
		return najzapadnijaTocka;
	}

	public void setNajzapadnijaTocka(Point najzapadnijaTocka) {
		this.najzapadnijaTocka = najzapadnijaTocka;
	}

	public Point getNajistocnijaTocka() {
		return najistocnijaTocka;
	}

	public void setNajistocnijaTocka(Point najistocnijaTocka) {
		this.najistocnijaTocka = najistocnijaTocka;
	}

	public Point getNajsjevernijaTocka() {
		return najsjevernijaTocka;
	}

	public void setNajsjevernijaTocka(Point najsjevernijaTocka) {
		this.najsjevernijaTocka = najsjevernijaTocka;
	}

	public Point getNajjuznijaTocka() {
		return najjuznijaTocka;
	}

	public void setNajjuznijaTocka(Point najjuznijaTocka) {
		this.najjuznijaTocka = najjuznijaTocka;
	}
}
