package com.fer.labust.lawnmower.model;

public class Line {
	private Point PrvaTocka;// (x2,y2)
	private Point DrugaTocka; // (x1,y1)
	private double Nagib;
	private double L;
	private boolean _vodoravan = false;
	private boolean _okomit = false;

	// JEDNADZBA PRAVCA
	// y=kx+l

	// jednadzba pravca kroz dvije tocke
	// y-y1=((y2-y1)/8x2-x1))(x-x1)
	// ili
	// Ax+By+C=0

	// koeficijent nagiba k
	// k= (y2-y1)/(x2-x1)

	// odsjecak na osi x -> l
	// l = y1 - k*x1

	public Line(Point a, Point b) {
		this.setPrvaTocka(a);
		this.setDrugaTocka(b);
		this.setNagib(odrediNagibPravca());
		this.setL(odrediOdsjecak());
	}

	public Line(double nagib, double odsjecakNaOsiY) {
		this.setNagib(nagib);
		this.setL(odsjecakNaOsiY);
	}

	private double odrediNagibPravca() {
		if (getPrvaTocka().getLongitude() == getDrugaTocka().getLongitude()) {
			_okomit = true;
			return 0;
		} else if (getPrvaTocka().getLatitude() == getDrugaTocka()
				.getLatitude()) {
			_vodoravan = true;
			return 0;
		} else {
			double brojnik = getPrvaTocka().getLatitude()
					- getDrugaTocka().getLatitude();
			double nazivnik = getPrvaTocka().getLongitude()
					- getDrugaTocka().getLongitude();
			return brojnik / nazivnik;
		}
	}

	private double odrediOdsjecak() {
		if ((getPrvaTocka().getLongitude() != getDrugaTocka().getLongitude())
				&& (getPrvaTocka().getLatitude() != getDrugaTocka()
						.getLatitude())) {
			return this.getDrugaTocka().getLatitude() - this.getNagib()
					* this.getDrugaTocka().getLongitude();
		} else {
			return 0;
		}
	}

	public Point TockaPresjeka(Line pravac) {
		Point tockaPresjeka = new Point();
		double x = 0;
		double y = 0;
		if (_okomit) {
			x = this.getPrvaTocka().getLongitude();
			y = pravac.getNagib() * x + pravac.getL();
		} else if (_vodoravan) {
			y = this.getPrvaTocka().getLatitude();
			x = (y - pravac.getL()) / pravac.getNagib();
		} else {
			if (this.getNagib() > pravac.getNagib()) {
				x = (pravac.getL() - this.getL())
						/ (this.getNagib() - pravac.getNagib());
				y = this.getNagib() * x + this.getL();
				tockaPresjeka.setLongitude(x);
				tockaPresjeka.setLatitude(y);
			} else {
				x = (this.getL() - pravac.getL())
						/ (pravac.getNagib() - this.getNagib());
				y = this.getNagib() * x + this.getL();
			}
		}
		if ((y > 90) || (y < -90)) // maknuti ovaj diooooooooooooooooooooooooo
		{
			x = 0;
			y = 0;
		}
		tockaPresjeka.setLongitude(x);
		tockaPresjeka.setLatitude(y);
		return tockaPresjeka;
	}

	public Point getPrvaTocka() {
		return PrvaTocka;
	}

	public void setPrvaTocka(Point prvaTocka) {
		PrvaTocka = prvaTocka;
	}

	public Point getDrugaTocka() {
		return DrugaTocka;
	}

	public void setDrugaTocka(Point drugaTocka) {
		DrugaTocka = drugaTocka;
	}

	public double getNagib() {
		return Nagib;
	}

	public void setNagib(double nagib) {
		Nagib = nagib;
	}

	public double getL() {
		return L;
	}

	public void setL(double l) {
		L = l;
	}
}
