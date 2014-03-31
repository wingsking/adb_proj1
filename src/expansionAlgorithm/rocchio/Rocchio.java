package expansionAlgorithm.rocchio;

import indexer.Posting;
import indexer.PostingNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import bing.Result;
import expansionAlgorithm.rocchio.sortAlgorithm.BM25;
import expansionAlgorithm.rocchio.sortAlgorithm.MaximumTf;
import expansionAlgorithm.rocchio.sortAlgorithm.NormalTf_Idf;
import expansionAlgorithm.rocchio.sortAlgorithm.SortAlgorithm;

public class Rocchio {

	/**
	 * these are the three rocchio controlling parameter a controls the old
	 * query B controls the relevant docs y controls the non-relevant docs taken
	 * from Modern Information Retrieval: A Brief Overview, Chapter 9
	 */
	private static final double a = 1; // we don't use this, because we never delete query
							// term
	private static final double B = 0.75;
	private static final double y = 0.15;

	/**
	 * 
	 * @param index
	 * @param docs
	 * @param queryTerms
	 * @return
	 * 
	 *         this method will calculate the best two terms to expand query
	 *         ret[0] the second highest score term ret[1] highest score term
	 */
	public static String[] calBestTwoTerm(HashMap<String, Posting> index, String query, ArrayList<Result> results) {

		String[] queryArr = query.split("\\+");
		HashSet<String> queryTerms = new HashSet<String>();
		for (String term : queryArr)
			queryTerms.add(term);

		// this arr stores the calculated term and their score, only storing two
		// highest terms
		Double[] termScore = new Double[] { Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY };
		String[] ret = new String[] { "", "" };

		SortAlgorithm sa = new NormalTf_Idf();

		if (sa instanceof BM25) {
			/**
			 * if we are using BM25, we can calculate the term score one term at
			 * a time
			 */

			Iterator<Map.Entry<String, Posting>> it = index.entrySet()
					.iterator();
			while (it.hasNext()) {
				Map.Entry<String, Posting> pairs = (Map.Entry<String, Posting>) it
						.next();
				String key = pairs.getKey();
				ArrayList<PostingNode> posting = pairs.getValue().getPosting();

				Double score = 0.0;

				// only calculate the terms did not appear in the original query
				if (!queryTerms.contains(key)) {
					for (PostingNode node : posting) {
						if (node.getDoc().getRelevant()) {
							score += B * sa.calScore(index, key, node, results);
						} else {
							score -= y * sa.calScore(index, key, node, results);
						}
					}

					// insertion sort on termScore, only 2 elements, so constant
					// time
					// only store postive score string
					if (score > 0) {
						if (termScore[0] < score && score <= termScore[1]) {
							termScore[0] = score;
							ret[0] = key;
						} else if (score > termScore[1]) {
							termScore[0] = termScore[1];
							ret[0] = ret[1];
							termScore[1] = score;
							ret[1] = key;
						}
						//System.out.println(key+":"+score);
					}
				}

			}
		} else {
			/**
			 * if we are using tf_idf or MaximumTf then because we need to
			 * calculate sqrt(w1^2+w2^2...) we cannot do it one term at a time
			 * instead, we calculate the raw score first, and storing the sum of
			 * w1^2+w2^2..., then do the divide operation at the end
			 */
			Iterator<Map.Entry<String, Posting>> it = index.entrySet()
					.iterator();
			while (it.hasNext()) {
				Map.Entry<String, Posting> pairs = (Map.Entry<String, Posting>) it
						.next();
				String key = pairs.getKey();
				ArrayList<PostingNode> posting = pairs.getValue().getPosting();

				for (PostingNode node : posting) {
					node.setRawScore(sa.calScore(index, key, node, results));
				}
			}

			// do the traverse once again to calculate the correct score
			// and storing the top 2 terms
			Iterator<Map.Entry<String, Posting>> it2 = index.entrySet()
					.iterator();
			while (it2.hasNext()) {
				Map.Entry<String, Posting> pairs = (Map.Entry<String, Posting>) it2
						.next();
				String key = pairs.getKey();
				ArrayList<PostingNode> posting = pairs.getValue().getPosting();

				double score= 0.0;
				// only calculate the terms did not appear in the original query
				if (!queryTerms.contains(key)) {
					for (PostingNode node : posting) {
						if (node.getDoc().getRelevant()) {
							score += B * node.getRawScore()/Math.sqrt(node.getDoc().getNormalizationSUm());
						} else {
							score -= y * node.getRawScore()/Math.sqrt(node.getDoc().getNormalizationSUm());
						}
					}
				}
				// insertion sort on termScore, only 2 elements, so constant
				// time
				// only store postive score string
				if (score > 0) {
					if (termScore[0] < score && score <= termScore[1]) {
						termScore[0] = score;
						ret[0] = key;
					} else if (score > termScore[1]) {
						termScore[0] = termScore[1];
						ret[0] = ret[1];
						termScore[1] = score;
						ret[1] = key;
					}
				}
				//System.out.println(key+":"+score);
			}

		}

		if (ret[0] == "") {
			if (ret[1] == "") {
				return new String[] {};
			} else {
				return new String[] { ret[1] };
			}
		} else {
			return ret;
		}

	}

}
