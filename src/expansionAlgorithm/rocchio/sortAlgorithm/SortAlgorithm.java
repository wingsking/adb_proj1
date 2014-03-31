package expansionAlgorithm.rocchio.sortAlgorithm;

import indexer.Posting;
import indexer.PostingNode;

import java.util.ArrayList;
import java.util.HashMap;

import bing.Result;

public interface SortAlgorithm {
	public Double calScore(HashMap<String, Posting> index, String term, PostingNode node, ArrayList<Result> results);
}
