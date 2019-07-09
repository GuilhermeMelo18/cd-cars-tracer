package br.cin.tbookmarks.recommender.database.contextual;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public enum LocationCountryContextualAttribute implements AbstractContextualAttribute{
	
	UNKNOWN(-1),
	UNITED_STATES(0),UNITED_KINGDOM(1),CANADA(2),AUSTRALIA(3),/*THAILAND(4),*/VENEZUELA(5),ISRAEL(6),
	FRANCE(7),INDIA(8),SWITZERLAND(9),/*ICELAND(10),*/IRELAND(11),/*SINGAPORE(12),*/NEW_ZEALAND(13),
	BRAZIL(14), /*MEXICO(15),*/SWEDEN(16),/*CAMEROON(17),NICARAGUA(18),NETHERLANDS(19),NORWAY(20),*/
	JAPAN(21),/*SRI_LANKA(22),LITHUANIA(23),*/GREECE(24),/*LAOS(25),*/RUSSIA(26),/*PARAGUAY(27),MOROCCO(28)
	,PHILIPPINES(29),TAIWAN(30),DENMARK(31),*/HONG_KONG(32),CHINA(33),/*MALAYSIA(34),GUYANA(35),
	PORTUGAL(36),*/SOUTH_AFRICA(37),GERMANY(38),/*MALTA(39),*/ITALY(40),/*ISLE_OF_MAN(41),BANGLADESH(42),
	COSTA_RICA(43),TURKEY(44),EGYPT(45),JORDAN(46),LATVIA(47),SPAIN(48),
	BRITISH_INDIAN_OCEAN_TERRITORY(49),DOMINICAN_REPUBLIC(50),PUERTO_RICO(51),LUXEMBOURG(52),*/
	SOUTH_KOREA(53),CZECH_REPUBLIC(54),/*SAUDI_ARABIA(55),*/INDONESIA(56),/*BARBADOS(57),FINLAND(58),
	SLOVENIA(59),GUATEMALA(60),KUWAIT(61),CROATIA(62),AUSTRIA(63),NIGERIA(64),MACEDONIA_FYROM(65),
	IRAQ(66),AFGHANISTAN(67),PAKISTAN(68),BERMUDA(69),CYPRUS(70),US_VIRGIN_ISLANDS(71),CHILE(72),
	URUGUAY(73),GEORGIA(74),ARGENTINA(75),PANAMA(76),SERBIA(77),UNITED_ARAB_EMIRATES(78),ZAMBIA(79),
	ANDORRA(80),COLOMBIA(81),BELGIUM(82),MONGOLIA(83),BAHRAIN(84),BRUNEI(85),JERSEY(86),JAMAICA(87)
	,HONDURAS(88),POLAND(89),HUNGARY(90),ETHIOPIA(91),ROMANIA(92),FEDERATED_STATES_OF_MICRONESIA(93)
	,OMAN(94),ANTARCTICA(95),CUBA(96),PERU(97),LEBANON(98),BOTSWANA(99),ZIMBABWE(100),
	EL_SALVADOR(101),ERITREA(102),SYRIA(103),UKRAINE(104),IRAN(105),ECUADOR(106),GUAM(107),SINT_MAARTEN(108),
	SLOVAKIA(109),BOLIVIA(110),NORFOLK_ISLAND(111),FRENCH_POLYNESIA(112)*/;

	private long code;

	private LocationCountryContextualAttribute(long value) {
		this.code = value;
	}

	@Override
	public long getCode() {
		
		return this.code;
	}

	public static LocationCountryContextualAttribute getInstanceByCode(long code){
		
		for(LocationCountryContextualAttribute d : LocationCountryContextualAttribute.values()){
			if(d.getCode() == code){
				return d;
			}
		}
		
		return null;
	}

	public static LocationCountryContextualAttribute getEnum(String name) {
		
		try{
			return valueOf(name.replaceAll("\\s", "_").toUpperCase());
		}catch(Exception e){
		
		/*	if (name.equalsIgnoreCase("U.S. Virgin Islands")) {
				return US_VIRGIN_ISLANDS;
			}else if (name.equalsIgnoreCase("Macedonia (FYROM)")) {
				return MACEDONIA_FYROM;
			}else{ */
				if(name.length()>1){
					System.err.println(name + " unknown country");
				}				
				return UNKNOWN;
		//	}
		}
	}
	
	@Override
	public List<AbstractContextualAttribute> valuesForTest() {
		
		HashSet<Long> aux = new HashSet<Long>();
		
		List<AbstractContextualAttribute> valuesForTest = new ArrayList<AbstractContextualAttribute>();
		for(LocationCountryContextualAttribute attr : LocationCountryContextualAttribute.values()){
			if(!attr.equals(LocationCountryContextualAttribute.UNKNOWN) && !aux.contains(attr.getCode())){
				valuesForTest.add(attr);
				aux.add(attr.getCode());
			}
		}
		return valuesForTest;
	}
}
