package com.fer.labust.lawnmower.services;

import java.util.ArrayList;
import java.util.List;


import com.fer.labust.lawnmower.model.Line;
import com.fer.labust.lawnmower.model.Poligon;
import com.fer.labust.lawnmower.model.Point;


public class PlannerService {
	public static final Double INFINITY = Double.MAX_VALUE;
    private Poligon _poligon;
    private int _razmak;
    //private List<double> _nagibiPravaca = new List<double>(); 

    private Poligon poligon;

    private int razmak;

    public PlannerService(Poligon p, int razmak)
    {
        this._poligon = p;
        this._razmak = razmak;
    }
     public PlannerService()
     {
         
     }

    public double OdrediDuzinuStaze(List<Point> tocke )
    {
        double duzina = 0;
        int brojTocaka = tocke.size()-1; //da ne trazi spajanje zadnje i nepostojece
        for (int i = 0; i < brojTocaka;i++ )
        {
            duzina = duzina + tocke.get(i).getDistanceTo(tocke.get(i + 1));
        }
        return duzina;
    }

    public List<Point> pronadiTockePresjeka(List<Point> vrhovi)
    {
        Poligon poligon = new Poligon(vrhovi);
        List<Point> tockePresjeka = new ArrayList<Point>();
        //tockePresjeka.AddRange(vrhovi);
        double korak = odrediKorak(poligon);
        korak = 0.5;//------------------------------------------------!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        Point a = new Point( poligon.getNajzapadnijaTocka().getLatitude(), poligon.getNajzapadnijaTocka().getLongitude() + korak);
        Point b = new Point( a.getLatitude() + 1, a.getLongitude());
        while (a.getLongitude() < poligon.getNajistocnijaTocka().getLongitude())
        	
        {
            Line pravac = new Line(a, b);
            
            for (Line pLine : poligon.getPravci())
            {
                Point tockaPresjeka = pravac.TockaPresjeka(pLine);
                if (tockaPresjeka.isBetween(pLine.getPrvaTocka(), pLine.getDrugaTocka()))
                {
                    tockePresjeka.add(tockaPresjeka);
                }
            }
            a.setLongitude( a.getLongitude() + korak );
            b.setLongitude(b.getLongitude() + korak);
        }
        //--------------------------List<Point> ttt= rotirajPoligon(Poligon.Vrhovi)
        return tockePresjeka;
    }

    private double odrediKorak(Poligon p)
    {
        double sLatitude = (p.getNajistocnijaTocka().getLatitude() + p.getNajzapadnijaTocka().getLatitude()) / 2;
        Point zapadna = new Point(sLatitude, p.getNajzapadnijaTocka().getLongitude());
        Point istocna = new Point(sLatitude, p.getNajistocnijaTocka().getLongitude());
        double ukupnaUdaljenost = zapadna.getDistanceTo(istocna);
        double brDjelova = ukupnaUdaljenost / this._razmak;
        double korak = (p.getNajistocnijaTocka().getLongitude() - p.getNajzapadnijaTocka().getLongitude()) / brDjelova;
        return korak;
    }

    public List<Point> rotirajTocke(List<Point> Tocke, double kutRotacije)
    {
        List<Point> rotiraniVrhovi = new ArrayList<Point>();
        for (Point point : Tocke) {
        	Point noviVrh = new Point();
            noviVrh.setLongitude(point.getLongitude()*Math.cos(kutRotacije) - point.getLatitude()*Math.sin(kutRotacije));
            noviVrh.setLatitude(point.getLongitude()*Math.sin(kutRotacije) + point.getLatitude()*Math.cos(kutRotacije));
            rotiraniVrhovi.add(noviVrh);
		}
        
//        foreach (Point point : Tocke)
//        {
//            Point noviVrh = new Point();
//            noviVrh.Longitude = point.Longitude*Math.Cos(kutRotacije) - point.Latitude*Math.Sin(kutRotacije);
//            noviVrh.Latitude = point.Longitude*Math.Sin(kutRotacije) + point.Latitude*Math.Cos(kutRotacije);
//            rotiraniVrhovi.Add(noviVrh);
//        }
        return rotiraniVrhovi;
    }

    public List<Point> odrediOptimalneTockePresjeka()
    {
        double duljinaStaze = INFINITY;
        double optimalniKutRotacije = 0;
        List<Point> tockePresjeka = new ArrayList<Point>();
        double kutRotacije = 0;
        while (kutRotacije <= Math.PI)
        {
            if(tockePresjeka.size()>0) tockePresjeka.clear();
            //if (vrhoviPoligona.Count>0) vrhoviPoligona.Clear();
            List<Point> vrhoviPoligona = rotirajTocke(_poligon.getVrhovi(), kutRotacije);
            tockePresjeka = pronadiTockePresjeka(vrhoviPoligona);
            double trenutnaDuzina = OdrediDuzinuStaze(tockePresjeka);
            if (trenutnaDuzina < duljinaStaze)
            {
                duljinaStaze = trenutnaDuzina;
                optimalniKutRotacije = kutRotacije;
            }
            kutRotacije = kutRotacije + Math.PI/30; //podesiti!!!!!!!!!!!!!!!!!!!!!
        }
        List<Point> optimalnoRotiraniPoligon = rotirajTocke(_poligon.getVrhovi(), optimalniKutRotacije);
        List<Point> optimalneTockePresjekaRotirane = new ArrayList<Point>();
        optimalneTockePresjekaRotirane = pronadiTockePresjeka(optimalnoRotiraniPoligon);
        List<Point> praveTockePresjeka = rotirajTocke(optimalneTockePresjekaRotirane, -optimalniKutRotacije);

        //List<Point> praveTockePresjeka2 = new List<Point>();
        //praveTockePresjeka2.Add(new Point(6.2196535107585, 1.2601154964138));
        //praveTockePresjeka2.Add(new Point(7.8065161066894, 1.8065161066894));
        //praveTockePresjeka2.Add(new Point(5.439307021517, 1.5202309928277));
        //praveTockePresjeka2.Add(new Point(8.6130322133787, 2.6130322133787));
        //praveTockePresjeka2.Add(new Point(4.6589605322755, 1.7803464892415));
        //praveTockePresjeka2.Add(new Point(9.4195483200681, 3.4195483200681));
        //praveTockePresjeka2.Add(new Point(3.878614043034, 2.0404619856553));
        //praveTockePresjeka2.Add(new Point(9.9367732558359, 4.1264534883282));
        //praveTockePresjeka2.Add(new Point(3.0982675537924, 2.3005774820692));
        //praveTockePresjeka2.Add(new Point(9.7112031019054, 4.5775937961893));
        //praveTockePresjeka2.Add(new Point(2.3179210645509, 2.560692978483));
        //praveTockePresjeka2.Add(new Point(9.4856329479748, 5.0287341040503));
        //praveTockePresjeka2.Add(new Point(1.5375745753094, 2.8208084748969));
        //praveTockePresjeka2.Add(new Point(9.2600627940443, 5.4798744119114));
       
        return praveTockePresjeka;
        //return optimalneTockePresjekaRotirane;
    }
	public Poligon getPoligon() {
		return poligon;
	}
	public void setPoligon(Poligon poligon) {
		this.poligon = poligon;
	}
	public int getRazmak() {
		return razmak;
	}
	public void setRazmak(int razmak) {
		this.razmak = razmak;
	}
}
