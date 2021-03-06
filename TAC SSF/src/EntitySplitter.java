import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

//This class splits a corpus file into individual documents
public class EntitySplitter {
	//Pattern endTag = Pattern.compile("</entity>");
	BufferedReader file;
	EntitySplitter(InputStream file) throws UnsupportedEncodingException{
		this.file = new BufferedReader(new InputStreamReader(file, "UTF-8"));
	}
	
	//Returns the next document or null
	public String getNext() throws IOException{
		StringBuffer out = new StringBuffer();
		String current;
		boolean start = false;
		while ((current = file.readLine())!=null){
			//System.out.println(current);
			if (current.contains("<document")){
				start = true;
			}
			if (start){
				out.append(current+"\n");
			}
			if (current.contains("</document>")){
				return out.toString();
			}
		}
		return null;
	}

	public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, IOException, SolrServerException {
		HttpSolrServer server = new HttpSolrServer("http://54.221.246.163:8984/solr/");
		Pattern p = Pattern.compile("^.*DOCID=\\\"(\\S*)\\\".*$");
		File folder = new File(args[0]);
		File[] files = null;
		if (folder.isDirectory()){
			files = folder.listFiles();
		}else{
			files = new File[]{folder};
		}
		for (File f: files){
			EntitySplitter ds = new EntitySplitter((new FileInputStream(f)));
			String current;
			System.out.println("Processing:"+f.getName());
			while ((current=ds.getNext())!=null){
				SolrInputDocument currentDoc = new SolrInputDocument();
				Matcher m = p.matcher(current.substring(0, current.indexOf('\n')));
				//System.out.println(current);
				m.matches();
				System.out.println(m.group(1));
				currentDoc.addField("id", m.group(1));
				currentDoc.addField("content", current);
				server.add(currentDoc);
			}
			f.delete();
			server.commit();
		}
	}

}
