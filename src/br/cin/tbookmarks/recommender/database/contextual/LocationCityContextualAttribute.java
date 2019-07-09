package br.cin.tbookmarks.recommender.database.contextual;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import br.cin.tbookmarks.recommender.database.AmazonCrossDataset;

public class LocationCityContextualAttribute implements
		AbstractContextualAttribute {
	
	private String name;
	
	private static final String citiesFile = AmazonCrossDataset.folderResources+"/cities.txt";
	
	private static ArrayList<LocationCityContextualAttribute> cities = new ArrayList<LocationCityContextualAttribute>();
	
	static{
		LocationCityContextualAttribute teste = new LocationCityContextualAttribute();
	}

	@Override
	public String name() {
		return this.name;
	}

	private long code;
	
	private LocationCityContextualAttribute(String name, long code){
		this.name = name;
		this.code = code;
	}

	private LocationCityContextualAttribute() {
		
		BufferedReader reader = null;
		InputStreamReader streamReader = null;
		
		try {
			File fileEN = new File(citiesFile);

			FileInputStream stream;

			String line = "";
		
			stream = new FileInputStream(fileEN);

			streamReader = new InputStreamReader(stream);
			reader = new BufferedReader(streamReader);
			
			line = reader.readLine();
			
			while (line != null) {
				String cities[] = line.split(",");
				for (int i = 0; i < cities.length; i++) {
					String city = cities[i].trim();
				
					String name = city.split("\\(")[0].trim();
					
					String cod = city.split("\\(")[1].split("\\)")[0].trim();
					
					LocationCityContextualAttribute l = new LocationCityContextualAttribute(name,new Long(cod));
					LocationCityContextualAttribute.cities.add(l);
				}
				
				line = reader.readLine();
					
				
			}
		
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	@Override
	public long getCode() {
		return this.code;
	}
	
	public static ArrayList<LocationCityContextualAttribute> values() {
		return cities;
	}

	public static LocationCityContextualAttribute getInstanceByCode(long code) {

		for (LocationCityContextualAttribute d : LocationCityContextualAttribute
				.values()) {
			if (d.getCode() == code) {
				return d;
			}
		}

		return null;
	}
	
	private static LocationCityContextualAttribute getInstanceByName(String name) {

		for (LocationCityContextualAttribute d : LocationCityContextualAttribute
				.values()) {
			if (d.name().equalsIgnoreCase(name)) {
				return d;
			}
		}

		return null;
	}

	public static LocationCityContextualAttribute getEnum(String name) {
		
		try{
			
			String state = name.replaceAll("\\s", "_").replaceAll("\\-", "_").replaceAll("\\'", "_").toUpperCase();
			return getInstanceByName(state);
		}catch(Exception e){
		
			if (name.equalsIgnoreCase("Charleville-MeziÃ¨res")) {
				return getInstanceByName("CHARLEVILLE_MEZIÈRES");
			}else if (name.equalsIgnoreCase("HÃ¨res")) {
				return getInstanceByName("HÈRES");
			}else if (name.equalsIgnoreCase("RÄ«ga")) {
				return getInstanceByName("RIGA");
			}else if (name.equalsIgnoreCase("VaraÅ¾din")) {
				return getInstanceByName("VARAŽDIN");
			}else if (name.equalsIgnoreCase("JaromÄ›Å™")) {
				return getInstanceByName("JAROMER");
			}else if (name.equalsIgnoreCase("Ã?frica")) {
				return getInstanceByName("ŸFRICA");
			}else if (name.equalsIgnoreCase("ParaÃ±aque")) {
				return getInstanceByName("PARAÑAQUE");
			}else if (name.equalsIgnoreCase("VrÅ¡ac")) {
				return getInstanceByName("VRŠAC");
			}else if (name.equalsIgnoreCase("VÃ¤sterÃ¥s")) {
				return getInstanceByName("VÄSTERÅS");
			}else if (name.equalsIgnoreCase("TÃ¸nsberg")) {
				return getInstanceByName("TØNSBERG");
			}else if (name.equalsIgnoreCase("DasmariÃ±as")) {
				return getInstanceByName("DASMARIÑAS");
			}else if (name.equalsIgnoreCase("ZÃ¼rich")) {
				return getInstanceByName("ZÜRICH");
			}else if (name.equalsIgnoreCase("Les HaudÃ¨res")) {
				return getInstanceByName("LES_HAUDÈRES");
			}else if (name.equalsIgnoreCase("LempÃ¤Ã¤lÃ¤")) {
				return getInstanceByName("LEMPÄÄLÄ");
			}else if (name.equalsIgnoreCase("MarÅ¡ov")) {
				return getInstanceByName("MARŠOV");
			}else if (name.equalsIgnoreCase("MAYAGÃ¼EZ")) {
				return getInstanceByName("MAYAGAEZ");
			}else if (name.equalsIgnoreCase("LEÃ³N")) {
				return getInstanceByName("LEON");
			}else if (name.equalsIgnoreCase("SUÅ‚KOWICE")) {
				return getInstanceByName("SUAKOWICE");
			}else if (name.equalsIgnoreCase("ESPAÃ±OLA")) {
				return getInstanceByName("ESPANOLA");
			}else if (name.equalsIgnoreCase("PEÃ±ALOLÃ©N")) {
				return getInstanceByName("PEALOLON");
			}else if (name.equalsIgnoreCase("BANSKÃ¡ BYSTRICA")) {
				return getInstanceByName("BANSKA_BYSTRICA");
			}else if (name.equalsIgnoreCase("BESANÃ§ON")) {
				return getInstanceByName("BESANAON");
			}else if (name.equalsIgnoreCase("TIMIÈ™OARA")) {
				return getInstanceByName("TIMIEOARA");
			}else{
				if(name.length()>1){
					System.err.println(name + " unknown city");
				}
				return getInstanceByName("UNKNOWN");
			}
			/*else if (name.equalsIgnoreCase("south carolina")) {
				return SOUTH_CAROLINA;
			}else if (name.equalsIgnoreCase("new mexico")) {
				return NEW_MEXICO;
			}else if (name.equalsIgnoreCase("new hampshire")) {
				return NEWHAMPSHIRE;
			}else if (name.equalsIgnoreCase("district of columbia")) {
				return DISTRICT_OF_COLUMBIA;
			}else if (name.equalsIgnoreCase("rhode island")) {
				return RHODE_ISLAND;
			}else if (name.equalsIgnoreCase("west virginia")) {
				return WEST_VIRGINIA;
			}else if (name.equalsIgnoreCase("north dakota")) {
				return NORTH_DAKOTA;
			}else if (name.equalsIgnoreCase("south dakota")) {
				return SOUTH_DAKOTA;
			}else{
				//System.out.println(name + " category unknown");
				return OTHER;
			}*/
		}
	}

	@Override
	public List<AbstractContextualAttribute> valuesForTest() {

		HashSet<Long> aux = new HashSet<Long>();

		List<AbstractContextualAttribute> valuesForTest = new ArrayList<AbstractContextualAttribute>();
		for (LocationCityContextualAttribute attr : LocationCityContextualAttribute
				.values()) {
			if (!attr.equals(LocationCityContextualAttribute.getEnum("UNKNOWN"))//FIXME: ZUTPHEN só para books e movies (economia de testes)
					&& !aux.contains(attr.getCode()) /*&& attr.getCode() <= LocationCityContextualAttribute.ZUTPHEN.code*/) {
				valuesForTest.add(attr);
				aux.add(attr.getCode());
			}
		}
		return valuesForTest;
	}

	
	/*public static void main(String[] args) {
		LocationCityContextualAttribute l = new LocationCityContextualAttribute();
		for(LocationCityContextualAttribute l2 : LocationCityContextualAttribute.values()){
			System.out.println(l2.name());
		}
	}*/
	
}
