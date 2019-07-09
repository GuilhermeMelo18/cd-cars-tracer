package br.cin.tbookmarks.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;

import br.cin.tbookmarks.client.Result;
import br.cin.tbookmarks.recommender.database.AbstractDataset;
import br.cin.tbookmarks.recommender.database.BooksTwitterDataset;
import br.cin.tbookmarks.recommender.database.EventsTwitterDataset;
import br.cin.tbookmarks.recommender.database.GroupLensDataset;
import br.cin.tbookmarks.recommender.database.contextual.ContextualCriteria;
import br.cin.tbookmarks.recommender.database.contextual.ContextualPreference;
import br.cin.tbookmarks.recommender.database.contextual.ContextualUserPreferenceArray;
import br.cin.tbookmarks.recommender.database.contextual.LocationCountryContextualAttribute;
import br.cin.tbookmarks.recommender.database.item.ItemDomain;
import br.cin.tbookmarks.recommender.evaluation.PredictionValues;

public final class Functions {
		
	public static final int numOfRatings(DataModel datamodel){
		int counter = 0;
		try {
			LongPrimitiveIterator iterator = datamodel.getUserIDs();
			
			while(iterator.hasNext()){
				long userId = iterator.nextLong();
				counter = counter + datamodel.getPreferencesFromUser(userId).getIDs().length;
			}
			
		} catch (TasteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return counter;
	}
	
	public static String httpGet(String urlStr, HashMap<String,String> requestProperties) throws IOException {
		  URL url = new URL(urlStr);
		  HttpURLConnection conn =
		      (HttpURLConnection) url.openConnection();
		  
		  //conn.setConnectTimeout(1000);
		  
		  if(requestProperties != null){
			  for(String key : requestProperties.keySet()){
				  conn.addRequestProperty(key, requestProperties.get(key));
			  }
		  }
		  

		  if (conn.getResponseCode() != 200) {
		    throw new IOException(conn.getResponseMessage());
		  }

		  // Buffer the result into a string
		  BufferedReader rd = new BufferedReader(
		      new InputStreamReader(conn.getInputStream()));
		  StringBuilder sb = new StringBuilder();
		  String line;
		  while ((line = rd.readLine()) != null) {
		    sb.append(line);
		  }
		  rd.close();

		  conn.disconnect();
		  return sb.toString();
		}
	
	public static final int[] getNumOfUsersAndOverlappedUsers(DataModel datamodel, AbstractDataset dataset, HashSet<ItemDomain> domains){

		int counterOverlapped = 0,counterUsers = 0;
				
		try {
			LongPrimitiveIterator iterator = datamodel.getUserIDs();
			
			while(iterator.hasNext()){
				counterUsers++;
				long userId = iterator.nextLong();
				long itemsFromUser[] = datamodel.getPreferencesFromUser(userId).getIDs(); 
				
				HashMap<ItemDomain, Integer> verifyOverlapping = new HashMap<ItemDomain, Integer>();
				
				
				for(int i=0; i < itemsFromUser.length;i++){
					
					ItemDomain itemDomain = dataset.getItemInformationByID(itemsFromUser[i]).getItemDomain();
					
					verifyOverlapping.put(itemDomain, verifyOverlapping.size());
					
					if(verifyOverlapping.containsKey(ItemDomain.BOOK) && verifyOverlapping.containsKey(ItemDomain.MUSIC)){
						counterOverlapped++;
						break;
					}else if(verifyOverlapping.containsKey(ItemDomain.MOVIE) && verifyOverlapping.containsKey(ItemDomain.MUSIC)) {
						counterOverlapped++;
						break;
					}
				}
			}
			
			System.out.println("Number of users:"+counterUsers+", overlapped: "+counterOverlapped);
			
			
		} catch (TasteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int info[] = {counterUsers,counterOverlapped};
		return info;
	
	}
	
	public static final void printNumOfItemsPerDomain(DataModel datamodel){
		HashMap<AbstractDataset,HashSet<Long>> numOfItemsPerDomain = new HashMap<AbstractDataset, HashSet<Long>>();
		numOfItemsPerDomain.put(GroupLensDataset.getInstance(), new HashSet<Long>());
		numOfItemsPerDomain.put(BooksTwitterDataset.getInstance(), new HashSet<Long>());
		numOfItemsPerDomain.put(EventsTwitterDataset.getInstance(), new HashSet<Long>());
		try {
			LongPrimitiveIterator iterator = datamodel.getUserIDs();
			
			while(iterator.hasNext()){
				long userId = iterator.nextLong();
				long itemsFromUser[] = datamodel.getPreferencesFromUser(userId).getIDs(); 
				for(int i=0; i < itemsFromUser.length;i++){
					if(GroupLensDataset.getInstance().getItemInformationByID(itemsFromUser[i]) != null){
						Long itemID= new Long(itemsFromUser[i]);
						if(!numOfItemsPerDomain.get(GroupLensDataset.getInstance()).contains(itemID)){
							numOfItemsPerDomain.get(GroupLensDataset.getInstance()).add(itemID);
						}
					}else if(BooksTwitterDataset.getInstance().getItemInformationByID(itemsFromUser[i]) != null){
						Long itemID= new Long(itemsFromUser[i]);
						if(!numOfItemsPerDomain.get(BooksTwitterDataset.getInstance()).contains(itemID)){
							numOfItemsPerDomain.get(BooksTwitterDataset.getInstance()).add(itemID);
						}
					}else if(EventsTwitterDataset.getInstance().getItemInformationByID(itemsFromUser[i]) != null){
						Long itemID= new Long(itemsFromUser[i]);
						if(!numOfItemsPerDomain.get(EventsTwitterDataset.getInstance()).contains(itemID)){
							numOfItemsPerDomain.get(EventsTwitterDataset.getInstance()).add(itemID);
						}
					}
				}
			}
			
			for(AbstractDataset abs:  numOfItemsPerDomain.keySet()){
				System.out.println("Number of items in "+abs.getClass().getSimpleName()+": "+numOfItemsPerDomain.get(abs).size());
			}
			
		} catch (TasteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static List<ItemDomain> getDatasetDomains(AbstractDataset dataset){
		ArrayList<ItemDomain> domains = new ArrayList<ItemDomain>();
		DataModel dm = dataset.getModel();
		
		try {
			LongPrimitiveIterator it = dm.getItemIDs();
			
			while(it.hasNext()){
				long itemID = it.nextLong();
				
				ItemDomain domain = dataset.getItemInformationByID(itemID).getItemDomain();
				
				if(!domains.contains(domain)){
					domains.add(domain);
				}
			}
			
		} catch (TasteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return domains;
	}
	
	public static List<ContextualCriteria> getDatasetContexts(AbstractDataset dataset){
		ArrayList<ContextualCriteria> contexts = new ArrayList<ContextualCriteria>();
		DataModel dm = dataset.getModel();
		
		try {
			LongPrimitiveIterator it = dm.getUserIDs();
			
			while(it.hasNext()){
				long userID = it.nextLong();
				
				ContextualUserPreferenceArray prefs = (ContextualUserPreferenceArray) dm.getPreferencesFromUser(userID);
				
				for (int i=0; i<prefs.length();i++) {
					ContextualCriteria cc = new ContextualCriteria(prefs.get(i).getContextualPreferences());
					if(!contexts.contains(cc)){
						contexts.add(cc);
					}
				}
				
			}
			
		} catch (TasteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return contexts;
	}
	
	public static boolean containsToken(String test, String token){
		
		test = test.toLowerCase();
		token = token.toLowerCase();
		
		String specialChars = "[<>/_\'\"\\\\$&+,.:;=%!?@#|{}\\*\\[\\]()\\s]";
		
		//String token = "we";
		
		String[] patterns = {token,specialChars+token+"$","^"+token+specialChars,specialChars+token+specialChars};
		
		//String test = ".dish.well.onesty)well";
		
		boolean contains = false;
		
		for(String patterS : patterns){
			Pattern pattern = Pattern.compile(patterS);
		    Matcher matcher = pattern.matcher(test);
		    if (matcher.find()){
		    	contains = true; 
		    	break;
		    } 
		}
		return contains;
	}
	
	public static String codeContextsToStringForFile(long c[]){
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < c.length; i++) {
			s.append(c[i]);
			s.append("|");
		}
		return s.toString().substring(0,s.toString().length()-1);
	}
	/*public static void main(String[] args) {
		
		String specialChars = "[<>/_\'\"\\\\$&+,.:;=%!?@#|{}\\*\\[\\]()\\s]";
		
		String token = "working out";
		
		String[] patterns = {specialChars+token+"$","^"+token+specialChars,specialChars+token+specialChars};
		
		String test = ".dish>working out.onesty)well";
		
		boolean contains = false;
		
		for(String patterS : patterns){
			Pattern pattern = Pattern.compile(patterS);
		    Matcher matcher = pattern.matcher(test);
		    if (matcher.find()){
		    	contains = true; 
		    	break;
		    } 
		}
		
		System.out.println(contains);

	    
		
	}*/
	/*public static void main(String[] args) {
		String countries[] = {"A'ali", "Aarau", "Aarhus", "Aberdeen", "Aberdeen Township", "Abilene", "Abington", "Acworth", "Ada", "Adamsville", "Addison", "Adel", "Adelaide", "Afton", "Agawam", "Agde", "Aiea", "Aiken", "Air Ronge", "Akron", "Alameda", "Alamo", "Albany", "Albion", "Albuquerque", "Aldie", "Alexandria", "Algona", "Algonquin", "Alhambra", "Alice", "Alice Springs", "Aligarh", "Aliso Viejo", "Allegan", "Allen", "Allen Park", "Allentown", "Alma", "Aloha", "Alpharetta", "Altamonte Springs", "Altoona", "Ama", "Amazon", "Ambavaram", "American Canyon", "Ames", "Amherst", "Amman", "Amstelveen", "Amsterdam", "Anaheim", "Anahuac", "Anchor Point", "Anchorage", "Anderson", "Andover", "Angeles", "Ankeny", "Ann Arbor", "Annandale", "Annapolis", "Antigonish", "Antioch", "Antwerp", "Apex", "Apollo Beach", "Apopka", "Appleton", "Arcadia", "Arlington", "Arnold", "Arvada", "Asan-si", "Ashburn", "Asheville", "Ashland", "Aston", "Astoria", "Asuncion", "Athens", "Atherton", "Athol", "Atlanta", "Atomic City", "Auburn", "Auckland", "Augusta", "Aurora", "Austin", "Azusa", "Baghdad", "Bagley", "Bainbridge", "Bainbridge Island", "Bakersfield", "Bala Cynwyd", "Balad", "Baldwin", "Baldwinsville", "Ballinger", "Ballston Lake", "Ballyshannon", "Balmes", "Baltimore", "Bancroft", "Bangkok", "Bangor", "Banner Elk", "Barberton", "Barcelona", "Barnegat Township", "Barnesville", "Barnet", "Barnstable", "Barre", "Barrie", "Barrington", "Barstow", "Bartlett", "Barton-upon-Humber", "Barueri", "Basel", "Bastrop", "Batavia", "Bath", "Baton Rouge", "Battle Creek", "Bay Point", "Bay Shore", "Bayonne", "Bayport", "Baytown", "Beach Park", "Beachwood", "Beauharnois", "Beaumont", "Beaune", "Beavercreek", "Beaverton", "Beckley", "Bedford", "Be'er Sheva", "Beijing", "Beirut", "Bel Air", "Belfair", "Belfast", "Bella Vista", "Belle Plaine", "Bellefontaine", "Bellefonte", "Belleville", "Bellevue", "Bellingham", "Bells", "Belmont", "Bend", "Bengaluru", "Benicia", "Bennettsville", "Bennington", "Bensalem", "Bentonville", "Berkeley", "Berkeley Springs", "Berkeley Township", "Berlin", "Bernards", "Bernardsville", "Berwick", "Bethel", "Bethel Park", "Bethesda", "Bethlehem", "Bethpage", "Bettendorf", "Beverly", "Beverly Hills", "Biddeford", "Big Rapids", "Big Stone Gap", "Big Sur", "Biloxi", "Binghamton", "Birmingham", "Biron", "Bismarck", "Blackpool", "Blacksburg", "Bladenboro", "Blaxland", "Bloom", "Bloomfield", "Bloomfield Hills", "Bloomington", "Blossburg", "Bluemont", "Blytheville", "Boca Raton", "Boerne", "Bogota", "Boise", "Bolingbrook", "Bolton", "Boom", "Boone", "Boonton", "Bosque Farms", "Bossier City", "Bossley Park", "Boston", "Bothell", "Boulder", "Bountiful", "Bowling Green", "Boxford", "Boynton Beach", "Bozeman", "Bradenton", "Brady", "Brahmapur", "Braintree", "Brampton", "Branchland", "Branchville", "Brandon", "Branford", "Branson", "Brasilia", "Brattleboro", "Brazil", "Bremerton", "Brentwood", "Brentwood Bay", "Brescia", "Brewer", "Brewster", "Brick", "Bridgeport", "Bridgeville", "Bridgewater", "Brisbane", "Bristol", "Bristolville", "Bristow", "Brockport", "Broken Arrow", "Bronx", "Bronxville", "Brookfield", "Brookland", "Brookline", "Brooklyn", "Brooklyn Center", "Brooklyn Park", "Brookshire", "Brooksville", "Brookville", "Browning", "Brownwood", "Bryan", "Bryant", "Bryson City", "Bucharest", "Buena Park", "Buena Vista", "Buenos Aires", "Buffalo", "Bullhead City", "Burbank", "Burien", "Burke", "Burlington", "Burnaby", "Burnham-on-Sea", "Burr Ridge", "Busan", "Butte City", "Bynum", "Caddo Mills", "Cadyville", "Cairo", "cal", "Calabasas", "Calais", "Caldwell", "Calgary", "Calhan", "Calhoun", "Cali", "Caliente", "Caloundra West", "Camarillo", "Cambridge", "Camden", "Cameron", "Cameron Park", "Camilla", "Camp Hill", "Campbell", "Canandaigua", "Canberra", "Canton", "Cape Coral", "Cape Girardeau", "Cape May", "Cape Town", "Caracas", "Carbondale", "Cardiff", "Carefree", "Carlisle", "Carlsbad", "Carmel-by-the-Sea", "Carolina", "Carpinteria", "Carrickmacross", "Carrollton", "Carson City", "Cartersville", "Carthage", "Cary", "Casper", "Cass Lake", "Castro Valley", "Cathlamet", "Catonsville", "Cave City", "Cedar City", "Cedar Falls", "Cedar Hills", "Cedar Park", "Cedar Rapids", "Celebration", "Centennial", "Centerville", "Central", "Central de Cabdella", "Centralia", "Centreville", "Cerulean", "Chagrin Falls", "Champaign", "Chandler", "Chanhassen", "Chapel Hill", "Chapin", "Charleston", "Charleville-Mezières", "Charlotte", "Charlottesville", "Charlton", "Charter Township of Clinton", "Chatham", "Chatsworth", "Chattanooga", "Chennai", "Chesapeake", "Cheshire", "Chester", "Chesterfield", "Cheverly", "Chevy Chase", "Cheyenne", "Chicago", "Chichen Itza", "China", "Chiniak", "Chita-shi", "Chocowinity", "Chula Vista", "Church Point", "Cincinnati", "Citrus Heights", "Ciudad de Mexico", "Claremont", "Clarksburg", "Clarksville", "Claxton", "Clayton", "Clear Lake Riviera", "Clearwater", "Cleburne", "Clermont", "Cleveland", "Cleveland Heights", "Clever", "Clinton", "Cloverdale", "Clovis", "Cocoa", "Coeur d'Alene", "Coffs Harbour", "Cogan Station", "Colfax", "College Park", "College Station", "Collegeville", "Collingdale", "Collingswood", "Colonial Heights", "Colora", "Colorado Springs", "Columbia", "Columbia Heights", "Columbiana", "Columbiaville", "Columbus", "Commerce", "Commerce charter Township", "Conception Bay South", "Concord", "Converse", "Conway", "Conyers", "Copenhagen", "Coral Gables", "Coral Springs", "Coralville", "Corbin", "Cordova", "Corfu", "Corinna", "Cornwall", "Corona", "Corpus Christi", "Cortlandt Manor", "Corvallis", "Cosmos", "Costa Mesa", "Coventry", "Covington", "Coxs Creek", "Cranberry Township", "Cranston", "Crest Hill", "Crestline", "Crestview", "Crockett", "Crofton", "Cross Roads", "Crossett", "Crown Point", "Crystal Lake", "Culiacan Rosales", "Cullman", "Cullowhee", "Culver City", "Cumberland", "Cumberland Gap", "Cumming", "Cupertino", "Curitiba", "Cut Bank", "Cuyahoga Falls", "Cypress", "Dade City", "Dallas", "Dallastown", "Dalton", "Daly City", "Damascus", "Dana Point", "Danbury", "Dandridge", "Danville", "Darien", "Darnestown", "Dartmouth", "Dasmariñas", "Davenport", "Davis", "Dayton", "Daytona Beach", "De Soto", "Dearborn", "Decatur", "Deepwater", "Deer Lake", "Deerfield", "Deerfield Beach", "DeLand", "Delft", "Delhi charter Township", "Delmar", "Delta", "Deltona", "Denmark", "Denton", "Denver", "Denville", "DeRidder", "Des Moines", "Des Plaines", "Desaignes", "Destin", "Detroit", "Devon", "DeWitt", "Dhaka", "Diamond Bar", "D'Iberville", "Diepenbeek", "Dix Hills", "Dixon", "Dobbs Ferry", "Doncaster", "Donetsk", "Dothan", "Douai", "Douglas", "Downers Grove", "Downey", "Downingtown", "Doylestown", "Driffield", "Dryden", "Dubai", "Dublin", "Dubois", "Dubuque", "Dulles", "Duluth", "Dumfries", "Dundee", "Dunning", "Durant", "Durham", "Dyer", "Eagan", "Eagle", "Eagle Mountain", "Eagletown", "Earlton", "Earth", "Easley", "East Bend", "East Bridgewater", "East Brunswick", "East Glacier Park Village", "East Haddam", "East Hanover", "East Haven", "East Lansing", "East Liverpool", "East Norriton", "East Point", "East Providence", "East Rochester", "East Rockaway", "East Williston", "East Windsor", "Eastern", "Eastman", "Easton", "Eau Claire", "Ebensburg", "Eddington", "Edgewater", "Edina", "Edinboro", "Edinburg", "Edinburgh", "Edison", "Edmond", "Edmonds", "Edmonton", "Edwardsville", "Eeklo", "Effingham", "Egg Harbor Township", "El Cajon", "El Dorado Hills", "El Paso", "Eldersburg", "Eldorado", "Elgin", "Elizabeth", "Elizabeth City", "Elizabethtown", "Elk Grove Village", "Elkhart", "Ellensburg", "Ellon", "Elmhurst", "Elmira", "Elora", "Eloy", "Emerald", "Emerald Hills", "Emigrant", "Encinitas", "Enfield", "England", "Epsom", "Erie", "Ernee", "Erumad", "Escanaba", "Escondido", "Essex", "Ester", "Estes Park", "Euclid", "Eugene", "Euless", "Eureka", "Eureka Springs", "Evans", "Evans City", "Evanston", "Evansville", "Everett", "Evora", "Ewa Beach", "Ewing Township", "Exeter", "Fahaheel", "Fair Lawn", "Fairbanks", "Fairfax", "Fairfax Station", "Fairfield", "Fairhaven", "Fairport", "Fairview", "Fall River", "Falls Church", "Falmouth", "Fargo", "Farmingdale", "Farmington", "Farmington Hills", "Fayetteville", "Federal Way", "Felton", "Ferndale", "Fernley", "Fes", "Fifield", "Finneytown", "Fish Creek", "Fishers", "Flagstaff", "Fleetwood", "Flint", "Floral Park", "Florence", "Florin", "Florissant", "Flower Mound", "Flowery Branch", "Flushing", "Foinikaria", "Folsom", "Forest Park", "Forestville", "Forsyth", "Fort Bragg", "Fort Collins", "Fort Hood", "Fort Knox", "Fort Lauderdale", "Fort Lee", "Fort Mill", "Fort Myers", "Fort Riley", "Fort Smith", "Fort Thomas", "Fort Walton Beach", "Fort Wayne", "Fort Worth", "Fort-Coulonge", "Foster City", "Fountain Hill", "Fountain Valley", "Framingham", "Francestown", "Francistown", "Franconia", "Frankford", "Frankfort", "Frankfurt", "Franklin", "Franklin Park", "Franklin Township", "Frederick", "Fredericksburg", "Fredericktown", "Freeman", "Fremont", "French Gulch", "Frenchtown", "Fresno", "Frisco", "Fukui", "Fukuoka", "Gaia", "Gainesville", "Gaithersburg", "Galesburg", "Galien", "Galveston", "Garden City", "Garden Grove", "Gardena", "Gardiner", "Gardnerville", "Garland", "Gehrde", "General Escobedo", "Geneseo", "Geneva", "Genoa", "Georgetown", "Germantown", "Gerrardstown", "Gettysburg", "Ghaziabad", "Gibbsboro", "Gig Harbor", "Gilbert", "Gilbert Plains", "Gillette", "Gillsville", "Gilroy", "Gisborne", "Gladwin", "Glasgow", "Glastonbury", "Glen Burnie", "Glen Ridge", "Glendale", "Glendora", "Glenmoore", "Glenolden", "Glens Falls", "Glenside", "Glenview", "Glocester", "Gloucester City", "Gloucester Township", "Gold Coast", "Goleta", "Goodfellow AFB", "Gothenburg", "Granada", "Grand Blanc", "Grand Cane", "Grand Canyon Village", "Grand Island", "Grand Junction", "Grand Prairie", "Grand Rapids", "Granite City", "Grantsville", "Grapevine", "Grayling", "Grayslake", "Great Neck", "Greeley", "Green", "Green Bay", "Green Valley", "Greenacres", "Greenbelt", "Greendale", "Greeneville", "Greenfield", "Greenleaf", "Greenport", "Greensboro", "Greentown", "Greenville", "Greenwich", "Greenwood", "Gresham", "Gretna", "Grinnell", "Griswold", "Grosse Pointe", "Groton", "Grover Beach", "Grovetown", "Guadalajara", "Guam", "Guantanamo", "Guatemala City", "Gurgaon", "Gurnee", "Guttenberg", "Hacienda Heights", "Haddon Heights", "Haddonfield", "Hadley", "Hager City", "Hagerstown", "Haifa", "Haledon", "Halethorpe", "Half Moon Bay", "Halifax", "Hamden", "Hamilton", "Hamilton Township", "Hammond", "Hampton", "Hanover", "Hanover Park", "Hansville", "Happy Valley", "Hardyston Township", "Hardyville", "Harpswell", "Harrisburg", "Harrisonburg", "Hartford", "Hartland", "Hasbrouck Heights", "Haslet", "Hatfield", "Hattiesburg", "Haugesund", "Havelock", "Haverhill", "Havre", "Havre de Grace", "Haymarket", "Hayward", "Hazlet", "Heber City", "Hebron", "Hell", "Helsinki", "Hempstead", "Henderson", "Hereford", "Hères", "Hernando", "Herndon", "Heroica Puebla de Zaragoza", "Heroica Veracruz", "Hershey", "Hickory", "High Point", "Highett", "Highland", "Highland Park", "Highland Springs", "Highlands Ranch", "Hilliard", "Hillsboro", "Hillsborough", "Hillsborough Township", "Hilo", "Himeji", "Hobart", "Hobbs", "Hoboken", "Hockessin", "Hoffman Estates", "Hohenwald", "Holbrook", "Holland", "Hollister", "Hollywood", "Holmdel", "Holyoke", "Homewood", "Homosassa", "Honolulu", "Hoodsport", "Hooksett", "Hoover", "Hopatcong", "Hopkins", "Hornbrook", "Horsham", "Houma", "Houston", "Hua Hin", "Hudson", "Hudsonville", "Hull", "Humacao", "Humble", "Hummelstown", "Huntersville", "Huntington", "Huntington Beach", "Huntington Woods", "Huntsville", "Hyattsville", "Hyderabad", "Hye", "Hyrum", "Ibiza", "Idaho Falls", "Idyllwild-Pine Cove", "Incline Village", "Independence", "Indian Trail", "Indiana", "Indianapolis", "Indianola", "Indio", "Ingalls", "Ingleside", "Inkster", "Innsbruck", "Interlochen", "Iowa City", "Iowa Falls", "Ipswich", "Irmo", "Iron River", "Irvine", "Irving", "Irwin", "Isesaki", "Ishpeming", "Island Heights", "Issaquah", "Istanbul", "Ithaca", "Jackson", "Jacksonville", "Jacksonville Beach", "Jalalabad", "Jamison City", "Janesville", "Jaromer", "Jasper", "Jeddah", "Jefferson", "Jefferson City", "Jeffersonville", "Jersey", "Jersey City", "Jersey Shore", "Jerusalem", "Jessup", "Jimtown", "Johannesburg", "Johns Creek", "Johnson City", "Joliet", "Jonesboro", "Jonesport", "Juarez", "Junction City", "Juneau", "Jupiter", "Jurupa Valley", "Justice", "Justin", "Kailua-Kona", "Kalamazoo", "Kalamazoo Township", "Kalispell", "Kampen", "Kaneohe", "Kanosh", "Kanpur", "Kansas City", "Karachi", "Karangasem", "Katy", "Kearney", "Kearns", "Keene", "Kefar Sava", "Keller", "Kellogg", "Kelowna", "Kenner", "Kennesaw", "Kennett Square", "Kennewick", "Kenosha", "Kensington", "Kent", "Keokuk", "Kernersville", "Kerzenheim", "Key West", "Keyport", "Kingston", "Kingsville", "Kirkcaldy", "Kirkland", "Kirksville", "Kitchener", "Kitee", "Knightdale", "Knox", "Knoxville", "Knysna", "Kodiak", "Kokomo", "Kolkata", "Kotzebue", "Krakow", "Kuala Lumpur", "Kuna", "Kutztown", "Kuwait City", "Kyoto", "La Crescenta-Montrose", "La Mesa", "La Mirada", "La Palma", "La Plume", "La Porte", "La Quinta", "La Zubia", "Lacey", "Lacombe", "Ladera Ranch", "Lady Lake", "Lafayette", "Lafayette Township", "Laguna Beach", "Laguna Niguel", "Lahaina", "Laie", "Lake Arrowhead", "Lake Bluff", "Lake Charles", "Lake City", "Lake Forest", "Lake George", "Lake Havasu City", "Lake Jackson", "Lake of the Woods", "Lake Oswego", "Lake Worth", "Lake Zurich", "Lakeland", "Lakeside", "Lakeside Park", "Lakeville", "Lakewood", "Lakher", "Lancaster", "Landgraaf", "Landisburg", "Landover Hills", "Landrum", "Lanesville", "Langley", "Lansdale", "Lansdowne", "Lansing", "Lantana", "Laporte", "Larchmont", "Largo", "Largs Bay", "Las Cruces", "Las Vegas", "Laurel", "Lausanne", "Lawrence", "Lawrence Township", "Lawrenceville", "Lawtey", "Lawton", "Layton", "League City", "Leawood", "Lebanon", "Leeds", "Lee's Summit", "Leesburg", "Lehi", "Leicester", "Leiden", "Leidschendam", "Lempäälä", "Lena", "Lennox", "Lenox", "Leominster", "Leon", "Leonardtown", "Les Haudères", "Leusden", "Levittown", "Lewis Center", "Lewis Run", "Lewisburg", "Lewiston", "Lexington", "Liberia", "Liberty", "Libertytown", "Lilburn", "Lima", "Limache", "Limerick", "Limestone", "Linares", "Lincoln", "Lincoln County", "Linden", "Lindenhurst", "Lino Lakes", "Lisbon", "Lithia Springs", "Little Chute", "Little Rock", "Littleton", "Live Oak", "Livermore", "Livingston", "Livingston Manor", "Livonia", "Lockhart", "Lockport", "Locust Valley", "Loda", "Lodi", "Logan", "Loganville", "Lombard", "London", "Long Beach", "Long Pond", "Longmeadow", "Longmont", "Longueuil", "Longview", "Longwood", "Lorain", "Los Altos", "Los Altos Hills", "Los Angeles", "Los Gatos", "Los ÿngeles", "Loudonville", "Louisville", "Loveland", "Lowell", "Lower Hutt", "Loyall", "Lubbock", "Lumberton", "Lusby", "Lussagnet-Lusson", "Lutherville-Timonium", "Lutz", "Luxembourg City", "Lynchburg", "Lynden", "Lyndhurst", "Lynn", "Lynnwood", "Macomb", "Macon", "Macungie", "Madera", "Madison", "Madisonville", "Madrid", "Magnet", "Mahtomedi", "Maineville", "Maitland", "Makati", "Malden", "Maldon", "Malibu", "Malvern", "Malverne", "Mamaroneck", "Managua", "Manama", "Manassas", "Manaus", "Manchester", "Manhattan", "Manhattan Beach", "Manila", "Mannheim", "Mansfield", "Manteca", "Manteno", "Mantua Township", "Maple Grove", "Maplewood", "Maracay", "Marblehead", "Marietta", "Marina", "Marine Corps Air Station Cherry Point", "Marion", "Marion Heights", "Markham", "Marmora", "Marrakesh", "Marriottsville", "Mars Hill", "Marshall", "Marshallberg", "Maršov", "Martin", "Martinez", "Martinsburg", "Maryland", "Marysville", "Maryville", "Mason", "Massapequa", "Massillon", "Matsuyama-shi", "Matthews", "Maumelle", "Mauriceville", "Maynard", "Mayville", "McAlester", "McAllen", "McCool", "McDonough", "McHenry", "McKinney", "McLean", "Meadow Vista", "Meaux", "Mebane", "Medford", "Media", "Medina", "Melbourne", "Melissa", "Melrose", "Memphis", "Mendham", "Mendon", "Menifee", "Menlo Park", "Menominee", "Mentor", "Merced", "Mercer Island", "Meriden", "Meridian charter Township", "Merlas", "Merrick", "Mesa", "Metairie", "Methuen", "Metropolis", "Mexico City", "Miami", "Miami Beach", "Miami Lakes", "Miamisburg", "Middlebury", "Middleton", "Middletown", "Midland", "Midlothian", "Midvale", "Midwest", "Milan", "Milford", "Mill Valley", "Millbrae", "Milledgeville", "Millsboro", "Millstadt", "Milltown", "Milpitas", "Milton", "Milwaukee", "Milwaukie", "Mineola", "Mineral Bluff", "Mineral Wells", "Minneapolis", "Minnetonka", "Minot", "Mishawaka", "Mississauga", "Missoula", "Missouri City", "Mo i Rana", "Mobile", "Modesto", "Modi'in-Maccabim-Re'ut", "Mogadore", "Moline", "Monmouth", "Monroe", "Monroe Township", "Monrovia", "Mont Alto", "Montclair", "Monterey", "Monterrey", "Montevideo", "Montgomery", "Monticello", "Montpelier", "Montreal", "Montrose", "Montville", "Monument", "Moon", "Moore", "Mooresville", "Moorhead", "Moorpark", "Moraine", "Moreno Valley", "Morgan Hill", "Morgantown", "Morristown", "Morrisville", "Morro Bay", "Mortehoe", "Moscow", "Mosul", "Mound", "Mount Holly", "Mount Joy", "Mount Juliet", "Mount Laurel", "Mount Olive Township", "Mount Pleasant", "Mount Prospect", "Mount Vernon", "Mountain Home", "Mountain House", "Mountain Top", "Mountain View", "Mountlake Terrace", "Mumbai", "Mundelein", "Munhall", "Munich", "Murfreesboro", "Murrieta", "Muscat", "Muskogee", "Mutare", "Nacogdoches", "Naknek", "Nampa", "Nanaimo", "Napa", "Naperville", "Naples", "Narita", "Nashua", "Nashville", "Natick", "Navi Mumbai", "Nazareth", "Neerabup", "Nemmeli", "Neshanic", "Neuchatel", "Neutral Bay", "Nevada", "Nevada City", "New Albany", "New Bedford", "New Bern", "New Brunswick", "New Cambria", "New Castle", "New City", "New Delhi", "New Gloucester", "New Harmony", "New Hartford", "New Haven", "New Holland", "New Hope", "New Lenox", "New London", "New Milford", "New Orleans", "New Philadelphia", "New Port Richey", "New Providence", "New Rochelle", "New York", "Newark", "Newburgh", "Newbury", "Newburyport", "Newcastle upon Tyne", "Newchurch", "Newport", "Newport Beach", "Newport News", "Newton", "Newtown", "Niagara Falls", "Nice", "Nicholasville", "Nijkerk", "Nijmegen", "Nitro", "Nixa", "Nokomis", "Norfolk", "Normal", "Norman", "Norridge", "Norristown", "North Adams", "North Bay Village", "North Bend", "North Bergen", "North Charleston", "North Fork", "North Las Vegas", "North Little Rock", "North Merrick", "North Miami Beach", "North Ogden", "North Olmsted", "North Pole", "North Potomac", "North Rustico", "North Springfield", "North Wilkesboro", "Northampton", "Northbrook", "Northfield", "Northford", "Northport", "Northville", "Norton", "Norwalk", "Norwood", "Notre Dame", "Nottingham", "Nova", "Novi", "Nunawading", "Nuneaton", "Nunya", "Nutley", "Oak Harbor", "Oak Hill", "Oak Island", "Oak Lawn", "Oak Park", "Oak Ridge", "Oakdale", "Oakland", "Oakley", "Oakton", "Oaktown", "Ocala", "Ocean Springs", "Oceanside", "Oconomowoc", "Odenton", "O'Fallon", "Ogden", "Ogdensburg", "Oita", "Oklahoma City", "Olathe", "Old Bridge Township", "Olean", "Olliergues", "Olney", "Olympia", "Omaha", "Ontario", "Ooltewah", "Oradell", "Orange", "Orange Park", "Oregon", "Orem", "Orion charter Township", "Orland", "Orland Park", "Orlando", "Ormond Beach", "Orting", "Osaka", "Oshkosh", "Oslo", "Ossining", "Oswego", "Ottawa", "Overland Park", "Owen Sound", "Owings Mills", "Oxford", "Oxford Charter Township", "Oxford Township", "Pacific Grove", "Pacifica", "Pagosa Springs", "Paharpur", "Paicines", "Pakse", "Palatine", "Palm Bay", "Palm Beach", "Palm Coast", "Palm Desert", "Palm Springs", "Palmdale", "Palo Alto", "Palos Park", "Pana", "Panama City", "Panama City Beach", "Paoli", "Paonia", "Papillion", "Parañaque", "Paris", "Park Ridge", "Parker", "Parkway-South Sacramento", "Parsonsfield", "Pasadena", "Paso Robles", "Patterson", "Pawnee City", "Peabody", "Peapack and Gladstone", "Pearl", "Pearland", "Peekskill", "Pella", "Pembroke Pines", "Penarth", "Penfield", "Pennsauken Township", "Pennsville Township", "Penrith", "Pensacola", "Peoria", "Perry", "Perth", "Perugia", "Petal", "Petaluma", "Peterborough", "Petoskey", "Pflugerville", "Philadelphia", "Phoenix", "Phoenixville", "Pikesville", "Pine", "Pine Valley", "Pinehurst", "Pineville", "Piney Flats", "Pioneer", "Piscataway Township", "Pittsburg", "Pittsburgh", "Pittsfield", "Placerville", "Placitas", "Plainfield", "Plainsboro Township", "Plano", "Plant City", "Plantation", "Pleasant Hill", "Pleasanton", "Plymouth", "Plymouth Meeting", "Pocatello", "Poinciana", "Ponce", "Ponte Vedra Beach", "Pontotoc", "Poplar Bluff", "Poquoson", "Port Angeles", "Port Huron", "Port Macquarie", "Port Townsend", "Port Washington", "Portal", "Porterville", "Portland", "Porto Alegre", "Portola Valley", "Portsmouth", "Potomac", "Potter", "Pottstown", "Poughkeepsie", "Poulsbo", "Powder Springs", "Powhatan", "Poynton", "Prague", "Prairie Village", "Prairieville", "Presque Isle", "Prichard", "Princeton", "Prineville", "Proctorville", "Providence", "Provo", "Puerto Plata", "Pullman", "Pune", "Punta Gorda", "Purcellville", "Puyallup", "Quakertown", "Quebec City", "Queen Creek", "Queens", "Quezon City", "Quincy", "Quinton", "Ra'anana", "Racine", "Radcliff", "Rainsville", "Raleigh", "Ramat Gan", "Rancho Cucamonga", "Rancho Mirage", "Rancho Palos Verdes", "Rancho Santa Margarita", "Randolph", "Rapid City", "Ravenna", "Ravensdale", "Raytown", "Reading", "Red Bank", "Red Deer", "Redding", "Redlands", "Redmond", "Redondo Beach", "Redwood City", "Rehoboth Beach", "Rehovot", "Reno", "Rensselaer", "Renton", "Rescue", "Reston", "Revere", "Rexburg", "Reykjavik", "Reynoldsburg", "Richardson", "Richfield", "Richland", "Richlands", "Richmond", "Ridge", "Ridgecrest", "Ridgefield", "Riga", "Rio de Janeiro", "Rio Grande", "Rio Rancho", "Ripley", "River Forest", "Riverside", "Riverton", "Riverview", "Roanoke", "Robbinsdale", "Robbinsville", "Rochdale", "Rochester", "Rochester Hills", "Rock Hill", "Rock Island", "Rock Spring", "Rock Springs", "Rockford", "Rockingham", "Rockland", "Rocklin", "Rockmart", "Rockport", "Rockville", "Rockville Centre", "Rocky Point", "Rocky River", "Rogers", "Rohan", "Rolla", "Rolling Hills Estates", "Rolling Meadows", "Roma", "Rome", "Rosamond", "Roseburg", "Rosedale", "Roseland", "Roselle", "Rosendale", "Roseville", "Roslyn", "Rossville", "Roswell", "Rotterdam", "Round Lake", "Round Rock", "Rowland Heights", "Rowville", "Royal Oak", "Rue", "Rural Retreat", "Rush", "Rushville", "Ruston", "Rutherfordton", "Sackets Harbor", "Saco", "Sacramento", "Saddle River", "Saint Albans", "Saint Augustine", "Saint Charles", "Saint Cloud", "Saint George", "Saint James", "Saint John", "Saint Joseph", "Saint Louis", "Saint Marys", "Saint Paul", "Saint Peters", "Saint Petersburg", "Saint Thomas", "Salado", "Salem", "Salina", "Saline", "Salisbury", "Salt Lake City", "Salt Point", "Sammamish", "San Angelo", "San Anselmo", "San Antonio", "San Bernardino", "San Bruno", "San Carlos", "San Clemente", "San Diego", "San Francisco", "San Jose", "San Juan", "San Juan Capistrano", "San Juan de Lurigancho", "San Leandro", "San Lorenzo", "San Luis Obispo", "San Marcos", "San Mateo", "San Pablo", "San Rafael", "San Ramon", "Sandown", "Sandton", "Sandusky", "Sandwich", "Sant Andreu de Llavaneres", "Santa Ana", "Santa Barbara", "Santa Clara", "Santa Clarita", "Santa Cruz", "Santa Fe", "Santa Maria", "Santa Monica", "Santa Paula", "Santa Rosa", "Santiago", "Sao Paulo", "Sarasota", "Saratoga", "Saratoga Springs", "Sarnia", "Saugatuck", "Saugus", "Sausalito", "Savannah", "Savoy", "Saylorsburg", "Scarborough", "Schaumburg", "Schenectady", "Schuyler", "Scituate", "Scottsdale", "Scranton", "Seabrook", "Seattle", "Sebastopol", "Sebring", "Seekonk", "Selah", "Selden", "Sellersville", "Seminole", "Semmes", "Seoul", "Seria", "Seven Hills", "Severn", "Sevierville", "Seymour", "Shah Alam", "Shamokin", "Shanghai", "Shawnee", "Sheboygan", "Shelby", "Shelbyville", "Shelton", "Sherborn", "Sherman", "Shinagawa", "Shinjuku", "Shipley", "Shoreline", "Shreveport", "Shuwaikh Educational", "Sidney", "Siena", "Sierra Vista", "Silsbee", "Silver Spring", "Silverdale", "Silverton", "Simi Valley", "Singapore", "Sioux City", "Sisters", "Skokie", "Skopje", "Slidell", "Smithfield", "Smiths Lake", "Smithsburg", "Smithtown", "Smyrna", "Snellville", "Snohomish", "Socorro", "Solihull", "Somers Point", "Somerset", "Somerville", "Soquel", "Sound Beach", "South Bend", "South Burlington", "South Charleston", "South Dayton", "South Hadley", "South Jordan", "South Kingstown", "South Lake Tahoe", "South Lyon", "South Milwaukee", "South Pasadena", "South Portland", "South Windsor", "Southampton", "Southend-on-Sea", "Southern Pines", "Southfield", "Southport", "Spain", "Spanaway", "Spanish Town", "Sparks", "Sparta Township", "Spearfish", "Spicewood", "Spinnerstown", "Spirit Lake", "Spokane", "Spotswood", "Spotsylvania", "Spring", "Spring Green", "Spring Hill", "Spring Valley", "Springfield", "Springville", "Stafford", "Stamford", "Starkville", "State College", "Staten Island", "Statesville", "Steelton", "Stephenville", "Sterling", "Sterling Heights", "Steubenville", "Stevens Point", "Stillwater", "Stockbridge", "Stockholm", "Stockton", "Stockton Springs", "Stone Mountain", "Stony Brook", "Stow", "Stowe", "Strafford", "Strasburg", "Stratford-upon-Avon", "Stratham", "Streamwood", "Stroossen", "Struthers", "Stuarts Draft", "Sturbridge", "Su Thep", "Suffern", "Sugar Land", "Sultan", "Summerville", "Summit", "Sumter", "Sun City Center", "Sun Prairie", "Sunny", "Sunnydale", "Sunnyvale", "Sunrise", "Superior", "Surabaya", "Surf City", "Surrey", "Surry Hills", "Swampscott", "Swansea", "Sweet Home", "Swords", "Sydney", "Sylacauga", "Syosset", "Syracuse", "Tachikawa", "Tacoma", "Taguig", "Taipei", "Takamatsu", "Takoma Park", "Tallahassee", "Talloires", "Tamarac", "Tampa", "Taos", "Tarpon Springs", "Tarragona", "Tarrytown", "Taunton", "Taylor", "Taylorsville", "Tbilisi", "T'bilisi", "Teaneck", "Tegucigalpa", "Tel Aviv-Yafo", "Temecula", "Tempe", "Temple", "Temple City", "Temple Terrace", "Terre Haute", "Tewksbury", "The Colony", "The Hague", "The Woodlands", "Thomasville", "Thornbury", "Thornton", "Thousand Oaks", "Thunder Bay", "Tigard", "Tilburg", "Tisbury", "Titusville", "Tiverton", "Tivoli", "Tokyo", "Toledo", "Tomball", "Toms River", "Tonkawa", "Tønsberg", "Topanga", "Topeka", "Topsham", "Toronto", "Torrance", "Torrington", "Totowa", "Toulouse", "Town of Rockingham", "Towson", "Tracy", "Travelers Rest", "Traverse City", "Trenton", "Trondheim", "Troy", "Trumansburg", "Tualatin", "Tuckahoe", "Tucker", "Tuckerton", "Tucson", "Tullahoma", "Tulsa", "Turku", "Tuscaloosa", "Tuscola", "Tuscumbia", "Tustin", "Tweed", "Twin Falls", "Tyler", "Tynemouth", "Uijeongbu-si", "Ukiah", "Umatilla", "Unadilla", "Unai", "Union", "Union City", "Unity", "Upland", "Upper Darby", "Uppsala", "Urayasu", "Urbana", "Utrecht", "Utsunomiya", "Vacaville", "Vail", "Valencia", "Vallejo", "Valley", "Valley Center", "Valparaiso", "Valrico", "Van Wert", "Vance", "Vancouver", "Varaždin", "Varnell", "Västerås", "Venedocia", "Veneta", "Venice", "Ventura", "Vernon Hills", "Vero Beach", "Verona", "Verona Beach", "Versailles", "Vicksburg", "Victor", "Victoria", "Victorville", "Vidor", "Vienna", "Vilnius", "Viola", "Virginia Beach", "Visalia", "Vista", "Voorhees Township", "Vršac", "Waco", "Wagram", "Waianae", "Waimea", "Wake Forest", "Wakefield", "Walden", "Waldorf", "Waldwick", "Walla Walla", "Wallagrass", "Walnut", "Walnut Cove", "Walnut Creek", "Walpole", "Waltham", "Walworth", "Warman", "Warner Robins", "Warren", "Warrensburg", "Warrenville", "Warroad", "Warsaw", "Warwick", "Washington", "Washington Terrace", "Wasilla", "Wassenaar", "Waterbury", "Waterford", "Watertown", "Waterville", "Watervliet", "Watkinsville", "Waukegan", "Wauwatosa", "Waverly", "Waycross", "Wayne", "Waynesboro", "Waynesburg", "Weaverville", "Webster", "Welland", "Wellesley", "Wellington", "Welshpool", "Wendover", "Wesley Chapel", "West", "West Allis", "West Bend", "West Bloomfield Township", "West Chester", "West Chicago", "West Covina", "West Deptford", "West Des Moines", "West Dundee", "West Frankfort", "West Hartford", "West Haven", "West Hollywood", "West Lafayette", "West Lawn", "West Linn", "West New York", "West Orange", "West Palm Beach", "West Saint Paul", "West Salem", "West Seneca", "West Springfield", "West Valley City", "West Wyoming", "Westampton", "Westborough", "Westbury", "Western Springs", "Westernport", "Westerville", "Westfield", "Westford", "Westhampton Beach", "Westlake Village", "Westland", "Westminster", "Westmont", "Westmoreland", "Weston", "Wexford", "Wheat Ridge", "Wheaton", "Wheeling", "White Hall", "White Oak", "Whitehall", "Whitewood", "Whittier", "Wichita", "Wichita Falls", "Wilkes-Barre", "Williamsburg", "Williamsport", "Williamsville", "Williston", "Willits", "Willoughby", "Wilmette", "Wilmington", "Wilton", "Winamac", "Wincham", "Winchester", "Windsor", "Winfield", "Wingello", "Winnemucca", "Winnetka", "Winnipeg", "Winnsboro", "Winona", "Winsham", "Winston-Salem", "Winter Haven", "Winter Park", "Winter Springs", "Winters", "Wintersville", "Wiscasset", "Wisconsin Rapids", "Wokingham", "Wood Dale", "Wood River", "Woodbine", "Woodbridge", "Woodbridge Township", "Woodinville", "Woodland", "Woodland Hills", "Woodstock", "Woolwich", "Worcester", "Wuhan", "Wyandotte", "Xi'an", "Yakima", "Yarmouth", "ÿfrica", "Yokohama", "Yonkers", "Yorba Linda", "York", "Yorktown", "Yorktown Heights", "Yosemite Valley", "Youngstown", "Youngsville", "Ypsilanti", "Yuma", "Zagreb", "Zephyrhills", "Zürich", "Zutphen"};
		for (int i = 0; i < countries.length; i++) {
			String country = countries[i].replaceAll("\\s", "_").toUpperCase();
			System.out.print(country+"("+i+")"+",");
		}
	}*/
	public static void main(String[] args) {
		//System.out.println("ARICA Y PARINACOTA REGION(311),MASOVIAN VOIVODESHIP(312),REGIÃ³N METROPOLITANA(313),ULAANBAATAR(314),HESSEN(315),HAWKE'S BAY(316),AUST-AGDER(317),YARACUY(318),SALTA(319),AKERSHUS(320),PANAMA(321),PAKTIKA(322),CANTABRIA(323),HLAVNÃ­ MÄ›STO PRAHA(324),VALENCIAN COMMUNITY(325),LARA(326),GORONTALO(327),SAN LUIS(328),SPECIAL CAPITAL REGION OF JAKARTA(329),MIDI-PYRÃ©NÃ©ES(330),MANAWATU-WANGANUI(331),NORTHLAND(332),BANSKÃ¡ BYSTRICA REGION(333),EASTERN PROVINCE(334),SANTIAGO(335),GIZA GOVERNORATE(336),AREQUIPA(337),SANTA CRUZ(338),NORTHEAST(339),QUÃ©BEC(340),MAYAGÃ¼EZ(341),NORTHWEST TERRITORIES(342),VENETO(343),CRETE(344),NUEVO LEÃ³N(345),QUERÃ©TARO(346),GUJARAT(347),BRANDENBURG(348),LOWER SAXONY(349),MIE PREFECTURE(350),MPUMALANGA(351),PASTAZA(352),CARINTHIA(353),HAMILTON(354),SINT MAARTEN(355),CANTON OF BERN(356),KÅ¿CHI PREFECTURE(357),LA VEGA(358),TOYAMA-KEN(359),MOLISE(360),FRANCHE-COMTÃ©(361),NATIONAL DISTRICT(362),ZEELAND(363),JAWA TIMUR(364),MAÅ‚OPOLSKIE(365),NORTHERN PROVINCE(366),TIMIÈ™ COUNTY(367),STATE OF RIO DE JANEIRO(368),UPRAVNA ENOTA AJDOVÅ¡Ä¿INA(369),ZLIN REGION(370),HAIFA DISTRICT(371),MADEIRA(372),SAINT MICHAEL(373),HOKKAIDO PREFECTURE(374),JIANGSU(375),HEDMARK(376),GYEONGSANGNAM-DO(377),Ã–STERGÃ¶TLANDS LÃ¤N(378),WALLOON REGION(379),MENDOZA(380),NORTH RHINE-WESTPHALIA(381),".replaceAll("\\s", "_").replaceAll("\\-", "_").replaceAll("\\'", "_"));
		for(int i=2262;i<=2818;i++){
			System.out.print(i+",");
		}
	}
	
	/*public static void main(String[] args) {
		Result r = new Result();
		
		PredictionValues pvs = new PredictionValues(1, 1, 2, 3);
		ArrayList<PredictionValues> predictionValues = new ArrayList<PredictionValues>();
		predictionValues.add(pvs);
		r.setPredictionValues(predictionValues);
		
		r.getPredictionValues().addAll(null);
		
		for(PredictionValues pvsTemp : r.getPredictionValues()){
			System.out.println(pvsTemp.getUserID()+" "+pvsTemp.getItemID()+" "+pvsTemp.getRealPref()+" "+pvsTemp.getEstimatedPref());
		}
		//pvs.get
	}*/
}
