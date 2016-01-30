package kz.flabs.webrule.rulefile;

public class Comment implements IRuleFileElement {
	private String comment;
	
	public Comment(String c){		
		comment = "<!--" + c + "-->";
	}
	
	public String toString(){
		return comment;
	}
	
}
