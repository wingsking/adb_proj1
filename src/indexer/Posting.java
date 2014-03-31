package indexer;
import java.util.*;
import bing.*;

/**
 * 
 * @author kevin
 * this class encapsulate the posting of each word, for example
 * the posting for "Gates" would be something like
 * Gates->[doc1,45]->[doc2,20]...
 * the Integer represents the term frequency in that doc
 *
 */
public class Posting {
	private ArrayList<PostingNode> posting;

	public Posting() {
		posting = new ArrayList<PostingNode>();
	}
	
	public void addDocToAPosting( PostingNode node){
		posting.add(node);
	}
	
	public int size(){
		return posting.size();
	}

	public ArrayList<PostingNode> getPosting() {
		return posting;
	}

	public void setPosting(ArrayList<PostingNode> posting) {
		this.posting = posting;
	}
	
}
