package br.cin.tbookmarks.recommender.algorithms;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import br.cin.tbookmarks.recommender.GenericUserBasedRecommenderCremonesi;
import br.cin.tbookmarks.recommender.GenericUserBasedRecommenderCremonesiRicardo;
import br.cin.tbookmarks.recommender.database.AbstractDataset;
import br.cin.tbookmarks.recommender.database.contextual.ContextualCriteria;
import br.cin.tbookmarks.recommender.database.item.ItemDomain;
import br.cin.tbookmarks.util.Functions;
import br.cin.tbookmarks.util.UsersPreferencesUtil;

public class RecommenderBuilderUserBasedNearestNeighborCremonesiRicardo implements
ContextualRecommenderBuilder {

	private int neiborSize/* = 475*/;
	private Class<? extends UserSimilarity> userSimilarity;
	private DataModel dataModel;
	
	
	public RecommenderBuilderUserBasedNearestNeighborCremonesiRicardo(int neiborSize, Class<? extends UserSimilarity> userSim) {
		this.neiborSize = neiborSize;
		this.userSimilarity = userSim;
	}
	
	
	@Override
	public String toString() {
		return "NearestNeighbor_UserBased-transClosureRicardo"+"(N="+neiborSize+")";
	}

	@Override
	public Recommender buildRecommender(DataModel model,
			ContextualCriteria criteria, IDRescorer rescorer,AbstractDataset dataset)
			throws TasteException {
		
		
		this.dataModel = model;
		
		UserSimilarity similarity = null;
		try {
			similarity = this.userSimilarity.getDeclaredConstructor(DataModel.class).newInstance(model);
			
			UserNeighborhood neighborhood = new NearestNUserNeighborhood(this.neiborSize,
					similarity, model);
			Recommender recommender = new GenericUserBasedRecommenderCremonesiRicardo(this.neiborSize,model,
					neighborhood, similarity);
			CachingRecommender cr = new CachingRecommender(recommender);
			return cr;
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public DataModel getDataModel() {
		return this.dataModel;
	}
	
	/*public static void main(String[] args) {
		
		int num_users = 4;
		
		float arrayUserUser[][] = new float[num_users][num_users];
		arrayUserUser[0][0] = 1;
		arrayUserUser[0][1] = 0.25f;
		arrayUserUser[0][2] = 0.75f;
		arrayUserUser[0][3] = 0.5f;
		arrayUserUser[1][0] = 0.25f;
		arrayUserUser[1][1] = 1;
		arrayUserUser[1][2] = 0.75f;
		arrayUserUser[1][3] = 0.25f;
		arrayUserUser[2][0] = 0.75f;
		arrayUserUser[2][1] = 0.75f;
		arrayUserUser[2][2] = 1;
		arrayUserUser[2][3] = 0.5f;
		arrayUserUser[3][0] = 0.5f;
		arrayUserUser[3][1] = 0.25f;
		arrayUserUser[3][2] = 0.5f;
		arrayUserUser[3][3] = 1;
		
		
		
		float copyArray[][] = copyArray(arrayUserUser,num_users);
		
		printArray(copyArray,num_users);
		
		for (int i = 0; i < num_users; i++) {
			for (int j = 0; j < num_users; j++) {
				if(i == j) continue;
				float value = arrayUserUser[i][j];
				if(value == 0) continue; //unknown similarity
				for(int k = 0; k < num_users; k++){//first step
					if(k == j || k == i) continue;
					if(value == arrayUserUser[j][k]){
						if(value > arrayUserUser[i][k]){
							copyArray[i][k] = value;
						}
						for(int w = 0; w < num_users; w++){//second step
							if(w == k || w == j) continue;
							if(value == arrayUserUser[k][w]){
								if(value > arrayUserUser[i][w]){
									copyArray[i][w] = value;
								}
							}
						}
					}
				}
			}
		}
		System.out.println();
		printArray(copyArray,num_users);
		System.out.println();
		printArray(arrayUserUser,num_users);
	}

	private static float[][] copyArray(float[][] arrayUserUser, int num_users) {
		float newArray[][] = new float[num_users][num_users];
		for (int i = 0; i < num_users; i++) {
			for (int j = 0; j < num_users; j++) {
				newArray[i][j] = arrayUserUser[i][j];
			}
		}
		return newArray;
	}

	private static void printArray(float[][] arrayUserUser, int num_users) {
		for (int i = 0; i < num_users; i++) {
			for (int j = 0; j < num_users; j++) {
				System.out.print(arrayUserUser[i][j]+" ");
			}
			System.out.print("\n");
		}
		
	}*/
}
