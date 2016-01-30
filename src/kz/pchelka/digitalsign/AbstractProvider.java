package kz.pchelka.digitalsign;

public interface AbstractProvider {

	
	public String sign(String object);
	
	public boolean verify(String object,String text);
	
	public boolean validate();
	
}
