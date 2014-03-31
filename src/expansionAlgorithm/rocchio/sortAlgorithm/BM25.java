package expansionAlgorithm.rocchio.sortAlgorithm;

import indexer.Posting;
import indexer.PostingNode;

import java.util.ArrayList;
import java.util.HashMap;

import bing.Result;

public class BM25 implements SortAlgorithm {

	private static int N = 10; // total doc count
	private static final double k1 = 1.2; // k and b adjusting parameters
	private static final double b = 0.75;

	@Override
	public Double calScore(HashMap<String, Posting> index, String term,
			PostingNode node, ArrayList<Result> results) {

		// the number of docments that containing this terms
		int nqi = index.get(term).size();
		N = results.size(); // storing the total results number, in case it is
							// smaller than 10

		int averageDocLength = 0;
		int lengthSum = 0;
		for (Result doc : results) {
			lengthSum += doc.getLength();
		}
		averageDocLength = lengthSum / N;
		int docLength = node.getDoc().getLength();

		/**
		 * in BM25, weight is wtf = ((k1+1)*tf)/(K+tf) * IDF
		 */

		return ((k1 + 1) * node.getFrequency())
				/ (K(averageDocLength, docLength) + node.getFrequency())
				* IDF(nqi);
	}

	private double IDF(int nqi) {
		return Math.log10((N - nqi + 0.5) / (nqi + 0.5));
	}

	/**
	 * K = k1*(1-b+b*docLength/avgLeng）
	 * 
	 * @return
	 */
	private double K(int avgLeng, int docLength) {
		return k1 * ((1 - b) + b * docLength / avgLeng);
	}

}
