package bing;

public class Result {
	private String url, title, description;
	private boolean relevant;
	private Integer length;
	private double normalizationSUm=0.0;
	private int maxTf;
	
	public Result(String u, String t, String d){
		url = u;
		title = t;
		description = d;
		relevant = false;
	}
	
	
	
	
	
	public int getMaxTf() {
		return maxTf;
	}





	public void setMaxTf(int maxTf) {
		this.maxTf = maxTf;
	}





	public double getNormalizationSUm() {
		return normalizationSUm;
	}





	public void setNormalizationSUm(double normalizationSUm) {
		this.normalizationSUm = normalizationSUm;
	}





	public void setRelevant(boolean relevant) {
		this.relevant = relevant;
	}



	public Integer getLength() {
		return length;
	}



	public void setLength(Integer length) {
		this.length = length;
	}



	public void setTitle(String t){
		title = t;
	}
	
	public String getTitle(){
		return title;
	}
	
	public void setDescription(String d){
		description = d;
	}
	
	public String getDescription(){
		return description;
	}
	
	public void setUrl(String u){
		url = u;
	}
	
	public String getUrl(){
		return url;
	}
	
	public boolean getRelevant(){
		return relevant;
	}
	
	public void print(){
		System.out.println("[\n URL: " + url + "\n Title: " + title + "\n Summary: " + description + "\n]");
	}
}
