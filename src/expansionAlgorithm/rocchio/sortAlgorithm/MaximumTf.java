package expansionAlgorithm.rocchio.sortAlgorithm;

import indexer.Posting;
import indexer.PostingNode;

import java.util.ArrayList;
import java.util.HashMap;

import bing.Result;

public class MaximumTf implements SortAlgorithm {
	private static final double a =0.4;
	

	@Override
	public Double calScore(HashMap<String, Posting> index, String term,
			PostingNode node, ArrayList<Result> results) {
		/**
		 * max tf ntf is
		 *  a+(1-a)*(tf/tfMax)
		 */
		int tf = node.getFrequency();
		double ntf = a+(1-a)*(tf/node.getDoc().getMaxTf());
		
		double idf = Math.log10(results.size()/index.get(term).size());
		double score = ntf*idf;
		node.getDoc().setNormalizationSUm(node.getDoc().getNormalizationSUm()+score*score);
		
		return score;
	}

}
