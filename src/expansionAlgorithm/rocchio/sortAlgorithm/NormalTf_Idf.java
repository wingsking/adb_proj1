package expansionAlgorithm.rocchio.sortAlgorithm;

import indexer.Posting;
import indexer.PostingNode;

import java.util.ArrayList;
import java.util.HashMap;

import bing.Result;

public class NormalTf_Idf implements SortAlgorithm {

	@Override
	public Double calScore(HashMap<String, Posting> index, String term,
			PostingNode node, ArrayList<Result> results) {
		
		int tf = node.getFrequency();
		double idf = Math.log10(results.size()/index.get(term).size());
		double score = tf*idf;
		node.getDoc().setNormalizationSUm(node.getDoc().getNormalizationSUm()+score*score);
		
		return score;
	}

}
