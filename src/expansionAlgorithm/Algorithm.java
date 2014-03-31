package expansionAlgorithm;

import indexer.Posting;

import java.util.ArrayList;
import java.util.HashMap;

import expansionAlgorithm.rocchio.Rocchio;
import expansionAlgorithm.termProximity.ProximityEval;
import bing.Result;

public class Algorithm {

	public static String expandQuery(HashMap<String, Posting> index,
			String query, ArrayList<Result> results,
			ArrayList<String> augmentedWords) throws RuntimeException{
		String expand[] = Rocchio.calBestTwoTerm(index, query, results);
		for (String str : expand)
			augmentedWords.add(str);
		return ProximityEval.orderExpanedQuery(index, expand, query, results);
	}

}
