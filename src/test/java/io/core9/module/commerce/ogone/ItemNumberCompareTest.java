package io.core9.module.commerce.ogone;

import java.util.Map;
import java.util.TreeMap;

public class ItemNumberCompareTest {

	public static void main(String[] args) {
		Map<String, String> items = new TreeMap<>(new ItemNumberComparator());
		items.put("AMOUNT", "1000");
		items.put("CURRENCY", "1000");
		items.put("LANGUAGE", "1000");
		items.put("OPERATION", "1000");
		items.put("ORDERID", "1000");
		items.put("PSPID", "1000");
		items.put("ITEMID1", "1000");
		items.put("ITEMNAME1", "1000");
		items.put("ITEMPRICE1", "1000");
		items.put("TAXINCLUDED1", "1000");
		items.put("ITEMID2", "1000");
		items.put("ITEMNAME2", "1000");
		items.put("ITEMPRICE2", "1000");
		items.put("TAXINCLUDED2", "1000");
		items.put("ITEMID10", "1000");
		items.put("ITEMNAME10", "1000");
		items.put("ITEMPRICE10", "1000");
		items.put("TAXINCLUDED1", "1000");
		items.put("ITEMID10", "1000");
		items.put("ITEMNAME10", "1000");
		items.put("ITEMPRICE10", "1000");
		items.put("TAXINCLUDED10", "1000");
		items.put("ITEMID3", "1000");
		items.put("ITEMNAME3", "1000");
		items.put("ITEMPRICE3", "1000");
		items.put("TAXINCLUDED3", "1000");
		for(Map.Entry<String, String> entry : items.entrySet()) {
			System.out.println(entry.getKey() + "\t:" + entry.getValue());
		}
	}

}
