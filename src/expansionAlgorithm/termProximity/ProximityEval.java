package expansionAlgorithm.termProximity;

import indexer.Posting;
import indexer.PostingNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import bing.Result;

public class ProximityEval {

	private static final double k1 = 1.2; // for normalizing the length
	private static final double b = 0.75;

	/**
	 * this method return the best ordered query
	 */
	public static String orderExpanedQuery(HashMap<String, Posting> index,
			String[] expand, String query, ArrayList<Result> results) throws RuntimeException{
		// storing the original query, use linked list to easily insert expanded
		// terms
		LinkedList<String> queryList = new LinkedList<String>(
				Arrays.asList(query.split("\\s+")));
		
		/**
		 * we will examine for every term in the expand term list, consider the
		 * following: (e1,q1),(q1,e1),(e1,q2),(q2,e1)... which pair is the
		 * highest? we mark down the highest score, then we add the e1 to the
		 * position denoted by the highest score pair.
		 * 
		 * We will check the highest score for e1 and e2 separately, don't
		 * insert first, we compare the two highest score, only adding the
		 * highest score term and expanding the query, then we now expand
		 * another term by calculating the the highest score position from the
		 * expanded query, the reason is follows:
		 * 
		 * Because in our algorithm, w(e,q)= 1/(|e-q|)^2 when |e-q|<=5 so if e1
		 * is always length 6 from q1, it will be 0; but some time, e1,e0,q1;
		 * after inserting e0 in between, it will be the best otherwise, we
		 * would just adding e1 to the end, so it becomes q1,e1,e0, not good.
		 * 
		 * Note that we also need to "normalize" the doc's length, otherwise we
		 * will favor the doc which is long and contains many this pair, so,
		 * similar to BM25, we use the following formula: W_Normalized(a,b) =
		 * (k1+1)*(sum(w(a,b)))/(K+sum(w(a,b)))
		 * 
		 * K here, like in BM25, is K = k1*(1-b+b*docLength/avgLength)
		 * 
		 */

		// the array used to mark the highest score when the expanding terms are
		// considered individually
		double[] individual_score = new double[] { 0.0, 0.0 };
		int[] individual_Add_Pos = new int[] { queryList.size(), queryList.size() };

		int avgDocLength = 0;
		int lengthSum = 0;
		int relevantCount = 0;
		for (Result doc : results) {
			if(doc.getRelevant()==true){
				lengthSum += doc.getLength();
				relevantCount++;
			}
		}
		avgDocLength = lengthSum / relevantCount; // calculate the average doc
													// length, for normalize

		for (int i = 0; i < expand.length; i++) {
			for (int j = 0; j < queryList.size(); j++) {
				// the list of docs containing the expanding term
				ArrayList<PostingNode> expandDocList = index.get(expand[i])
						.getPosting();
				// the list of docs containing the query term
				ArrayList<PostingNode> queryDocList = index.get(
						queryList.get(j)).getPosting();

				double eqScoreSum = 0.0;
				double qeScoreSum = 0.0;

				for (PostingNode queryDoc : queryDocList) {
					PostingNode expandDoc = null;
					// find the doc which has both the query term and the expand
					// term
					for (PostingNode exDoc : expandDocList) {
						if (queryDoc.getDoc() == exDoc.getDoc())
							expandDoc = exDoc;
					}

					if (expandDoc != null && expandDoc.getDoc().getRelevant()==true) {
						// we only calculate the score when both query term and
						// expand term
						// is present, otherwise the score is 0, so don't bother

						// calculate (e,q)
						double eqScore = calProxiScore(expandDoc, queryDoc,
								avgDocLength);
						eqScoreSum += eqScore;

						// calcullate (q,e)
						double qeScore = calProxiScore(queryDoc, expandDoc,
								avgDocLength);
						qeScoreSum += qeScore;

					}

				}

				// mark down the highest score using this expand term and the
				// original query
				int addPos = (eqScoreSum < qeScoreSum) ? 1 : 0;
				if (individual_score[i] < Math.max(eqScoreSum, qeScoreSum)) {
					individual_score[i] = Math.max(eqScoreSum, qeScoreSum);
					individual_Add_Pos[i] = addPos+j;
				}

			}
		}

		// now we add the term who's score is the biggest
		if (expand.length == 1) {
			// if we only expanding one term
			queryList.add(individual_Add_Pos[0], expand[0]);
		} else if (expand.length == 2) {
			/**
			 * there are two term needed to be add we first add the term with
			 * the highest score, then recalculate the position which the other
			 * term should be added to
			 */
			int addWhich = (individual_score[0] < individual_score[1]) ? 1 : 0;
			queryList.add(individual_Add_Pos[addWhich], expand[addWhich]);

			// now recalculate the score of the other one

			double maxScore = 0.0;
			int addPos = queryList.size();
			for (int j = 0; j < queryList.size(); j++) {
				// the list of docs containing the expanding term
				ArrayList<PostingNode> expandDocList = index.get(expand[1-addWhich])
						.getPosting();
				// the list of docs containing the query term
				ArrayList<PostingNode> queryDocList = index.get(
						queryList.get(j)).getPosting();

				double eqScoreSum = 0.0;
				double qeScoreSum = 0.0;

				for (PostingNode queryDoc : queryDocList) {
					PostingNode expandDoc = null;
					// find the doc which has both the query term and the expand
					// term
					for (PostingNode exDoc : expandDocList) {
						if (queryDoc.getDoc() == exDoc.getDoc())
							expandDoc = exDoc;
					}

					if (expandDoc != null) {
						// we only calculate the score when both query term and
						// expand term
						// is present, otherwise the score is 0, so don't bother

						// calculate (e,q)
						double eqScore = calProxiScore(expandDoc, queryDoc,
								avgDocLength);
						eqScoreSum += eqScore;

						// calcullate (q,e)
						double qeScore = calProxiScore(queryDoc, expandDoc,
								avgDocLength);
						qeScoreSum += qeScore;

					}

				}

				// mark down the highest score using this expand term and the
				// original query
				int addPosRelative = (eqScoreSum < qeScoreSum) ? 1 : 0;
				if (maxScore < Math.max(eqScoreSum, qeScoreSum)) {
					maxScore = Math.max(eqScoreSum, qeScoreSum);
					addPos = addPosRelative+j;
				}

			}
			queryList.add(addPos, expand[1-addWhich]);
			
		}
		
		
		StringBuffer sb = new StringBuffer();
		for(String str : queryList){
			sb.append( str+" ");
		}
		sb.deleteCharAt(sb.length()-1);
		
		return sb.toString();
	}

	// correspond to the pair (a,b)
	private static double calProxiScore(PostingNode a, PostingNode b, int avgDocLength) {
		ArrayList<Integer> aPosList = a.getPosList();
		ArrayList<Integer> bPosList = b.getPosList();

		int i = 0, j = 0;
		double sum = 0.0;

		while (i < aPosList.size() && j < bPosList.size()) {
			int diff = bPosList.get(j) - aPosList.get(i); // a in front of b

			if (diff > 0 && diff <= 5) {
				// in range, cal culate score
				sum += 1 / (diff * diff);
				i++;
			} else if (diff <= 0) {
				// now b is in front of a, so b++
				j++;
			} else {
				// a is 5 words+ in front of b, so a++
				i++;
			}
		}

		// return the normalized score
		return (k1 + 1)
				* (sum / (K(avgDocLength, a.getDoc().getLength()) + sum));
	}

	private static double K(int avgLeng, int docLength) {
		return k1 * ((1 - b) + b * docLength / avgLeng);
	}
}
