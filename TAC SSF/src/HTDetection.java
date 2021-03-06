import java.util.*;

public class HTDetection {
	static HTLastOzan ht_ow;
	static HTOpinionFinder ht_of;
	static HTDetectionML ht_ml;
	public Sentence sent;
	public HTParser parser;
	public List<NamedEntity> NEs;
	public String author;
	public String aOffset;
	
	public HTDetection(Sentence sent, HTParser parser, List<NamedEntity> NEs, String author, String aOffset){
		ht_ow = new HTLastOzan();
		ht_of = new HTOpinionFinder();
		ht_ml = new HTDetectionML();
		
		this.sent = sent;
		this.parser = parser;
		this.NEs = NEs;
		this.author = author;
		this.aOffset = aOffset;
	}
	
	public HashMap<String, String> getHT(HashMap<String, String> OFterms, ArrayList<String> OWterms){
		HashMap<String, String> HT = new HashMap<String, String>();
		HashMap<String, String> results = new HashMap<String, String>();
		
		if (!OFterms.isEmpty()){
			results = getHTbasedOF(OFterms);
			if (!results.isEmpty())
				HT.putAll(results);
			
			results = getHTbasedML(OFterms);
			if (!results.isEmpty())
				HT.putAll(results);
		}
		
		if (!OWterms.isEmpty()){
			results = getHTbasedOW(OWterms);
			if (!results.isEmpty()){
				HT.putAll(getHTbasedOW(OWterms));
			}
		}
		
		ArrayList<String> tmp = new ArrayList<String>();
		Set<String> tmpset = OFterms.keySet();
		Iterator<String> iter = tmpset.iterator();
		while (iter.hasNext()){
			tmp.add(OFterms.get(iter.next()));
		}
		if (!tmp.isEmpty()){
			results = getHTbasedOW(tmp);
			if (!results.isEmpty())
				HT.putAll(results);
		}
		
		return HT;
	}
	
	public HashMap<String, String> getHTbasedOF(HashMap<String, String> polterms){
		HashSet<String> poltermsTmp = new HashSet<String>();
		
		Set<String> keyset = polterms.keySet();
		Iterator<String> iter = keyset.iterator();
		while(iter.hasNext()){
			String offset = iter.next();
			String[] toks = offset.split("_");
			poltermsTmp.add(sent.sent.substring(Integer.parseInt(toks[0]), Integer.parseInt(toks[1])));
		}
		
		return ht_of.process(sent, parser.dependencyString, poltermsTmp, NEs, author, aOffset);
	}
	
	public HashMap<String, String> getHTbasedOW(ArrayList<String> polterms){
		HashSet<String> NEsInString = new HashSet<String>();
		for (NamedEntity ne : NEs){
			NEsInString.add(ne.entity);
		}
		
		ArrayList<String> poltermsTmp = new ArrayList<String>();
		for(String polterm: polterms){
			poltermsTmp.add(polterm);
		}
		
		return ht_ow.process(sent.sent, parser, poltermsTmp, NEsInString, sent.beg, sent.end);
	}
	
	public HashMap<String, String> getHTbasedML(HashMap<String, String> polterms){
		HashSet<String> poltermsTmp = new HashSet<String>();
		Set<String> keyset = polterms.keySet();
		Iterator<String> iter = keyset.iterator();
		while(iter.hasNext()){
			String offset = iter.next();
			String[] toks = offset.split("_");
			poltermsTmp.add(sent.sent.substring(Integer.parseInt(toks[0]), Integer.parseInt(toks[1])));
		}
		
		return ht_ml.process(sent, parser.dependencyString, poltermsTmp, NEs, author, aOffset);
	}

}
